package com.welab.wefe.gateway.api.service.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.*;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.*;

/**
 *
 */
@javax.annotation.Generated(
        value = "by gRPC proto compiler (version 1.29.0)",
        comments = "Source: gateway-service.proto")
public final class NetworkDataTransferProxyServiceGrpc {

    private NetworkDataTransferProxyServiceGrpc() {
    }

    public static final String SERVICE_NAME = "com.welab.wefe.gateway.api.service.proto.NetworkDataTransferProxyService";

    // Static method descriptors that strictly reflect the proto.
    private static volatile io.grpc.MethodDescriptor<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta,
            com.welab.wefe.gateway.api.meta.basic.BasicMetaProto.ReturnStatus> getPushMethod;

    @io.grpc.stub.annotations.RpcMethod(
            fullMethodName = SERVICE_NAME + '/' + "push",
            requestType = com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta.class,
            responseType = com.welab.wefe.gateway.api.meta.basic.BasicMetaProto.ReturnStatus.class,
            methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta,
            com.welab.wefe.gateway.api.meta.basic.BasicMetaProto.ReturnStatus> getPushMethod() {
        io.grpc.MethodDescriptor<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta, com.welab.wefe.gateway.api.meta.basic.BasicMetaProto.ReturnStatus> getPushMethod;
        if ((getPushMethod = NetworkDataTransferProxyServiceGrpc.getPushMethod) == null) {
            synchronized (NetworkDataTransferProxyServiceGrpc.class) {
                if ((getPushMethod = NetworkDataTransferProxyServiceGrpc.getPushMethod) == null) {
                    NetworkDataTransferProxyServiceGrpc.getPushMethod = getPushMethod =
                            io.grpc.MethodDescriptor.<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta, com.welab.wefe.gateway.api.meta.basic.BasicMetaProto.ReturnStatus>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(SERVICE_NAME, "push"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta.getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.welab.wefe.gateway.api.meta.basic.BasicMetaProto.ReturnStatus.getDefaultInstance()))
                                    .setSchemaDescriptor(new NetworkDataTransferProxyServiceMethodDescriptorSupplier("push"))
                                    .build();
                }
            }
        }
        return getPushMethod;
    }

    private static volatile io.grpc.MethodDescriptor<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta,
            com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta> getPushDataMethod;

    @io.grpc.stub.annotations.RpcMethod(
            fullMethodName = SERVICE_NAME + '/' + "pushData",
            requestType = com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta.class,
            responseType = com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta.class,
            methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
    public static io.grpc.MethodDescriptor<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta,
            com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta> getPushDataMethod() {
        io.grpc.MethodDescriptor<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta, com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta> getPushDataMethod;
        if ((getPushDataMethod = NetworkDataTransferProxyServiceGrpc.getPushDataMethod) == null) {
            synchronized (NetworkDataTransferProxyServiceGrpc.class) {
                if ((getPushDataMethod = NetworkDataTransferProxyServiceGrpc.getPushDataMethod) == null) {
                    NetworkDataTransferProxyServiceGrpc.getPushDataMethod = getPushDataMethod =
                            io.grpc.MethodDescriptor.<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta, com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
                                    .setFullMethodName(generateFullMethodName(SERVICE_NAME, "pushData"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta.getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta.getDefaultInstance()))
                                    .setSchemaDescriptor(new NetworkDataTransferProxyServiceMethodDescriptorSupplier("pushData"))
                                    .build();
                }
            }
        }
        return getPushDataMethod;
    }

    /**
     * Creates a new async stub that supports all call types for the service
     */
    public static NetworkDataTransferProxyServiceStub newStub(io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<NetworkDataTransferProxyServiceStub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<NetworkDataTransferProxyServiceStub>() {
                    @Override
                    public NetworkDataTransferProxyServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new NetworkDataTransferProxyServiceStub(channel, callOptions);
                    }
                };
        return NetworkDataTransferProxyServiceStub.newStub(factory, channel);
    }

