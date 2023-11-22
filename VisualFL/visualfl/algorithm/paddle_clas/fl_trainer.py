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
import click
import paddle
from visualfl.paddle_fl.trainer._trainer import FedAvgTrainer
from visualfl.utils import data_loader
from visualfl.utils.tools import *
from visualfl.utils.consts import TaskStatus,ComponentName,TaskResultType

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
    from visualfl import get_data_dir
    from ppdet.utils import checkpoint

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
        device = config_json.get("device", "cpu")
        use_vdl = config_json.get("use_vdl", False)
        resume_checkpoint = config_json.get("resume", False)
        save_model_dir = "model"
        save_checkpoint_dir = "checkpoint"


        with open(algorithm_config) as f:
            algorithm_config_dict = json.load(f)

        batch_size = algorithm_config_dict.get("batch_size", 1024)
        need_shuffle = algorithm_config_dict.get("need_shuffle", True)
        max_iter = algorithm_config_dict.get("max_iter")
        download_url = algorithm_config_dict.get("download_url")
        data_name = algorithm_config_dict.get("data_name")

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

        logging.debug(f"loading data")
        feed_list = trainer.load_feed_list(feeds)
        feeder = fluid.DataFeeder(feed_list=feed_list, place=place)
        logging.debug(f"data loader ready")

        data_dir = data_loader.job_download(download_url, job_id, get_data_dir())
        labelpath = os.path.join(data_dir,"label_list.txt")
        TaskDao(task_id).save_task_result({"label_path":labelpath},ComponentName.CLASSIFY,TaskResultType.LABEL)
        reader = data_loader.train(data_dir=data_dir)
        if need_shuffle:
            reader = fluid.io.shuffle(
                reader=reader,
                buf_size=1000,
            )
        train_loader = paddle.batch(reader=reader, batch_size=batch_size)

        epoch_id = -1
        step = 0
        TaskDao(task_id).init_task_progress(max_iter)
        TaskDao(task_id).start_task()
        # if resume_checkpoint:
        #     try:
        #         epoch_id = TaskDao(task_id).get_task_progress()
        #         checkpoint.load_checkpoint(trainer.exe, trainer._main_program, f"checkpoint/{epoch_id}")
        #         logging.debug(f"use_checkpoint epoch_id: {epoch_id}")
        #     except Exception as e:
        #         logging.error(f"task id {task_id} train error {e}")
        #         raise Exception(f"train error as task id {task_id} ")

        if use_vdl:
            from visualdl import LogWriter
            vdl_writer = LogWriter("vdl_log")

        while epoch_id < max_iter:
            epoch_id += 1
            if not trainer.scheduler_agent.join(epoch_id):
                logging.debug(f"not join, waiting next round")
                continue

            logging.debug(f"epoch {epoch_id} start train")

            for step_id, data in enumerate(train_loader()):
                outs = trainer.run(feeder.feed(data), fetch=trainer._target_names)
                if use_vdl:
                    stats = {
                        k: np.array(v).mean() for k, v in zip(trainer._target_names, outs)
                    }
                    for loss_name, loss_value in stats.items():
                        vdl_writer.add_scalar(loss_name, loss_value, step)
                        save_data_to_db(task_id, loss_name,loss_value,step,ComponentName.CLASSIFY)
                step += 1
                logging.debug(f"step: {step}, outs: {outs}")

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
        logging.error(f"task id {task_id} train error {e}")
        TaskDao(task_id).update_task_status(TaskStatus.ERROR, str(e))
        raise Exception(f"train error as task id {task_id} ")


if __name__ == "__main__":
    fl_trainer()
