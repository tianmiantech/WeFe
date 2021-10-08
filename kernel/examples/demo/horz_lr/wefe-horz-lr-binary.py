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

from kernel.examples.handler.component import DataIO
from kernel.examples.handler.component import Evaluation
from kernel.examples.handler.component import HorzLR
from kernel.examples.handler.handler import Handler
from kernel.examples.handler.interface import Data
from kernel.examples.handler.utils.tools import load_job_config, JobConfig


def main(config="../../config.yaml", param="./lr_config.yaml", namespace="wefe_data"):
    # obtain config
    if isinstance(config, str):
        config = load_job_config(config)
    parties = config.parties
    promoter = parties.promoter[0]
    provider = parties.provider[0]
    arbiter = parties.arbiter[0]
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
    handler = Handler(job_id="job_0005", work_mode=work_mode, backend=backend, db_type=db_type, fl_type="horizontal")
    # set job initiator
    handler.set_initiator(role='promoter', member_id=promoter)
    # set participants information
    handler.set_roles(promoter=promoter, provider=provider, arbiter=arbiter)

    # define DataIO components
    dataio_0 = DataIO(name="dataio_0")  # start component numbering at 0
    # get DataIO member instance of promoter
    dataio_0_promoter_member_instance = dataio_0.get_member_instance(role='promoter', member_id=promoter)
    # configure DataIO for promoter
    dataio_0_promoter_member_instance.component_param(table=promoter_train_data, with_label=True, output_format="dense")
    # get and configure DataIO member instance of provider
    dataio_0.get_member_instance(role='provider', member_id=provider).component_param(table=provider_train_data,
                                                                                      with_label=True)

    lr_param = {
    }

    config_param = {
        "penalty": param["penalty"],
        "max_iter": param["max_iter"],
        "alpha": param["alpha"],
        "learning_rate": param["learning_rate"],
        "optimizer": param.get("optimizer", "sgd"),
        "batch_size": param.get("batch_size", -1),
        "init_param": {
            "init_method": param.get("init_method", 'random_uniform')
        },
        "encrypt_param": {
            "method": None
        }
    }
    lr_param.update(config_param)
    print(f"lr_param: {lr_param}")
    horz_lr_0 = HorzLR(name='horz_lr_0', **lr_param)

    evaluation_0 = Evaluation(name='evaluation_0')
    evaluation_0.get_member_instance(role='promoter', member_id=promoter).component_param(need_run=True,
                                                                                          eval_type="binary")
    evaluation_0.get_member_instance(role='provider', member_id=provider).component_param(need_run=False)

    # add components to handler, in order of task execution
    handler.add_component(dataio_0, output_data_type=["train"])
    handler.add_component(horz_lr_0, data=Data(train_data=dataio_0.name))
    handler.add_component(evaluation_0, data=Data(data=horz_lr_0.name))

    # compile handler once finished adding modules, this step will form conf and dsl files for running job
    handler.compile()

    # fit model
    handler.fit()
    # query component summary
    print(handler.get_metric_summary(name='evaluation_0', component_name="horz_lr_0"))


if __name__ == "__main__":
    parser = argparse.ArgumentParser("WEFE HORZ LR BINARY JOB")
    parser.add_argument("-config", type=str,
                        help="config file")
    parser.add_argument("-param", type=str,
                        help="config file for params")
    args = parser.parse_args()
    if args.config is not None:
        main(args.config, args.param)
    else:
        main()
