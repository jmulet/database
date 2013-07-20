/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iesapp.util;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author usuari
 */
public class MySorter {
    private ArrayList<String> fields;
    private HashMap<String,Boolean> ascdesc;
    private final ArrayList<String> origfields;
    
    
    public MySorter(String[] camps)
    {
        fields = new ArrayList<String>();
        origfields = new ArrayList<String>();
        ascdesc = new HashMap<String,Boolean>();
        for (int i=0; i<camps.length; i++)
        {
            fields.add(camps[i]);
            origfields.add(camps[i]);
            ascdesc.put(camps[i],true);
        }
        
    }
    
    public void setFirst(String mfield)
    {
        if(fields.contains(mfield))
        {
            int index = fields.indexOf(mfield);
            fields.add(0, fields.get(index));
            fields.remove(index+1);
            
            //toogle asc/desc
            if(ascdesc.containsKey(mfield))
            {
                boolean q = ascdesc.get(mfield);
                ascdesc.put(mfield, !q);
            }
        }
    }
    
    public String getMysqlOrder()
    {
        
        String txt = " ORDER BY ";
       
        String order =" DESC";
        if(ascdesc.get(fields.get(0))) {
            order = " ASC";
        } 
        txt += fields.get(0) + order;
        
                
        if(fields.size()>1)
        {
            for(int i=1; i<fields.size(); i++)
            {   
                    order =" DESC";
                    if(ascdesc.get(fields.get(i))) {
                    order = " ASC";
                } 
                    txt += ", "+fields.get(i) + order;
            }
        }
        
       // System.out.println(txt);
        return txt;
    }

    public void reset() {
        fields.clear(); 
        for(int i=0; i<origfields.size(); i++)
        {
            fields.add(origfields.get(i));
        }
        
        for(String ky: ascdesc.keySet())
        {
             ascdesc.put(ky,true);
        }
    }
    
}
