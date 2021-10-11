package com.welab.wefe.gateway.api.service.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 *
 */
@javax.annotation.Generated(
        value = "by gRPC proto compiler (version 1.29.0)",
        comments = "Source: gateway-service.proto")
public final class TransferServiceGrpc {

    private TransferServiceGrpc() {
    }

    public static final String SERVICE_NAME = "com.welab.wefe.gateway.api.service.proto.TransferService";

    // Static method descriptors that strictly reflect the proto.
    private static volatile io.grpc.MethodDescriptor<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta,
            com.welab.wefe.gateway.api.meta.basic.BasicMetaProto.ReturnStatus> getSendMethod;

    @io.grpc.stub.annotations.RpcMethod(
            fullMethodName = SERVICE_NAME + '/' + "send",
            requestType = com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta.class,
            responseType = com.welab.wefe.gateway.api.meta.basic.BasicMetaProto.ReturnStatus.class,
            methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta,
            com.welab.wefe.gateway.api.meta.basic.BasicMetaProto.ReturnStatus> getSendMethod() {
        io.grpc.MethodDescriptor<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta, com.welab.wefe.gateway.api.meta.basic.BasicMetaProto.ReturnStatus> getSendMethod;
        if ((getSendMethod = TransferServiceGrpc.getSendMethod) == null) {
            synchronized (TransferServiceGrpc.class) {
                if ((getSendMethod = TransferServiceGrpc.getSendMethod) == null) {
                    TransferServiceGrpc.getSendMethod = getSendMethod =
                            io.grpc.MethodDescriptor.<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta, com.welab.wefe.gateway.api.meta.basic.BasicMetaProto.ReturnStatus>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(SERVICE_NAME, "send"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta.getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.welab.wefe.gateway.api.meta.basic.BasicMetaProto.ReturnStatus.getDefaultInstance()))
                                    .setSchemaDescriptor(new TransferServiceMethodDescriptorSupplier("send"))
                                    .build();
                }
            }
        }
        return getSendMethod;
    }

    private static volatile io.grpc.MethodDescriptor<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta,
            com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta> getRecvMethod;

    @io.grpc.stub.annotations.RpcMethod(
            fullMethodName = SERVICE_NAME + '/' + "recv",
            requestType = com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta.class,
            responseType = com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta.class,
            methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta,
            com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta> getRecvMethod() {
        io.grpc.MethodDescriptor<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta, com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta> getRecvMethod;
        if ((getRecvMethod = TransferServiceGrpc.getRecvMethod) == null) {
            synchronized (TransferServiceGrpc.class) {
                if ((getRecvMethod = TransferServiceGrpc.getRecvMethod) == null) {
                    TransferServiceGrpc.getRecvMethod = getRecvMethod =
                            io.grpc.MethodDescriptor.<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta, com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(SERVICE_NAME, "recv"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta.getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta.getDefaultInstance()))
                                    .setSchemaDescriptor(new TransferServiceMethodDescriptorSupplier("recv"))
                                    .build();
                }
            }
        }
        return getRecvMethod;
    }

    private static volatile io.grpc.MethodDescriptor<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta,
            com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta> getCheckStatusNowMethod;

    @io.grpc.stub.annotations.RpcMethod(
            fullMethodName = SERVICE_NAME + '/' + "checkStatusNow",
            requestType = com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta.class,
            responseType = com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta.class,
            methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta,
            com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta> getCheckStatusNowMethod() {
        io.grpc.MethodDescriptor<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta, com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta> getCheckStatusNowMethod;
        if ((getCheckStatusNowMethod = TransferServiceGrpc.getCheckStatusNowMethod) == null) {
            synchronized (TransferServiceGrpc.class) {
                if ((getCheckStatusNowMethod = TransferServiceGrpc.getCheckStatusNowMethod) == null) {
                    TransferServiceGrpc.getCheckStatusNowMethod = getCheckStatusNowMethod =
                            io.grpc.MethodDescriptor.<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta, com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(SERVICE_NAME, "checkStatusNow"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta.getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta.getDefaultInstance()))
                                    .setSchemaDescriptor(new TransferServiceMethodDescriptorSupplier("checkStatusNow"))
                                    .build();
                }
            }
        }
        return getCheckStatusNowMethod;
    }

