package com.daydreamer.raft.protocol.entity;

import com.daydreamer.raft.protocol.constant.NodeRole;
import com.daydreamer.raft.protocol.constant.NodeStatus;
import com.daydreamer.raft.transport.connection.Connection;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Daydreamer
 * <p>
 * It is the mapping of member node
 */
public class Member {
    
    /**
     * member id
     */
    private String memberId;
    
    /**
     * ip
     */
    private String ip;
    
    /**
     * port
     */
    private int port;
    
    /**
     * ip:port or domain
     */
    private String address;
    
    /**
     * last active time
     */
    private long lastActiveTime;
    
    /**
     * term of member
     */
    private AtomicInteger term = new AtomicInteger(0);
    
    /**
     * id in term
     */
    private AtomicLong logId = new AtomicLong(0);
    
    /**
     * role
     */
    private AtomicReference<NodeRole> role = new AtomicReference<>(NodeRole.FOLLOWER);
    
    /**
     * healthy or not
     */
    private AtomicReference<NodeStatus> status = new AtomicReference<>(NodeStatus.DOWN);
    
    /**
     * conn
     */
    private Connection connection;
    
    public void setTerm(int term) {
        this.term.set(term);
    }
    
    public void setLogId(long logId) {
        this.logId.set(logId);
    }
    
    public long getLastActiveTime() {
        return lastActiveTime;
    }
    
    public void setLastActiveTime(long lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    public void setConnection(Connection connection) {
        this.connection = connection;
    }
    
    public NodeStatus getStatus() {
        return status.get();
    }
    
    public String getMemberId() {
        return memberId;
    }
    
    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }
    
    public NodeRole getRole() {
        return role.get();
    }
    
    public boolean setRole(NodeRole newRole, NodeRole oldRole) {
        return this.role.compareAndSet(oldRole, newRole);
    }
    
    public boolean setStatus(NodeStatus newStatus, NodeStatus expected) {
        return this.status.compareAndSet(expected, newStatus);
    }
    
    public void setStatus(NodeStatus newStatus) {
        this.status.set(newStatus);
    }
    
    public void setRole(NodeRole newRole) {
        this.role.set(newRole);
    }
    
    public int getTerm() {
        return term.get();
    }
    
    public void increaseTerm() {
        this.term.incrementAndGet();
    }
    
    public long getLogId() {
        return logId.get();
    }
    
    public void increaseLogId() {
        this.logId.incrementAndGet();
    }
    
    public String getIp() {
        return ip;
    }
    
    public void setIp(String ip) {
        this.ip = ip;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
}
