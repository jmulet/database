/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iesapp.database.vscrud;

import org.iesapp.database.MyDatabase;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Josep
 */
public final class GenericCrud implements Crud {

    private final Object beanObj;
    private static final HashMap<String, ArrayList<BeanTableMapping>> tablesFields = new HashMap<String, ArrayList<BeanTableMapping>>();
    private final ArrayList<BeanTableMapping> tableFields;
    private final String fromQuery;
    private final MyDatabase database;

    public GenericCrud(Object beanObj, String fromQuery, MyDatabase database) {
        this.database = database;
        this.beanObj = beanObj;
        this.fromQuery = fromQuery;

        //Check for annotations      
       
        if (tablesFields.containsKey(beanObj.getClass().getName())) {
            tableFields = tablesFields.get(beanObj.getClass().getName());
        } else {
            tableFields = new ArrayList<BeanTableMapping>();
            //System.out.println("\tAbout to parse annotations:"+beanObj.getClass());
            //We must access to the input class as well as all its possible subclasses
            Class<?> currentClass = beanObj.getClass();
            while(currentClass!=null)
            {
            String defaultTable = fromQuery;
            if (currentClass.isAnnotationPresent(DefaultTableMapping.class)) {
                DefaultTableMapping annotation = currentClass.getAnnotation(DefaultTableMapping.class);
                defaultTable = annotation.tableName();
             }
              
            for (Field field : currentClass.getDeclaredFields()) {
                //System.out.println("\t\t Found field:"+field.getName());
                if (field.isAnnotationPresent(TableMapping.class)) {
                    //System.out.println("\t\t Found field with annotation:"+field.getName());
                    TableMapping annotation = field.getAnnotation(TableMapping.class);
                    BeanTableMapping beanTableMapping = new BeanTableMapping();
                    String currentTableName = annotation.tableName();
                    if (currentTableName.isEmpty()) {
                        currentTableName = defaultTable;
                    }
                    String tableFieldName = annotation.tableField();
                    if (tableFieldName.isEmpty()) {
                        tableFieldName = field.getName();
                    }
                    beanTableMapping.setBeanFieldName(field.getName());
                    beanTableMapping.setTableFieldName(tableFieldName);
                    beanTableMapping.setTableName(currentTableName);
                    beanTableMapping.setReadable(annotation.mode().contains("r"));
                    beanTableMapping.setWritable(annotation.mode().contains("w"));
                    beanTableMapping.setHoldingClass(currentClass);
                    if(field.isAnnotationPresent(PK.class))
                    {
                        beanTableMapping.setPk(true);
                        beanTableMapping.setAutoincrement(field.getAnnotation(PK.class).autoIncrement());
                    }
                    tableFields.add(beanTableMapping);
                }
                 
                }
                currentClass = currentClass.getSuperclass();
            }

            tablesFields.put(beanObj.getClass().getName(), tableFields);
        }
        
        //System.out.println("instantiated genericcrud "+beanObj.getClass()+": "+tableFields.size());

    }
   
    
    @Override
    public int load() {
        
          String clause="";
          String delimiter = "";
          HashMap<BeanTableMapping, Object> map = this.getPkValues();
          Object[] pksObjs = new Object[map.size()];
          int i = 0;
          for(BeanTableMapping mapping : map.keySet())
          {
              Object get = map.get(mapping);
              clause += delimiter + mapping.getTableFieldName()+"=? ";
              delimiter = " AND ";
              pksObjs[i] = get;
              i += 1;
          }
          
          
        try {
             String SQL1 = "SELECT * FROM "+fromQuery;
             if(!clause.isEmpty()){
                 SQL1 += " WHERE " + clause;  //PKs condition
             }
             SQL1 += " LIMIT 1";    //load limits to ONE result (use list instead)
             
             //System.out.println(SQL1+ " ");
             for(Object o: pksObjs)
             {
                 //System.out.println("\t"+ "o="+o);
             }
             PreparedStatement st = database.createPreparedStatement(SQL1);
             ResultSet rs = database.getPreparedResultSet(pksObjs, st);
             if(rs!=null && rs.next())
             {
                 //Iteration is done over ANNOTATED beanfields not over tablecolumns
                 //This way, one can skip both table fields and 
                 for(BeanTableMapping mapping: tableFields)
                 {
                        if(mapping.isReadable())
                        {
                            Method setter = new PropertyDescriptor(mapping.beanFieldName, mapping.holdingClass).getWriteMethod();
                            setter.invoke(beanObj, rs.getObject(mapping.tableFieldName));
                        }
                  }
//                 for(int i=1; i<metaData.getColumnCount()+1; i++)
//                 {
//                     String name = metaData.getColumnName(i);
//                     Method setter = new PropertyDescriptor(name, beanObj.getClass()).getWriteMethod();
//                     setter.invoke(beanObj, rs.getObject(i));
//                 }
             }
             rs.close();
             st.close();
        } catch (Exception ex) {
            Logger.getLogger(GenericCrud.class.getName()).log(Level.SEVERE, null, ex);
        }  
        return 0;
    }
    
