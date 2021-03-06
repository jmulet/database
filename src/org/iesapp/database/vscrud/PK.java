/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iesapp.database.vscrud;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Josep
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PK {
    boolean autoIncrement() default true;
}
