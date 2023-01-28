package com.daydreamer.raft.common.threadpool.impl;

import com.daydreamer.raft.api.collection.MemorySafeLinkedBlockingQueue;
import com.daydreamer.raft.common.annotation.SPIImplement;
import com.daydreamer.raft.common.entity.RaftConfig;
import com.daydreamer.raft.common.threadpool.ThreadPoolFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@SPIImplement("cacheThreadPoolFactory")
public class CacheThreadPoolFactory implements ThreadPoolFactory {

    private Map<Object, Executor> executors = new ConcurrentHashMap<>();

    private RaftConfig raftConfig;

    public CacheThreadPoolFactory() {
        // add hook method to shut down all
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    @Override
    public Executor getExecutor(Object key) {
        Executor executor = executors.get(key);
        if (executor == null) {
            executor = new ThreadPoolExecutor(raftConfig.getDefaultThreadPoolCoreThread(),
                    raftConfig.getDefaultThreadPoolMaxThread(),
                    Integer.MAX_VALUE, TimeUnit.MICROSECONDS,
                    new MemorySafeLinkedBlockingQueue<>(),
                    (run) -> {
                        Thread thread = new Thread(run);
                        thread.setName("[CacheThreadPool] - Thread: " + thread.hashCode());
                        return thread;
                    }, new ThreadPoolExecutor.AbortPolicy());
            executors.putIfAbsent(key, executor);
        }
        return executors.get(key);
    }

    @Override
    public int getOrder() {
        return ThreadPoolFactory.MIN_PRIORITY;
    }

    private void shutdown() {
        executors.values().forEach((e) -> {
            if (e instanceof ThreadPoolExecutor) {
                ((ThreadPoolExecutor) e).shutdown();
            }
        });
    }

    public void setRaftConfig(RaftConfig activeProperties) {
        this.raftConfig = activeProperties;
    }
}