    //@Override
    @Override
     public ArrayList list(String conditions) {
        ArrayList list = new ArrayList();
        try {
            //Retrieve pkValue from
             String SQL1 = "SELECT * FROM "+fromQuery;
             if(!conditions.isEmpty()){
                 SQL1 +=" WHERE "+conditions;
             }
             Statement st = database.createStatement();
             ResultSet rs = database.getResultSet(SQL1, st);
             ResultSetMetaData metaData = rs.getMetaData();
             while(rs!=null && rs.next())
             {
                 Class<? extends Object> aClass = beanObj.getClass();
                 Object newInstance = aClass.newInstance();
                 //Iteration is done over ANNOTATED beanfields not over tablecolumns
                 //This way, one can skip both table fields and 
                 for(BeanTableMapping mapping: tableFields)
                 {
                        if(mapping.isReadable())
                        {
                            Method setter = new PropertyDescriptor(mapping.beanFieldName, mapping.holdingClass).getWriteMethod();
                            Object obj = rs.getObject(mapping.tableFieldName);
                            //Deal with type conversion 
                            //System.out.println("writing to method"+mapping.beanFieldName+" obj class"+obj.getClass());
                            
                            setter.invoke(newInstance, obj);
                        }
                  }
                 list.add(newInstance);
             }
             rs.close();
             st.close();
        } catch (Exception ex) {
            Logger.getLogger(GenericCrud.class.getName()).log(Level.SEVERE, null, ex);
        }  
        return list;
    }
     
