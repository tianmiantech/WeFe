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

from kernel.examples.handler.component.dataio import DataIO
from kernel.examples.handler.component.evaluation import Evaluation
from kernel.examples.handler.component.horz_secureboost import HorzSecureBoost
from kernel.examples.handler.handler import Handler
from kernel.examples.handler.interface.data import Data
from kernel.examples.handler.utils.tools import JobConfig
from kernel.examples.handler.utils.tools import load_job_config


def main(config="../../config.yaml", param='./binary_config.yaml', namespace="wefe_data"):
    # obtain config
    if isinstance(config, str):
        config = load_job_config(config)

    if isinstance(param, str):
        param = JobConfig.load_from_file(param)

    parties = config.parties
    promoter = parties.promoter[0]
    provider = parties.provider[0]
    arbiter = parties.arbiter[0]

    backend = config.backend
    work_mode = config.work_mode
    db_type = config.db_type
    data_base = config.data_base_dir

    data_promoter = param["data_promoter"]
    data_provider = param["data_provider"]
    promoter_data_table = param.get("data_promoter_train")
    provider_data_table = param.get("data_provider_train")

    promoter_train_data = {"name": param['data_promoter_train'], "namespace": namespace}
    provider_train_data = {"name": param['data_provider_train'], "namespace": namespace}
    promoter_validate_data = {"name": param['data_promoter_val'], "namespace": namespace}
    provider_validate_data = {"name": param['data_provider_val'], "namespace": namespace}

    handler_upload = Handler().set_roles(promoter=promoter, provider=provider)
    handler_upload.add_upload_data(file=os.path.join(data_base, data_promoter),
                                   table_name=promoter_data_table,
                                   namespace=namespace,
                                   head=1, partition=1)
    handler_upload.add_upload_data(file=os.path.join(data_base, data_provider),
                                   table_name=provider_data_table,
                                   namespace=namespace,
                                   head=1, partition=1)
    handler_upload.upload(work_mode=work_mode, backend=backend)

    handler = Handler(job_id="job_horz_sbt_0001", work_mode=work_mode, backend=backend, db_type=db_type,
                      fl_type="horizontal").set_initiator(role='promoter', member_id=promoter).set_roles(
        promoter=promoter, provider=provider, arbiter=arbiter)

    dataio_0, dataio_1 = DataIO(name="dataio_0"), DataIO(name='dataio_1')

    dataio_0.get_member_instance(role='promoter', member_id=promoter).component_param(table=promoter_train_data,
                                                                                      with_label=True,
                                                                                      output_format="dense")
    dataio_0.get_member_instance(role='provider', member_id=provider).component_param(table=provider_train_data,
                                                                                      with_label=True,
                                                                                      output_format="dense")

    dataio_1.get_member_instance(role='promoter', member_id=promoter).component_param(table=promoter_validate_data,
                                                                                      with_label=True,
                                                                                      output_format="dense")
    dataio_1.get_member_instance(role='provider', member_id=provider).component_param(table=provider_validate_data,
                                                                                      with_label=True,
                                                                                      output_format="dense")

    horz_secureboost_0 = HorzSecureBoost(name="horz_secureboost_0",
                                         num_trees=param['tree_num'],
                                         task_type=param['task_type'],
                                         objective_param={"objective": param['loss_func']},
                                         tree_param={
                                             "max_depth": param['tree_depth']
                                         },
                                         validation_freqs=1,
                                         subsample_feature_rate=1,
                                         learning_rate=param['learning_rate'],
                                         bin_num=50
                                         )

    evaluation_0 = Evaluation(name='evaluation_0', eval_type=param['eval_type'])

    handler.add_component(dataio_0, output_data_type=["train"])
    # handler.add_component(dataio_1,output_data_type=["evaluation"])
    handler.add_component(horz_secureboost_0, data=Data(train_data=dataio_0.name))
    handler.add_component(evaluation_0, data=Data(horz_secureboost_0.name))

    handler.compile()
    handler.fit()

    print(handler.get_metric_summary(name='evaluation_0', component_name="horz_secureboost_0"))


if __name__ == "__main__":
    parser = argparse.ArgumentParser("WEFE HORZ SBT DEMO")
    parser.add_argument("-config", type=str,
                        help="config file")
    args = parser.parse_args()
    if args.config is not None:
        main(args.config)
    else:
        main()
