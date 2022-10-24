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

from flask import Flask
from flask import request

from index import handler

app = Flask(__name__)


class Context(object):
    """
        Construct a context object
    """

    def __init__(self, request_id):
        self.requestId = request_id


@app.route('/invoke', methods=['POST'])
def event_invoke():
    rid = request.headers.get('x-fc-request-id')
    data = request.stream.read()
    context = Context(rid)
    return handler(data, context)


@app.route('/')
def hello():
    return 'hello docker&flask'


### 阿里云不需要，腾讯云需要
if __name__ == '__main__':
    app.run(debug=True, port=9000, host='0.0.0.0')
