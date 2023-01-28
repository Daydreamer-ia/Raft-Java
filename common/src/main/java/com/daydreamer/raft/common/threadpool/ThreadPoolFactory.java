package com.daydreamer.raft.common.threadpool;

import com.daydreamer.raft.common.annotation.SPI;

import java.util.concurrent.Executor;

/**
 * @author Daydreamer
 */
@SPI("adaptiveThreadPoolFactory")
public interface ThreadPoolFactory {

    /**
     * max priority
     */
    int MAX_PRIORITY = Integer.MAX_VALUE;

    /**
     * min priority
     */
    int MIN_PRIORITY = Integer.MIN_VALUE;

    /**
     * get {@link Executor} by key
     *
     * @param key key
     * @return Executor
     */
    Executor getExecutor(Object key);

    /**
     * the larger order, the faster invoke
     *
     * @return order
     */
    int getOrder();
}
