package com.daydreamer.raft.common.service;

import com.daydreamer.raft.common.utils.MD5Utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
    
    /**
     * file hash
     */
    private String hash = "";
    
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
        // init
        init();
    }
    
    /**
     * init method
     */
    private void init() {
        // load
        load();
        // add job
        executorService.execute(this::changeDetectJob);
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
            // calculate hash
            hash = MD5Utils.getFileMD5String(new File(filePath));
        } catch (Exception e) {
            LOGGER.severe("[PropertiesReader] - Fail to load properties, because: " + e.getLocalizedMessage());
        }
        return p;
    }
    
    
    /**
     * watch job
     */
    private void changeDetectJob() {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalStateException("[PropertiesReader] - Properties loss, file name: " + filePath);
        }
        try {
            String strHash = MD5Utils.getFileMD5String(file);
            // if change
            if (!strHash.equals(hash)) {
                populateProperties(load(), properties);
            }
        } catch (IOException e) {
            throw new IllegalStateException("[PropertiesReader] - Properties loss, file name: " + filePath);
        }
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
     * @param properties       properties
     * @param activeProperties active properties
     */
    public abstract void populateProperties(Properties properties, T activeProperties);
}
