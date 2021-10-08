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

import pandas as pd
from sklearn.ensemble.gradient_boosting import GradientBoostingClassifier
from sklearn.metrics import roc_auc_score, accuracy_score

from kernel.examples.handler.utils.tools import JobConfig


def main(config="../../config.yaml", param="./multi_config.yaml"):
    # obtain config
    if isinstance(param, str):
        param = JobConfig.load_from_file(param)

    data_promoter = param["data_promoter"]
    data_provider = param["data_provider"]
    idx = param["idx"]
    label_name = param["label_name"]

    print('config is {}'.format(config))
    if isinstance(config, str):
        config = JobConfig.load_from_file(config)
        data_base_dir = config["data_base_dir"]
        print('data base dir is', data_base_dir)
    else:
        data_base_dir = config.data_base_dir

    # prepare data
    df_promoter = pd.read_csv(os.path.join(data_base_dir, data_promoter), index_col=idx)
    df_provider = pd.read_csv(os.path.join(data_base_dir, data_provider), index_col=idx)
    df = df_promoter.join(df_provider, rsuffix='provider')
    y = df[label_name]
    X = df.drop(label_name, axis=1)
    clf = GradientBoostingClassifier(random_state=0, n_estimators=50, learning_rate=0.3)
    clf.fit(X, y)
    y_pred = clf.predict(X)

    try:
        auc_score = roc_auc_score(y, y_pred)
    except:
        print(f"no auc score available")

    acc = accuracy_score(y, y_pred)
    result = {"accuracy": acc}
    print('multi result', result)
    return {}, result


if __name__ == "__main__":
    parser = argparse.ArgumentParser("GBDT MULTI JOB")
    parser.add_argument("-param", type=str, default="./multi_config.yaml",
                        help="config file for params")
    args = parser.parse_args()
    main(param=args.param)
