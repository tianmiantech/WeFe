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

# Copyright (c) 2020 The FedVision Authors. All Rights Reserved.
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
import os

import click

from visualfl.paddle_fl.trainer._trainer import FedAvgTrainer
from visualfl import get_data_dir
from ppdet.core.workspace import create
from visualfl.db.task_progress_dao import TaskProgressDao
from visualdl import LogWriter,LogReader
from visualfl.utils.tools import *

@click.command()
@click.option("--job-id", type=str, required=True)
@click.option("--task-id", type=str, required=True)
@click.option("--scheduler-ep", type=str, required=True)
@click.option("--trainer-id", type=int, required=True)
@click.option("--trainer-ep", type=str, required=True)
@click.option(
    "--main-program",
    type=click.Path(exists=True, file_okay=True, dir_okay=False),
    required=True,
)
@click.option(
    "--startup-program",
    type=click.Path(exists=True, file_okay=True, dir_okay=False),
    required=True,
)
@click.option(
    "--send-program",
    type=click.Path(exists=True, file_okay=True, dir_okay=False),
    required=True,
)
@click.option(
    "--recv-program",
    type=click.Path(exists=True, file_okay=True, dir_okay=False),
    required=True,
)
@click.option(
    "--feed-names",
    type=click.Path(exists=True, file_okay=True, dir_okay=False),
    required=True,
)
@click.option(
    "--target-names",
    type=click.Path(exists=True, file_okay=True, dir_okay=False),
    required=True,
)
@click.option(
    "--strategy",
    type=click.Path(exists=True, file_okay=True, dir_okay=False),
    required=True,
)
@click.option(
    "--feeds",
    type=click.Path(exists=True, file_okay=True, dir_okay=False),
    required=True,
)
@click.option(
    "--config",
    type=click.Path(exists=True, file_okay=True, dir_okay=False),
    required=True,
)
@click.option(
    "--algorithm-config",
    type=click.Path(exists=True, file_okay=True, dir_okay=False),
    required=True,
)
def fl_trainer(
    job_id: str,
    task_id: str,
    trainer_id: int,
    trainer_ep: str,
    scheduler_ep: str,
    main_program,
    startup_program,
    send_program,
    recv_program,
    feed_names,
    target_names,
    strategy,
    feeds,
    config,
    algorithm_config,
):
    import numpy as np
    import paddle.fluid as fluid

    from ppdet.core.workspace import load_config
    from ppdet.data import create_reader
    from ppdet.utils import checkpoint
    from ppdet.utils.check import check_config, check_version

    logging.basicConfig(
        filename="trainer.log",
        filemode="w",
        format="%(asctime)s %(name)s:%(levelname)s:%(message)s",
        datefmt="%d-%M-%Y %H:%M:%S",
        level=logging.DEBUG,
    )

    with open(config) as f:
        config_json = json.load(f)
    max_iter = config_json["max_iter"]
    device = config_json.get("device", "cpu")
    use_vdl = config_json.get("use_vdl", False)
    resume_checkpoint = config_json.get("resume", True)
    save_model_dir = "model"
    save_checkpoint_dir = "checkpoint"
    log_dir = "vdl_log"

    logging.debug(f"training program begin")
    trainer = FedAvgTrainer(scheduler_ep=scheduler_ep, trainer_ep=trainer_ep)
    logging.debug(f"job program loading")
    trainer.load_job(
        main_program=main_program,
        startup_program=startup_program,
        send_program=send_program,
        recv_program=recv_program,
        feed_names=feed_names,
        target_names=target_names,
        strategy=strategy,
    )
    logging.debug(f"job program loaded")
    place = fluid.CPUPlace() if device != "cuda" else fluid.CUDAPlace(0)

    logging.debug(f"trainer starting with place {place}")
    trainer.start(place)
    logging.debug(f"trainer stared")

    cfg = load_config(algorithm_config)
    check_config(cfg)
    check_version()

    logging.debug(f"loading data")
    feed_list = trainer.load_feed_list(feeds)
    feeder = fluid.DataFeeder(feed_list=feed_list, place=place)
    logging.debug(f"data loader ready")

    epoch_id = 0
    vdl_loss_step = 0
    # vdl_mAP_step = 0
    TaskProgressDao.init_task_progress(task_id, max_iter)
    if resume_checkpoint:
        vdl_loss_step = checkpoint.global_step()
        epoch_id = round(vdl_loss_step / max_iter)
        checkpoint.load_checkpoint(trainer.exe, trainer._main_program, f"checkpoint/{epoch_id}")
        logging.debug(f"use_checkpoint epoch_id: {epoch_id}")
        TaskProgressDao.set_task_progress(task_id, epoch_id)
    # elif cfg.pretrain_weights and not ignore_params:
    #     checkpoint.load_and_fusebn(trainer.exe, trainer._main_program, cfg.pretrain_weights)
    # elif cfg.pretrain_weights:
    #     checkpoint.load_params(
    #         trainer.exe, trainer._main_program, cfg.pretrain_weights, ignore_params=ignore_params)

    # redirect dataset path to VisualFL/data
    cfg.TrainReader["dataset"].dataset_dir = os.path.join(
        get_data_dir(), cfg.TrainReader["dataset"].dataset_dir
    )

    data_loader = create_reader(
        cfg.TrainReader, max_iter, cfg, devices_num=1, num_trainers=1
    )
    logging.error(f"{cfg.TrainReader['dataset']}")

    if use_vdl:
        vdl_writer = LogWriter("vdl_log")

    while epoch_id < max_iter:
        if not trainer.scheduler_agent.join(epoch_id):
            logging.debug(f"not join, waiting next round")
            continue

        logging.debug(f"epoch {epoch_id} start train")

        for step_id, data in enumerate(data_loader()):
            outs = trainer.run(feeder.feed(data), fetch=trainer._target_names)
            if use_vdl:
                stats = {
                    k: np.array(v).mean() for k, v in zip(trainer._target_names, outs)
                }
                for loss_name, loss_value in stats.items():
                    vdl_writer.add_scalar(loss_name, loss_value, vdl_loss_step)
                    get_data_to_db(task_id,log_dir,loss_name,"loss","paddle_detection")
                vdl_loss_step += 1
            logging.debug(f"step: {vdl_loss_step}, outs: {outs}")

        # save model
        logging.debug(f"saving model at {epoch_id}-th epoch")
        trainer.save_model(os.path.join(save_model_dir,str(epoch_id)))

        # info scheduler
        trainer.scheduler_agent.finish()
        checkpoint.save(trainer.exe, trainer._main_program, os.path.join(save_checkpoint_dir,str(epoch_id)))
        epoch_id += 1
        TaskProgressDao.add_task_progress(task_id, 1)
    logging.debug(f"reach max iter, finish training")


if __name__ == "__main__":
    fl_trainer()
