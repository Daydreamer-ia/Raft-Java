package com.daydreamer.raft.protocol.core.impl;

import com.daydreamer.raft.api.entity.Request;
import com.daydreamer.raft.api.entity.Response;
import com.daydreamer.raft.api.entity.base.CommittedResponse;
import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.api.entity.request.AppendEntriesRequest;
import com.daydreamer.raft.api.entity.response.AppendEntriesResponse;
import com.daydreamer.raft.api.entity.response.ServerErrorResponse;
import com.daydreamer.raft.protocol.core.LogSender;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.entity.Member;
import com.daydreamer.raft.protocol.storage.ReplicatedStateMachine;
import com.daydreamer.raft.transport.connection.Connection;
import com.daydreamer.raft.transport.connection.ResponseCallBack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * @author Daydreamer
 */
public class DefaultLogSender implements LogSender {
    
    private Logger LOGGER = LoggerFactory.getLogger(DefaultLogSender.class.getSimpleName());
    
    private RaftMemberManager raftMemberManager;
    
    private ReplicatedStateMachine replicatedStateMachine;
    
    public DefaultLogSender(RaftMemberManager raftMemberManager, ReplicatedStateMachine replicatedStateMachine) {
        this.replicatedStateMachine = replicatedStateMachine;
        this.raftMemberManager = raftMemberManager;
    }
    
    @Override
    public synchronized boolean appendLog(Member member, LogEntry logEntry) throws Exception {
        // append log request
        ArrayList<LogEntry> logEntries = new ArrayList<LogEntry>();
        logEntries.add(logEntry);
        AppendEntriesRequest originRequest = buildAppendLogRequest(logEntries);
        Connection connection = member.getConnection();
        if (connection != null) {
            AppendEntriesResponse response = new AppendEntriesResponse(false);
            // need to syn log
            while (!response.isAccepted()) {
                member.setLogId(originRequest.getLastLogId() - 1);
                Future<Response> future = connection.request(originRequest);
                // block wait
                Response commonResponse = future.get();
                // member may down
                // copy log next time
                if (commonResponse == null) {
                    return false;
                }
                // if submit log has committed
                if (commonResponse instanceof ServerErrorResponse) {
                    LOGGER.error("Node has committed the log, member: " + member.getAddress());
                    return false;
                } else if (commonResponse instanceof AppendEntriesResponse) {
                    response = (AppendEntriesResponse) commonResponse;
                }
                // find last log
                if (originRequest.getLastLogId() - 1 < 0) {
                    originRequest.setLastTerm(0);
                    originRequest.setLastLogId(-1);
                    continue;
                }
                LogEntry lastLog = replicatedStateMachine.getLogById(originRequest.getLastLogId() - 1);
                int lastTerm = 0;
                long lastLogId = -1;
                if (originRequest.getLastLogId() - 2 >= 0) {
                    LogEntry lastLastLog = replicatedStateMachine.getLogById(originRequest.getLastLogId() - 2);
                    lastLogId = lastLastLog.getLogId();
                    lastTerm = lastLastLog.getTerm();
                }
                originRequest.setLastTerm(lastTerm);
                originRequest.setLastLogId(lastLogId);
                originRequest.getLogEntries().add(0, lastLog);
            }
            return true;
        }
        return false;
    }
    
    /**
     * build request
     *
     * @param logEntries log entries
     */
    private AppendEntriesRequest buildAppendLogRequest(List<LogEntry> logEntries) {
        Member self = raftMemberManager.getSelf();
        AppendEntriesRequest appendEntriesRequest = new AppendEntriesRequest();
        appendEntriesRequest.setCurrentLogId(self.getLogId());
        appendEntriesRequest.setCurrentTerm(self.getTerm());
        appendEntriesRequest.setLogEntries(logEntries);
        appendEntriesRequest.setPayload(true);
        long lastLogId = logEntries.get(0).getLogId() - 1;
        int lastTerm = 0;
        if (lastLogId >= 0) {
            LogEntry lastLog = replicatedStateMachine.getLogById(lastLogId);
            lastTerm = lastLog.getTerm();
        }
        appendEntriesRequest.setLastLogId(lastLogId);
        appendEntriesRequest.setLastTerm(lastTerm);
        return appendEntriesRequest;
    }
    
    @Override
    public synchronized boolean commit(Member member, Request request) throws Exception {
        Connection connection = member.getConnection();
        if (connection != null) {
            Response response = connection.request(request, 2500);
            if (response instanceof CommittedResponse) {
                return ((CommittedResponse) response).isAccepted();
            }
        }
        return false;
    }
    
    @Override
    public boolean batchRequestMembers(Request request, List<Member> members, Predicate<Response> predicate)
            throws Exception {
        // begin to request
        AtomicInteger count = new AtomicInteger(1);
        CountDownLatch countDownLatch = new CountDownLatch(members.size());
        for (Member member : members) {
            try {
                Connection connection = member.getConnection();
                connection.request(request, new ResponseCallBack() {
                
                    @Override
                    public void onSuccess(Response response) {
                        if (predicate.test(response)) {
                            count.incrementAndGet();
                        }
                        countDownLatch.countDown();
                    }
                
                    @Override
                    public void onFail(Exception e) {
                        // nothing to do
                        countDownLatch.countDown();
                    }
                
                    @Override
                    public void onTimeout() {
                        // nothing to do
                    }
                });
            } catch (Exception e) {
                // nothing to do
                e.printStackTrace();
            }
        }
        countDownLatch.await(members.size() * 5000, TimeUnit.MICROSECONDS);
        return count.get() > (raftMemberManager.getAllMember().size() + 1) / 2;
    }
}
