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

import os
import traceback
import click
from visualfl import __logs_dir__
from visualfl.paddle_fl.trainer._trainer import FedAvgTrainer
from visualfl import get_data_dir
from visualfl.db.task_dao import TaskDao
from visualdl import LogWriter,LogReader
from visualfl.utils.consts import TaskStatus,ComponentName,TaskResultType
from visualfl.utils.tools import *
from visualfl.algorithm.paddle_detection._merge_config import merger_algorithm_config

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
    from visualfl.utils import data_loader

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

    try:
        with open(config) as f:
            config_json = json.load(f)
        max_iter = config_json["max_iter"]
        device = config_json.get("device", "cpu")
        use_vdl = config_json.get("use_vdl", False)
        resume_checkpoint = config_json.get("resume", False)
        save_model_dir = "model"
        save_checkpoint_dir = "checkpoint"


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

        with open(algorithm_config) as f:
            algorithm_config_json = json.load(f)

        download_url = algorithm_config_json.get("download_url")
        data_name = algorithm_config_json.get("data_name")

        data_dir = data_loader.job_download(download_url, job_id, get_data_dir())
        labelpath = os.path.join(data_dir, "label_list.txt")
        TaskDao(task_id).save_task_result({"label_path":labelpath}, ComponentName.DETECTION, TaskResultType.LABEL)
        cfg = merger_algorithm_config(algorithm_config_json,os.path.basename(data_dir))
        check_config(cfg)
        check_version()

        logging.debug(f"loading data")
        feed_list = trainer.load_feed_list(feeds)
        feeder = fluid.DataFeeder(feed_list=feed_list, place=place)
        logging.debug(f"data loader ready")

        epoch_id = -1
        vdl_loss_step = 0
        # vdl_mAP_step = 0
        TaskDao(task_id).init_task_progress(max_iter)
        if resume_checkpoint:
            try:
                epoch_id = TaskDao(task_id).get_task_progress()
                # vdl_loss_step = checkpoint.global_step()
                # epoch_id = round(vdl_loss_step / max_iter)
                checkpoint.load_checkpoint(trainer.exe, trainer._main_program, f"checkpoint/{epoch_id}")
                logging.debug(f"use_checkpoint epoch_id: {epoch_id}")
            except Exception as e:
                logging.error(f"task id {task_id} train error {e}")

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
            epoch_id += 1
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
                        save_data_to_db(task_id, loss_name, loss_value,vdl_loss_step,ComponentName.DETECTION)
                vdl_loss_step += 1
                logging.debug(f"step: {vdl_loss_step}, outs: {outs}")

            # save model
            logging.debug(f"saving model at {epoch_id}-th epoch")
            trainer.save_model(os.path.join(save_model_dir,str(epoch_id)))

            # info scheduler
            trainer.scheduler_agent.finish()
            checkpoint.save(trainer.exe, trainer._main_program, os.path.join(save_checkpoint_dir,str(epoch_id)))
            TaskDao(task_id).add_task_progress(1)

        TaskDao(task_id).update_task_status(TaskStatus.SUCCESS)
        TaskDao(task_id).finish_task_progress()
        TaskDao(task_id).update_serving_model(type=TaskResultType.LOSS)
        logging.debug(f"reach max iter, finish training")
    except Exception as e:
        logging.error(f"task id {task_id} train error: {e}")
        TaskDao(task_id).update_task_status(TaskStatus.ERROR,str(e))
        raise Exception(f"train error as task id {task_id} ")


if __name__ == "__main__":
    fl_trainer()
