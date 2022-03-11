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

import os
from visualfl import __basedir__
from ppdet.core.workspace import create,load_config

def merger_algorithm_config(algorithm_config,data_name=None):


    program_full_path = os.path.join(__basedir__, 'algorithm', 'paddle_detection')
    architecture = algorithm_config["architecture"]
    config_name = f'{architecture}.yml'
    default_algorithm_config = os.path.join(program_full_path, "configs", architecture.split('_')[0], config_name)
    cfg = load_config(default_algorithm_config)

    cfg["max_iters"] = algorithm_config["max_iter"]
    cfg["inner_step"] = algorithm_config["inner_step"]
    num_class = algorithm_config["num_classes"]
    cfg["num_classes"] = num_class if architecture.startswith("yolo") or architecture.startswith("ppyolo") else num_class+1
    cfg["LearningRate"].base_lr = algorithm_config["base_lr"]
    # cfg.TrainReader["inputs_def"]["image_shape"] = algorithm_config["image_shape"]
    cfg.TrainReader["batch_size"] = algorithm_config["batch_size"]
    cfg.TrainReader["dataset"].dataset_dir = data_name
    cfg.EvalReader["dataset"].dataset_dir = data_name

    return cfg


