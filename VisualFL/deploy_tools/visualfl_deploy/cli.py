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


import typer
from visualfl_deploy import _deploy, _generate_template, _service

app = typer.Typer()

app.add_typer(_deploy.app, name="deploy")
app.add_typer(_service.app, name="services")
app.add_typer(_generate_template.app, name="template")

if __name__ == "__main__":
    app()