    /**
     * Creates a new blocking-style stub that supports unary and streaming output calls on the service
     */
    public static NetworkDataTransferProxyServiceBlockingStub newBlockingStub(
            io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<NetworkDataTransferProxyServiceBlockingStub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<NetworkDataTransferProxyServiceBlockingStub>() {
                    @Override
                    public NetworkDataTransferProxyServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new NetworkDataTransferProxyServiceBlockingStub(channel, callOptions);
                    }
                };
        return NetworkDataTransferProxyServiceBlockingStub.newStub(factory, channel);
    }

    /**
     * Creates a new ListenableFuture-style stub that supports unary calls on the service
     */
    public static NetworkDataTransferProxyServiceFutureStub newFutureStub(
            io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<NetworkDataTransferProxyServiceFutureStub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<NetworkDataTransferProxyServiceFutureStub>() {
                    @Override
                    public NetworkDataTransferProxyServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new NetworkDataTransferProxyServiceFutureStub(channel, callOptions);
                    }
                };
        return NetworkDataTransferProxyServiceFutureStub.newStub(factory, channel);
    }

    /**
     *
     */
    public static abstract class NetworkDataTransferProxyServiceImplBase implements io.grpc.BindableService {

        /**
         *
         */
        public void push(com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta request,
                         io.grpc.stub.StreamObserver<com.welab.wefe.gateway.api.meta.basic.BasicMetaProto.ReturnStatus> responseObserver) {
            asyncUnimplementedUnaryCall(getPushMethod(), responseObserver);
        }

        /**
         *
         */
        public io.grpc.stub.StreamObserver<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta> pushData(
                io.grpc.stub.StreamObserver<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta> responseObserver) {
            return asyncUnimplementedStreamingCall(getPushDataMethod(), responseObserver);
        }

        @Override
        public final io.grpc.ServerServiceDefinition bindService() {
            return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
                    .addMethod(
                            getPushMethod(),
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta,
                                            com.welab.wefe.gateway.api.meta.basic.BasicMetaProto.ReturnStatus>(
                                            this, METHODID_PUSH)))
                    .addMethod(
                            getPushDataMethod(),
                            asyncBidiStreamingCall(
                                    new MethodHandlers<
                                            com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta,
                                            com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta>(
                                            this, METHODID_PUSH_DATA)))
                    .build();
        }
    }

    /**
     *
     */
    public static final class NetworkDataTransferProxyServiceStub extends io.grpc.stub.AbstractAsyncStub<NetworkDataTransferProxyServiceStub> {
        private NetworkDataTransferProxyServiceStub(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected NetworkDataTransferProxyServiceStub build(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new NetworkDataTransferProxyServiceStub(channel, callOptions);
        }

        /**
         *
         */
        public void push(com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta request,
                         io.grpc.stub.StreamObserver<com.welab.wefe.gateway.api.meta.basic.BasicMetaProto.ReturnStatus> responseObserver) {
            asyncUnaryCall(
                    getChannel().newCall(getPushMethod(), getCallOptions()), request, responseObserver);
        }

        /**
         *
         */
        public io.grpc.stub.StreamObserver<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta> pushData(
                io.grpc.stub.StreamObserver<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta> responseObserver) {
            return asyncBidiStreamingCall(
                    getChannel().newCall(getPushDataMethod(), getCallOptions()), responseObserver);
        }
    }

    /**
     *
     */
    public static final class NetworkDataTransferProxyServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<NetworkDataTransferProxyServiceBlockingStub> {
        private NetworkDataTransferProxyServiceBlockingStub(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected NetworkDataTransferProxyServiceBlockingStub build(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new NetworkDataTransferProxyServiceBlockingStub(channel, callOptions);
        }

        /**
         *
         */
        public com.welab.wefe.gateway.api.meta.basic.BasicMetaProto.ReturnStatus push(com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta request) {
            return blockingUnaryCall(
                    getChannel(), getPushMethod(), getCallOptions(), request);
        }
    }

    /**
     *
     */
    public static final class NetworkDataTransferProxyServiceFutureStub extends io.grpc.stub.AbstractFutureStub<NetworkDataTransferProxyServiceFutureStub> {
        private NetworkDataTransferProxyServiceFutureStub(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected NetworkDataTransferProxyServiceFutureStub build(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new NetworkDataTransferProxyServiceFutureStub(channel, callOptions);
        }

        /**
         *
         */
        public com.google.common.util.concurrent.ListenableFuture<com.welab.wefe.gateway.api.meta.basic.BasicMetaProto.ReturnStatus> push(
                com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta request) {
            return futureUnaryCall(
                    getChannel().newCall(getPushMethod(), getCallOptions()), request);
        }
    }

    private static final int METHODID_PUSH = 0;
    private static final int METHODID_PUSH_DATA = 1;

    private static final class MethodHandlers<Req, Resp> implements
            io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
        private final NetworkDataTransferProxyServiceImplBase serviceImpl;
        private final int methodId;

        MethodHandlers(NetworkDataTransferProxyServiceImplBase serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                case METHODID_PUSH:
                    serviceImpl.push((com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta) request,
                            (io.grpc.stub.StreamObserver<com.welab.wefe.gateway.api.meta.basic.BasicMetaProto.ReturnStatus>) responseObserver);
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
                case METHODID_PUSH_DATA:
                    return (io.grpc.stub.StreamObserver<Req>) serviceImpl.pushData(
                            (io.grpc.stub.StreamObserver<com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto.TransferMeta>) responseObserver);
                default:
                    throw new AssertionError();
            }
        }
    }

    private static abstract class NetworkDataTransferProxyServiceBaseDescriptorSupplier
            implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
        NetworkDataTransferProxyServiceBaseDescriptorSupplier() {
        }

        @Override
        public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
            return TransferServiceProto.getDescriptor();
        }

        @Override
        public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
            return getFileDescriptor().findServiceByName("NetworkDataTransferProxyService");
        }
    }

    private static final class NetworkDataTransferProxyServiceFileDescriptorSupplier
            extends NetworkDataTransferProxyServiceBaseDescriptorSupplier {
        NetworkDataTransferProxyServiceFileDescriptorSupplier() {
        }
    }

    private static final class NetworkDataTransferProxyServiceMethodDescriptorSupplier
            extends NetworkDataTransferProxyServiceBaseDescriptorSupplier
            implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
        private final String methodName;

        NetworkDataTransferProxyServiceMethodDescriptorSupplier(String methodName) {
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
            synchronized (NetworkDataTransferProxyServiceGrpc.class) {
                result = serviceDescriptor;
                if (result == null) {
                    serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
                            .setSchemaDescriptor(new NetworkDataTransferProxyServiceFileDescriptorSupplier())
                            .addMethod(getPushMethod())
                            .addMethod(getPushDataMethod())
                            .build();
                }
            }
        }
        return result;
    }
}
