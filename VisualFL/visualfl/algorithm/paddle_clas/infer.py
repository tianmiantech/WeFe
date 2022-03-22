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

import os
import sys
import json
__dir__ = os.path.dirname(os.path.abspath(__file__))
sys.path.append(__dir__)
sys.path.append(os.path.abspath(os.path.join(__dir__, '../../')))

import argparse
import numpy as np
import paddle
import paddle.fluid as fluid
from visualfl.algorithm.paddle_clas import models
import cv_utils as utils
from visualfl.db.task_dao import TaskDao
from visualfl.utils.consts import ComponentName,TaskResultType
import logging
FORMAT = '%(asctime)s-%(levelname)s: %(message)s'
logging.basicConfig(level=logging.INFO, format=FORMAT)
logger = logging.getLogger(__name__)


def parse_args():
    def str2bool(v):
        return v.lower() in ("true", "t", "1")

    parser = argparse.ArgumentParser()
    parser.add_argument("--job_id", type=str, required=False)
    parser.add_argument("--task_id", type=str,required=True)
    parser.add_argument("-i", "--infer_dir", type=str,required=True)
    parser.add_argument("-c", "--config", type=str,required=True)
    parser.add_argument("--weights", type=str,required=True)
    parser.add_argument("--output_dir", type=str,required=False)
    parser.add_argument("--use_gpu", type=str2bool, default=False)

    return parser.parse_args()


def create_predictor(args):
    with open(args.config) as f:
        config = json.load(f)

    def create_input(config):
        image = fluid.layers.data(
            name='image', shape=config.get("image_shape"), dtype='float32')
        return image

    def create_model(architecture, model, input, class_dim=1000):
        if architecture == "GoogLeNet":
            out, _, _ = model.net(input=input, class_dim=class_dim)
        else:
            out = model.net(input=input, class_dim=class_dim)
            out = fluid.layers.softmax(out)
        return out

    architecture = config.get("architecture")
    model = models.__dict__[architecture]()

    place = fluid.CUDAPlace(0) if args.use_gpu else fluid.CPUPlace()
    exe = fluid.Executor(place)

    startup_prog = fluid.Program()
    infer_prog = fluid.Program()
    with fluid.program_guard(infer_prog, startup_prog):
        with fluid.unique_name.guard():
            image = create_input(config)
            out = create_model(architecture, model, image,config.get("num_classes"))
            exe.run(startup_prog)

    infer_prog = infer_prog.clone(for_test=True)
    fluid.load(
        program=infer_prog, model_path=args.weights, executor=exe)

    return exe, infer_prog, [image.name], [out.name]


def create_operators():
    size = 224
    img_mean = [0.485, 0.456, 0.406]
    img_std = [0.229, 0.224, 0.225]
    img_scale = 1.0 / 255.0

    decode_op = utils.DecodeImage()
    resize_op = utils.ResizeImage(resize_short=256)
    crop_op = utils.CropImage(size=(size, size))
    normalize_op = utils.NormalizeImage(
        scale=img_scale, mean=img_mean, std=img_std)
    totensor_op = utils.ToTensor()

    return [decode_op, resize_op, crop_op, normalize_op, totensor_op]


def preprocess(fname, ops):
    data = open(fname, 'rb').read()
    for op in ops:
        data = op(data)

    return data


def postprocess(outputs, topk=5):
    output = outputs[0]
    prob = np.array(output).flatten()
    index = prob.argsort(axis=0)[-topk:][::-1].astype('int32')
    return zip(index, prob[index])


def get_image_list(img_file):
    imgs_lists = []
    if img_file is None or not os.path.exists(img_file):
        raise Exception("not found any img file in {}".format(img_file))

    img_end = ['jpg', 'png', 'jpeg', 'JPEG', 'JPG', 'bmp']
    if os.path.isfile(img_file) and img_file.split('.')[-1] in img_end:
        imgs_lists.append(img_file)
    elif os.path.isdir(img_file):
        for single_file in os.listdir(img_file):
            if single_file.split('.')[-1] in img_end:
                imgs_lists.append(os.path.join(img_file, single_file))
    if len(imgs_lists) == 0:
        raise Exception("not found any img file in {}".format(img_file))
    return imgs_lists


def main():
    args = parse_args()
    operators = create_operators()
    exe, program, feed_names, fetch_names = create_predictor(args)
    image_list = get_image_list(args.infer_dir)
    TaskDao(args.task_id).save_task_result({"status": "running"}, ComponentName.CLASSIFY,type=TaskResultType.INFER)
    task_result = TaskDao(args.task_id).get_task_result(TaskResultType.LABEL)
    if not task_result:
        raise Exception(f"task result is None as task id: {args.task_id}")
    label_file = json.loads(task_result.result).get("label_path")
    cats = []
    with open(label_file) as f:
        for line in f.readlines():
            lines = line.replace('\n','').split(' ')
            cats.append(' '.join(lines[1:]))
    infer_result = {}
    img_probs = []
    for idx, filename in enumerate(image_list):
        data = preprocess(filename, operators)
        data = np.expand_dims(data, axis=0)
        outputs = exe.run(program,
                          feed={feed_names[0]: data},
                          fetch_list=fetch_names,
                          return_numpy=False)
        probs = postprocess(outputs)
        logger.debug("current image: {}".format(filename))
        infer_probs = []
        for idx, prob in probs:
            logger.debug("\tclass id: {:d}, probability: {:.4f}".format(idx, prob))
            infer_probs.append({"class_id":idx,"class_name":cats[idx],"prob":prob})
        infer_dict = {"image": os.path.basename(filename), "infer_probs": infer_probs}
        img_probs.append(infer_dict)
    infer_result["result"] = img_probs
    infer_result["status"] = "finish"
    TaskDao(task_id=args.task_id).save_task_result(infer_result, ComponentName.CLASSIFY, type=TaskResultType.INFER)

if __name__ == "__main__":
    main()