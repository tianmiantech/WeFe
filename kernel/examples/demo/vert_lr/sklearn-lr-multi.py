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



import argparse
import os

import pandas
from sklearn.linear_model import SGDClassifier
from sklearn.metrics import precision_score, accuracy_score, recall_score

from kernel.examples.handler.utils.tools import load_job_config, JobConfig


def main(config="../../config.yaml", param="./multi_config.yaml"):
    # obtain config
    if isinstance(param, str):
        param = JobConfig.load_from_file(param)
    assert isinstance(param, dict)
    data_promoter = param["data_promoter"]
    data_provider = param["data_provider"]

    idx = param["idx"]
    label_name = param["label_name"]

    if isinstance(config, str):
        config = load_job_config(config)
    data_base_dir = config.data_base_dir

    config_param = {
        "penalty": param["penalty"],
        "max_iter": param["max_iter"],
        "alpha": param["alpha"],
        "learning_rate": "optimal",
        "eta0": param["learning_rate"]
    }

    # prepare data
    df_promoter = pandas.read_csv(os.path.join(data_base_dir, data_promoter), index_col=idx)
    df_provider = pandas.read_csv(os.path.join(data_base_dir, data_provider), index_col=idx)

    df = df_promoter.join(df_provider, rsuffix="provider")
    y = df[label_name]
    X = df.drop(label_name, axis=1)
    # lm = LogisticRegression(max_iter=20)
    lm = SGDClassifier(loss="log", **config_param, shuffle=False)
    lm_fit = lm.fit(X, y)
    y_pred = lm_fit.predict(X)

    recall = recall_score(y, y_pred, average="macro")
    pr = precision_score(y, y_pred, average="macro")
    acc = accuracy_score(y, y_pred)

    result = {"accuracy": acc, "recall": recall, "pr": pr}
    print(result)
    return {}, result


if __name__ == "__main__":
    parser = argparse.ArgumentParser("LR MULTI SKLEARN JOB")
    parser.add_argument("-param", type=str, default="./multi_config.yaml",
                        help="config file for params")
    args = parser.parse_args()
    main(param=args.param)
