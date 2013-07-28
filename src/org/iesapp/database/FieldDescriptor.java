/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iesapp.database;

/**
*
* @author Josep
*/
public class FieldDescriptor {
    
    public static final int OK = 0;
    public static final int FIELD = 1;
    public static final int TYPE = 2;
    public static final int ISNULL = 8;
    public static final int KEY = 16;
    public static final int DEFAULTS = 32;
    public static final int EXTRA = 64;

    public static String toHuman(int result) {
        String diff = "";
        if ((result & FIELD) == FIELD) {
            diff += "FIELD;";
        }
        if ((result & TYPE) == TYPE) {
            diff += "TYPE;";
        }
        if ((result & ISNULL) == ISNULL) {
            diff += "ISNULL;";
        }
        if ((result & KEY) == KEY) {
            diff += "KEY;";
        }
        if ((result & DEFAULTS) == DEFAULTS) {
            diff += "DEFAULTS;";
        }
        if ((result & EXTRA) == EXTRA) {
            diff += "EXTRA;";
        }
        return diff;
    }
        
    protected String name;
    protected String type;
    protected boolean nulo;
    protected String key;
    protected String defecte;
    protected String extra;
    protected boolean autoIncrement;
    protected boolean primaryKey;
    
    public FieldDescriptor()
    {
        //Default
    }
    
    public FieldDescriptor(String field, String type, boolean isNull, String key, String defaults, String extra, boolean primaryKey, boolean autoIncrement) {
        this.name = field;
        this.type = type;
        this.nulo = isNull;
        this.key = key;
        this.defecte = defaults == null ? "(NULL)" : defaults;
        this.extra = extra;
        this.primaryKey = primaryKey;
        this.autoIncrement = autoIncrement;
    }
    
    
      public int compare(FieldDescriptor td1) {
            int result = 0;
            
            if(!this.name.equals(td1.name))
            {
                result |= FIELD;
            }
            if(!this.defecte.equals(td1.defecte))
            {
                result |= DEFAULTS;
            }
            if(!this.extra.equals(td1.extra))
            {
                result |= EXTRA;
            }
            if(this.nulo!=td1.nulo)
            {
                result |= ISNULL;
            }
            if(!this.key.equals(td1.key))
            {
                result |= KEY;
            }
            if(!this.type.equals(td1.type))
            {
                result |= TYPE;
            }
        return result;
        }
     
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isNulo() {
        return nulo;
    }

    public void setNulo(boolean nulo) {
        this.nulo = nulo;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDefecte() {
        return defecte;
    }

    public void setDefecte(String defecte) {
        this.defecte = defecte;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }
    
    @Override
    public String toString()
    {
        return "\tDescriptor: field="+name+", type="+type+", null="+this.nulo+", autoincrement="+this.autoIncrement+
                ", defaults="+this.defecte+", extra="+this.extra;
    }
            
}