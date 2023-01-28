package com.daydreamer.raft.api.collection.impl;

import com.daydreamer.raft.api.collection.RejectPolicy;

import java.util.Queue;

/**
 * @author Daydreamer
 */
public class DiscardOldestRejectPolicy<E> implements RejectPolicy<E> {

    @Override
    public void reject(E ele, Queue<E> queue) {
        queue.poll();
        queue.add(ele);
    }
}
