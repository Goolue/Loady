import com.sun.org.apache.xalan.internal.xsltc.compiler.CompilerException;

import javax.tools.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by oded on 9/12/2017.
 */
public class SrcLoader {


    public static Class loadClassFromFile(String pathStr, String className) throws ClassNotFoundException
    {
        Path path = Paths.get(pathStr);
//        File file = new File(path.toAbsolutePath().toUri());
        Class toReturn = null;
        try
        {
            URL[] urls = {path.toAbsolutePath().toUri().toURL()};
            URLClassLoader loader = URLClassLoader.newInstance(urls);
            toReturn = loader.loadClass(className);

        } catch (MalformedURLException e)
        {
            //TODO: this
            e.printStackTrace();
        }
        return toReturn;
    }

    /**
     * Get a Class object by compiling and loading a '.java' file.
     * the '.class' file created by compilation is deleted when the JVM terminates.
     * @param pathStr a relative to absolute path to the '.java' file.
     *             example: "some\dir\path"
     * @param className name of the file to load, including or not including
     *                  the '.java' suffix;
     * @return Class object representing the class of the file {@param className}
     * @throws CompilerException if the file to load cannot be compiled by
     * the java compiler
     * @throws ClassNotFoundException if the file is not a '.java' file.
     */
    public static Class loadClassFromJavaFile(String pathStr, String className)
            throws CompilerException, ClassNotFoundException
    {
        String fileName;
        Class toReturn = null;

        if (className.endsWith(".java"))
        {
            fileName = className;
            className = className.substring(0, className.length() - 5);
        }
        else if (className.contains("."))
        {
            throw new ClassNotFoundException("the file is not a '.java' file");
        }
        else
        {
            fileName = className + ".java";
        }
        try
        {
            boolean compilationSuccess = compile(pathStr + "\\" + fileName);
            if (compilationSuccess)
            {
                toReturn = loadClassFromFile(pathStr, className);
                deleteClassFileOnExit(pathStr, className + ".class");
            }
            else
            {
                throw new CompilerException("Unable to compile the file " + fileName);
            }
        }
        catch (IOException ignored) { }

        return toReturn;
    }

    private static void deleteClassFileOnExit(String pathStr, String fileName)
    {
        File classFile = new File(pathStr + "\\" + fileName);
        classFile.deleteOnExit();
    }

    private static boolean compile(String pathStr) throws IOException
    {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnosticsCollector = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnosticsCollector, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager
                .getJavaFileObjectsFromStrings(Collections.singletonList(pathStr));
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnosticsCollector, null, null, compilationUnits);
        boolean success = task.call();
        fileManager.close();
        return success;
    }

    public static  List<Class> loadClassesFromJar(String path) throws ClassNotFoundException, FileNotFoundException
    {
        return loadClassFromJarHaving(path, aClass -> true);
    }

    /**
     * Get a list of all the classes in a jar file found at the {@code path}
     * that satisfy {@param predicate}.
     * @param path a relative to absolute path to the jar file, including
     *             the jar file's name.
     *             example: "some\dir\path\someJar.jar"
     * @param predicate predicate to be satisfied
     * @return a List of classes found in the jar file that satisfied {@param predicate}.
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     */
    public static  List<Class> loadClassFromJarHaving(String path, Predicate<Class> predicate)
            throws ClassNotFoundException, FileNotFoundException
    {
        List<Class> classes = null;
        try(JarFile jar = new JarFile(path))
        {
            URL[] urls = { new URL("jar:file:" + path+"!/") };
            URLClassLoader loader = URLClassLoader.newInstance(urls);

            classes = getClassesFromJar(predicate, jar, loader);
        } catch (IOException e)
        {
            return null;
        }
        return classes;
    }

    private static  List<Class> getClassesFromJar(Predicate<Class> predicate, JarFile jar,
                                   URLClassLoader loader) throws ClassNotFoundException
    {
        Enumeration<JarEntry> entries = jar.entries();
        List<Class> classes = new ArrayList<>();
        while (entries.hasMoreElements())
        {
            JarEntry je = entries.nextElement();
            if(je.isDirectory() || !je.getName().endsWith(".class"))
            {
                continue;
            }
            String className = je.getName().substring(0,je.getName().length()-6);
            className = className.replace('/', '.');
            Class c = loader.loadClass(className);
            if (predicate.test(c))
            {
                classes.add(c);
            }
        }
        return classes;
    }

}
