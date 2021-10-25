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

import click
from paddle import fluid

from paddle_fl.core.master.job_generator import JobGenerator
from paddle_fl.core.strategy.fl_strategy_base import (
    FedAvgStrategy,
)


class CNN(object):
    def __init__(self):
        inputs = fluid.layers.data(name="img", shape=[1, 28, 28], dtype="float64")
        label = fluid.layers.data(name="label", shape=[1], dtype="int64")
        conv_pool_1 = fluid.nets.simple_img_conv_pool(
            input=inputs,
            num_filters=20,
            filter_size=5,
            pool_size=2,
            pool_stride=2,
            act="relu",
        )
        conv_pool_2 = fluid.nets.simple_img_conv_pool(
            input=conv_pool_1,
            num_filters=50,
            filter_size=5,
            pool_size=2,
            pool_stride=2,
            act="relu",
        )
        predict = self.predict = fluid.layers.fc(
            input=conv_pool_2, size=62, act="softmax"
        )
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
    inner_step = config_json["inner_step"]

    model = CNN()
    job_generator = JobGenerator()
    job_generator.set_losses([model.loss])
    job_generator.set_optimizer(fluid.optimizer.Adam(0.001))
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
