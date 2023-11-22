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
import tarfile

from visualfl_deploy import __base_dir__, __visualfl_tarball__, __BASE_NAME__

_project_base = __base_dir__.parent.parent



def _build():
    __visualfl_tarball__.parent.mkdir(exist_ok=True, parents=True)
    with tarfile.open(__visualfl_tarball__, "w:gz") as tar:
        for path in [
            _project_base.joinpath("visualfl"),
            _project_base.joinpath("depends", "PaddleDetection", "ppdet"),
            _project_base.joinpath("depends", "PaddleFL", "python", "paddle_fl","paddle_fl"),
            _project_base.joinpath("data"),
            _project_base.joinpath("examples"),
            _project_base.joinpath("script"),
            _project_base.joinpath("requirements.txt"),
            _project_base.joinpath("config.properties"),
        ]:
            tar.add(path, f"{__BASE_NAME__}/{os.path.basename(path)}")


def _clean():
    if __visualfl_tarball__.exists():
        os.remove(__visualfl_tarball__)


if __name__ == "__main__":
    _clean()
    _build()
