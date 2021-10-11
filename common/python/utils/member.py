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

# Copyright 2019 The FATE Authors. All Rights Reserved.
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

class Member(object):
    """
    Uniquely identify
    """

    def __init__(self, role, member_id):
        self.role = role
        self.member_id = member_id

    def __hash__(self):
        return (self.role, self.member_id).__hash__()

    def __str__(self):
        return f"Member(role={self.role}, member_id={self.member_id})"

    def __repr__(self):
        return self.__str__()

    def __lt__(self, other):
        return (self.role, self.member_id) < (other.role, other.member_id)

    def __eq__(self, other):
        return self.member_id == other.member_id and self.role == other.role

    def get_member_id(self):
        return str(self.member_id)

    def to_pb(self):
        from common.python.protobuf.pyproto import gateway_meta_pb2
        return gateway_meta_pb2.Member(memberId=f"{self.member_id}", memberName=self.role)
