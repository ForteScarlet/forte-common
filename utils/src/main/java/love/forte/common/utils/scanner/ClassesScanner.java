/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  simple-robot-core
 * File     FileScanner.java
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 *
 */

package love.forte.common.utils.scanner;


import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 类扫描器。
 *
 * @author ForteScarlet
 */
public class ClassesScanner implements Scanner<String, Class<?>> {

    /**
     * 储存结果的Set集合
     */
    private Set<Class<?>> eleStrategySet = new HashSet<>();

    /**
     * 默认使用的类加载器
     */
    private final ClassLoader classLoader;

    /**
     * 构造，指定默认的类加载器为当前线程的类加载器
     */
    public ClassesScanner() {
        classLoader = Thread.currentThread().getContextClassLoader();
    }

    /**
     * 使用指定的类加载器
     */
    public ClassesScanner(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }


    /**
     * 根据过滤规则查询
     *
     * @param classFilter class过滤规则
     */
    @Override
    public ClassesScanner scan(String packageName, Predicate<Class<?>> classFilter) {
        eleStrategySet.addAll(addClass(packageName, classFilter));
        return this;
    }

    // /**
    //  * 根据过滤规则查询, 查询全部
    //  */
    // @Override
    // public ClassesScanner scan(String packageName) {
    //     eleStrategySet.addAll(addClass(packageName, c -> true));
    //     return this;
    // }

    /**
     * 获取包下所有实现了superStrategy的类并加入list
     *
     * @param classFilter class过滤器
     */
    private Set<Class<?>> addClass(String packageName, Predicate<Class<?>> classFilter) {
        URL url = classLoader.getResource(packageName.replace(".", "/"));
        //如果路径为null，抛出异常
        if (url == null) {
            throw new RuntimeException("The package path does not exist: " + packageName);
        }

        //路径字符串
        String protocol = url.getProtocol();
        //如果是文件类型，使用文件扫描
        if ("file".equals(protocol)) {
            // 本地自己可见的代码
            return findClassLocal(packageName, classFilter);
            //如果是jar包类型，使用jar包扫描
        } else if ("jar".equals(protocol)) {
            // 引用jar包的代码
            return findClassJar(packageName, classFilter);
        }
        return Collections.emptySet();
    }

    /**
     * 本地查找
     *
     * @param packName
     */
    private Set<Class<?>> findClassLocal(final String packName, final Predicate<Class<?>> classFilter) {
        Set<Class<?>> set = new HashSet<>();
        URI uri;
        try {
            uri = classLoader.getResource(packName.replace(".", "/")).toURI();
        } catch (URISyntaxException | NullPointerException e) {
            throw new RuntimeException("Strategy resource not found.", e);
        }

        File file = new File(uri);

        file.listFiles(chiFile -> {
            if (chiFile.isDirectory()) {
                //如果是文件夹，递归扫描
                if (packName.length() == 0) {
                    set.addAll(findClassLocal(chiFile.getName(), classFilter));
                } else {
                    set.addAll(findClassLocal(packName + "." + chiFile.getName(), classFilter));
                }
            } else if (chiFile.getName().endsWith(".class")) {
                Class<?> clazz;
                try {
                    clazz = classLoader.loadClass(packName + "." + chiFile.getName().replace(".class", ""));
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("Packet scan is abnormal.", e);
                }
                if (clazz != null && classFilter.test(clazz)) {
                    set.add(clazz);
                }
            }
            return false;
        });

        return set;
    }

    /**
     * jar包查找
     */
    private Set<Class<?>> findClassJar(final String packName, final Predicate<Class<?>> classFilter) {
        Set<Class<?>> set = new HashSet<>();
        String pathName = packName.replace(".", "/");
        JarFile jarFile;
        try {
            URL url = classLoader.getResource(pathName);
            //noinspection AlibabaLowerCamelCaseVariableNaming,ConstantConditions
            JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
            jarFile = jarURLConnection.getJarFile();
        } catch (IOException | NullPointerException e) {
            throw new IllegalStateException("Strategy resource not found.", e);
        }

        Enumeration<JarEntry> jarEntries = jarFile.entries();
        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            String jarEntryName = jarEntry.getName();

            if (jarEntryName.contains(pathName) && !jarEntryName.equals(pathName + "/")) {
                //递归遍历子目录
                if (jarEntry.isDirectory()) {
                    String clazzName = jarEntry.getName().replace("/", ".");
                    int endIndex = clazzName.lastIndexOf(".");
                    String prefix = "";
                    if (endIndex > 0) {
                        prefix = clazzName.substring(0, endIndex);
                    }
                    set.addAll(findClassJar(prefix, classFilter));
                }
                if (jarEntry.getName().endsWith(".class")) {
                    Class<?> clazz;
                    try {
                        clazz = classLoader.loadClass(jarEntry.getName().replace("/", ".").replace(".class", ""));
                    } catch (ClassNotFoundException e) {
                        throw new IllegalStateException("An exception occurred during package scan: class could not be loaded.", e);
                    }
                    //判断，如果符合，添加
                    if (clazz != null && classFilter.test(clazz)) {
                        set.add(clazz);
                    }
                }
            }
        }
        return set;
    }



    /**
     * 获取最终的扫描结果，并作为一个集合返回。
     *
     * @return 最终的扫描结果
     */
    @Override
    public Collection<Class<?>> getCollection() {
        Set<Class<?>> classSet = this.eleStrategySet;
        this.eleStrategySet = new HashSet<>();
        return classSet;
    }
}