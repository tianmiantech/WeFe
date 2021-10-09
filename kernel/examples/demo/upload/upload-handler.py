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



import argparse
import os

from kernel.examples.handler.handler import Handler
from kernel.examples.handler.utils.tools import load_job_config, JobConfig


# path to data
# default fate installation path

def main(config="../../config.yaml", param="./upload_config.yaml", namespace="wefe_data"):
    if isinstance(config, str):
        config = load_job_config(config)
    parties = config.parties
    promoter = parties.promoter[0]
    provider = parties.provider[0]
    backend = config.backend
    work_mode = config.work_mode
    db_type = config.db_type
    data_base = config.data_base_dir

    if isinstance(param, str):
        param = JobConfig.load_from_file(param)
    assert isinstance(param, dict)
    data_promoter = param["data_promoter"]
    data_provider = param["data_provider"]
    head = param["head"]
    partition = param["partition"]
    promoter_data_table = param['promoter_data_table']
    provider_data_table = param['provider_data_table']

    handler_upload = Handler().set_roles(promoter=promoter, provider=provider)
    # add upload data info
    # original csv file path
    handler_upload.add_upload_data(file=os.path.join(data_base, data_promoter),
                                   table_name=promoter_data_table,  # table name
                                   namespace=namespace,
                                   head=head, partition=partition)

    handler_upload.add_upload_data(file=os.path.join(data_base, data_provider),
                                   table_name=provider_data_table,
                                   namespace=namespace,
                                   head=head, partition=partition)

    # upload all data
    handler_upload.upload(work_mode=work_mode, backend=backend, db_type=db_type)


if __name__ == "__main__":
    parser = argparse.ArgumentParser("PIPELINE DEMO")
    parser.add_argument("--base", "-b", type=str,
                        help="data base, path to directory that contains examples/data")

    args = parser.parse_args()
    if args.base is not None:
        main(args.base)
    else:
        main()
