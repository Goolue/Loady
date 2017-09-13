import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
        Class toReturn = null
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
     *
     * @param pathStr
     * @param className
     * @return
     * @throws ClassNotFoundException
     */
    public static Class loadClassFromJavaFile(String pathStr, String className)
    {
        String fileName;
        if (className.endsWith(".java"))
        {
            fileName = className;
            className = className.substring(0, className.length() - 5);
        }
        else
        {
            fileName = className + ".java";
        }
        compile(pathStr, fileName);

        Class toReturn = null;
        try
        {
            toReturn = loadClassFromFile(pathStr, className);
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        deleteClassFileOnExit(pathStr, className + ".class");

        return toReturn;
    }

    private static void deleteClassFileOnExit(String pathStr, String fileName)
    {
        File classFile = new File(pathStr + "\\" + fileName);
        classFile.deleteOnExit();
    }

    private static void compile(String pathStr, String fileName)
    {
        Path path = Paths.get(pathStr + "\\" + fileName);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, path.toAbsolutePath().toString());
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
