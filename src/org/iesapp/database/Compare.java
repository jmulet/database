/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iesapp.database;

import java.util.ArrayList;

//ALTER TABLE `curso2011`.`actividades`  CHANGE `descripcion` `descripcion` VARCHAR(255) NOT NULL
/**
 *
 * @author Josep
 */
public class Compare {
    private final MyDatabase mysql1;
    private final MyDatabase mysql2;
    private final String schema1;
    private final String schema2;

    /**
     * 1=LAST DATABASE VERSION
     * 2=OLD DATABASE TO COMPARE AGAINST 1
     * @param mysql1
     * @param schema1
     * @param mysql2
     * @param schema2 
     */
    public Compare(MyDatabase mysql1, String schema1, MyDatabase mysql2, String schema2)
    {
        this.mysql1 = mysql1;
        this.mysql2 = mysql2;
        this.schema1 = schema1;
        this.schema2 = schema2;
    }
    
    public ArrayList<CompareIncidence> compareStructure()
    {
        ArrayList<CompareIncidence> listIncidencies = new ArrayList<CompareIncidence>();
        //First thing compare number of tables
        ArrayList<String> listTables1 = mysql1.listTables(schema1);
        ArrayList<String> listTables2 = mysql2.listTables(schema2);
        
        for(String tb1: listTables1)
        {
            if(!listTables2.contains(tb1))
            {
                listIncidencies.add(new CompareIncidence(CompareIncidence.TABlE_MISSING, 
                        schema2, tb1, null, null, 0));
                 
            }
        }
        
//Now check the structure of every tablename which match in both databases
        for(String tb1: listTables1)
        {
             
            if(!listTables2.contains(tb1))
            {
                continue; //skip
            }
            ArrayList<FieldDescriptor> descriptorsFor1 = mysql1.getDescriptorForTable(schema1+"."+ tb1);
            ArrayList<FieldDescriptor> descriptorsFor2 = mysql2.getDescriptorForTable(schema2+"."+ tb1);
            
            for(FieldDescriptor td1: descriptorsFor1)
            {
                FieldDescriptor found2 = findDescriptor(td1, descriptorsFor2);
                if(found2==null)
                {
                    listIncidencies.add(new CompareIncidence(CompareIncidence.FIELD_MISSING, 
                        schema2, tb1, td1, td1, 0));
                 
                }
                else
                {
                    int result = found2.compare(td1);
                    if(result!=FieldDescriptor.OK)
                    {
                      
                        listIncidencies.add(new CompareIncidence(CompareIncidence.FIELD_MISMATCH, 
                        schema2, tb1, found2, td1, result));
                 
                         
                            
                       
                    }
                }
            }
        }
        return listIncidencies;
    }
    
    /**
     * Returns those incidencies which have not been solved
     * Returns empty array if everything has been sorted out
     * @param listFound
     * @return 
     */
    public ArrayList<CompareIncidence> fixStructure(ArrayList<CompareIncidence> listFound)
    {
        ArrayList<CompareIncidence> notSolved = new ArrayList<CompareIncidence>();
        for(CompareIncidence ci: listFound)
        {
            if(ci.incidenceType==CompareIncidence.TABlE_MISSING)
            {
                    String tb1 = ci.getAffectsTable();
                    //This will only work if schema1 & 2 are in the same host
                    String SQL1 = "CREATE TABLE "+schema2+"."+tb1+" LIKE "+schema1+"."+tb1;
                    int nup1 = mysql1.executeUpdate(SQL1);
                    SQL1="INSERT "+schema2+"."+tb1+" (SELECT * FROM "+schema1+"."+tb1+")";
                    int nup2 = mysql1.executeUpdate(SQL1);
                    System.out.println("\t Table "+tb1+" copied from "+schema1+" to "+schema2);
                    //Make sure everything went ok
                    boolean doesTableExists = mysql1.doesTableExists(schema2, tb1);
                    if(!doesTableExists)
                    {
                        notSolved.add(ci);
                    }
                
            }
            else if(ci.incidenceType==CompareIncidence.FIELD_MISSING)
            {
                 String tb1 = ci.affectsTable;
                 FieldDescriptor td1 = ci.getCorrectField();
                 boolean addMissingField = addMissingField(schema2, tb1, td1);  
                 if(!addMissingField)
                 {
                     notSolved.add(ci);
                 }
            }
            else if(ci.incidenceType==CompareIncidence.FIELD_MISMATCH)
            {
                 String tb1 = ci.affectsTable;
                 FieldDescriptor found2 = ci.getAffectsField();
                 FieldDescriptor td1 = ci.getCorrectField();
                 alterField(schema2, tb1, found2, td1);   
            }
        }
        return notSolved;
    }
    
