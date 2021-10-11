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

class BaseApiInput:

    def check(self):
        """
        Check whether the parameter is valid, subclass please override this method,
        when the parameter is invalid, please throw an exception directly.
        """

    @staticmethod
    def required(params):
        """
        Confirm that the parameter passed in is not empty
        """
        for item in params:
            if item is None or item == '':
                raise Exception("api param can not empty value")