    @Override
    public int save() {
        try{
       
        if(isValidPK())
        {
            return update();
        }
        else
        {
            return insert();
        }
        }
        catch (Exception ex) {
            Logger.getLogger(GenericCrud.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return 0;
    }

    @Override
    public int delete() {
            String clause = "";
            String delimiter = "";
            HashMap<BeanTableMapping, Object> map = this.getPkValues();
            Object[] pksObjs = new Object[map.size()];
            int i = 0;
            for (BeanTableMapping mapping : map.keySet()) {
                Object get = map.get(mapping);
                clause += delimiter + mapping.getTableFieldName() + "=? ";
                delimiter = " AND ";
                pksObjs[i] = get;
                i += 1;
            }

            
            String SQL1 = "DELETE FROM " + fromQuery;
            if (!clause.isEmpty()) {
                SQL1 += " WHERE " + clause;  //PKs condition
            }
            SQL1 += " LIMIT 1";    
            int nup = 0;
            if(!clause.isEmpty()){
                nup = database.preparedUpdate(SQL1, pksObjs);
            }
 
        return nup;
    }

    @Override
    public boolean exists() {
        
            String clause = "";
            String delimiter = "";
            HashMap<BeanTableMapping, Object> map = this.getPkValues();
            Object[] pksObjs = new Object[map.size()];
            int i = 0;
            for (BeanTableMapping mapping : map.keySet()) {
                Object get = map.get(mapping);
                clause += delimiter + mapping.getTableFieldName() + "=? ";
                delimiter = " AND ";
                pksObjs[i] = get;
                i += 1;
            }
             String SQL1 = "SELECT * FROM "+fromQuery;
             if(!clause.isEmpty()){
                 SQL1 += " WHERE " + clause;  //PKs condition
             }
             SQL1 += " LIMIT 1";    //load limits to ONE result (use list instead)
         try{
             PreparedStatement st = database.createPreparedStatement(SQL1);
             ResultSet rs = database.getPreparedResultSet(pksObjs, st);
             if(rs!=null && rs.next())
             {
                 return true;
             }
        } catch (Exception ex) {
            Logger.getLogger(GenericCrud.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private boolean isValidPK() {
        HashMap<BeanTableMapping, Object> pkValues = this.getPkValues();
        for(Object pkValue: pkValues.values())
        {
            if(pkValue==null)
            {
                return false;
            }
                if(pkValue instanceof Integer)
                {
                     int pkValueInt = ((Number) pkValue).intValue();
                     if(pkValueInt<=0)
                     {
                         return false;
                     }
                }
                else
                {
                     if(pkValue.toString().isEmpty())
                     {
                         return false;
                }
            }
        }
        return true;
    }
    
    private int update() {
        
        int n = getWritableFieldsCount();
        String fields = "";
        String separator = "";
        Object[] valuesObj = new Object[n];
        int i =0;
        String clause = "";
        String delimiter = "";
          
        for(BeanTableMapping mapping: tableFields)
        {
            if(!mapping.isAutoincrement() && mapping.isWritable())
            {
                try {
                    fields += separator+mapping.getTableName()+"."+mapping.getTableFieldName()+"=?";
                    separator = ",";
                    Method getter = new PropertyDescriptor(mapping.getBeanFieldName(), mapping.getHoldingClass()).getReadMethod();
                    valuesObj[i] = getter.invoke(beanObj);
                    i += 1;
                } catch (Exception ex) {
                    Logger.getLogger(GenericCrud.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        }
        
        for(BeanTableMapping mapping: tableFields)
        {
            if(mapping.isPk())
            {
                clause = delimiter+mapping.getTableName()+"."+mapping.getTableFieldName()+"=?";
                delimiter = "AND";
                try {
                    Method getter = new PropertyDescriptor(mapping.getBeanFieldName(), mapping.getHoldingClass()).getReadMethod();
                    valuesObj[i] = getter.invoke(beanObj);  
                    i +=1;
                } catch (Exception ex) {
                    Logger.getLogger(GenericCrud.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        int nup = 0;
        if(!clause.isEmpty())
        {
            String SQL1 = "UPDATE "+fromQuery+" SET "+ fields + " WHERE "+clause;
            //System.out.println("UPDATE: "+SQL1);
            for(Object o: valuesObj)
            {
                //System.out.println("\t o="+o);
            }
            database.preparedUpdate(SQL1,valuesObj);
        }
        return nup;
    }

    private int insert() {
        
        int n = getWritableFieldsCount();
        String fields = "";
        String separator = "";
        String values = "";
        Object[] valuesObj = new Object[n];
        int i =0;
         
          
        for(BeanTableMapping mapping: tableFields)
        {
            if(!mapping.isAutoincrement() || mapping.isWritable())
            {
                try {
                    fields += separator+mapping.getTableName()+"."+mapping.getTableFieldName();
                    values += separator+"?";
                    separator = ",";
                    Method getter = new PropertyDescriptor(mapping.getBeanFieldName(), mapping.getHoldingClass()).getReadMethod();
                    valuesObj[i] = getter.invoke(beanObj);
                    i += 1;
                } catch (Exception ex) {
                    Logger.getLogger(GenericCrud.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        }
        
        
        int nup = 0;
        
        String SQL1 = "INSERT INTO "+fromQuery+" ("+ fields + ")  VALUES("+values+")";
        HashMap<String, Object> preparedUpdateKEYS = database.preparedUpdateKEYS(SQL1,valuesObj);
        for(String tableCol: preparedUpdateKEYS.keySet())
        {
            //Search corresponding bean mapping
            BeanTableMapping currentMapping = null;
            for(BeanTableMapping mapping: tableFields)
            {
                if(mapping.getTableFieldName().equals(tableCol))
                {
                    currentMapping = mapping;
                    break;
                }
            }
            if(currentMapping!=null)
            {
                try {
                    Method setter = new PropertyDescriptor(currentMapping.getBeanFieldName(), currentMapping.getHoldingClass()).getWriteMethod();
                    setter.invoke(beanObj, preparedUpdateKEYS.get(tableCol));
                } catch (Exception ex) {
                    Logger.getLogger(GenericCrud.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        return nup;
      }

    
    
    private HashMap<BeanTableMapping,Object> getPkValues() {
        HashMap<BeanTableMapping,Object> map = new HashMap<BeanTableMapping,Object>();
        try {
            for(BeanTableMapping mapping: tableFields)
            {
                if(mapping.isPk())
                {
                Method getter = new PropertyDescriptor(mapping.getBeanFieldName(), mapping.getHoldingClass()).getReadMethod();
                Object obj = getter.invoke(beanObj);
                map.put(mapping, obj);
                }
                }
        } catch (Exception ex) {
            Logger.getLogger(GenericCrud.class.getName()).log(Level.SEVERE, null, ex);
        }
        return map;
    }

    private int getWritableFieldsCount() {
        int n = 0;
        for(BeanTableMapping mapping: tableFields)
        {
            if(mapping.isWritable() || mapping.isPk())
            {
                n +=1;
            }
        }
        return n;
    }
    
     
    
}