    /**
     * Creates a new async stub that supports all call types for the service
     */
    public static TransferServiceStub newStub(io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<TransferServiceStub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<TransferServiceStub>() {
                    @Override
                    public TransferServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new TransferServiceStub(channel, callOptions);
                    }
                };
        return TransferServiceStub.newStub(factory, channel);
    }

    /**
     * Creates a new blocking-style stub that supports unary and streaming output calls on the service
     */
    public static TransferServiceBlockingStub newBlockingStub(
            io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<TransferServiceBlockingStub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<TransferServiceBlockingStub>() {
                    @Override
                    public TransferServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new TransferServiceBlockingStub(channel, callOptions);
                    }
                };
        return TransferServiceBlockingStub.newStub(factory, channel);
    }

    /**
     * Creates a new ListenableFuture-style stub that supports unary calls on the service
     */
    public static TransferServiceFutureStub newFutureStub(
            io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<TransferServiceFutureStub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<TransferServiceFutureStub>() {
                    @Override
                    public TransferServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new TransferServiceFutureStub(channel, callOptions);
                    }
                };
        return TransferServiceFutureStub.newStub(factory, channel);
    }

    /**
     *
     */
    public static abstract class TransferServiceImplBase implements io.grpc.BindableService {

        /**
         *
         */
        public void send(com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta request,
                         io.grpc.stub.StreamObserver<com.welab.wefe.gateway.api.meta.basic.BasicMetaProto.ReturnStatus> responseObserver) {
            asyncUnimplementedUnaryCall(getSendMethod(), responseObserver);
        }

        /**
         *
         */
        public void recv(com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta request,
                         io.grpc.stub.StreamObserver<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta> responseObserver) {
            asyncUnimplementedUnaryCall(getRecvMethod(), responseObserver);
        }

        /**
         * <pre>
         * check the transfer status, return immediately
         * </pre>
         */
        public void checkStatusNow(com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta request,
                                   io.grpc.stub.StreamObserver<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta> responseObserver) {
            asyncUnimplementedUnaryCall(getCheckStatusNowMethod(), responseObserver);
        }

        @Override
        public final io.grpc.ServerServiceDefinition bindService() {
            return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
                    .addMethod(
                            getSendMethod(),
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta,
                                            com.welab.wefe.gateway.api.meta.basic.BasicMetaProto.ReturnStatus>(
                                            this, METHODID_SEND)))
                    .addMethod(
                            getRecvMethod(),
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta,
                                            com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta>(
                                            this, METHODID_RECV)))
                    .addMethod(
                            getCheckStatusNowMethod(),
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta,
                                            com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta>(
                                            this, METHODID_CHECK_STATUS_NOW)))
                    .build();
        }
    }

    /**
     *
     */
    public static final class TransferServiceStub extends io.grpc.stub.AbstractAsyncStub<TransferServiceStub> {
        private TransferServiceStub(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected TransferServiceStub build(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new TransferServiceStub(channel, callOptions);
        }

        /**
         *
         */
        public void send(com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta request,
                         io.grpc.stub.StreamObserver<com.welab.wefe.gateway.api.meta.basic.BasicMetaProto.ReturnStatus> responseObserver) {
            asyncUnaryCall(
                    getChannel().newCall(getSendMethod(), getCallOptions()), request, responseObserver);
        }

        /**
         *
         */
        public void recv(com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta request,
                         io.grpc.stub.StreamObserver<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta> responseObserver) {
            asyncUnaryCall(
                    getChannel().newCall(getRecvMethod(), getCallOptions()), request, responseObserver);
        }

        /**
         * <pre>
         * check the transfer status, return immediately
         * </pre>
         */
        public void checkStatusNow(com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta request,
                                   io.grpc.stub.StreamObserver<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta> responseObserver) {
            asyncUnaryCall(
                    getChannel().newCall(getCheckStatusNowMethod(), getCallOptions()), request, responseObserver);
        }
    }

    /**
     *
     */
    public static final class TransferServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<TransferServiceBlockingStub> {
        private TransferServiceBlockingStub(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected TransferServiceBlockingStub build(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new TransferServiceBlockingStub(channel, callOptions);
        }

        /**
         *
         */
        public com.welab.wefe.gateway.api.meta.basic.BasicMetaProto.ReturnStatus send(com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta request) {
            return blockingUnaryCall(
                    getChannel(), getSendMethod(), getCallOptions(), request);
        }

        /**
         *
         */
        public com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta recv(com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta request) {
            return blockingUnaryCall(
                    getChannel(), getRecvMethod(), getCallOptions(), request);
        }

        /**
         * <pre>
         * check the transfer status, return immediately
         * </pre>
         */
        public com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta checkStatusNow(com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta request) {
            return blockingUnaryCall(
                    getChannel(), getCheckStatusNowMethod(), getCallOptions(), request);
        }
    }

    /**
     *
     */
    public static final class TransferServiceFutureStub extends io.grpc.stub.AbstractFutureStub<TransferServiceFutureStub> {
        private TransferServiceFutureStub(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected TransferServiceFutureStub build(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new TransferServiceFutureStub(channel, callOptions);
        }

        /**
         *
         */
        public com.google.common.util.concurrent.ListenableFuture<com.welab.wefe.gateway.api.meta.basic.BasicMetaProto.ReturnStatus> send(
                com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta request) {
            return futureUnaryCall(
                    getChannel().newCall(getSendMethod(), getCallOptions()), request);
        }

        /**
         *
         */
        public com.google.common.util.concurrent.ListenableFuture<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta> recv(
                com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta request) {
            return futureUnaryCall(
                    getChannel().newCall(getRecvMethod(), getCallOptions()), request);
        }

        /**
         * <pre>
         * check the transfer status, return immediately
         * </pre>
         */
        public com.google.common.util.concurrent.ListenableFuture<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta> checkStatusNow(
                com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta request) {
            return futureUnaryCall(
                    getChannel().newCall(getCheckStatusNowMethod(), getCallOptions()), request);
        }
    }

    private static final int METHODID_SEND = 0;
    private static final int METHODID_RECV = 1;
    private static final int METHODID_CHECK_STATUS_NOW = 2;

    private static final class MethodHandlers<Req, Resp> implements
            io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
        private final TransferServiceImplBase serviceImpl;
        private final int methodId;

        MethodHandlers(TransferServiceImplBase serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                case METHODID_SEND:
                    serviceImpl.send((com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta) request,
                            (io.grpc.stub.StreamObserver<com.welab.wefe.gateway.api.meta.basic.BasicMetaProto.ReturnStatus>) responseObserver);
                    break;
                case METHODID_RECV:
                    serviceImpl.recv((com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta) request,
                            (io.grpc.stub.StreamObserver<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta>) responseObserver);
                    break;
                case METHODID_CHECK_STATUS_NOW:
                    serviceImpl.checkStatusNow((com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta) request,
                            (io.grpc.stub.StreamObserver<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta>) responseObserver);
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

    private static abstract class TransferServiceBaseDescriptorSupplier
            implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
        TransferServiceBaseDescriptorSupplier() {
        }

        @Override
        public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
            return TransferServiceProto.getDescriptor();
        }

        @Override
        public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
            return getFileDescriptor().findServiceByName("TransferService");
        }
    }

    private static final class TransferServiceFileDescriptorSupplier
            extends TransferServiceBaseDescriptorSupplier {
        TransferServiceFileDescriptorSupplier() {
        }
    }

    private static final class TransferServiceMethodDescriptorSupplier
            extends TransferServiceBaseDescriptorSupplier
            implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
        private final String methodName;

        TransferServiceMethodDescriptorSupplier(String methodName) {
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
            synchronized (TransferServiceGrpc.class) {
                result = serviceDescriptor;
                if (result == null) {
                    serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
                            .setSchemaDescriptor(new TransferServiceFileDescriptorSupplier())
                            .addMethod(getSendMethod())
                            .addMethod(getRecvMethod())
                            .addMethod(getCheckStatusNowMethod())
                            .build();
                }
            }
        }
        return result;
    }
}
