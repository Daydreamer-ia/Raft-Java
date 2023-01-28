package com.daydreamer.raft.api.collection;

import java.util.Queue;

/**
 * @author Daydreamer
 */
public interface RejectPolicy<E> {

    void reject(E ele, Queue<E> queue);
}
