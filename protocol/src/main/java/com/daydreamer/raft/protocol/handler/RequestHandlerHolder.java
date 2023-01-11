package com.daydreamer.raft.protocol.handler;


import com.daydreamer.raft.common.annotation.SPIMethodInit;
import com.daydreamer.raft.common.loader.RaftServiceLoader;
import com.daydreamer.raft.transport.constant.ResponseRepository;
import com.daydreamer.raft.api.entity.Request;
import com.daydreamer.raft.api.entity.Response;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Daydreamer
 * <p>
 * request handler registry
 */
@SuppressWarnings("all")
public class RequestHandlerHolder {

    /**
     * registry
     */
    public final Map<Class<? extends Request>, RequestHandler<Request, Response>> registry = new ConcurrentHashMap<>();

    private final String groupKey;

    public RequestHandlerHolder(String groupKey) {
        this.groupKey = groupKey;
        init();
    }

    private void init() {
        try {
            RaftServiceLoader<RequestHandler> loader = RaftServiceLoader.getLoader(groupKey, RequestHandler.class);
            for (RequestHandler handler : loader.getAll()) {
                registry.put(handler.getSource(), handler);
            }
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
    public Response handle(Request request) {
        RequestHandler<Request, Response> handler = registry.get(request.getClass());
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
    public void register(RequestHandler<? extends Request, ? extends Response> handler) {
        if (!registry.containsKey(handler.getSource())) {
            registry.put(handler.getSource(), (RequestHandler<Request, Response>) handler);
        }
    }
}
