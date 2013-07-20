/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iesapp.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

/**
 *
 * @author Josep
 */
public class JSEngine {
    
    private long lastCompiled;
    private ScriptEngine engine;
    private CompiledScript compiled;
    private static HashMap<String,JSEngine> engines = new HashMap<String,JSEngine>();
    protected final File scriptFile;
     
    //Retrieves the js script associated to this class
    private JSEngine(File sf, String contextRoot) {
         this.scriptFile = sf;
        if (scriptFile.exists()) {
            javax.script.ScriptEngineManager manager = new javax.script.ScriptEngineManager();
            engine = manager.getEngineByName("js");
            compile();
            //System.out.println("Engine "+scriptFile.getAbsolutePath()+" created at "+lastCompiled);
        }
        else
        {
             //System.out.println("Engine "+scriptFile.getAbsolutePath()+" not found");
        }

    }
    
    private void compile()
    {
        if(engine==null)
        {
            return;
        }
        try {
            Compilable compilable = (Compilable) engine;
            //Bear in mind that script is encrypted and must be decrypted in first place
            String code = "";
            try {
                Encryption enc = new Encryption();
                FileInputStream fis = new FileInputStream(scriptFile);
                DataInputStream in = new DataInputStream(fis);
                String readUTF = in.readUTF();
                code = enc.decrypt(readUTF);
                fis.close();
                in.close();
            } catch (Exception ex) {
                Logger.getLogger(JSEngine.class.getName()).log(Level.SEVERE, null, ex);
            }
            //System.out.println("compiling code....\n"+code);
            compiled = compilable.compile(code);
            lastCompiled = new java.util.Date().getTime();
            //System.out.println("Engine "+getScriptFile().getAbsolutePath()+" compiled at "+lastCompiled);
            } catch (Exception ex) {
                Logger.getLogger(JSEngine.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    public static JSEngine getJSEngine(Class c, String contextRoot)
    {
        JSEngine eng;
        
        String jsname = c.getName().replaceAll("\\.", "-");
        File sf = new File(contextRoot + File.separator + "scripts" + File.separator + jsname + ".jsx");
        if(engines.containsKey(c.getName()))
        {
            eng = engines.get(c.getName());
            long lastModified = sf.lastModified(); 
            if(lastModified>eng.lastCompiled)
            {
                eng.compile();
            }
             
        }
        else
        {
             eng = new JSEngine(sf, contextRoot);
             engines.put(c.getName(), eng);
             
        }
        return eng;
    }
    
    public static void removeEngine(Class c)
    {
        engines.remove(c.getName());
    }
    
    public static void removeAllEngines()
    {
        engines.clear();
    }
    
    public Bindings getBindings()
    {
        if(compiled==null)
        {
            return new SimpleBindings();
        }
        return compiled.getEngine().getBindings(ScriptContext.ENGINE_SCOPE);
    }
    
    public Object evalBindings(Bindings bindings) throws ScriptException
    {
        if(compiled==null)
        {
            return null;
        }
        return compiled.eval(bindings);
    }
    
    public Object invokeFunction(String fname) throws ScriptException, NoSuchMethodException
    {
        if(compiled == null)
        {
            return null;
        }
        Invocable inv = (Invocable) compiled.getEngine();
        return inv.invokeFunction(fname);
    }
    
    public Object invokeFunction(String fname, Object... obj) throws ScriptException, NoSuchMethodException
    {
        if(compiled == null)
        {
            return null;
        }
        Invocable inv = (Invocable) compiled.getEngine();
        return inv.invokeFunction(fname, obj);
    }
    
    public CompiledScript getCompiledScript()
    {
        return compiled;
    }

    public File getScriptFile() {
        return scriptFile;
    }
}
