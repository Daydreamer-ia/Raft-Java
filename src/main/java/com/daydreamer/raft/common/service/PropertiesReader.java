package com.daydreamer.raft.common.service;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author Daydreamer
 */
public abstract class PropertiesReader<T extends ActiveProperties> {
    
    private static final Logger LOGGER = Logger.getLogger(PropertiesReader.class.getSimpleName());
    
    /**
     * watch dog
     */
    private WatchService watchService;
    
    /**
     * target file
     */
    private String filePath;
    
    /**
     * executor
     */
    private ExecutorService executorService;
    
    /**
     * properties loader
     */
    private T properties;
    
    public PropertiesReader(String filePath, T properties) {
        this.filePath = filePath;
        this.properties = properties;
        executorService = new ThreadPoolExecutor(1, 1, 1000, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<>(), r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("Watch-Dog-Thread-For-File: " + filePath);
            thread.setUncaughtExceptionHandler((t, e) -> {
                LOGGER.severe("[PropertiesReader] - Fail to execute watch job, because: " + e.getLocalizedMessage());
            });
            return thread;
        });
        // load
        load();
        // listen
        listen();
        // add job
        executorService.execute(this::init);
    }
    
    /**
     * load properties
     *
     * @return properties
     */
    private Properties load() {
        Properties p = new Properties();
        try {
            // load
            InputStream in = new BufferedInputStream(new FileInputStream(filePath));
            p.load(in);
            // populate
            populateProperties(p, properties);
        } catch (Exception e) {
            LOGGER.severe("[PropertiesReader] - Fail to load properties, because: " + e.getLocalizedMessage());
        }
        return p;
    }
    
    private void listen() {
        // TODO here are some problem
//        try {
//            // listen
//            watchService = FileSystems.getDefault().newWatchService();
//            Paths.get(filePath).register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
//        } catch (Exception e) {
//            e.printStackTrace();
//            LOGGER.severe("[PropertiesReader] - Fail to listen to properties, because: " + e.getMessage());
//        }
    }
    
    /**
     * watch job
     */
    private void init() {
        // TODO here are some problem
//        while (true) {
//            try {
//                WatchKey key = null;
//                while ((key = watchService.take()) != null) {
//                    // there is a modify event here
//                    populateProperties(load(), properties);
//                    // reset listen poll
//                    key.reset();
//                }
//            } catch (Exception e) {
//                // nothing to do
//                LOGGER.severe("[PropertiesReader] - Fail to action when file change, because: " + e.getLocalizedMessage());
//            }
//        }
    }
    
    /**
     * properties
     *
     * @return properties
     */
    public T getProperties() {
        return properties;
    }
    
    /**
     * base on the properties to do some
     *
     * @param properties properties
     * @param activeProperties active properties
     */
    public abstract void populateProperties(Properties properties, T activeProperties);
}
