# Copyright 2021 The WeFe Authors. All Rights Reserved.
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

from flask import Flask, request

from flow.web.api.base import response_builder
from flow.web.api.base.api_executer import ApiExecutor
from flow.web.utils.regex_router_converter import RegexRouterConverter

app = Flask(__name__)

# Here is the name of the defined data type and registered in url_map
app.url_map.converters['regex'] = RegexRouterConverter


# Use custom routing rules
@app.route('/<regex:path>', methods=['GET', 'POST'])
def execute_api(path):
    output = ApiExecutor.execute(request)
    return response_builder.build(output)


if __name__ == '__main__':
    # from waitress import serve
    # serve(app, host="0.0.0.0", port=5000)
    app.run(host="0.0.0.0", port=5000)
