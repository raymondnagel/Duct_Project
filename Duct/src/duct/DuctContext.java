
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package duct;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * DUCT: Dynamic User Code Toolkit
 * 
 * A DuctContext encapsulates a single JavaScript execution environment and
 * its bindings to the accessible Java code.
 * 
 * @author rnagel
 */
public final class DuctContext {
    private Bindings bindings = null;
    private final ArrayList<Class> classes = new ArrayList<>();
    private final ArrayList<Class> ductClasses = new ArrayList<>();
    private ScriptEngineManager scriptEngineManager = null;
    private ScriptEngine javaScriptEngine = null;
    
    // Constructor(s):
    
    public DuctContext()
    {
        scriptEngineManager = new ScriptEngineManager();
        javaScriptEngine = scriptEngineManager.getEngineByName("nashorn");   
        evaluateJavascript("load(\"nashorn:mozilla_compat.js\");");
        bindings = javaScriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);  
    }

    
    
    
    // Methods for working with JavaScript:
            
    public void evaluateJavascript(String code)
    {
        try {
            javaScriptEngine.eval(code);
        } catch (ScriptException ex) {
            DuctTools.showError(ex);
        }
    }
    public void evaluateJavascriptAndThrow(String code) throws ScriptException
    {
        javaScriptEngine.eval(code);
    }    
    public Bindings getBindings()
    {
        return javaScriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
    }    
    public String suggestIdentifier(Class type)
    {
        String prefix = type.getSimpleName().toLowerCase().charAt(0) + type.getSimpleName().substring(1);
        String id = null;
        int counter = 0;
        while (id == null)
        {
            if (getBindings().containsKey(prefix + counter))
                counter++;
            else
                id = prefix + counter;
        }
        return id;
    }
    public void printBindings()
    {
        bindings.entrySet().forEach((entry) -> {
            System.out.println(entry.getKey() + "=>" + entry.getValue());
        });
    }
    
    
    
    // Methods for working with the object registry:    
    
    public boolean isObjectRegistered(String identifier)
    {
        return bindings.containsKey(identifier);
    }    
    public void registerObject(String identifier, Object object)
    {
        if (!isObjectRegistered(identifier))
        {
            DuctTools.printLine(object.getClass().getCanonicalName() + " " + identifier + " was registered.");
            javaScriptEngine.put(identifier, object);
        }
        else        
        {
            DuctTools.printLine("\"" + identifier + "\" was set: " + javaScriptEngine.get(identifier).toString() + " -> " + object.toString());        
            javaScriptEngine.put(identifier, object);
        }
    }  
    public static Method[] getObjectMethods(Object object)
    {
        return getClassMethods(object.getClass());
    }            

    
    
    
    // Methods for working with the class registry:
    
    public boolean importPackage(String packageName)
    {
        try {
            List<Class> classes = getClasses(packageName);
            if (classes.size() >= 1)
            {                            
                for (int c = 0; c < classes.size(); c++)
                {
                    registerClass(classes.get(c));
                }
                DuctTools.printLine("Classes in package \"" + packageName + "\" were registered.");
                return true;
            }
            else
            {
                DuctTools.printLine("Package \"" + packageName + "\" is native, or contains no classes.");
                DuctTools.printLine("Attempting standard JavaScript importPackage (not DUCT-registered)...");
                evaluateJavascriptAndThrow("importPackage(Packages." + packageName + ");");
                DuctTools.printLine("Standard JavaScript importPackage completed without exception. Classes could not be registered in DUCT.");
                return true;
            }
        } catch (Exception ex)
        {
            DuctTools.printLine("There was an error importing package \"" + packageName + "\": " + ex.toString());
            return false;
        }        
    }    
    public boolean isClassRegistered(Class c)
    {
        return classes.contains(c);
    }    
    public boolean registerClass(Class c)
    {                
        if (!isClassRegistered(c))    
        {            
            try {
                evaluateJavascriptAndThrow("importClass(Packages." + c.getCanonicalName() + ");");
                classes.add(c);
                DuctTools.printLine(c.getCanonicalName() + " was registered."); 
            return true;
            } catch (ScriptException ex) {
                DuctTools.printLine("There was an error registering class \"" + c.getCanonicalName() + "\": " + ex.toString());
                return false;
            }            
        }
        else
        {
            DuctTools.printLine(c.getCanonicalName() + " is already registered.");
            return false;
        }
    }    
    public ArrayList<Class> getRegisteredClasses()
    {
        return classes;
    }
    public static Method[] getClassMethods(Class anyClass)
    {
        return anyClass.getMethods();
    }        

    
    
    
    // Methods for working with Duct (util) class registry:
    
    public boolean isDuctClassRegistered(Class c)
    {
        return ductClasses.contains(c);
    }    
    public boolean registerDuctClass(Class c)
    {                
        if (!isDuctClassRegistered(c))    
        {            
            try {
                evaluateJavascriptAndThrow("importClass(Packages." + c.getCanonicalName() + ");");
                ductClasses.add(c);
                DuctTools.printLine(c.getCanonicalName() + " [DUCT util] was registered."); 
                return true;
            } catch (ScriptException ex) {
                DuctTools.printLine("There was an error registering class \"" + c.getCanonicalName() + "\" [DUCT util]: " + ex.toString());
                return false;
            }            
        }
        else
        {
            DuctTools.printLine(c.getCanonicalName() + " is already registered.");
            return false;
        }
    }    
    public ArrayList<Class> getRegisteredDuctClasses()
    {
        return ductClasses;
    }
    
    
    
    

    public static boolean isPrimitiveWrap(Object obj)
    {
        return (Boolean.class.isInstance(obj) ||
                Character.class.isInstance(obj) ||
                Byte.class.isInstance(obj) ||
                Short.class.isInstance(obj) ||
                Integer.class.isInstance(obj) ||
                Long.class.isInstance(obj) ||
                Float.class.isInstance(obj) ||
                Double.class.isInstance(obj) ||
                String.class.isInstance(obj) ||
                Void.class.isInstance(obj)
                );
    }
    
    public static boolean isPrimitiveWrapper(Class cls)
    {
        return (Boolean.class.isAssignableFrom(cls) ||
                Character.class.isAssignableFrom(cls) ||
                Byte.class.isAssignableFrom(cls) ||
                Short.class.isAssignableFrom(cls) ||
                Integer.class.isAssignableFrom(cls) ||
                Long.class.isAssignableFrom(cls) ||
                Float.class.isAssignableFrom(cls) ||
                Double.class.isAssignableFrom(cls) ||
                String.class.isAssignableFrom(cls) ||
                Void.class.isAssignableFrom(cls)
                );
    }
    
    // Private methods:

    
    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    private static List<Class> getClasses(String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String fileName = resource.getFile();
            String fileNameDecoded = URLDecoder.decode(fileName, "UTF-8");
            dirs.add(new File(fileNameDecoded));
        }
        ArrayList<Class> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            String fileName = file.getName();
            if (file.isDirectory()) {
                assert !fileName.contains(".");
                classes.addAll(findClasses(file, packageName + "." + fileName));
            } else if (fileName.endsWith(".class") && !fileName.contains("$")) {
                Class _class;
                try {
                    _class = Class.forName(packageName + '.' + fileName.substring(0, fileName.length() - 6));
                } catch (ExceptionInInitializerError e) {
                    // happen, for example, in classes, which depend on 
                    // Spring to inject some beans, and which fail, 
                    // if dependency is not fulfilled
                    _class = Class.forName(packageName + '.' + fileName.substring(0, fileName.length() - 6),
                            false, Thread.currentThread().getContextClassLoader());
                }
                classes.add(_class);
            }
        }
        return classes;
    }

    private static String[] splitParams(String params)
    {
        StringBuilder builder = new StringBuilder();
        boolean parOn = false;
        for (int c = 0; c < params.length(); c++)
        {
            if (!parOn && params.charAt(c) == ',')
                builder.append("^");
            else
                builder.append(params.charAt(c));
            
            if (params.charAt(c) == '(')
                parOn = true;
            else if (params.charAt(c) == ')')
                parOn = false;
        }
        return builder.toString().split("\\^");
    }
}
