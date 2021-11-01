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
from ppdet.utils.eval_utils import parse_fetches, eval_run, eval_results


@click.command()
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
    checkpoint_path = config_json.get("checkpoint_path", None)
    need_eval = config_json.get("need_eval", False)
    model_dir = "model"
    checkpoint_dir = "checkpoint"
    output_eval = "eval"

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

    # if need_eval:
    #     eval_prog = fluid.Program()
    #     with fluid.program_guard(eval_prog, trainer._startup_program):
    #         with fluid.unique_name.guard():
    #             model = create(main_arch)
    #             inputs_def = cfg['EvalReader']['inputs_def']
    #             feed_vars, eval_loader = model.build_inputs(**inputs_def)
    #             fetches = model.eval(feed_vars)
    #     eval_prog = eval_prog.clone(True)
    #
    #     eval_reader = create_reader(cfg.EvalReader, devices_num=1)
    #     # When iterable mode, set set_sample_list_generator(eval_reader, place)
    #     eval_loader.set_sample_list_generator(eval_reader)
    #
    #     # parse eval fetches
    #     extra_keys = []
    #     if cfg.metric == 'COCO':
    #         extra_keys = ['im_info', 'im_id', 'im_shape']
    #     if cfg.metric == 'VOC':
    #         extra_keys = ['gt_bbox', 'gt_class', 'is_difficult']
    #     if cfg.metric == 'WIDERFACE':
    #         extra_keys = ['im_id', 'im_shape', 'gt_bbox']
    #     eval_keys, eval_values, eval_cls = parse_fetches(fetches, eval_prog,
    #                                                      extra_keys)
    #
    #
    #     compiled_eval_prog = fluid.CompiledProgram(eval_prog)

    ignore_params = cfg.finetune_exclude_pretrained_params \
        if 'finetune_exclude_pretrained_params' in cfg else []

    epoch_id = 0
    best_box_ap_list = [0.0, 0]  # [map, iter]

    if checkpoint_path:
        checkpoint.load_checkpoint(trainer.exe, trainer._main_program, checkpoint_path)
        epoch_id = checkpoint.global_step()
        logging.debug(f"use_checkpoint epoch_id: {epoch_id}")
    # elif cfg.pretrain_weights and not ignore_params:
    #     checkpoint.load_and_fusebn(trainer.exe, trainer._main_program, cfg.pretrain_weights)
    # elif cfg.pretrain_weights:
    #     checkpoint.load_params(
    #         trainer.exe, trainer._main_program, cfg.pretrain_weights, ignore_params=ignore_params)

    # whether output bbox is normalized in model output layer
    # is_bbox_normalized = False
    # if hasattr(model, 'is_bbox_normalized') and \
    #         callable(model.is_bbox_normalized):
    #     is_bbox_normalized = model.is_bbox_normalized()
    #
    # # if map_type not set, use default 11point, only use in VOC eval
    # map_type = cfg.map_type if 'map_type' in cfg else '11point'

    # redirect dataset path to VisualFL/data
    cfg.TrainReader["dataset"].dataset_dir = os.path.join(
        get_data_dir(), cfg.TrainReader["dataset"].dataset_dir
    )

    data_loader = create_reader(
        cfg.TrainReader, max_iter, cfg, devices_num=1, num_trainers=1
    )
    logging.error(f"{cfg.TrainReader['dataset']}")

    if use_vdl:
        from visualdl import LogWriter

        vdl_writer = LogWriter("vdl_log")
        vdl_loss_step = 0
        vdl_mAP_step = 0

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
                vdl_loss_step += 1
            logging.debug(f"step: {vdl_loss_step}, outs: {outs}")

        # if (epoch_id > 0 and epoch_id % cfg.snapshot_iter == 0 or epoch_id == cfg.max_iters - 1):
        #     save_name = str(epoch_id) if epoch_id != cfg.max_iters - 1 else "model_final"
        #     checkpoint.save(trainer.exe, trainer._main_program, os.path.join(checkpoint_dir, save_name))
        #
        #     if need_eval:
        #         # evaluation
        #         resolution = None
        #         if 'Mask' in cfg.architecture:
        #             resolution = model.mask_head.resolution
        #         results = eval_run(
        #             trainer.exe,
        #             compiled_eval_prog,
        #             eval_loader,
        #             eval_keys,
        #             eval_values,
        #             eval_cls,
        #             cfg,
        #             resolution=resolution)
        #         box_ap_stats = eval_results(
        #             results, cfg.metric, cfg.num_classes, resolution,
        #             is_bbox_normalized, output_eval, map_type,
        #             cfg['EvalReader']['dataset'])
        #
        #         # use vdl_paddle to log mAP
        #         if use_vdl:
        #             vdl_writer.add_scalar("mAP", box_ap_stats[0], vdl_mAP_step)
        #             vdl_mAP_step += 1
        #
        #         if box_ap_stats[0] > best_box_ap_list[0]:
        #             best_box_ap_list[0] = box_ap_stats[0]
        #             best_box_ap_list[1] = epoch_id
        #             checkpoint.save(trainer.exe, trainer._main_program,
        #                             os.path.join(checkpoint_dir, "best_model"))
        #         logging.info("Best test box ap: {}, in iter: {}".format(
        #             best_box_ap_list[0], best_box_ap_list[1]))
        # save model
        logging.debug(f"saving model at {epoch_id}-th epoch")
        trainer.save_model(f"model/{epoch_id}")

        # info scheduler
        trainer.scheduler_agent.finish()
        checkpoint.save(trainer.exe, trainer._main_program, f"checkpoint/{epoch_id}")

        epoch_id += 1
    logging.debug(f"reach max iter, finish training")


if __name__ == "__main__":
    fl_trainer()
