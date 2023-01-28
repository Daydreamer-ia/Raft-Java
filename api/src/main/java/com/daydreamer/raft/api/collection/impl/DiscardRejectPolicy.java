package com.daydreamer.raft.api.collection.impl;

import com.daydreamer.raft.api.collection.RejectPolicy;

import java.util.Queue;

/**
 * @author Daydreamer
 */
public class DiscardRejectPolicy<E> implements RejectPolicy<E> {
    @Override
    public void reject(E ele, Queue<E> queue) {
        // nothing to do
    }
}
