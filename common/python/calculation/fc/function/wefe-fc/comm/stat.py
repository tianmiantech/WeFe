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

import time


class Stat(object):

    def __init__(self):
        self.count = 0
        self.acc_time = 0

    def incr(self):
        self.count += 1

    def acc_time_start(self):
        self.time_start = time.time()

    def acc_time_end(self):
        self.acc_time += time.time() - self.time_start

    def print_acc_time(self):
        pass
