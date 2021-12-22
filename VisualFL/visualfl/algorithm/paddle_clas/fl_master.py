# Copyright (c) 2020 PaddlePaddle Authors. All Rights Reserved.
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
import json
import logging
import importlib

import click
from paddle import fluid
from paddle_fl.core.master.job_generator import JobGenerator
from paddle_fl.core.strategy.fl_strategy_base import (
    FedAvgStrategy,
)

class Model(object):
    def __init__(self):
        self.feeds = None
        self.startup_program = None
        self.loss = None


    def build_program(self,inputs,label,num_classes,architecture='CNN'):
        module = importlib.import_module('visualfl.algorithm.paddle_clas.models')
        model = getattr(module, architecture)()
        out = model.net(input=inputs,class_dim=num_classes)

        predict = fluid.layers.softmax(out)
        cost = fluid.layers.cross_entropy(input=predict, label=label)
        accuracy = fluid.layers.accuracy(input=predict, label=label)
        self.loss = fluid.layers.mean(cost)
        self.startup_program = fluid.default_startup_program()

        self.feeds = [inputs, label]
        self.targets = [self.loss, accuracy]


@click.command()
@click.option("--ps-endpoint", type=str, required=True)
@click.option(
    "-c",
    "--config",
    type=click.Path(file_okay=True, dir_okay=False, exists=True),
    required=True,
)
@click.option(
    "--algorithm-config", type=click.Path(exists=True, file_okay=True, dir_okay=False)
)
def fl_master(algorithm_config, ps_endpoint, config):
    logging.basicConfig(
        level=logging.DEBUG, format="%(asctime)s-%(levelname)s: %(message)s"
    )

    logger = logging.getLogger(__name__)  # noqa: F841
    with open(config) as f:
        config_json = json.load(f)
    worker_num = config_json["worker_num"]

    with open(algorithm_config) as f:
        algorithm_config_dict = json.load(f)

    inner_step = algorithm_config_dict.get("inner_step")
    base_lr = algorithm_config_dict.get("base_lr", 0.001)
    image_shape = algorithm_config_dict.get("image_shape")
    num_classes = algorithm_config_dict.get("num_classes")
    architecture = algorithm_config_dict.get("architecture")

    inputs = fluid.layers.data(name="img", shape=image_shape, dtype="float64")
    label = fluid.layers.data(name="label", shape=[1], dtype="int64")

    model = Model()
    model.build_program(inputs,label,num_classes,architecture)

    job_generator = JobGenerator()
    job_generator.set_losses([model.loss])
    job_generator.set_optimizer(fluid.optimizer.Adam(base_lr))
    job_generator.set_startup_program(model.startup_program)
    job_generator.set_infer_feed_and_target_names(
        [feed.name for feed in model.feeds], [target.name for target in model.targets]
    )
    job_generator.set_feeds(model.feeds)

    strategy = FedAvgStrategy()
    strategy.fed_avg = True
    strategy._inner_step = inner_step

    endpoints = [ps_endpoint]
    output = "compile"
    job_generator.generate_fl_job(
        strategy, server_endpoints=endpoints, worker_num=worker_num, output=output
    )


if __name__ == "__main__":
    fl_master()


