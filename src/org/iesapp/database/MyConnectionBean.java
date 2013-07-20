/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iesapp.database;

/**
 *
 * @author Josep
 */
public class MyConnectionBean {
    
    protected String host = "localhost";
    protected String user = "root";
    protected String pwd = "";
    protected String db = "";
    protected String parameters = "";

    public MyConnectionBean()
    {
        
    }
    
    public MyConnectionBean(String host, String db, String user, String pass, String parameters)
    {
        this.host = host;
        this.db = db;
        this.user = user;
        this.pwd = pass;
        this.parameters = parameters;
    }
    
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
    
}
