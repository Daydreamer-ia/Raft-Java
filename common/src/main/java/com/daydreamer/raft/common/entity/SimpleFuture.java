package com.daydreamer.raft.common.entity;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Daydreamer
 * <p>
 * future
 */
public class SimpleFuture<T> implements Future<T> {
    
    private T data;
    
    private Exception throwable;
    
    /**
     * single thread pool
     */
    private ExecutorService executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<>(),
            r -> {
                Thread thread = new Thread(r);
                thread.setName("Future-Thread");
                thread.setDaemon(true);
                return thread;
            });
    
    private volatile boolean isCancel = false;
    
    private volatile boolean finish = false;
    
    public SimpleFuture(Callable<T> callable) {
        Runnable task = () -> {
            try {
                data = callable.call();
            } catch (Exception e) {
                // nothing to do
                throwable = e;
            }
            finish = true;
            // close
            executor.shutdown();
        };
        executor.execute(task);
    }
    
    @Override
    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        if (isCancel) {
            return isCancel;
        }
        isCancel = mayInterruptIfRunning;
        notifyAll();
        // clear
        executor.shutdown();
        return isCancel;
    }
    
    @Override
    public boolean isCancelled() {
        return isCancel;
    }
    
    @Override
    public boolean isDone() {
        return finish;
    }
    
    @Override
    public synchronized T get() throws InterruptedException {
        while (!finish) {
            wait(200);
        }
        return data;
    }
    
    @Override
    public synchronized T get(long timeout, TimeUnit unit) throws InterruptedException {
        long begin = System.currentTimeMillis();
        long remain = unit.toMillis(timeout);
        while (remain > 0 && !finish) {
            wait(200);
            remain = remain - (System.currentTimeMillis() - begin);
        }
        return data;
    }
    
    /**
     * if exception
     *
     * @return exception
     */
    public synchronized Exception getException() throws InterruptedException {
        while (!finish) {
            wait(200);
        }
        return throwable;
    }
}
