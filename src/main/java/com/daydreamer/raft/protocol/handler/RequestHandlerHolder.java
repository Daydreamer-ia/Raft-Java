package com.daydreamer.raft.protocol.handler;


import com.daydreamer.raft.protocol.aware.RaftMemberManagerAware;
import com.daydreamer.raft.protocol.aware.RaftServerAware;
import com.daydreamer.raft.protocol.core.AbstractRaftServer;
import com.daydreamer.raft.protocol.core.FollowerNotifier;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.transport.constant.ResponseRepository;
import com.daydreamer.raft.transport.entity.Request;
import com.daydreamer.raft.transport.entity.Response;
import com.sun.org.apache.bcel.internal.generic.FADD;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Daydreamer
 *
 * request handler registry
 */
public class RequestHandlerHolder {
    
    /**
     * registry
     */
    public static final Map<Class<? extends Request>, RequestHandler<Request, Response>> REGISTRY = new ConcurrentHashMap<>();
    
    private static final String HANDLER_PACKAGE = "com/daydreamer/raft/protocol/handler/impl";
    
    private static final String CLASSPATH_PREFIX = "src/main/java/";
    
    private static final String PACKAGE_SEPARATOR = ".";
    
    private static RaftMemberManager raftMemberManager;
    
    private static FollowerNotifier followerNotifier;
    
    private static AbstractRaftServer abstractRaftServer;
    
    /**
     * whether init
     */
    private static AtomicBoolean finishInit = new AtomicBoolean(false);
    
    /**
     * private constructor
     */
    private RequestHandlerHolder() {}
    
    /**
     * scan package and init
     *
     * @param raftMemberManager raftMemberManager
     * @param followerNotifier followerNotifier
     * @param abstractRaftServer abstractRaftServer
     */
    public synchronized static void init(RaftMemberManager raftMemberManager, FollowerNotifier followerNotifier,AbstractRaftServer abstractRaftServer) {
        if (finishInit.get()) {
            return;
        }
        RequestHandlerHolder.raftMemberManager = raftMemberManager;
        RequestHandlerHolder.abstractRaftServer = abstractRaftServer;
        RequestHandlerHolder.followerNotifier = followerNotifier;
        try {
            // load instance
            File file = new File(CLASSPATH_PREFIX + HANDLER_PACKAGE);
            File[] files = file.listFiles();
            String packagePrefix = HANDLER_PACKAGE.replaceAll("/", ".");
            if (files != null) {
                // register no args constructor handler
                for (File child : files) {
                    String clazzName = packagePrefix + PACKAGE_SEPARATOR + child.getName().replace(".java", "");
                    Class<?> clazz = Class.forName(clazzName);
                    RequestHandler<Request, Response> handler = (RequestHandler<Request, Response>) clazz.newInstance();
                    if (handler instanceof RaftMemberManagerAware) {
                        ((RaftMemberManagerAware) handler).setRaftMemberManager(raftMemberManager);
                    }
                    if (handler instanceof RaftServerAware) {
                        ((RaftServerAware) handler).setRaftServer(abstractRaftServer);
                    }
                    REGISTRY.put(handler.getSource(), handler);
                }
            }
            finishInit.set(true);
        } catch (Exception e) {
            throw new IllegalStateException("Can not load base handler for request", e);
        }
    }
    
    /**
     * delegate the request to handler
     *
     * @param request request
     * @return response
     */
    public static Response handle(Request request) {
        RequestHandler<Request, Response> handler = REGISTRY.get(request.getClass());
        // if cannot find handler
        if (handler == null) {
            return ResponseRepository.NOT_HANDLER_FOUND;
        }
        return handler.handle(request);
    }
    
    /**
     * register new handler
     *
     * @param handler handler
     */
    public static void register(RequestHandler<? extends Request, ? extends Response> handler) {
        if (!REGISTRY.containsKey(handler.getSource())) {
            REGISTRY.put(handler.getSource(), (RequestHandler<Request, Response>) handler);
        }
    }
}
