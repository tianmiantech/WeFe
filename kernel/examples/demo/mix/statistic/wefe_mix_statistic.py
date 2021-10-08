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

import argparse
import os

from kernel.examples.handler.component import DataIO, HorzStatistic
from kernel.examples.handler.component import Intersection
from kernel.examples.handler.component import MixStatistic
from kernel.examples.handler.handler import Handler
from kernel.examples.handler.interface import Data
from kernel.examples.handler.utils.tools import load_job_config, JobConfig


def main(config="../../config.yaml", param="./config.yaml", namespace="wefe_data"):
    # obtain config
    if isinstance(config, str):
        config = load_job_config(config)
    parties = config.parties
    promoters = parties.promoter
    providers = parties.provider
    backend = config.backend
    work_mode = config.work_mode
    db_type = config.db_type
    data_base = config.data_base_dir

    if isinstance(param, str):
        param = JobConfig.load_from_file(param)
    assert isinstance(param, dict)

    data_promoters = param["data"]["promoter"]

    data_providers = param["data"]["provider"]
    if len(providers) != len(data_providers):
        raise ValueError(f'data_provider {len(data_providers)} providers {len(providers)} num is not same')

    table_promoters = param["table"]["promoter"]
    table_providers = param["table"]["provider"]
    if len(providers) != len(table_providers):
        raise ValueError(f'table_providers {len(table_providers)} providers {len(providers)} num is not same')

    promoter_train_datas = []
    for table_promoter in table_promoters:
        promoter_train_datas.append({'name': table_promoter, 'namespace': namespace})

    provider_train_datas = []
    for table_provider in table_providers:
        provider_train_datas.append({"name": table_provider, "namespace": namespace})

    handler_upload = Handler().set_roles(promoter=promoters, provider=providers)

    for idx, data_promoter in enumerate(data_promoters):
        handler_upload.add_upload_data(file=os.path.join(data_base, data_promoter),
                                       table_name=table_promoters[idx],
                                       namespace=namespace,
                                       head=1, partition=1)

    for idx, data_provider in enumerate(data_providers):
        handler_upload.add_upload_data(file=os.path.join(data_base, data_provider),
                                       table_name=table_providers[idx],
                                       namespace=namespace,
                                       head=1, partition=1)

    handler_upload.upload(work_mode=work_mode, backend=backend)

    # initialize handler
    # job_id = "job_" + time.strftime("%Y%m%d%H%M%S")
    job_id = "job_20210729112537"
    handler = Handler(job_id=job_id, backend=backend, work_mode=work_mode, db_type=db_type, fl_type='vertical')
    handler.set_initiator(role='promoter', member_id=promoters[0])
    handler.set_roles(promoter=promoters, provider=providers)

    # define DataIO components
    dataio_0 = DataIO(name='dataio_1')  # start component numbering at 0
    # for idx, promoter in enumerate(promoters):
    #     dataio_0.get_member_instance(role='promoter', member_id=promoter).component_param(
    #         table=promoter_train_datas[idx],
    #         with_label=True,
    #         output_format="dense")

    # dataio_1 = DataIO(name='dataio_1')
    for idx, provider in enumerate(providers):
        dataio_0.get_member_instance(role='provider', member_id=provider).component_param(
            table=provider_train_datas[idx],
            with_label=False)

    # define Intersection component
    intersection_0 = Intersection(name="intersection_0", intersect_method="dh", sync_intersect_ids=True)

    statistic_param = {
        "col_names": []
    }

    mix_statistic_0 = MixStatistic(name="mix_statistic_0", **statistic_param)

    # add components to handler, in order of task execution
    handler.add_component(dataio_0, output_data_type=['train'])
    # handler.add_component(dataio_1, output_data_type=['train'])
    # handler.add_component(intersection_0, data=Data(data=dataio_0.name), output_data_type=["train"])
    # handler.add_component(mix_statistic_0, data=Data(train_data=dataio_0.name))
    statistic_params = {
        'col_names': ['x0', 'x1', 'x2', 'x3', 'x4', 'x5', 'x6', 'x7', 'x8', 'x9', 'x10', 'x11', 'x12', 'x13', 'x14',
                      'x15', 'x16', 'x17', 'x18', 'x19', 'x20', 'x21', 'x22', 'x23', 'x24', 'x25', 'x26', 'x27', 'x28',
                      'x29']}
    horz_statistic = HorzStatistic(name='horz_statistic_0', **statistic_params)
    handler.add_component(horz_statistic, data=Data(train_data=dataio_0.name), output_data_type=["train"])

    # compile handler once finished adding modules, this step will form conf and dsl files for running job
    handler.compile()

    # fit model
    handler.fit()
    # query component summary
    # print(handler.get_metric_summary(name='evaluation_0', component_name="vert_lr_0"))


if __name__ == "__main__":
    parser = argparse.ArgumentParser("mix statistic JOB")
    parser.add_argument("-c", "--config", type=str,
                        help="config file", default="../../../multi_config.yaml")
    parser.add_argument("-p", "--param", type=str,
                        help="config file for params", default="./config.yaml")
    args = parser.parse_args()
    main(args.config, args.param)
