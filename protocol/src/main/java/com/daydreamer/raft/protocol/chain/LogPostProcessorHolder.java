package com.daydreamer.raft.protocol.chain;

import com.daydreamer.raft.protocol.handler.RequestHandlerHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Daydreamer
 * <p>
 * registry for {@link LogPostProcessor}
 */
public class LogPostProcessorHolder {
    
    private static final String PROCESSOR_PACKAGE = "com/daydreamer/raft/protocol/chain/impl";
    
    private static final String PACKAGE_SEPARATOR = ".";
    
    private static final String CLASS_FORMAT = ".class";
    
    private static final String EMPTY = "";
    
    private List<LogPostProcessor> postProcessors = new ArrayList<>();
    
    /**
     * whether init
     */
    private AtomicBoolean finishInit = new AtomicBoolean(false);
    
    public LogPostProcessorHolder() {
        init();
    }
    
    /**
     * init {@link LogPostProcessor}
     */
    private synchronized void init() {
        if (finishInit.get()) {
            return;
        }
        try {
            // load instance
            File file = new File(
                    Objects.requireNonNull(RequestHandlerHolder.class.getClassLoader().getResource(PROCESSOR_PACKAGE))
                            .getFile());
            File[] files = file.listFiles();
            String packagePrefix = PROCESSOR_PACKAGE.replaceAll("/", ".");
            if (files != null) {
                // register no args constructor handler
                for (File child : files) {
                    String clazzName = packagePrefix + PACKAGE_SEPARATOR + child.getName().replace(CLASS_FORMAT, EMPTY);
                    Class<?> clazz = Class.forName(clazzName);
                    LogPostProcessor logPostProcessor = (LogPostProcessor) clazz.newInstance();
                    postProcessors.add(logPostProcessor);
                }
                finishInit.set(true);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Can not load base processor for request", e);
        }
    }
    
    /**
     * get all processors
     *
     * @return all processors
     */
    public List<LogPostProcessor> getPostProcessors() {
        return Collections.unmodifiableList(postProcessors);
    }
    
    /**
     * register new processor
     *
     * @param logPostProcessor new processor
     */
    public void register(LogPostProcessor logPostProcessor) {
        postProcessors.add(logPostProcessor);
    }
}
