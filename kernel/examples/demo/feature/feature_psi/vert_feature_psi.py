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
import time

from kernel.examples.handler.component import DataIO
from kernel.examples.handler.component import Intersection
from kernel.examples.handler.component import VertBinning
from kernel.examples.handler.component import VertFeaturePSI
from kernel.examples.handler.handler import Handler
from kernel.examples.handler.interface import Data
from kernel.examples.handler.utils.tools import load_job_config, JobConfig
from kernel.utils import consts


def main(config="../../config.yaml", param="./config.yaml", namespace="wefe_data"):
    # obtain config
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
    promoter_data_table = param.get("promoter_data_table")
    provider_data_table = param.get("provider_data_table")

    promoter_train_data = {"name": promoter_data_table, "namespace": namespace}
    provider_train_data = {"name": provider_data_table, "namespace": namespace}

    handler_upload = Handler().set_roles(promoter=promoter, provider=provider)
    handler_upload.add_upload_data(file=os.path.join(data_base, data_promoter),
                                   table_name=promoter_data_table,
                                   namespace=namespace,
                                   head=1, partition=1)
    handler_upload.add_upload_data(file=os.path.join(data_base, data_provider),
                                   table_name=provider_data_table,
                                   namespace=namespace,
                                   head=1, partition=1)
    handler_upload.upload(work_mode=work_mode, backend=backend, db_type=db_type)

    # initialize handler
    job_id = "job_" + time.strftime("%Y%m%d%H%M%S")
    # job_id = "job-20210315174852"
    handler = Handler(job_id=job_id, backend=backend, work_mode=work_mode, db_type=db_type, fl_type='vertical')
    handler.set_initiator(role='promoter', member_id=promoter)
    handler.set_roles(promoter=promoter, provider=provider)

    # define DataIO components
    dataio_0 = DataIO(name="dataio_0")  # start component numbering at 0
    dataio_0_promoter_member_instance = dataio_0.get_member_instance(role='promoter', member_id=promoter)
    dataio_0_promoter_member_instance.component_param(table=promoter_train_data, with_label=True, output_format="dense")
    dataio_0.get_member_instance(role='provider', member_id=provider).component_param(table=provider_train_data,
                                                                                      with_label=False)

    # define Intersection component
    intersection_0 = Intersection(name="intersection_0", intersect_method="dh", sync_intersect_ids=True)

    vert_param = {
        'method': consts.RECURSIVE_QUERY
    }

    vert_binning_0 = VertBinning(name="vert_binning_0", **vert_param)

    psi_param ={
        'bin_names':['x1','x2'],
        'bin_num' :5,
        "method" : 'bucket'
    }
    vert_feature_psi_0 = VertFeaturePSI(name = "vert_feature_psi_0", **psi_param)
    # add components to handler, in order of task execution
    handler.add_component(dataio_0)
    handler.add_component(intersection_0, data=Data(data=dataio_0.name), output_data_type=["train"])
    # handler.add_component(vert_binning_0, data=Data(train_data=intersection_0.name))
    handler.add_component(vert_feature_psi_0, data=Data(train_data=intersection_0.name), output_data_type=["evaluation"])

    # compile handler once finished adding modules, this step will form conf and dsl files for running job
    handler.compile()

    # fit model
    handler.fit()
    # query component summary
    # print(handler.get_metric_summary(name='evaluation_0', component_name="vert_lr_0"))


if __name__ == "__main__":
    parser = argparse.ArgumentParser("WEFE VERT BINNING JOB")
    parser.add_argument("-config", type=str,
                        help="config file", default="../../../config.yaml")
    parser.add_argument("-param", type=str,
                        help="config file for params", default="./config.yaml")
    args = parser.parse_args()
    if args.config is not None:
        main(args.config, args.param)
    else:
        main()
