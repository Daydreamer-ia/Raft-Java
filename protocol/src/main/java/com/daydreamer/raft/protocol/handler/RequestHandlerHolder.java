package com.daydreamer.raft.protocol.handler;


import com.daydreamer.raft.protocol.aware.RaftServerAware;
import com.daydreamer.raft.protocol.aware.StorageRepositoryAware;
import com.daydreamer.raft.protocol.core.AbstractRaftServer;
import com.daydreamer.raft.protocol.core.AbstractFollowerNotifier;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.aware.RaftMemberManagerAware;
import com.daydreamer.raft.protocol.storage.StorageRepository;
import com.daydreamer.raft.transport.constant.ResponseRepository;
import com.daydreamer.raft.api.entity.Request;
import com.daydreamer.raft.api.entity.Response;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Daydreamer
 * <p>
 * request handler registry
 */
public class RequestHandlerHolder {
    
    /**
     * registry
     */
    public static final Map<Class<? extends Request>, RequestHandler<Request, Response>> REGISTRY = new ConcurrentHashMap<>();
    
    private static final String HANDLER_PACKAGE = "com/daydreamer/raft/protocol/handler/impl";
    
    private static final String PACKAGE_SEPARATOR = ".";
    
    private static final String CLASS_FORMAT = ".class";
    
    private static final String EMPTY = "";
    
    private static RaftMemberManager raftMemberManager;
    
    private static AbstractRaftServer abstractRaftServer;
    
    private static StorageRepository storageRepository;
    
    /**
     * whether init
     */
    private static AtomicBoolean finishInit = new AtomicBoolean(false);
    
    /**
     * private constructor
     */
    private RequestHandlerHolder() {
    }
    
    /**
     * scan package and init
     *
     * @param raftMemberManager  raftMemberManager
     * @param storageRepository   storageRepository
     * @param abstractRaftServer abstractRaftServer
     */
    public synchronized static void init(RaftMemberManager raftMemberManager,
            AbstractRaftServer abstractRaftServer, StorageRepository storageRepository) {
        if (finishInit.get()) {
            return;
        }
        RequestHandlerHolder.raftMemberManager = raftMemberManager;
        RequestHandlerHolder.abstractRaftServer = abstractRaftServer;
        RequestHandlerHolder.storageRepository = storageRepository;
        try {
            // load instance
            File file = new File(Objects.requireNonNull(RequestHandlerHolder.class
                            .getClassLoader().getResource(HANDLER_PACKAGE)).getFile());
            File[] files = file.listFiles();
            String packagePrefix = HANDLER_PACKAGE.replaceAll("/", ".");
            if (files != null) {
                // register no args constructor handler
                for (File child : files) {
                    String clazzName = packagePrefix + PACKAGE_SEPARATOR + child.getName().replace(CLASS_FORMAT, EMPTY);
                    Class<?> clazz = Class.forName(clazzName);
                    RequestHandler<Request, Response> handler = (RequestHandler<Request, Response>) clazz.newInstance();
                    // inject aware
                    injectAware(handler);
                    // register
                    REGISTRY.put(handler.getSource(), handler);
                }
            }
            finishInit.set(true);
        } catch (Exception e) {
            throw new IllegalStateException("Can not load base handler for request", e);
        }
    }
    
    /**
     * inject aware
     *
     * @param handler handler
     */
    private static void injectAware(RequestHandler<Request, Response> handler) {
        if (handler instanceof RaftMemberManagerAware) {
            ((RaftMemberManagerAware) handler).setRaftMemberManager(raftMemberManager);
        }
        if (handler instanceof RaftServerAware) {
            ((RaftServerAware) handler).setRaftServer(abstractRaftServer);
        }
        if (handler instanceof StorageRepositoryAware) {
            ((StorageRepositoryAware) handler).setStorageRepository(storageRepository);
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
