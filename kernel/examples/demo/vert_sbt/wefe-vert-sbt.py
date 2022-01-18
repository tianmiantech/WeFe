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
import random
from kernel.examples.handler.component import DataIO
from kernel.examples.handler.component import Evaluation
from kernel.examples.handler.component import Intersection
from kernel.examples.handler.component import VertSecureBoost
from kernel.examples.handler.handler import Handler
from kernel.examples.handler.interface import Data
from kernel.examples.handler.utils.tools import load_job_config, JobConfig


def main(config="../../config.yaml", param="./binary_config.yaml", namespace="wefe_data"):
    # obtain config
    if isinstance(config, str):
        config = load_job_config(config)

    if isinstance(param, str):
        param = JobConfig.load_from_file(param)

    parties = config.parties
    promoter = parties.promoter[0]
    provider = parties.provider[0]

    backend = config.backend
    work_mode = config.work_mode
    db_type = config.db_type
    data_base = config.data_base_dir

    data_promoter = param["data_promoter"]
    data_provider = param["data_provider"]
    promoter_data_table = param.get("data_promoter_train")
    provider_data_table = param.get("data_provider_train")

    # data sets
    promoter_train_data = {"name": param['data_promoter_train'], "namespace": namespace}
    provider_train_data = {"name": param['data_provider_train'], "namespace": namespace}
    # promoter_validate_data = {"name": param['data_promoter_val'], "namespace": namespace}
    # provider_validate_data = {"name": param['data_provider_val'], "namespace": namespace}

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

    # init handler
    handler = Handler(job_id="job_vertsbt_new"+ str(random.randint(0, 999999999999)), backend=backend, work_mode=work_mode, db_type=db_type,
                      fl_type='vertical') \
        .set_initiator(role="promoter", member_id=promoter) \
        .set_roles(promoter=promoter, provider=provider)

    # set data reader and data-io
    dataio_0, dataio_1 = DataIO(name="dataio_0"), DataIO(name="dataio_1")

    dataio_0.get_member_instance(role="promoter", member_id=promoter).component_param(table=promoter_train_data,
                                                                                      with_label=True,
                                                                                      output_format="dense")
    dataio_0.get_member_instance(role="provider", member_id=provider).component_param(table=provider_train_data,
                                                                                      with_label=False)
    # dataio_1.get_member_instance(role="promoter", member_id=promoter).component_param(table=promoter_validate_data,with_label=True, output_format="dense")
    # dataio_1.get_member_instance(role="provider", member_id=provider).component_param(table=provider_validate_data,with_label=False)

    # data intersect component
    intersect_0 = Intersection(name="intersection_0", intersect_method="dh", sync_intersect_ids=True)
    # intersect_1 = Intersection(name="intersection_1",intersect_method="dh",sync_intersect_ids=True)

    # secure boost component
    vert_secure_boost_0 = VertSecureBoost(name="vert_secure_boost_0",
                                          num_trees=param['tree_num'],
                                          task_type=param['task_type'],
                                          objective_param={"objective": param['loss_func']},
                                          encrypt_param={"method": "Paillier"},
                                          tree_param={"max_depth": param['tree_depth']},
                                          validation_freqs=10,
                                          learning_rate=param['learning_rate']
                                          )

    # evaluation component
    evaluation_0 = Evaluation(name="evaluation_0", eval_type=param['eval_type'])

    handler.add_component(dataio_0)
    # handler.add_component(dataio_1)
    handler.add_component(intersect_0, data=Data(data=dataio_0.name), output_data_type=['train'])
    # handler.add_component(intersect_1, data=Data(data=dataio_1.name),output_data_type=['evaluation'])
    handler.add_component(vert_secure_boost_0, data=Data(train_data=intersect_0.name))
    handler.add_component(evaluation_0, data=Data(data=vert_secure_boost_0.name))

    handler.compile()
    handler.fit()

    print(handler.get_metric_summary(name='evaluation_0', component_name="vert_secure_boost_0"))


if __name__ == "__main__":
    parser = argparse.ArgumentParser("VERT SBT JOB")
    parser.add_argument("-config", type=str,
                        help="config file")
    parser.add_argument("-param", type=str,
                        help="config file for params")
    args = parser.parse_args()
    if args.config is not None:
        main(args.config, args.param)
    else:
        main()
