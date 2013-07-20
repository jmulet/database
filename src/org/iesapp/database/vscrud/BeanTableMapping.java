/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iesapp.database.vscrud;

/**
 *
 * @author Josep
 */
public class BeanTableMapping {
    protected String beanFieldName="";
    protected String tableFieldName="";
    protected String tableName="";
    protected boolean readable=true;
    protected boolean writable=true;
    protected Class<?> holdingClass;
    protected boolean pk = false;
    protected boolean autoincrement = false;

    public String getBeanFieldName() {
        return beanFieldName;
    }

    public void setBeanFieldName(String beanFieldName) {
        this.beanFieldName = beanFieldName;
    }

    public String getTableFieldName() {
        return tableFieldName;
    }

    public void setTableFieldName(String tableFieldName) {
        this.tableFieldName = tableFieldName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public boolean isReadable() {
        return readable;
    }

    public void setReadable(boolean readable) {
        this.readable = readable;
    }

    public boolean isWritable() {
        return writable;
    }

    public void setWritable(boolean writable) {
        this.writable = writable;
    }

    public Class<?> getHoldingClass() {
        return holdingClass;
    }

    public void setHoldingClass(Class<?> holdingClass) {
        this.holdingClass = holdingClass;
    }

    public boolean isPk() {
        return pk;
    }

    public void setPk(boolean pk) {
        this.pk = pk;
    }

    public boolean isAutoincrement() {
        return autoincrement;
    }

    public void setAutoincrement(boolean autoincrement) {
        this.autoincrement = autoincrement;
    }
}
