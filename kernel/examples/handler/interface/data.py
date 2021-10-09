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

class Data(object):
    def __init__(self, data=None, train_data=None, eval_data=None, test_data=None, predict_input=None,
                 result_data=None):
        self._normal = data
        self._train = train_data
        self._evaluation = eval_data
        self._test_data = test_data
        self._predict_input = predict_input
        self._result_data_set = result_data

    def __getattr__(self, data_key):
        if data_key == "train_data":
            return self._train

        elif data_key == "eval_data":
            return self._evaluation

        elif data_key == "data":
            return self._normal

        elif data_key == "result_data":
            return self._result_data_set

        elif data_key == "test_data":
            return self._test_data

        elif data_key == "predict_input":
            return self._predict_input

        else:
            raise ValueError("data key {} not support".format(data_key))
