/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.iesapp.database;

import com.sun.rowset.CachedRowSetImpl;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.rowset.CachedRowSet;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

/**
 *
 * @author Josep
 * 21-07-12: "?zeroDateTimeBehavior=convertToNull" arregla el problema dels 0000-00-00 en els date-time
 * 21-07-12: afegit membre setCatalog(String schema) per seleccionar la base de dades del host.
 */
public class MyDatabase {

   // MySql.java

    private String _host = "";
    private String _db = "";
    private String _user = "";
    private String _pass = "";
    private boolean _isConnected = false;
    private Connection _connection = null;
    private int _type;
    private String _accessFile;
    //Support multiple staments
    private String lastError = null;
    //To set the port add to the host string :3306 or whatever;
    protected String lastPstm;
    volatile protected HTMLDocument document;
    volatile private Element body;
    volatile private long logid = 0;
    volatile protected int maxLogLines = 100; // 0=disable logger
    private final String _parameters;
    
   
    public MyDatabase(String host, String db, String user, String pass, String parameters){
        
            this._host = host;
            this._db = db;
            this._user = user;
            this._pass = pass;
            this._type = 2;
            this._parameters = parameters;
           
            initializeLogger();
    }

    public MyDatabase(MyConnectionBean bean){

        this._host = bean.host;
        this._db = bean.db;
        this._user = bean.user;
        this._pass = bean.pwd;
        this._type = 2;
        this._parameters = bean.parameters;
        
        initializeLogger();
    }

