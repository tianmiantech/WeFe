# -*- coding: utf-8 -*-

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

# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: fl_job.proto

from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import symbol_database as _symbol_database
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()




DESCRIPTOR = _descriptor.FileDescriptor(
  name='fl_job.proto',
  package='visualfl',
  syntax='proto3',
  serialized_options=None,
  serialized_pb=b'\n\x0c\x66l_job.proto\x12\x08visualfl\"t\n\x16PaddleFLAggregatorTask\x12\x14\n\x0cscheduler_ep\x18\x01 \x01(\t\x12\x14\n\x0cmain_program\x18\x02 \x01(\x0c\x12\x17\n\x0fstartup_program\x18\x03 \x01(\x0c\x12\x15\n\rconfig_string\x18\x04 \x01(\t\"\xc4\x02\n\x12PaddleFLWorkerTask\x12\x14\n\x0cscheduler_ep\x18\x01 \x01(\t\x12\x12\n\ntrainer_id\x18\x02 \x01(\r\x12\x12\n\ntrainer_ep\x18\x03 \x01(\t\x12\x12\n\nentrypoint\x18\x04 \x01(\t\x12\x14\n\x0cmain_program\x18\x05 \x01(\x0c\x12\x17\n\x0fstartup_program\x18\x06 \x01(\x0c\x12\x14\n\x0csend_program\x18\x07 \x01(\x0c\x12\x14\n\x0crecv_program\x18\x08 \x01(\x0c\x12\x12\n\nfeed_names\x18\t \x01(\x0c\x12\x14\n\x0ctarget_names\x18\n \x01(\x0c\x12\x10\n\x08strategy\x18\x0b \x01(\x0c\x12\r\n\x05\x66\x65\x65\x64s\x18\x0c \x01(\x0c\x12\x15\n\rconfig_string\x18\r \x01(\t\x12\x1f\n\x17\x61lgorithm_config_string\x18\x0e \x01(\tb\x06proto3'
)




_PADDLEFLAGGREGATORTASK = _descriptor.Descriptor(
  name='PaddleFLAggregatorTask',
  full_name='visualfl.PaddleFLAggregatorTask',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='scheduler_ep', full_name='visualfl.PaddleFLAggregatorTask.scheduler_ep', index=0,
      number=1, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=b"".decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='main_program', full_name='visualfl.PaddleFLAggregatorTask.main_program', index=1,
      number=2, type=12, cpp_type=9, label=1,
      has_default_value=False, default_value=b"",
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='startup_program', full_name='visualfl.PaddleFLAggregatorTask.startup_program', index=2,
      number=3, type=12, cpp_type=9, label=1,
      has_default_value=False, default_value=b"",
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='config_string', full_name='visualfl.PaddleFLAggregatorTask.config_string', index=3,
      number=4, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=b"".decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=26,
  serialized_end=142,
)


_PADDLEFLWORKERTASK = _descriptor.Descriptor(
  name='PaddleFLWorkerTask',
  full_name='visualfl.PaddleFLWorkerTask',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='scheduler_ep', full_name='visualfl.PaddleFLWorkerTask.scheduler_ep', index=0,
      number=1, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=b"".decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='trainer_id', full_name='visualfl.PaddleFLWorkerTask.trainer_id', index=1,
      number=2, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='trainer_ep', full_name='visualfl.PaddleFLWorkerTask.trainer_ep', index=2,
      number=3, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=b"".decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='entrypoint', full_name='visualfl.PaddleFLWorkerTask.entrypoint', index=3,
      number=4, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=b"".decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='main_program', full_name='visualfl.PaddleFLWorkerTask.main_program', index=4,
      number=5, type=12, cpp_type=9, label=1,
      has_default_value=False, default_value=b"",
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='startup_program', full_name='visualfl.PaddleFLWorkerTask.startup_program', index=5,
      number=6, type=12, cpp_type=9, label=1,
      has_default_value=False, default_value=b"",
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='send_program', full_name='visualfl.PaddleFLWorkerTask.send_program', index=6,
      number=7, type=12, cpp_type=9, label=1,
      has_default_value=False, default_value=b"",
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='recv_program', full_name='visualfl.PaddleFLWorkerTask.recv_program', index=7,
      number=8, type=12, cpp_type=9, label=1,
      has_default_value=False, default_value=b"",
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='feed_names', full_name='visualfl.PaddleFLWorkerTask.feed_names', index=8,
      number=9, type=12, cpp_type=9, label=1,
      has_default_value=False, default_value=b"",
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='target_names', full_name='visualfl.PaddleFLWorkerTask.target_names', index=9,
      number=10, type=12, cpp_type=9, label=1,
      has_default_value=False, default_value=b"",
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='strategy', full_name='visualfl.PaddleFLWorkerTask.strategy', index=10,
      number=11, type=12, cpp_type=9, label=1,
      has_default_value=False, default_value=b"",
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='feeds', full_name='visualfl.PaddleFLWorkerTask.feeds', index=11,
      number=12, type=12, cpp_type=9, label=1,
      has_default_value=False, default_value=b"",
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='config_string', full_name='visualfl.PaddleFLWorkerTask.config_string', index=12,
      number=13, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=b"".decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='algorithm_config_string', full_name='visualfl.PaddleFLWorkerTask.algorithm_config_string', index=13,
      number=14, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=b"".decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=145,
  serialized_end=469,
)

DESCRIPTOR.message_types_by_name['PaddleFLAggregatorTask'] = _PADDLEFLAGGREGATORTASK
DESCRIPTOR.message_types_by_name['PaddleFLWorkerTask'] = _PADDLEFLWORKERTASK
_sym_db.RegisterFileDescriptor(DESCRIPTOR)

PaddleFLAggregatorTask = _reflection.GeneratedProtocolMessageType('PaddleFLAggregatorTask', (_message.Message,), {
  'DESCRIPTOR' : _PADDLEFLAGGREGATORTASK,
  '__module__' : 'fl_job_pb2'
  # @@protoc_insertion_point(class_scope:visualfl.PaddleFLAggregatorTask)
  })
_sym_db.RegisterMessage(PaddleFLAggregatorTask)

PaddleFLWorkerTask = _reflection.GeneratedProtocolMessageType('PaddleFLWorkerTask', (_message.Message,), {
  'DESCRIPTOR' : _PADDLEFLWORKERTASK,
  '__module__' : 'fl_job_pb2'
  # @@protoc_insertion_point(class_scope:visualfl.PaddleFLWorkerTask)
  })
_sym_db.RegisterMessage(PaddleFLWorkerTask)


# @@protoc_insertion_point(module_scope)
