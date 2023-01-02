package com.daydreamer.raft.common.loader;

import com.daydreamer.raft.common.annotation.SPI;
import com.daydreamer.raft.common.annotation.SPIImplement;
import com.daydreamer.raft.common.entity.Holder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
     * key: clazz name
     */
    private static Map<String, Holder> INSTANCE = new ConcurrentHashMap<>();

    /**
     * store all LazyLoader have instanced
     */
    private static Map<Class<?>, RaftServiceLoader<?>> LOADERS = new ConcurrentHashMap<>();

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
                              ServiceFactory serviceFactory) {
        this.clazz = interfaceClazz;
        this.classLoader = classLoader;
        this.serviceFactory = serviceFactory;
        this.isLoaded = new AtomicBoolean(false);
        this.clazzNames = new HashSet<>();
    }

    private static <T> RaftServiceLoader<T> getLoader(Class<T> interfaceClazz,
                                                      ClassLoader classLoader,
                                                      ServiceFactory serviceFactory) {
        RaftServiceLoader<?> lazyLoader = LOADERS.get(interfaceClazz);
        if (Objects.nonNull(lazyLoader)) {
            return (RaftServiceLoader<T>) lazyLoader;
        }
        LOADERS.putIfAbsent(interfaceClazz, new RaftServiceLoader<>(interfaceClazz, classLoader, serviceFactory));
        return (RaftServiceLoader<T>) LOADERS.get(interfaceClazz);
    }

    public static <T> RaftServiceLoader<T> getLoader(Class<T> interfaceClazz, ClassLoader classLoader) {
        classLoader = classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader;
        RaftServiceLoader<ServiceFactory> factory = getLoader(ServiceFactory.class, classLoader, null);
        return getLoader(interfaceClazz, classLoader, factory.getDefault());
    }

    public static <T> RaftServiceLoader<T> getLoader(Class<T> interfaceClazz) {
        return getLoader(interfaceClazz, ClassLoader.getSystemClassLoader());
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
        return (T) instances.get(key).getObject();
    }

    public String getDefaultKey() {
        return this.defaultKey;
    }

    private synchronized void loadClazz() {
        if (this.defaultImpl != null || this.defaultKey != null) {
            return;
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

    private synchronized void loadSub() {
        if (isLoaded.get()) {
            return;
        }
        if (classLoader == null) {
            this.classLoader = ClassLoader.getSystemClassLoader();
        }
        // load all clazz names
        if (clazzNames.isEmpty()) {
            for (String path : PATH) {
                clazzNames.addAll(loadResources(path, this.clazz.getName()));
            }
        }
        Iterator<String> iterator = clazzNames.iterator();
        while (iterator.hasNext()) {
            String className = iterator.next();
            try {
                Class<?> clazz = Class.forName(className, false, classLoader);
                // if contains
                if (INSTANCE.containsKey(clazz.getName())) {
                    continue;
                }
                Holder holder = new Holder();
                INSTANCE.put(clazz.getName(), holder);
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
                if (clazz.getClass().isAssignableFrom(ServiceFactory.class)) {
                    inject(instance);
                }
                // put
                instances.put(key, holder);
                holder.setObject(instance);
                if (defaultKey.equals(key)) {
                    defaultImpl = (T) instance;
                }
            } catch (Exception e) {
                e.printStackTrace();
                INSTANCE.remove(clazz.getName());
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

    private Set<String> loadResources(String dir, String type) {
        String fileName = dir + type;
        Set<String> classNames = new HashSet<>();
        try {
            Enumeration<java.net.URL> urls = classLoader.getResources(fileName);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    java.net.URL resourceURL = urls.nextElement();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceURL.openStream(),
                            StandardCharsets.UTF_8))){
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
        return defaultImpl;
    }

    private void inject(Object instance) {
        if (serviceFactory != null) {
            Method[] allSetter = getAllSetter(instance.getClass());
            for (Method method : allSetter) {
                String key = getSetterProperty(method);
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

    private Method[] getAllSetter(Class<?> clz) {
        Method[] declaredMethods = clz.getDeclaredMethods();
        ArrayList<Method> res = new ArrayList<>();
        for (Method declaredMethod : declaredMethods) {
            if (declaredMethod.getName().startsWith("set")) {
                res.add(declaredMethod);
            }
        }
        return (Method[]) res.toArray();
    }
}
