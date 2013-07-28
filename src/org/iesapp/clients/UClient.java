/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iesapp.clients;

/**
 *
 * @author Josep
 */
public interface UClient {
    
    /**
     * Returns the current client version
     * @return 
     */
    public String getClientVersion();

    /**
     * Returns the result of the check
     * in text 
     * It will create database if not found
     * If database found will perform compatibility checks
     * @param year : academic year
     * @return 
    */
    public String checkDatabases(int year);
    
    /**
     * It will work if a checkDatabases has been run before.
     * It will return the result of the check
     * Empty string if everything has been fixed
     * @return 
     */
    public String fixDatabases();
            
}
