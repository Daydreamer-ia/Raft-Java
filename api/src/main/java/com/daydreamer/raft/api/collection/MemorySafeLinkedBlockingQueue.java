package com.daydreamer.raft.api.collection;

import com.daydreamer.raft.api.collection.impl.AbortRejectPolicy;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Daydreamer
 */
public class MemorySafeLinkedBlockingQueue<E> extends LinkedBlockingQueue<E> {

    /**
     * default reject policy
     */
    private static final RejectPolicy DEFAULT_REJECT_POLICY = new AbortRejectPolicy<>();

    /**
     * default min memory at least
     */
    private static final long DEFAULT_MIN_MEMORY_AT_LEAST = 200 * 1024;

    /**
     * leave 200m at least
     */
    private long minMemoryAtLeast = DEFAULT_MIN_MEMORY_AT_LEAST;

    /**
     * reject policy
     */
    private RejectPolicy<E> rejectPolicy = DEFAULT_REJECT_POLICY;

    public MemorySafeLinkedBlockingQueue(long minMemoryAtLeast) {
        this.minMemoryAtLeast = minMemoryAtLeast;
    }

    public MemorySafeLinkedBlockingQueue(RejectPolicy<E> rejectPolicy) {
        this.rejectPolicy = rejectPolicy;
    }

    public MemorySafeLinkedBlockingQueue(long minMemoryAtLeast, RejectPolicy<E> rejectPolicy) {
        this.minMemoryAtLeast = minMemoryAtLeast;
        this.rejectPolicy = rejectPolicy;
    }

    public MemorySafeLinkedBlockingQueue(int capacity, long minMemoryAtLeast, RejectPolicy<E> rejectPolicy) {
        super(capacity);
        this.minMemoryAtLeast = minMemoryAtLeast;
        this.rejectPolicy = rejectPolicy;
    }

    public MemorySafeLinkedBlockingQueue(Collection<? extends E> c, long minMemoryAtLeast, RejectPolicy<E> rejectPolicy) {
        super(c);
        this.minMemoryAtLeast = minMemoryAtLeast;
        this.rejectPolicy = rejectPolicy;
    }

    /**
     * is there free memory
     *
     * @return whether allow to operate
     */
    private boolean hasFreeMemory() {
        return Runtime.getRuntime().freeMemory() > minMemoryAtLeast;
    }

    @Override
    public void put(final E e) throws InterruptedException {
        if (hasFreeMemory()) {
            super.put(e);
        } else {
            rejectPolicy.reject(e, this);
        }
    }

    public long getMinMemoryAtLeast() {
        return minMemoryAtLeast;
    }

    public void setMinMemoryAtLeast(long minMemoryAtLeast) {
        this.minMemoryAtLeast = minMemoryAtLeast;
    }

    @Override
    public boolean offer(final E e, final long timeout, final TimeUnit unit) throws InterruptedException {
        if (!hasFreeMemory()) {
            return false;
        }
        return super.offer(e, timeout, unit);
    }

    @Override
    public boolean offer(final E e) {
        if (!hasFreeMemory()) {
            rejectPolicy.reject(e, this);
            return false;
        }
        return super.offer(e);
    }
}
