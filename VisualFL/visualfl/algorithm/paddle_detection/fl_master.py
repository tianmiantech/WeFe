# Copyright (c) 2019 PaddlePaddle Authors. All Rights Reserved.
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

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function
import os
import json
import logging
import click
from paddle import fluid
from visualfl.algorithm.paddle_detection._empty_optimizer import (
    EmptyOptimizer,
)
from paddle_fl.core.master.job_generator import JobGenerator
from paddle_fl.core.strategy.fl_strategy_base import (
    FedAvgStrategy,
)
from ppdet.core.workspace import load_config, create
from ppdet.utils.check import check_version, check_config
from visualfl.algorithm.paddle_detection._merge_config import merger_algorithm_config


class Model(object):
    def __init__(self):
        self.feeds = None
        self.startup_program = None
        self.loss = None

    def build_program(self, config):

        with open(config) as f:
            algorithm_config_dict = json.load(f)

        cfg = merger_algorithm_config(algorithm_config_dict)
        check_config(cfg)
        check_version()

        lr_builder = create("LearningRate")
        optimizer_builder = create("OptimizerBuilder")

        # build program
        self.startup_program = fluid.Program()
        train_program = fluid.Program()
        with fluid.program_guard(train_program, self.startup_program):
            with fluid.unique_name.guard():
                model = create(cfg.architecture)

                inputs_def = cfg["TrainReader"]["inputs_def"]
                # can't compile with dataloader now.
                inputs_def["use_dataloader"] = False
                feed_vars, _ = model.build_inputs(**inputs_def)

                train_fetches = model.train(feed_vars)
                loss = train_fetches["loss"]
                lr = lr_builder()
                optimizer = optimizer_builder(lr)
                optimizer.minimize(loss)

        self.loss = loss
        self.feeds = feed_vars


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

    model = Model()
    model.build_program(algorithm_config)

    job_generator = JobGenerator()
    job_generator.set_losses([model.loss])
    job_generator.set_optimizer(EmptyOptimizer())  # optimizer defined in Model
    job_generator.set_startup_program(model.startup_program)
    job_generator.set_infer_feed_and_target_names(
        [name for name in model.feeds], [model.loss.name]
    )
    job_generator.set_feeds(model.feeds.values())

    strategy = FedAvgStrategy()
    strategy.fed_avg = True
    strategy.inner_step = 1

    endpoints = [ps_endpoint]
    output = "compile"
    job_generator.generate_fl_job(
        strategy, server_endpoints=endpoints, worker_num=worker_num, output=output
    )


if __name__ == "__main__":
    fl_master()
