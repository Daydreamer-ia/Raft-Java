package com.daydreamer.raft.protocol.storage.impl;

import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.protocol.exception.LogException;
import com.daydreamer.raft.protocol.storage.ReplicatedStateMachine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MemoryReplicatedStateMachineTest {
    
    @Test
    void committedAppend() throws LogException {
        ReplicatedStateMachine replicatedStateMachine = new MemoryReplicatedStateMachine();
        LogEntry logEntry = new LogEntry(0, 0);
        Assertions.assertTrue(replicatedStateMachine.append(logEntry));
        logEntry = new LogEntry(0, 1);
        Assertions.assertTrue(replicatedStateMachine.append(logEntry));
        logEntry = new LogEntry(0, 2);
        Assertions.assertTrue(replicatedStateMachine.append(logEntry));
        logEntry = new LogEntry(0, 3);
        Assertions.assertTrue(replicatedStateMachine.append(logEntry));
        logEntry = new LogEntry(1, 4);
        Assertions.assertTrue(replicatedStateMachine.append(logEntry));
        // committed all
        Assertions.assertTrue(replicatedStateMachine.commit(1, 4));
        // committed again
        Assertions.assertTrue(replicatedStateMachine.commit(1, 4));
        
        // committed fail
        Assertions.assertFalse(replicatedStateMachine.commit(1, 5));
        
        // append more
        logEntry = new LogEntry(1, 5);
        Assertions.assertTrue(replicatedStateMachine.append(logEntry));
        logEntry = new LogEntry(1, 6);
        Assertions.assertTrue(replicatedStateMachine.append(logEntry));
        // committed all
        Assertions.assertTrue(replicatedStateMachine.commit(1, 6));
    }
    
    @Test
    void notCommittedAppend() throws LogException {
        ReplicatedStateMachine replicatedStateMachine = new MemoryReplicatedStateMachine();
        LogEntry logEntry = new LogEntry(0, 0);
        Assertions.assertTrue(replicatedStateMachine.append(logEntry));
        logEntry = new LogEntry(0, 1);
        Assertions.assertTrue(replicatedStateMachine.append(logEntry));
        logEntry = new LogEntry(0, 2);
        Assertions.assertTrue(replicatedStateMachine.append(logEntry));
        logEntry = new LogEntry(0, 3);
        Assertions.assertTrue(replicatedStateMachine.append(logEntry));
        logEntry = new LogEntry(1, 4);
        Assertions.assertTrue(replicatedStateMachine.append(logEntry));
        
        // append fail
        logEntry = new LogEntry(1, 6);
        Assertions.assertFalse(replicatedStateMachine.append(logEntry));
        logEntry = new LogEntry(1, 7);
        Assertions.assertFalse(replicatedStateMachine.append(logEntry));
        logEntry = new LogEntry(1, 8);
        Assertions.assertFalse(replicatedStateMachine.append(logEntry));
        
        // append true
        logEntry = new LogEntry(2, 5);
        Assertions.assertTrue(replicatedStateMachine.append(logEntry));
        logEntry = new LogEntry(2, 6);
        Assertions.assertTrue(replicatedStateMachine.append(logEntry));
        
        // cover
        logEntry = new LogEntry(1, 5);
        Assertions.assertTrue(replicatedStateMachine.append(logEntry));
        logEntry = new LogEntry(1, 6);
        Assertions.assertTrue(replicatedStateMachine.append(logEntry));
    }
}