    //todo: when two primary keys will fail drop primary key, add primary key (x,y)
    private boolean addMissingField(String schema, String table, FieldDescriptor desc)
    {
        //How many primary keys does the correct table contain?
// 
         ArrayList<FieldDescriptor> descriptors = mysql1.getDescriptorForTable(schema1+"."+table);
         int numPKs = 0;
//        for(FieldDescriptor fd: descriptors)
//        {
//            if(fd.isPrimaryKey())
//            {
//                numPKs +=1;
//            }
//        }
        
        String notNull = desc.isNulo()?" ":" NOT NULL ";
        String defaults= desc.defecte.equals("(NULL)")?" ":" DEFAULT '"+desc.defecte+"' ";
        String autoIncrement = desc.isAutoIncrement()?" AUTO_INCREMENT ":" ";
        String primary = desc.isPrimaryKey()?" , ADD PRIMARY KEY ("+desc.getName()+") " : " ";
        String SQL = "ALTER TABLE "+schema+"."+table+" ADD "+desc.getName()+" "+desc.getType()+" "+
               defaults + notNull + autoIncrement + primary;
        int nup = mysql2.executeUpdate(SQL);
        
        //Make sure everything went ok
        boolean fixed = false;
        descriptors = mysql2.getDescriptorForTable(schema+"."+table);
        for(FieldDescriptor fd: descriptors)
        {
            if(fd.compare(desc)==FieldDescriptor.OK)
            {
                fixed = true;
                break;
            }
        }
        return fixed;
    }
    
    
    private int alterField(String schema, String table, FieldDescriptor old, FieldDescriptor desc)
    {
        String notNull = desc.isNulo()?" ":" NOT NULL ";
        String defaults= desc.defecte.equals("(NULL)")?" ":" DEFAULT '"+desc.defecte+"' ";
        String autoIncrement = desc.isAutoIncrement()?" AUTO_INCREMENT ":" ";
        String primary = "";
        if(!old.isPrimaryKey())
        {
            primary  = desc.isPrimaryKey()?" , ADD PRIMARY KEY ("+desc.getName()+") " : " ";
        }
        String SQL = "ALTER TABLE "+schema+"."+table+" CHANGE "+desc.getName()+" "+desc.getName()+" "+
                desc.getType()+" "+
               defaults + notNull + autoIncrement + primary;
        
        int nup = mysql2.executeUpdate(SQL);
        System.out.println(nup+" "+SQL);
        return nup;
    }
    

    private FieldDescriptor findDescriptor(FieldDescriptor td1, ArrayList<FieldDescriptor> descriptorsFor2) {
        FieldDescriptor pointer = null;
        for(FieldDescriptor td2: descriptorsFor2)
        {
            if(td2.name.equalsIgnoreCase(td1.name))
            {
                pointer = td2;
                break;
            }
        }
        return pointer;
    }
    
    
            
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MyConnectionBean conBean = new MyConnectionBean();
        conBean.setDb("curso2012");
        conBean.setHost("localhost");
        conBean.setPwd("");
        conBean.setUser("root");
        MyDatabase db = new MyDatabase(conBean);
        db.connect();
        
        Compare comp = new Compare(db,"curso2012",db,"curso2010");
        ArrayList<CompareIncidence> compareStructure = comp.compareStructure();
        for(CompareIncidence ci: compareStructure)
        {
            System.out.println(ci);
        }
        
        System.out.println("-----------------------------------------------");
        
        ArrayList<CompareIncidence> notSolved = comp.fixStructure(compareStructure);
        for(CompareIncidence ci: notSolved)
        {
            System.out.println(ci);
        }
    }
    
}
