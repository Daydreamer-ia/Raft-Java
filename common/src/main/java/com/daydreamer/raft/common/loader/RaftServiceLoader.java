package com.daydreamer.raft.common.loader;

import com.daydreamer.raft.common.annotation.SPI;
import com.daydreamer.raft.common.annotation.SPIImplement;
import com.daydreamer.raft.common.annotation.SPIMethodInit;
import com.daydreamer.raft.common.annotation.SPISetter;
import com.daydreamer.raft.common.entity.Holder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author Daydreamer
 * <p>
 * It is used to load service from disk by java spi
 */
@SuppressWarnings("all")
public class RaftServiceLoader<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RaftServiceLoader.class);

    /**
     * store all instances have instanced
     * key: group
     * value :{
     *     key: clazz name
     *     value: instance
     * }
     */
    private static Map<String, Map<String, Holder>> INSTANCE = new ConcurrentHashMap<>();

    /**
     * store all LazyLoader have instanced
     */
    private static Map<String, Map<Class<?>, RaftServiceLoader<?>>> LOADERS = new ConcurrentHashMap<>();

    /**
     * store all instances have instanced
     */
    private Map<String, Holder> instances = new ConcurrentHashMap<>();

    private static final String[] PATH = {"META-INF/services/", "META-INF/raft/"};

    /**
     * interface clazz
     */
    private Class<T> clazz;

    /**
     * classloader
     */
    private ClassLoader classLoader;

    /**
     * default
     */
    private T defaultImpl;

    /**
     * loader group key
     */
    private String groupKey;

    /**
     * default
     */
    private Class<?> defaultClazz;

    /**
     * default
     */
    private String defaultKey;

    /**
     * whether loaded
     */
    private AtomicBoolean isLoaded;

    /**
     * service factory
     */
    private ServiceFactory serviceFactory;

    /**
     * support class names
     */
    private Set<String> clazzNames;

    private RaftServiceLoader(Class<T> interfaceClazz,
                              ClassLoader classLoader,
                              ServiceFactory serviceFactory,
                              String groupKey) {
        this.clazz = interfaceClazz;
        this.classLoader = classLoader;
        this.serviceFactory = serviceFactory;
        this.isLoaded = new AtomicBoolean(false);
        this.clazzNames = new HashSet<>();
        this.groupKey = groupKey;
    }

    private static <T> RaftServiceLoader<T> getLoader(Class<T> interfaceClazz,
                                                      ClassLoader classLoader,
                                                      ServiceFactory serviceFactory,
                                                      String groupKey) {
        if (groupKey == null) {
            throw new IllegalStateException("group key is null");
        }
        LOADERS.putIfAbsent(groupKey, new ConcurrentHashMap<>());
        RaftServiceLoader<?> lazyLoader = LOADERS.get(groupKey).get(interfaceClazz);
        if (Objects.nonNull(lazyLoader)) {
            return (RaftServiceLoader<T>) lazyLoader;
        }
        LOADERS.get(groupKey)
                .putIfAbsent(interfaceClazz, new RaftServiceLoader<>(interfaceClazz, classLoader, serviceFactory, groupKey));
        return (RaftServiceLoader<T>) LOADERS.get(groupKey).get(interfaceClazz);
    }

    public static <T> RaftServiceLoader<T> getLoader(String groupKey, Class<T> interfaceClazz, ClassLoader classLoader) {
        classLoader = classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader;
        RaftServiceLoader<ServiceFactory> factory = getLoader(ServiceFactory.class, classLoader, null, groupKey);
        return getLoader(interfaceClazz, classLoader, factory.getDefault(), groupKey);
    }

    public static <T> RaftServiceLoader<T> getLoader(String groupKey, Class<T> interfaceClazz) {
        return getLoader(groupKey, interfaceClazz, ClassLoader.getSystemClassLoader());
    }

    public List<T> getAll() {
        // load clazz
        loadClazz();
        // load impl
        loadSub();
        return instances.values().stream()
                .map(item -> (T) item.getObject())
                .collect(Collectors.toList());
    }

    public Set<String> getAllSupportClassName() {
        return Collections.unmodifiableSet(this.clazzNames);
    }

    public T getInstance(String key) {
        // load clazz
        loadClazz();
        // load impl
        loadSub();
        return (T) Optional.ofNullable(instances.get(key))
                .orElseGet(Holder::new)
                .getObject();
    }

    public String getDefaultKey() {
        return this.defaultKey;
    }

    public void addInstance(String key, T object) {
        Holder holder = new Holder();
        holder.setClazz(object.getClass());
        holder.setObject(object);
        // put
        instances.put(key, holder);
        INSTANCE.putIfAbsent(groupKey, new ConcurrentHashMap<>());
        INSTANCE.get(groupKey).put(holder.getClazz().getName(), holder);
    }

    private synchronized void loadClazz() {
        if (this.defaultImpl != null || this.defaultKey != null) {
            return;
        }
        if (!Modifier.isInterface(clazz.getModifiers())
                && !Modifier.isAbstract(clazz.getModifiers())) {
            throw new RuntimeException("Class " + clazz + " is not interface!");
        }
        SPI spi = clazz.getDeclaredAnnotation(SPI.class);
        if (Objects.isNull(spi)) {
            throw new RuntimeException("SPI load fail, because clazz: "
                    + clazz + " do not mark @SPI to declare default key!");
        }
        this.defaultKey = spi.value();
        if (Objects.isNull(defaultKey)) {
            throw new RuntimeException("SPI load fail, because clazz: "
                    + clazz + " do not mark @SPI to declare default key!");
        }
    }

    private void setGroupKey(Object obj) {
        if (obj instanceof GroupAware) {
            ((GroupAware) obj).setGroupKey(this.groupKey);
        }
    }

    private synchronized void loadSub() {
        if (isLoaded.get()) {
            return;
        }
        // fill classLoader
        getClassLoader();
        // load all clazz names
        if (clazzNames.isEmpty()) {
            for (String path : PATH) {
                clazzNames.addAll(loadResources(path, this.clazz.getName()));
            }
        }
        Iterator<String> iterator = clazzNames.iterator();
        while (iterator.hasNext()) {
            String className = iterator.next();
            Class<?> clazz = null;
            try {
                clazz = Class.forName(className, false, classLoader);
                // if contains
                INSTANCE.putIfAbsent(groupKey, new ConcurrentHashMap<>());
                if (INSTANCE.get(groupKey).containsKey(clazz.getName())) {
                    continue;
                }
                Holder holder = new Holder();
                INSTANCE.get(groupKey).put(clazz.getName(), holder);
                SPIImplement implement = clazz.getDeclaredAnnotation(SPIImplement.class);
                if (Objects.isNull(implement)) {
                    throw new RuntimeException("SPI load fail, because clazz: "
                            + clazz + " do not mark @SPIImplement to declare key!");
                }
                String key = implement.value();
                if (Objects.isNull(key)) {
                    throw new RuntimeException("SPI load fail, because clazz: "
                            + clazz + " do not mark @SPIImplement to declare key!");
                }
                if (defaultKey.equals(key)) {
                    defaultClazz = clazz;
                }
                // if repeat
                if (instances.containsKey(key)) {
                    continue;
                }
                Object instance = clazz.newInstance();
                // inject properties, ServiceFactory do not
                if (!clazz.getClass().isAssignableFrom(ServiceFactory.class)) {
                    inject(instance);
                }
                // set group key
                setGroupKey(instance);
                // init if necessary
                init(instance);
                // put
                instances.put(key, holder);
                holder.setObject(instance);
                if (defaultKey.equals(key)) {
                    defaultImpl = (T) instance;
                }
            } catch (Exception e) {
                e.printStackTrace();
                INSTANCE.get(groupKey).remove(clazz.getName());
                throw new RuntimeException("Fail to instance clazz: " + clazz
                        + ", because " + e.getLocalizedMessage());
            }
        }
        if (defaultClazz == null) {
            throw new RuntimeException("No default implement found for " + clazz
                    + ", default key: " + defaultKey);
        }
        isLoaded.set(true);
    }

    private void getClassLoader() {
        if (classLoader == null) {
            this.classLoader = Thread.currentThread().getContextClassLoader();
        }
        if (classLoader == null) {
            this.classLoader = ClassLoader.getSystemClassLoader();
        }
        if (classLoader == null) {
            this.classLoader = RaftServiceLoader.class.getClassLoader();
        }
    }

    private void init(Object obj) throws InvocationTargetException, IllegalAccessException {
        Class<?> tmp = obj.getClass();
        while (!tmp.equals(Object.class)) {
            Method[] declaredMethods = tmp.getDeclaredMethods();
            for (Method method :declaredMethods) {
                if (method.isAnnotationPresent(SPIMethodInit.class)) {
                    // alow private method
                    method.setAccessible(true);
                    method.invoke(obj, null);
                }
            }
            tmp = tmp.getSuperclass();
        }
    }

    private Set<String> loadResources(String dir, String type) {
        String fileName = dir + type;
        Set<String> classNames = new HashSet<>();
        try {
            Enumeration<java.net.URL> urls = classLoader.getResources(fileName);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    java.net.URL resourceURL = urls.nextElement();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceURL.openStream(),
                            StandardCharsets.UTF_8))) {
                        String line = "";
                        while ((line = reader.readLine()) != null) {
                            classNames.add(line);
                        }
                    }
                }
            }
        } catch (Exception t) {
            LOGGER.error("Exception occurred when loading class (interface: " +
                    type + ", description file: " + fileName + ").", t.getLocalizedMessage());
        }
        return classNames;
    }

    public T getDefault() {
        // load clazz
        loadClazz();
        // load impl
        loadSub();
        return defaultImpl;
    }

    private void inject(Object instance) {
        if (serviceFactory != null) {
            List<Method> allSetter = getAllSetter(instance.getClass());
            for (Method method : allSetter) {
                String key = getSetterProperty(method);
                if (method.isAnnotationPresent(SPISetter.class)) {
                    SPISetter setter = method.getDeclaredAnnotation(SPISetter.class);
                    if (setter.value() != null) {
                        key = setter.value();
                    }
                }
                Class<?> type = method.getParameterTypes()[0];
                Object dependency = serviceFactory.getDependency(type, key);
                try {
                    if (dependency != null) {
                        method.setAccessible(true);
                        method.invoke(instance, dependency);
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Fail to invoke method: " + method.getName()
                            + " for clazz: " + instance.getClass()
                            + ", because " + e.getLocalizedMessage());
                }
            }
        }
    }

    private String getSetterProperty(Method method) {
        return method.getName().length() > 3 ? method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4) : "";
    }

    private List<Method> getAllSetter(Class<?> clz) {
        ArrayList<Method> res = new ArrayList<>();
        Class<?> tempClass = clz;
        while (tempClass != null) {
            Method[] declaredMethods = tempClass.getDeclaredMethods();
            // collect all setter method
            for (Method declaredMethod : declaredMethods) {
                if (declaredMethod.getName().startsWith("set")) {
                    res.add(declaredMethod);
                }
            }
            // collect super class
            tempClass = tempClass.getSuperclass();
        }
        return res;
    }
}
