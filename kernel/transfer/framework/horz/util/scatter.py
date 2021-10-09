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

class Scatter(object):

    def __init__(self, provider_variable, promoter_variable):
        """
        scatter values from promoter and providers

        Args:
            provider_variable: a variable represents `Provider -> Arbiter`
            promoter_variable: a variable represent `Promoter -> Arbiter`

        Examples:

            >>> from kernel.transfer.framework.horztal.fcUtil import scatter
            >>> s = scatter.Scatter(provider_variable, promoter_variable)
            >>> for v in s.get():
                    print(v)


        """
        self._provider_variable = provider_variable
        self._promoter_variable = promoter_variable

    def get(self, suffix=tuple(), provider_ids=None):
        """
        create a generator of values from promoter and providers.

        Args:
            suffix: tag suffix
            provider_ids: ids of providers to get value from.
                If None provided, get values from all providers.
                If a list of int provided, get values from all providers listed.

        Returns:
            a generator of scatted values

        Raises:
            if provider_ids is neither None nor a list of int, ValueError raised
        """
        yield self._promoter_variable.get(idx=0, suffix=suffix)
        if provider_ids is None:
            provider_ids = -1
        for ret in self._provider_variable.get(idx=provider_ids, suffix=suffix):
            yield ret