    private void initializeLogger()
    {
         
            HTMLEditorKit editorkit = new HTMLEditorKit();
            document = (HTMLDocument) editorkit.createDefaultDocument(); 
            Element root = document.getDefaultRootElement();
            try {
            document.insertAfterStart(root, " <html> "
                            + "<head>"
                            + "<title>An example HTMLDocument</title>"
                            + "<style type=\"text/css\">"
                            + "  div {text-align:left; text-indent:2px; font-family:arial; font-size:10px}"
                            + "  ul { color: grey; }"
                            + "  .especial{ text-align:left; max-width: 140px;min-width:140;}"
                            + "  .select{ text-indent:12px; text-align:left; color:black; font-family:arial; font-size:10px}"
                            + "  .update{ text-indent:12px; text-align:left; color:blue; font-family:arial; font-size:10px}"
                            + "  .insert{ text-indent:12px;text-align:left; color:rgb(0,100,255); font-family:arial; font-size:10px}"
                            + "  .delete{ text-indent:12px;text-align:left; color:rgb(200,100,0); font-family:arial; font-size:10px}"
                            + "  .error{ text-indent:2px;text-align:left; color:red; font-family:arial; font-size:10px}"                                  
                            + "  .date{text-align:left; color:grey; font-family:courier; font-size:8px; max-width: 140px;min-width:140;}" 
                            + "  .title{text-align:left; color:blue; font-family:arial; font-weight:bold; font-size:14px}"
                            + "  .comment{text-align:left; color:grey; font-family:arial;  font-style:italic; font-size:10px}"
                            + "  .summary{text-align:left; color:green; font-family:arial; font-size:10px}"
                            + "  .warning{text-align:left; color:black; background-color:yellow;font-weight:bold; font-family: Courier New,Courier,monospace; font-size:10px}"
                            + "</style>"
                            + "</head>"
                            + "<body id='body'>"                        
                            + "</body>"
                            + "</html>");
            body = document.getElement("body");
        } catch (BadLocationException ex) {
            Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        //            document.insertString(0,, null);
        //        } catch (BadLocationException ex) {
        //            Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
        //        }
    }
    
     public MyDatabase(String accessFile){
        this._accessFile = accessFile;
        this._type = 1;
        this._parameters = "";
    }
     
    public MyConnectionBean getConBean()
    {
        return new MyConnectionBean( _host, _db, _user, _pass, _parameters);
    }
     

    public boolean connect(){
      
        lastError = null;
        
        if(_type == 1)  //MS-ACCESS
        {
            try {
                //MSACCES
                Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
                String host = "jdbc:odbc:Driver={Microsoft Access Driver " + "(*.mdb, *.accdb)};DBQ=" + _accessFile;
                //                    Properties p = new Properties();
                //                    p.put("charSet", "utf-8");
                //                    p.put("lc_ctype", "utf-8");
                //                    p.put("encoding", "utf-8");
                this._connection = DriverManager.getConnection(host); //,p
                this._isConnected = true;
              }
               catch (Exception e) {
                   this._isConnected = false;
                   //System.out.print("ERROR en la connexió a MS-ACCESS "+this._db+":"+e);
              }

        }
        else if(_type == 2) //MYSQL
        {               ////&characterEncoding=Cp1252
//            Properties p = new Properties();
//            p.put("charSet", "utf-8");
//            p.put("lc_ctype", "utf-8");
//            p.put("encoding", "utf-8");
//            p.put("user", this._user);
//            p.put("password", this._pass);
            
            String url = "jdbc:mysql://" + this._host + "/" + this._db; // + "?zeroDateTimeBehavior=convertToNull";
            url += _parameters.isEmpty()?"":("?"+this._parameters);
           
            try {
                    Class.forName("com.mysql.jdbc.Driver").newInstance();
                    this._connection = DriverManager.getConnection(url, this._user, this._pass); 
                    this._isConnected = true;
                }
                catch (Exception e) {
                   this._isConnected = false;
                   postError(e,"Error connecting to database");
                   //System.out.print("ERROR en la connexió a MySQL "+this._db+":"+e);
                   lastError = e.getMessage();
                }
        }

        
        return this._isConnected;
    }


//    public PreparedStatement prepareStatement(String sql)
//    {
//        PreparedStatement ps = this.prepareStatement(sql);
//        return ps;
//    }

    public int setCatalog(String db)
    {
        lastError = null;
        int nup = 0;
      
        try {
             if(_connection==null || _connection.isClosed()) {
                return nup;
            }
            _connection.setCatalog(db);
            nup = 1;
        } catch (SQLException ex) {            
            lastError = ex.getMessage();
        }
        
        return nup;
    }
    
    public Statement createStatement() throws SQLException
    {
          return this._connection.createStatement();
    }
     
    public PreparedStatement createPreparedStatement(String SQL) throws SQLException
    {
          return this._connection.prepareStatement(SQL);
    }
    
    /**
     * Cached implementation of the resultSet
     * @param sql
     * @return 
     */
    public synchronized ResultSet getResultSet(final String sql)
    {
        
         CachedRowSetImpl crs = null;
         if(_connection == null) {
             return null;
         }

        try{
// We get unpredictable behaviour using this way            
//             crs = new CachedRowSetImpl();
//             crs.setCommand(sql);
//             crs.execute(_connection);
// better to populate the cache from resultset
            
            crs = new CachedRowSetImpl();
            Statement st = _connection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            crs.populate(rs);
            if(rs!=null)
            {
                rs.close();
                st.close();
            }
            if(maxLogLines>0){
                 postSelect(sql);
            }
             
        }
        catch (Exception sqle){
            postError(sqle, sql);
            //System.out.println(sqle.getMessage());
            if(maxLogLines>0){
                lastError = sqle.getMessage();
            }
        }
        
        return crs;
    }
            
            
    /**
     * Non-cached implementation of the resultset
     * @param sql
     * @param st
     * @return 
     */
    public synchronized ResultSet getResultSet(final String sql, final Statement st){
        lastError = null;
        ResultSet set = null;
         if(_connection == null) {
             return null;
        }

        try{
             set = st.executeQuery(sql);
             if(maxLogLines>0) {
                postSelect(sql);
            }
        }
        catch (Exception sqle){
            if(maxLogLines>0) {
                postError(sqle,sql);
            }
            //System.out.println(sqle.getMessage());
            lastError = sqle.getMessage();
        }
        
        return set;
    }

    //Just an alias
    public ResultSet getResultSet2(String sql, Statement st){
        return getResultSet(sql,st);
    }

    public ResultSet getResultSet2(String sql){
        return getResultSet(sql);
    }

    public ResultSet getPreparedResultSet(Object[] obj, PreparedStatement pstm)
    {
        lastError = null;
        ResultSet set = null;
        int n=obj.length;
        if(_connection == null) {
           return null;
        }
        
        try {
         
        for(int i=0; i<n; i++)
        {
            updatePstm(i, pstm, obj[i]);
        }

        set = pstm.executeQuery();
        lastPstm = pstm.toString();
        
        } catch (SQLException ex) {
            if(maxLogLines>0) {
                postError(ex,lastPstm);
            }
            lastError = ex.getMessage();
            Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }

        return set;
    }
    
    /**
     * Chached version
     * @param obj
     * @param pstm
     * @return 
     */
    public ResultSet getPreparedResultSet(String sql, Object[] obj)
    {
        
        lastError = null;
        ResultSet set = null;
        CachedRowSet cached = null;
        int n=obj.length;
        if(_connection == null) {
           return null;
        }
       
        
        try {
            cached = new CachedRowSetImpl();
            PreparedStatement pstm = _connection.prepareStatement(sql);  
        for(int i=0; i<n; i++)
        {
           updatePstm(i, pstm, obj[i]);
        }

        set = pstm.executeQuery();
        
        cached.populate(set);
        lastPstm = pstm.toString();
        if(maxLogLines>0){
            int index = lastPstm.indexOf(":");
            postSelect(lastPstm.substring(index));
        }
        set.close();
        pstm.close();
        
        } catch (Exception ex) {
            if(maxLogLines>0) {
                postError(ex, lastPstm);
            }
            lastError = ex.getMessage();
            Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }

        return cached;
    }
    
  

    public void truncate(String table)
    {
       int nup = this.executeUpdate("TRUNCATE TABLE "+table);
    }


    public int preparedUpdate(String sql, Object[] obj)
    {
        lastError = "";
        int n=obj.length;
        int nup = 0;
        
        try {
         PreparedStatement pstm = _connection.prepareStatement(sql);
        
        for(int i=0; i<n; i++)
        {
           updatePstm(i, pstm, obj[i]);
        }

        nup = pstm.executeUpdate();
        lastPstm = pstm.toString();
        if(maxLogLines>0){
        int index = lastPstm.indexOf(":");
        postUpdate(lastPstm.substring(index), nup);
        }
        pstm.close();

        } catch (Exception ex) {
            if(maxLogLines>0){
             postError(ex,lastPstm);
            }
             lastError = ex.getMessage();
             Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }

        return nup;
    }

    
     //Igual que l'anterior però retorna l'ID (suposant que la ID es un integer i
     // que la taula nomes te una primary KEY)
     //de l'entrada que s'ha fet el DARRER update
     public int preparedUpdateID(String sql, Object[] obj){
        int id = 0;
        HashMap map = preparedUpdateKEYS(sql, obj);
        
        //Make sure that it only works for tables with one key
        if(map!=null && !map.isEmpty())
        {
            if(map.size()>1) {
                //System.out.println("preparedUpdateID cannot be used in tables with more than one KEY.\nUse preparedUpdateKEYS instead");
                return id;
            }
            
             id = ( (Number) map.values().iterator().next()).intValue();
            
        }
        return id;
     }

     //General key retrieve with multiple keys of any kind
     public HashMap<String,Object> preparedUpdateKEYS(String sql, Object[] obj){
     
        HashMap<String,Object> map = null;
        lastError = null;
        int n = obj.length;
        int nup = 0;
        PreparedStatement pstm0 = null;
       
        try {
            pstm0 = _connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); // added
        for(int i=0; i<n; i++)
        {
           updatePstm(i, pstm0, obj[i]);
        }

        nup = pstm0.executeUpdate();
        lastPstm = pstm0.toString();
            try {
                if(maxLogLines>0){
                int index = lastPstm.indexOf(":");
                postUpdate(lastPstm.substring(index), nup);
                }
            } catch (BadLocationException ex) {
                Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
            }

        /**
         * Support for Tables with mutiples keys of any kind
         */
        if(nup>0)
        {
            try {
                ResultSet rs = pstm0.getGeneratedKeys();
                ResultSetMetaData metaData = rs.getMetaData();
                int nc = metaData.getColumnCount();
                if (rs!=null && rs.next()){
                    map = new HashMap<String,Object>();
                    for(int i=1; i<nc+1; i++)
                    {
                       map.put(metaData.getColumnName(i), rs.getObject(i));
                    }
                    rs.close();
                }
            } catch (SQLException ex) {
                if(maxLogLines>0){
                postError(ex,"");
                }
                lastError = ex.getMessage();
                Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
            }
           
        }
        
        pstm0.close();   
        } catch (SQLException ex) {
            if(maxLogLines>0){
                postError(ex,"");
            }
                Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return map;
    }


    //Retorna quantes files han estat updatejades
    public synchronized int executeUpdate(String sql){

        int nupdates = 0;
        if(_connection == null) {
            return 0;
        }

        try{             
            Statement st = this._connection.createStatement();
            nupdates = st.executeUpdate(sql);
            if(maxLogLines>0){
                postUpdate(sql, nupdates);
            }
            st.close();    
        }
        catch (Exception sqle){ 
            if(maxLogLines>0){
                postError(sqle, sql);   
            }
            //System.out.println(sqle.getMessage());
        }

        return nupdates;
    }

    //Igual que l'anterior però retorna l'ID
    //de l'entrada que s'ha fet el DARRER update
     public int executeUpdateID(String sql){

        lastError = null;
        int nupdates = 0;
        if(_connection == null) {
             return 0;
         }
        Statement st=null;
        try{
           
            st = this._connection.createStatement();
            nupdates = st.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            if(maxLogLines>0){
                postUpdate(sql,nupdates);
            }
        }
        catch (Exception sqle){
            if(maxLogLines>0){
                postError(sqle,sql);
            }
            lastError = sqle.getMessage();
            //System.out.println(sqle.getMessage());
        }
        
        int id = -1;
        if(nupdates >0)
        {
            try {
                ResultSet rs = st.getGeneratedKeys();
                ResultSetMetaData metaData = rs.getMetaData();
                if(metaData.getColumnCount()>1)
                {
                    //System.out.println("executeUpdateID cannot be used in tables with more than one KEY.\nUse executeUpdateKEYS instead");
                    return id;
                }
                if (rs!=null && rs.next()){
                     id = rs.getInt(1);
                     rs.close();
                }
                st.close();
            } catch (SQLException ex) {
                if(maxLogLines>0){
                    postError(ex,"");
                }
                lastError = ex.getMessage();
                Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
            }
           
        }
            
        return id;
    }
    

    public Object[] executeUpdateKEYS(String sql){

        lastError = null;
        Object[] map = null;
        int nupdates = 0;
        if(_connection == null) return map;

        Statement st = null;
        try{
             
            st = this._connection.createStatement();
            nupdates = st.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            if(maxLogLines>0){
            postUpdate(sql,nupdates);
            }
        }
        catch (Exception sqle){
            if(maxLogLines>0){
                postError(sqle,sql);
            }
            lastError = sqle.getMessage();
            //System.out.println(sqle.getMessage());
        }
        
       
        if(nupdates>0)
        {
            try {
                ResultSet rs = st.getGeneratedKeys();
                ResultSetMetaData metaData = rs.getMetaData();
                int nc = metaData.getColumnCount();
                if (rs!=null && rs.next()){
                    map = new Object[nc];
                    for(int i=1; i<nc+1; i++)
                    {
                       map[i-1] = rs.getObject(i);
                    }
                    rs.close();
                }
                st.close();
            } catch (SQLException ex) {
                if(maxLogLines>0) {
                    postError(ex,"");
                }
                lastError = ex.getMessage();
                Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
            }
           
        }
            
        return map;
    }
     
    public void closeStatement()
    {
//        try {
//            st.close();
//        } catch (SQLException ex) {
//           lastError = ex.getMessage();
//           //System.out.println(ex);
//        }
    }

   public boolean isClosed(){

       lastError = null;
        boolean stat = true;
        if(_connection == null) {
           return true;
       }

        try{
            stat = this._connection.isClosed();
        }
        catch (SQLException sqle){
            if(maxLogLines>0) {
                postError(sqle,"");
            }
            lastError = sqle.getMessage();
            //System.out.println(sqle.getMessage());
        }

        return stat;
    }

   public void close()
   {
       lastError = null;
        if(_connection == null ) {
           return;
       }

        try {
            this._connection.close();
        } catch (SQLException ex) {
            if(maxLogLines>0) {
                postError(ex,"");
            }
           lastError = ex.getMessage();
           //System.out.println(ex);
        }
   }

  public Connection getConnection()
  {
        return _connection;
  }

  // For any javaObject being serializable,
  // byte[] bites = db.getByteObject(object)
  // preparedStatement ps // 
  // ps.setObject(bites);
  public byte[] getByteObject(Object javaObject)
  {

        byte[] data=null;
        java.sql.PreparedStatement ps=null;
        String sql=null;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(javaObject);
            oos.flush();
            oos.close();
            bos.close();

            data = bos.toByteArray();

        } catch (IOException ex) {
            if(maxLogLines>0) {
                postError(ex,"");
            }
            Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
       }

      return data;
  }


