package com.daydreamer.raft.protocol.handler;


import com.daydreamer.raft.transport.constant.ResponseRepository;
import com.daydreamer.raft.transport.entity.Request;
import com.daydreamer.raft.transport.entity.Response;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    
    /**
     * private constructor
     */
    private RequestHandlerHolder() {}
    
    static {
        try {
            // load instance
            File file = new File(CLASSPATH_PREFIX + HANDLER_PACKAGE);
            File[] files = file.listFiles();
            String packagePrefix = HANDLER_PACKAGE.replaceAll("/", ".");
            if (files != null) {
                for (File child : files) {
                    String clazzName = packagePrefix + PACKAGE_SEPARATOR + child.getName().replace(".java", "");
                    Class<?> clazz = Class.forName(clazzName);
                    RequestHandler<Request, Response> handler = (RequestHandler<Request, Response>) clazz.newInstance();
                    REGISTRY.put(handler.getSource(), handler);
                }
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
    public static Response handle(Request request) {
        RequestHandler<Request, Response> handler = REGISTRY.get(request.getClass());
        // if cannot find handler
        if (handler == null) {
            return ResponseRepository.NOT_HANDLER_FOUND;
        }
        return handler.handle(request);
    }
}
