# Copyright 2021 The WeFe Authors. All Rights Reserved.
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



import argparse
import os

import numpy as np
import pandas
import tensorflow as tf
from sklearn import metrics
from sklearn.preprocessing import LabelEncoder
from tensorflow import keras
from tensorflow.keras import optimizers
from tensorflow.keras.utils import to_categorical

from kernel.examples.handler.utils.tools import JobConfig


def build(param, shape1, shape2):
    input1 = tf.keras.layers.Input(shape=(shape1,))
    x1 = tf.keras.layers.Dense(units=param["bottom_layer_units"], activation='tanh',
                               kernel_initializer=keras.initializers.RandomUniform(minval=-1, maxval=1, seed=123))(
        input1)
    input2 = tf.keras.layers.Input(shape=(shape2,))
    x2 = tf.keras.layers.Dense(units=param["bottom_layer_units"], activation='tanh',
                               kernel_initializer=keras.initializers.RandomUniform(minval=-1, maxval=1, seed=123))(
        input2)

    concat = tf.keras.layers.Concatenate(axis=-1)([x1, x2])
    out1 = tf.keras.layers.Dense(units=param["interactive_layer_units"], activation='relu',
                                 kernel_initializer=keras.initializers.RandomUniform(minval=-1, maxval=1, seed=123))(
        concat)
    out2 = tf.keras.layers.Dense(units=param["top_layer_units"], activation=param["top_act"],
                                 kernel_initializer=keras.initializers.RandomUniform(minval=-1, maxval=1, seed=123))(
        out1)
    model = tf.keras.models.Model(inputs=[input1, input2], outputs=out2)
    opt = getattr(optimizers, param["opt"])(lr=param["learning_rate"])
    model.compile(optimizer=opt, loss=param["loss"])

    return model


def main(config="../../config.yaml", param="./param_conf_binary.yaml"):
    if isinstance(config, str):
        config = JobConfig.load_from_file(config)
        data_base_dir = config["data_base_dir"]
    else:
        data_base_dir = config.data_base_dir

    if isinstance(param, str):
        param = JobConfig.load_from_file(param)
    data_guest = param["data_promoter"]
    data_host = param["data_provider"]

    idx = param["idx"]
    label_name = param["label_name"]
    # prepare data
    Xb = pandas.read_csv(os.path.join(data_base_dir, data_guest), index_col=idx)
    Xa = pandas.read_csv(os.path.join(data_base_dir, data_host), index_col=idx)
    y = Xb[label_name]
    if param["loss"] == "categorical_crossentropy":
        labels = y.copy()
        label_encoder = LabelEncoder()
        y = label_encoder.fit_transform(y)
        y = to_categorical(y)

    Xb = Xb.drop(label_name, axis=1)
    model = build(param, Xb.shape[1], Xa.shape[1])
    model.fit([Xb, Xa], y, epochs=param["epochs"], verbose=0, batch_size=param["batch_size"], shuffle=True)

    eval_result = {}
    for metric in param["metrics"]:
        if metric.lower() == "auc":
            predict_y = model.predict([Xb, Xa])
            y_p = []
            for item in predict_y:
                if item > 0.5:
                    item = 1
                else:
                    item = 0
                y_p.append(item)
            y_p = np.array(y_p)
            auc = metrics.roc_auc_score(y, predict_y)
            # recall = metrics.recall_score(y, y_p , average="macro")
            # pr = metrics.precision_score(y, y_p ,average="macro")
            acc = metrics.accuracy_score(y, y_p)
            fpr, tpr, thresholds = metrics.roc_curve(y, y_p)

            ks = max(tpr - fpr)
            eval_result["auc"] = auc
            eval_result["ks"] = ks
            eval_result["accuracy"] = acc
        elif metric == "accuracy":
            predict_y = np.argmax(model.predict([Xb, Xa]), axis=1)
            predict_y = label_encoder.inverse_transform(predict_y)
            acc = metrics.accuracy_score(y_true=labels, y_pred=predict_y)
            eval_result["accuracy"] = acc

    print(eval_result)
    data_summary = {}
    return data_summary, eval_result


if __name__ == "__main__":
    parser = argparse.ArgumentParser("BENCHMARK-QUALITY SKLEARN JOB")
    parser.add_argument("-param", type=str,
                        help="config file for params")
    # args = parser.parse_args()
    # if args.config is not None:
    #     main(args.param)
    main()