    public static boolean tryConnection(String host, String db, String user, String pass, String params, boolean silent)
    {
        MyDatabase mycon = new MyDatabase(host, db, user, pass, params);
        boolean q = mycon.connect();

        if(!silent)
        {
        if(q)
        {
             JOptionPane.showMessageDialog(null, "La connexió amb la base de dades ha estat un èxit.");

        }
        else
        {
             JOptionPane.showMessageDialog(null, "No ha estat possible la connexió amb la base de dades.");
        }
        }
        mycon.close();
        return q;

    }


    public java.util.Date getServerDate()
    {
        lastError = null;
        String SQL1 = "SELECT NOW() AS fecha";
        java.sql.Timestamp sqlDate = null;
        try {
            Statement st = this.createStatement();
            ResultSet rs1 = this.getResultSet(SQL1,st);
       
            while (rs1 != null && rs1.next()) {
                sqlDate = rs1.getTimestamp("fecha");
            }
            rs1.close();
            st.close();
       } catch (SQLException ex) {
           if(maxLogLines>0) {
               postError(ex,"");
           }
            lastError = ex.getMessage();
            Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }

        if(sqlDate!=null) {
            return new java.util.Date(sqlDate.getTime());
        }
        else {
            return new java.util.Date();
        }
    }

    
     public java.sql.Timestamp getServerNow()
     {
        lastError = null;
        String SQL1 = "SELECT NOW() AS fecha";
        
        java.sql.Timestamp sqlDate = null;
        try {
            Statement st = this.createStatement();
            ResultSet rs1 = this.getResultSet(SQL1,st);
            while (rs1 != null && rs1.next()) {
                sqlDate = rs1.getTimestamp("fecha");
            }
            rs1.close();
            st.close();
        } catch (SQLException ex) {
            if(maxLogLines>0) {
                postError(ex,"");
            }
            lastError = ex.getMessage();
            Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }

         if(sqlDate!=null) {
             return sqlDate;
         }
        else {
             return new java.sql.Timestamp(new java.util.Date().getTime());
         }
    }

