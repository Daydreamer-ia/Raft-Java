package com.daydreamer.raft.common.service;

import com.daydreamer.raft.common.utils.MD5Utils;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Daydreamer
 */
public abstract class PropertiesReader<T extends ActiveProperties> {
    
    private static final Logger LOGGER = Logger.getLogger(PropertiesReader.class);
    
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
    
    /**
     * whether open
     */
    private boolean open;
    
    public PropertiesReader(String filePath, T properties, boolean open) {
        this.filePath = filePath;
        this.properties = properties;
        this.open = open;
        if (this.open) {
            executorService = new ThreadPoolExecutor(1, 1, 1000, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<>(), r -> {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("Watch-Dog-Thread-For-File: " + filePath);
                thread.setUncaughtExceptionHandler((t, e) -> {
                    LOGGER.error("Fail to execute watch job, because: " + e.getLocalizedMessage());
                });
                return thread;
            });
            // init
            init();
        }
    }
    
    public boolean isOpen() {
        return open;
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
        InputStream in = null;
        try {
            // load
            File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource(filePath)).getFile());
            if (!file.exists()) {
                throw new IllegalArgumentException("[PropertiesReader] - Cannot find property file, bad path: " + filePath);
            }
            in = new BufferedInputStream(new FileInputStream(file));
            p.load(in);
            // populate
            populateProperties(p, properties);
            // calculate hash
            hash = MD5Utils.getFileMD5String(file);
        } catch (Exception e) {
            LOGGER.error("Fail to load properties, because: " + e.getLocalizedMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // nothing to do
                }
            }
        }
        return p;
    }
    
    
    /**
     * watch job
     */
    private void changeDetectJob() {
        File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource(filePath)).getFile());
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
     * close
     */
    public void close() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }
    
    /**
     * base on the properties to do some
     *
     * @param properties       properties
     * @param activeProperties active properties
     */
    public abstract void populateProperties(Properties properties, T activeProperties);
}
