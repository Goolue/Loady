import org.junit.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by oded on 9/12/2017.
 */
public class SrcLoaderTest {

    private final String classToLoadName = "SomeClass";
    private final String jarToLoadName = "jarForTests.jar";
    private final String relativePathStr = "compiledClassesForTests";
    private final String intFieldName = "intField";
    private final int intFieldValue = 1;
    private final String methodName = "OnePlusOneMethod";
    private final int methodReturnValue = 2;

    private Class classResult;

    @org.junit.Before
    public void setUp() throws Exception
    {
        classResult = SrcLoader.loadClassFromFile(relativePathStr, classToLoadName);
    }

    @org.junit.After
    public void tearDown() throws Exception
    {
        classResult = null;
    }

    @org.junit.Test
    public void loadClassFromFileLoadsClass() throws Exception
    {
        Assert.assertNotNull(classResult);
    }

    @org.junit.Test
    public void loadClassFromFileCorrectFields() throws Exception
    {
        Field[] fields = classResult.getDeclaredFields();

        Assert.assertEquals(1, fields.length);
        fields[0].setAccessible(true);
        Assert.assertTrue(fields[0].getName().equals(intFieldName));
        Assert.assertEquals(intFieldValue, fields[0].getInt(classResult.newInstance()));
    }

    @org.junit.Test
    public void loadClassFromFileCorrectMethods() throws Exception
    {
        Method[] methods = classResult.getDeclaredMethods();

        Assert.assertEquals(1, methods.length);
        methods[0].setAccessible(true);
        Assert.assertTrue(methods[0].getName().equals(methodName));
        Assert.assertEquals(methodReturnValue, methods[0].invoke(classResult.newInstance()));
    }

    @org.junit.Test (expected = ClassNotFoundException.class)
    public void loadClassFromFileEmptyPath() throws Exception
    {
        SrcLoader.loadClassFromFile("", classToLoadName);
    }

    @org.junit.Test (expected = ClassNotFoundException.class)
    public void loadClassFromFileEmptyClassName() throws Exception
    {
        SrcLoader.loadClassFromFile("", "");
    }

    @org.junit.Test
    public void loadClassesFromJarNotNull() throws Exception
    {
        List<Class> lstResult = SrcLoader.loadClassesFromJar(relativePathStr + "\\" + jarToLoadName);
        Assert.assertNotNull(lstResult);
    }

    @org.junit.Test
    public void loadClassesFromJarCorrectSize() throws Exception
    {
        List<Class> lstResult = SrcLoader.loadClassesFromJar(relativePathStr + "\\" + jarToLoadName);
        Assert.assertEquals(1, lstResult.size());
    }

    @org.junit.Test
    public void loadClassesFromJarCorrectClass() throws Exception
    {
        List<Class> lstResult = SrcLoader.loadClassesFromJar(relativePathStr + "\\" + jarToLoadName);
        Class classFromJar = lstResult.get(0);
        Assert.assertEquals(classToLoadName, classFromJar.getName());
    }

    @org.junit.Test
    public void loadClassesFromJarEmptyPath() throws Exception
    {
        Assert.assertNull(SrcLoader.loadClassesFromJar(""));
    }

    @org.junit.Test
    public void loadClassFromJarHavingAllPassPredicateNotNull() throws Exception
    {
        Predicate allPass = o -> true;
        List<Class> results = SrcLoader.loadClassFromJarHaving(relativePathStr + "\\" + jarToLoadName, allPass);
        Assert.assertNotNull(results);
    }

    @org.junit.Test
    public void loadClassFromJarHavingAllPassPredicateSize() throws Exception
    {
        Predicate allPass = o -> true;
        List<Class> results = SrcLoader.loadClassFromJarHaving(relativePathStr + "\\" + jarToLoadName, allPass);
        assert results != null;
        Assert.assertEquals(1, results.size());
    }

    @org.junit.Test
    public void loadClassFromJarHavingAllPassCorrectClass() throws Exception
    {
        Predicate allPass = o -> true;
        List<Class> results = SrcLoader.loadClassFromJarHaving(relativePathStr + "\\" + jarToLoadName, allPass);
        assert results != null;
        Assert.assertEquals(classToLoadName, results.get(0).getName());
    }

    @org.junit.Test
    public void loadClassFromJarHavingNonePassIsEmpty() throws Exception
    {
        Predicate allPass = o -> false;
        List<Class> results = SrcLoader.loadClassFromJarHaving(relativePathStr + "\\" + jarToLoadName, allPass);
        assert results != null;
        Assert.assertTrue(results.isEmpty());
    }

    @org.junit.Test
    public void loadClassFromJarHavingFilterByName() throws Exception
    {
        Predicate<Class> allPass = aClass -> aClass.getName().equals(classToLoadName);
        List<Class> results = SrcLoader.loadClassFromJarHaving(relativePathStr + "\\" + jarToLoadName, allPass);
        assert results != null;
        Assert.assertTrue(!results.isEmpty());
    }

}