     public java.sql.Time getServerTime()
    {
        lastError = null;
        String SQL1 = "SELECT CURRENT_TIME() AS hora";
        java.sql.Time sqlTime = null;
        try {
            Statement st = this.createStatement();
            ResultSet rs1 = this.getResultSet(SQL1,st);
            while (rs1 != null && rs1.next()) {
                sqlTime = rs1.getTime("hora");
            }
            rs1.close();
            st.close();
        } catch (SQLException ex) {
            if(maxLogLines>0) {
                postError(ex,"");
            }
            lastError = ex.getMessage();
            Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sqlTime;
    }

    // Returns the last id of the table (assuming int type)
     public int getLastId(String table, String idname)
     {
        lastError = null;
        int idmax = -1;
        String SQL1 = "SELECT MAX("+idname+") AS idmax FROM "+table;
        
        try {
            Statement st = this.createStatement();
            ResultSet rs1 = this.getResultSet(SQL1,st);
            while (rs1 != null && rs1.next()) {
                idmax = rs1.getInt("idmax");
            }
            rs1.close();
            st.close();
        } catch (SQLException ex) {
            if(maxLogLines>0) {
                postError(ex,"");
            }
             lastError = ex.getMessage();
            Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return idmax;
     }

    public void print() {
       //System.out.println("host="+_host + " ; db="+_db + " ; user="+_user + " ; pass="+_pass);
    }

 /**
  * Dbs must be in the same host
  * @param tablename
  * @param dbFrom
  * @param dbTo
  * @return 
  */
    public int copyDataTableBetweenDBs(String tablename, String dbFrom, String dbTo)
    {
       String SQL1 = "INSERT INTO `"+dbTo+"`.`"+tablename+"` (SELECT * FROM `"+dbFrom+"`.`"+tablename+"`)";
       return this.executeUpdate(SQL1);
    }    

/**
 * Get a description of the fields in a given table
 * @param tableName
 * @return 
 */
    public ArrayList<FieldDescriptor> getDescriptorForTable(String tableName)
    {
        lastError = null;
        ArrayList<FieldDescriptor> list = new ArrayList<FieldDescriptor>();
        
        String SQL1 = "SHOW COLUMNS FROM " + tableName;
       
        try {          
             
            
            int i = 0;
            Statement st = this.createStatement();
            ResultSet rs1 = this.getResultSet(SQL1,st);
            while (rs1 != null && rs1.next()) {
                
                FieldDescriptor fd = new FieldDescriptor();
                fd.setName(rs1.getString("Field"));
                fd.setType(rs1.getString("Type"));
                fd.setNulo(rs1.getString("Null").equalsIgnoreCase("YES"));
                String key = rs1.getString("Key");
                fd.setKey( key );
                String defecte = rs1.getString("Default");
                defecte = defecte==null?"(NULL)":defecte;
                defecte = defecte.replaceAll("'", "");
                fd.setDefecte( defecte  );
                String extra = rs1.getString("Extra");
                fd.setExtra( extra );
                fd.setAutoIncrement( extra.equalsIgnoreCase("auto_increment") );
                fd.setPrimaryKey(key.equalsIgnoreCase("PRI"));

                list.add(fd);                            
            }
            if(rs1!=null)
            {
                rs1.close();
                st.close();
            }

        } catch (SQLException ex) {
            if(maxLogLines>0) {
                postError(ex,"");
            }
            lastError = ex.getMessage();
            Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }

        
        return list;
    }

    
    /**
     * Returns the next auto_increment value for the given table in the selected schema
     */
    
    public int getNextAutoIncrementForTable(String tableName)
    {
        lastError = null;
        int ai = 0;
        String schema;
        try {
            schema = _connection.getCatalog();
            String SQL1 = "SELECT AUTO_INCREMENT FROM information_schema.TABLES WHERE TABLE_NAME = '"+
                      tableName+"' AND TABLE_SCHEMA = '"+schema+"'";
            
            Statement st = this.createStatement();
            ResultSet rs1 = this.getResultSet(SQL1,st);
            if(rs1!=null && rs1.next())
            {
                ai = rs1.getInt(1);
            }
            rs1.close();
            st.close();
        } catch (SQLException ex) {
            if(maxLogLines>0) {
                postError(ex,"");
            }
             lastError = ex.getMessage();
            Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
     
        return ai;
    }

    public String getLastError() {
        return lastError;
    }

    /**
     * @return the lastPstm
     */
    public String getLastPstm() {
        
        String txt = lastPstm.replaceAll("'", "''");
        int pos = txt.indexOf(":");
        txt = txt.substring(pos+1).trim();
        return txt;
    }

    public HTMLDocument getDocument() {
        return document;
    }

    private void postUpdate(final String sql, final int nup) throws BadLocationException, IOException {
        
          String classHtml = "update";
          String txt = sql.trim().toUpperCase();
          if(txt.startsWith("INSERT"))
          {
              classHtml = "insert";
          }
          else if(txt.trim().toUpperCase().startsWith("DELETE"))
          {
              classHtml = "delete";
          }
          
        String txt2 = columnLayout(new java.util.Date().toString(), "date", sql + " : updated=" + nup, classHtml);
        document.insertBeforeEnd(body, txt2);

    }


     private void postSelect(final String sql) throws BadLocationException, IOException {
        
          String txt = columnLayout(new java.util.Date().toString(), "date", sql, "select");
          document.insertBeforeEnd(body, txt);
         
    }

    private void postError(Exception sqle, String sql) {
        try{
            String txt2 = columnLayout(new java.util.Date().toString(), "date", sqle.toString()+ " : "+ sql, "error");              
            //System.out.println(txt2);
            document.insertBeforeEnd(body, txt2);
        } catch (Exception ex) {
            Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }  
        
    }
    
    private String columnLayout(final String col1, final String className1, final String col2, final String className2)
    {
        logid += 1;
        if(logid > maxLogLines)
        {
            logid = 1;            
        }
        Element element = document.getElement("lr"+logid);
        if(element!=null)
        {
            try {
                document.remove(element.getStartOffset(), element.getEndOffset()-element.getStartOffset());
            } catch (Exception ex) {
                Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
            }  
        }
            
//        "<tr id='lr"+logid+"'> <td class='special'><div class='"+className1+"'>" + col1 + 
//                ": </div></td><td><div class='" + className2 + "'>" + safeHtml(col2) + "</div></td></tr>";
        return "<div id='lr"+logid+"' style='text-indent:-5px'> <span class='"+className1+"'>" + col1 + 
                ": </span><span class='" + className2 + "'>" + HtmlEscape.escape(col2) + "</span></div>";
    }

    public int getMaxLogLines() {
        return maxLogLines;
    }

    public void setMaxLogLines(int maxLogLines) {
        this.maxLogLines = maxLogLines;
    }

    private void updatePstm(int i, PreparedStatement pstm, Object obj) throws SQLException {
         
        if (obj instanceof Integer) {
            Integer val = (Integer) obj;
            if(val!=null)
            {
                pstm.setInt(i + 1, val);
            }
            else
            {
                pstm.setNull(i+1, java.sql.Types.INTEGER);
            }
        } else if (obj instanceof String) {
            String val = (String) obj;
            if(val!=null)
            {
                pstm.setString(i + 1, val);
            }
            else
            {
                pstm.setNull(i+1, java.sql.Types.VARCHAR);
            }
        } else if (obj instanceof java.sql.Date) {
            java.sql.Date val = (java.sql.Date) obj;
            if (val != null) {
                pstm.setDate(i + 1, val);
            } else {
                pstm.setNull(i + 1, java.sql.Types.DATE);
            }
        } else if (obj instanceof java.sql.Timestamp) {
            java.sql.Timestamp val = (java.sql.Timestamp) obj;
            if(val!=null)
            {
                pstm.setTimestamp(i + 1, val);
            }
            else
            {
                pstm.setNull(i + 1, java.sql.Types.TIMESTAMP);
            }
        } else if (obj instanceof java.util.Date) {
            java.util.Date md = (java.util.Date) obj;
            if(md!=null)
            {
                java.sql.Date val = new java.sql.Date(md.getTime());
                pstm.setDate(i + 1, val);
            }
            else
            {
                 pstm.setNull(i + 1, java.sql.Types.DATE);
            }
        } else if (obj instanceof Double) {
            Float val = ((Number) obj).floatValue();
            if(obj != null)
            {
                pstm.setFloat(i + 1, val);
            }
            else
            {
                pstm.setNull(i + 1, java.sql.Types.FLOAT);
            }
        } else if (obj instanceof java.sql.Time) {
            java.sql.Time val = ((java.sql.Time) obj);
            if(val!=null)
            {
                pstm.setTime(i + 1, val);
            }
            else
            {
                pstm.setNull(i + 1, java.sql.Types.TIME);
            }
            
        } else if (obj instanceof java.lang.Boolean) {
            Boolean val = ((Boolean) obj);
            if(val!=null)
            {
                pstm.setBoolean(i + 1, val);
            }
            else
            {
                pstm.setNull(i + 1, java.sql.Types.BOOLEAN);
            }
        } 
        else if (obj instanceof byte[]) {
            Boolean val = ((Boolean) obj);
            if(val!=null)
            {
                pstm.setBoolean(i + 1, val);
            }
            else
            {
                pstm.setNull(i + 1, java.sql.Types.BOOLEAN);
            }
        } 
        else {
            pstm.setObject(i + 1, obj);
        }

    }
    
/**
 * Determines if tableName table exists in databaseName schema
 * @param databaseName
 * @param tableName
 * @return 
 */
    public boolean doesTableExists(String databaseName, String tableName)
    {
        boolean exists = false;
        String SQL1  = "SELECT count(*) FROM information_schema.TABLES "+
                       "WHERE (TABLE_SCHEMA = '"+databaseName+"') AND (TABLE_NAME = '"+tableName+"')";
        try {
            Statement st = this.createStatement();
            ResultSet rs = this.getResultSet(SQL1,st);
            if(rs!=null && rs.next())
            {
                exists = rs.getInt(1)>0;
            }
            rs.close();
            st.close();
        } catch (SQLException ex) {
            Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return exists;
    }
     
    /**
     * Determines if databaseName schema exists
     * @param databaseName
     * @return 
     */
    public boolean doesSchemaExists(String databaseName)
    {
        boolean exists = false;
        String SQL1  = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '"+databaseName+"'";
        try {
            Statement st = this.createStatement();
            ResultSet rs = this.getResultSet(SQL1,st);
            if(rs!=null && rs.next())
            {
                exists = rs.getInt(1)>0;
            }
            rs.close();
            st.close();
        } catch (SQLException ex) {
            Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return exists;
    }

    public ArrayList<String> listTables(String databaseName) {
        ArrayList<String> list = new ArrayList<String>();
        String SQL1 = "SELECT table_name FROM information_schema.TABLES "
                + "WHERE TABLE_SCHEMA = '" + databaseName + "'";
        try {
            Statement st = this.createStatement();
            ResultSet rs = this.getResultSet(SQL1, st);
            while (rs != null && rs.next()) {
                list.add(rs.getString(1));
            }
            if (rs != null) {
                rs.close();
                st.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    public ArrayList<String> listTables() throws SQLException {

        return listTables(this._connection.getCatalog());
    }

}

