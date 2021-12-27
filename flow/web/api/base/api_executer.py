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

import os
import time

from flow.utils.bean_util import BeanUtil
from flow.web.api.base.base_api import BaseApi
from flow.web.api.base.dto.base_api_input import BaseApiInput
from flow.web.api.base.dto.base_api_output import BaseApiOutput
from common.python.utils.log_utils import schedule_logger


class ApiExecutor:
    """
    Unified entrance to all APIs
    """

    @staticmethod
    def execute(request) -> BaseApiOutput:
        start_time = time.time()

        # Create api input object
        api_params = ApiExecutor.build_api_params(request)
        # Create api and input instance
        api, input = ApiExecutor.create_api_instance(request)
        if api is None:
            return BaseApiOutput.fail(-1, '未找到 api：' + request.path)

        if input is None:
            return BaseApiOutput.fail(-1, 'api 未声明 input class：' + request.path)

        output: BaseApiOutput = None
        try:
            # Fill the parameters into the input object
            BeanUtil.dict_to_model(input, **api_params)
            # Parameter validity check
            input.check()
            # execute api
            output = api.run(input)
        except Exception as e:
            output = BaseApiOutput.fail(-1, str(e))

        # calculation time
        output.spend = int((time.time() - start_time) * 1000)

        return output

    @staticmethod
    def create_api_instance(request) -> (BaseApi, BaseApiInput):
        """
        Create an api instance based on the request path

        Parameters
        ----------
        request
            The http request

        Returns
        -------
        The api instance
        """

        # Convert the path to lower case uniformly, so that the external case can be accessed.
        api_path = request.path.lower()
        # backslash compatible
        api_path = api_path.replace('\\', '/')
        # remove leading and trailing slashes
        api_path = api_path.strip('/')

        # splicing api file path
        array = api_path.split('/')
        file_path = os.path.abspath(os.path.dirname(__file__)).lower()
        array.insert(0, 'flow.web.api')
        api_file_name = '.'.join(array) + '_api'

        # create an api instance
        try:
            module_meta = __import__(api_file_name, globals(), locals(), ['Api', 'Input'])
        except ModuleNotFoundError:
            return None, None

        api_class_meta = getattr(module_meta, 'Api')
        api = api_class_meta()

        input_class_meta = getattr(module_meta, 'Input')
        input = input_class_meta()

        return api, input

    @staticmethod
    def build_api_params(request) -> dict:
        """
        Build api request parameter object
        """

        get_params = request.args
        post_params = request.get_json()
        all_params = get_params.to_dict()

        if post_params is not None:
            all_params.update(post_params)

        return all_params
