package com.daydreamer.raft.common.threadpool.impl;

import com.daydreamer.raft.common.annotation.SPIImplement;
import com.daydreamer.raft.common.annotation.SPIMethodInit;
import com.daydreamer.raft.common.loader.GroupAware;
import com.daydreamer.raft.common.loader.RaftServiceLoader;
import com.daydreamer.raft.common.threadpool.ThreadPoolFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * @author Daydreamer
 */
@SPIImplement("adaptiveThreadPoolFactory")
public class AdaptiveThreadPoolFactory implements ThreadPoolFactory, GroupAware {

    private final List<ThreadPoolFactory> threadPoolFactories = new ArrayList<>();

    private String groupKey;

    @SPIMethodInit
    private void init() {
        // add all thread pool
        List<ThreadPoolFactory> all = RaftServiceLoader
                .getLoader(groupKey, ThreadPoolFactory.class)
                .getAll();
        threadPoolFactories.addAll(all);
        threadPoolFactories.remove(this);
        threadPoolFactories.sort(Comparator.comparingInt(ThreadPoolFactory::getOrder));
    }

    @Override
    public Executor getExecutor(Object key) {
        for (ThreadPoolFactory threadPoolFactory : threadPoolFactories) {
            Executor executor = threadPoolFactory.getExecutor(key);
            if (executor != null) {
                return executor;
            }
        }
        return null;
    }

    @Override
    public int getOrder() {
        return ThreadPoolFactory.MAX_PRIORITY;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }
}
