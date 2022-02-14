import os
from visualfl import __basedir__
from ppdet.core.workspace import create,load_config

def merger_algorithm_config(algorithm_config):

    program_full_path = os.path.join(__basedir__, 'algorithm', 'paddle_detection')
    default_config_name = 'default_algorithm_config.yml'
    architecture = algorithm_config["architecture"]
    default_algorithm_config = os.path.join(program_full_path, "configs", architecture.lower(), default_config_name)
    cfg = load_config(default_algorithm_config)

    cfg["max_iters"] = algorithm_config["max_iter"]
    cfg["inner_step"] = algorithm_config["inner_step"]
    cfg["num_classes"] = algorithm_config["num_classes"]
    cfg["LearningRate"].base_lr = algorithm_config["base_lr"]
    cfg.TrainReader["inputs_def"]["image_shape"] = algorithm_config["image_shape"]
    cfg.TrainReader["batch_size"] = algorithm_config["batch_size"]

    return cfg



