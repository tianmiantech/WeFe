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

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import os, sys
# add python path of PadleDetection to sys.path
parent_path = os.path.abspath(os.path.join(__file__, *(['..'] * 2)))
if parent_path not in sys.path:
    sys.path.append(parent_path)

import paddle
from paddle import fluid

from ppdet.core.workspace import load_config, merge_config, create
from ppdet.utils.cli import ArgsParser
from ppdet.utils.check import check_config, check_version, enable_static_mode
import ppdet.utils.checkpoint as checkpoint
import yaml,json
from visualfl.db.task_dao import TaskDao
from visualfl.utils.consts import TaskResultType

import logging
from ppdet.utils.export_utils import dump_infer_config, prune_feed_vars
FORMAT = '%(asctime)s-%(levelname)s: %(message)s'
logging.basicConfig(level=logging.INFO, format=FORMAT)
logger = logging.getLogger(__name__)


def save_serving_model(FLAGS, exe, feed_vars, test_fetches, infer_prog):
    cfg_name = os.path.basename(FLAGS.config).split('.')[0]
    save_dir = os.path.join(FLAGS.output_dir, cfg_name)
    feed_var_names = [var.name for var in feed_vars.values()]
    fetch_list = sorted(test_fetches.items(), key=lambda i: i[0])
    target_vars = [var[1] for var in fetch_list]
    feed_var_names = prune_feed_vars(feed_var_names, target_vars, infer_prog)
    serving_client = os.path.join(FLAGS.output_dir, 'serving_client')
    serving_server = os.path.join(FLAGS.output_dir, 'serving_server')
    logger.info(
        "Export serving model to {}, client side: {}, server side: {}. input: {}, output: "
        "{}...".format(FLAGS.output_dir, serving_client, serving_server,
                       feed_var_names, [str(var.name) for var in target_vars]))
    feed_dict = {x: infer_prog.global_block().var(x) for x in feed_var_names}
    fetch_dict = {x.name: x for x in target_vars}
    import paddle_serving_client.io as serving_io
    serving_client = os.path.join(save_dir, 'serving_client')
    serving_server = os.path.join(save_dir, 'serving_server')
    serving_io.save_model(
        client_config_folder=serving_client,
        server_model_folder=serving_server,
        feed_var_dict=feed_dict,
        fetch_var_dict=fetch_dict,
        main_program=infer_prog)


def main():
    cfg = load_config(FLAGS.config)
    merge_config(FLAGS.opt)
    check_config(cfg)
    check_version()

    main_arch = cfg.architecture

    dataset = cfg.TestReader['dataset']
    task_result = TaskDao(FLAGS.task_id).get_task_result(TaskResultType.LABEL)
    if task_result:
        dataset.anno_path = json.loads(task_result.result).get("label_path")

    # Use CPU for exporting inference model instead of GPU
    place = fluid.CPUPlace()
    exe = fluid.Executor(place)

    model = create(main_arch)

    startup_prog = fluid.Program()
    infer_prog = fluid.Program()
    with fluid.program_guard(infer_prog, startup_prog):
        with fluid.unique_name.guard():
            inputs_def = cfg['TestReader']['inputs_def']
            inputs_def['use_dataloader'] = False
            feed_vars, _ = model.build_inputs(**inputs_def)
            test_fetches = model.test(feed_vars)
    infer_prog = infer_prog.clone(True)

    exe.run(startup_prog)
    checkpoint.load_params(exe, infer_prog, cfg.weights)

    save_serving_model(FLAGS, exe, feed_vars, test_fetches, infer_prog)
    dump_infer_config(FLAGS, cfg)


if __name__ == '__main__':
    enable_static_mode()
    parser = ArgsParser()
    parser.add_argument(
        "--task_id",
        type=str,
        default=None)
    parser.add_argument(
        "--output_dir",
        type=str,
        default="output",
        help="Directory for storing the output model files.")

    FLAGS = parser.parse_args()
    main()
