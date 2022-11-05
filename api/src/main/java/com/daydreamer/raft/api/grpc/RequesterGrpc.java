package com.daydreamer.raft.api.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 **
 *service
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.42.0)",
    comments = "Source: grpc.proto")
public final class RequesterGrpc {

  private RequesterGrpc() {}

  public static final String SERVICE_NAME = "Requester";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<Message,
      Message> getRequestMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "request",
      requestType = Message.class,
      responseType = Message.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<Message,
      Message> getRequestMethod() {
    io.grpc.MethodDescriptor<Message, Message> getRequestMethod;
    if ((getRequestMethod = RequesterGrpc.getRequestMethod) == null) {
      synchronized (RequesterGrpc.class) {
        if ((getRequestMethod = RequesterGrpc.getRequestMethod) == null) {
          RequesterGrpc.getRequestMethod = getRequestMethod =
              io.grpc.MethodDescriptor.<Message, Message>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "request"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  Message.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  Message.getDefaultInstance()))
              .setSchemaDescriptor(new RequesterMethodDescriptorSupplier("request"))
              .build();
        }
      }
    }
    return getRequestMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static RequesterStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RequesterStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RequesterStub>() {
        @Override
        public RequesterStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RequesterStub(channel, callOptions);
        }
      };
    return RequesterStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static RequesterBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RequesterBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RequesterBlockingStub>() {
        @Override
        public RequesterBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RequesterBlockingStub(channel, callOptions);
        }
      };
    return RequesterBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static RequesterFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RequesterFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RequesterFutureStub>() {
        @Override
        public RequesterFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RequesterFutureStub(channel, callOptions);
        }
      };
    return RequesterFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   **
   *service
   * </pre>
   */
  public static abstract class RequesterImplBase implements io.grpc.BindableService {

    /**
     */
    public void request(Message request,
        io.grpc.stub.StreamObserver<Message> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRequestMethod(), responseObserver);
    }

    @Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getRequestMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                Message,
                Message>(
                  this, METHODID_REQUEST)))
          .build();
    }
  }

  /**
   * <pre>
   **
   *service
   * </pre>
   */
  public static final class RequesterStub extends io.grpc.stub.AbstractAsyncStub<RequesterStub> {
    private RequesterStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected RequesterStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RequesterStub(channel, callOptions);
    }

    /**
     */
    public void request(Message request,
        io.grpc.stub.StreamObserver<Message> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRequestMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   **
   *service
   * </pre>
   */
  public static final class RequesterBlockingStub extends io.grpc.stub.AbstractBlockingStub<RequesterBlockingStub> {
    private RequesterBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected RequesterBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RequesterBlockingStub(channel, callOptions);
    }

    /**
     */
    public Message request(Message request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRequestMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   **
   *service
   * </pre>
   */
  public static final class RequesterFutureStub extends io.grpc.stub.AbstractFutureStub<RequesterFutureStub> {
    private RequesterFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected RequesterFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RequesterFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<Message> request(
        Message request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRequestMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_REQUEST = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final RequesterImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(RequesterImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_REQUEST:
          serviceImpl.request((Message) request,
              (io.grpc.stub.StreamObserver<Message>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class RequesterBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    RequesterBaseDescriptorSupplier() {}

    @Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return RequestRpc.getDescriptor();
    }

    @Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Requester");
    }
  }

  private static final class RequesterFileDescriptorSupplier
      extends RequesterBaseDescriptorSupplier {
    RequesterFileDescriptorSupplier() {}
  }

  private static final class RequesterMethodDescriptorSupplier
      extends RequesterBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    RequesterMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (RequesterGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new RequesterFileDescriptorSupplier())
              .addMethod(getRequestMethod())
              .build();
        }
      }
    }
    return result;
  }
}
