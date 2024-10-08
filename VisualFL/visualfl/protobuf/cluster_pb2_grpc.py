# Copyright 2021 Tianmian Tech. All Rights Reserved.
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Generated by the gRPC Python protocol compiler plugin. DO NOT EDIT!
import grpc

import visualfl.protobuf.cluster_pb2 as cluster__pb2


class ClusterManagerStub(object):
    """service in cluster manager called by worker
    """

    def __init__(self, channel):
        """Constructor.

        Args:
            channel: A grpc.Channel.
        """
        self.Enroll = channel.unary_stream(
                '/visualfl.ClusterManager/Enroll',
                request_serializer=cluster__pb2.Enroll.REQ.SerializeToString,
                response_deserializer=cluster__pb2.Enroll.REP.FromString,
                )
        self.UpdateTaskStatus = channel.unary_unary(
                '/visualfl.ClusterManager/UpdateTaskStatus',
                request_serializer=cluster__pb2.UpdateStatus.REQ.SerializeToString,
                response_deserializer=cluster__pb2.UpdateStatus.REP.FromString,
                )
        self.TaskSubmit = channel.unary_unary(
                '/visualfl.ClusterManager/TaskSubmit',
                request_serializer=cluster__pb2.TaskSubmit.REQ.SerializeToString,
                response_deserializer=cluster__pb2.TaskSubmit.REP.FromString,
                )
        self.TaskResourceRequire = channel.unary_unary(
                '/visualfl.ClusterManager/TaskResourceRequire',
                request_serializer=cluster__pb2.TaskResourceRequire.REQ.SerializeToString,
                response_deserializer=cluster__pb2.TaskResourceRequire.REP.FromString,
                )


class ClusterManagerServicer(object):
    """service in cluster manager called by worker
    """

    def Enroll(self, request, context):
        """service for worker: enroll and fetch tasks
        """
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def UpdateTaskStatus(self, request, context):
        """service for worker: update status or heartbeat
        """
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def TaskSubmit(self, request, context):
        """service for master: submit task to cluster
        """
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def TaskResourceRequire(self, request, context):
        """Missing associated documentation comment in .proto file"""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')


def add_ClusterManagerServicer_to_server(servicer, server):
    rpc_method_handlers = {
            'Enroll': grpc.unary_stream_rpc_method_handler(
                    servicer.Enroll,
                    request_deserializer=cluster__pb2.Enroll.REQ.FromString,
                    response_serializer=cluster__pb2.Enroll.REP.SerializeToString,
            ),
            'UpdateTaskStatus': grpc.unary_unary_rpc_method_handler(
                    servicer.UpdateTaskStatus,
                    request_deserializer=cluster__pb2.UpdateStatus.REQ.FromString,
                    response_serializer=cluster__pb2.UpdateStatus.REP.SerializeToString,
            ),
            'TaskSubmit': grpc.unary_unary_rpc_method_handler(
                    servicer.TaskSubmit,
                    request_deserializer=cluster__pb2.TaskSubmit.REQ.FromString,
                    response_serializer=cluster__pb2.TaskSubmit.REP.SerializeToString,
            ),
            'TaskResourceRequire': grpc.unary_unary_rpc_method_handler(
                    servicer.TaskResourceRequire,
                    request_deserializer=cluster__pb2.TaskResourceRequire.REQ.FromString,
                    response_serializer=cluster__pb2.TaskResourceRequire.REP.SerializeToString,
            ),
    }
    generic_handler = grpc.method_handlers_generic_handler(
            'visualfl.ClusterManager', rpc_method_handlers)
    server.add_generic_rpc_handlers((generic_handler,))


 # This class is part of an EXPERIMENTAL API.
class ClusterManager(object):
    """service in cluster manager called by worker
    """

    @staticmethod
    def Enroll(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_stream(request, target, '/visualfl.ClusterManager/Enroll',
            cluster__pb2.Enroll.REQ.SerializeToString,
            cluster__pb2.Enroll.REP.FromString,
            options, channel_credentials,
            call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def UpdateTaskStatus(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/visualfl.ClusterManager/UpdateTaskStatus',
            cluster__pb2.UpdateStatus.REQ.SerializeToString,
            cluster__pb2.UpdateStatus.REP.FromString,
            options, channel_credentials,
            call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def TaskSubmit(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/visualfl.ClusterManager/TaskSubmit',
            cluster__pb2.TaskSubmit.REQ.SerializeToString,
            cluster__pb2.TaskSubmit.REP.FromString,
            options, channel_credentials,
            call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def TaskResourceRequire(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/visualfl.ClusterManager/TaskResourceRequire',
            cluster__pb2.TaskResourceRequire.REQ.SerializeToString,
            cluster__pb2.TaskResourceRequire.REP.FromString,
            options, channel_credentials,
            call_credentials, compression, wait_for_ready, timeout, metadata)
