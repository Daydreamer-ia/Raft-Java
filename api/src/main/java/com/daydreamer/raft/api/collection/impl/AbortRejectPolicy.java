package com.daydreamer.raft.api.collection.impl;

import com.daydreamer.raft.api.collection.RejectPolicy;

import java.util.Queue;
import java.util.concurrent.RejectedExecutionException;

/**
 * @author Daydreamer
 */
public class AbortRejectPolicy<E> implements RejectPolicy<E> {

    @Override
    public void reject(E ele, Queue<E> queue) {
        throw new RejectedExecutionException("No more memory can be used!");
    }
}
