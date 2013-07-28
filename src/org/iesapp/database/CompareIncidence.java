/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iesapp.database;

/**
 * Generate the result of a Database comparision
 * @author Josep
 */
public class CompareIncidence {
    public static final int TABlE_MISSING = 1;
    public static final int FIELD_MISSING = 2;
    public static final int FIELD_MISMATCH = 3;
    protected String affectsSchema;
    protected String affectsTable;
    protected FieldDescriptor affectsField;
    protected int fieldComparison;
    protected int incidenceType;
    protected FieldDescriptor correctField;
    
    public CompareIncidence(int incidenceType, String affectsSchema, String affectsTable, FieldDescriptor affectsField, 
            FieldDescriptor correctField, int fieldComparison)
    {
        this.incidenceType = incidenceType;
        this.affectsSchema = affectsSchema;
        this.affectsTable = affectsTable;
        this.affectsField = affectsField;
        this.correctField = correctField;
        this.fieldComparison = fieldComparison;
    }
    
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("");
        if(this.incidenceType==TABlE_MISSING)
        {
            builder.append(" * TABLE_MISSING `").append(this.affectsTable).
                    append("` in schema `").append(this.affectsSchema).append("`\n");
        }
        else if(this.incidenceType==FIELD_MISSING)
        {
            builder.append(" * FIELD_MISSING `").append(this.correctField.name).
                    append("` in table `").append(this.affectsSchema).append("`.").
                    append(this.affectsTable).append("\n");
        }
        else if(this.incidenceType==FIELD_MISMATCH)
        {
            builder.append(" * FIELD_MISMATCH `").append(this.correctField.name).
                    append("` in table `").append(this.affectsSchema).append("`.").
                    append(this.affectsTable).append(":").append("\n\t").
                    append("        is: ").append(this.affectsField).append("\n\t").
                    append(" should be: ").append(this.correctField).append("\n");
        }
        return builder.toString();
    }

    public String getAffectsSchema() {
        return affectsSchema;
    }

    public void setAffectsSchema(String affectsSchema) {
        this.affectsSchema = affectsSchema;
    }

    public String getAffectsTable() {
        return affectsTable;
    }

    public void setAffectsTable(String affectsTable) {
        this.affectsTable = affectsTable;
    }

    public FieldDescriptor getAffectsField() {
        return affectsField;
    }

    public void setAffectsField(FieldDescriptor affectsField) {
        this.affectsField = affectsField;
    }

    public int getFieldComparison() {
        return fieldComparison;
    }

    public void setFieldComparison(int fieldComparison) {
        this.fieldComparison = fieldComparison;
    }

    public int getIncidenceType() {
        return incidenceType;
    }

    public void setIncidenceType(int incidenceType) {
        this.incidenceType = incidenceType;
    }

    public FieldDescriptor getCorrectField() {
        return correctField;
    }

    public void setCorrectField(FieldDescriptor correctField) {
        this.correctField = correctField;
    }
}
