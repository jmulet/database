/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iesapp.database.vscrud;

import java.util.ArrayList;

/**
 *
 * @author Josep
 */
public interface Crud {
    public int load();
    public ArrayList list(String conditions);
    public int save();
    public int delete();
    public boolean exists();
}
