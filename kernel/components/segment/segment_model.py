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



import hashlib
import random

import gmpy2
import numpy as np

from common.python.common.exception.custom_exception import HasNoLabelError
from common.python.utils import log_utils
from kernel.base.instance import Instance
from kernel.components.segment.param import SegmentParam
from kernel.model_base import ModelBase
from kernel.security.diffie_hellman import DiffieHellman
from kernel.transfer.variables.transfer_class.segment_transfer_variable import SegmentTransferVariable
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class SegmentModelBase(ModelBase):

    def __init__(self):
        super().__init__()

        self.transfer_variable = SegmentTransferVariable()
        self.model_param = SegmentParam()
        self.header = None
        self.sid_name = None
        self.label_idx = None
        self.delimitor = ','
        self.final_feature_columns = None

        self.schema = None
        self.label = None

        self.provider_p = None
        self.provider_g = None
        self.provider_r = random.SystemRandom().getrandbits(128)
        self.promoter_key = None

        self.promoter_p = None
        self.promoter_g = None
        self.promoter_r = None
        self.provider_key = None

    def _init_model(self, segment_param):
        self.mode = segment_param.mode
        self.train_ratio = segment_param.train_ratio
        self.random_num = segment_param.random_num
        self.feature_columns = segment_param.feature_columns
        self.with_label = segment_param.with_label
        self.label_name = segment_param.label_name
        self.label_type = segment_param.label_type

    def filter_features(self, data_instances):
        """

        filter the selected features

        Parameters
        ----------
        data_instances: data list

        Returns
        -------
        final_input_data: data list after filter

        """
        self.schema = data_instances.schema
        self.header = self.schema["header"]
        self.sid_name = self.schema.get("sid", "")
        if self.sid_name == "":
            self.sid_name = self.schema.get("sid_name", "")
        if "label_name" in self.schema.keys():
            self.label = self.schema['label_name']
        LOGGER.debug("src_header is {}".format(self.header))
        LOGGER.debug("sid_name is {}".format(self.sid_name))
        LOGGER.debug("schema is {}".format(self.schema))
        if self.feature_columns == '' or self.feature_columns is None:
            self.feature_columns = self.header
        if isinstance(self.feature_columns, str):
            header_index = [self.header.index(item) for item in self.feature_columns.split(",")]
        else:
            header_index = [self.header.index(item) for item in self.feature_columns]

        # generate instances
        input_data = data_instances.mapValues(lambda v: [v.label] + list(v.features[header_index]))
        final_input_data = input_data.map(lambda k, v: (k, self.to_instance(v[1:], v[0])))

        return final_input_data

    def is_vert(self):
        return self.mode == consts.VERT

    def horz_segment(self, data_instances):
        """

        cut the data in horizontal task

        Parameters
        ----------
        data_instances: data list

        Returns
        -------
        promoter_train_data_with_header: train data
        promoter_test_data_with_header: test data

        """

        # determine if there is a label in dataset
        data_instances_take = data_instances.take()
        if data_instances_take[0][1].label is None:
            raise HasNoLabelError()

        # collect data that has positive label
        positive_data_list = data_instances.filter(lambda k, v: int(v.label) == 1)
        # collect data that has negative label
        negative_data_list = data_instances.filter(lambda k, v: int(v.label) != 1)

        # split positive data into train and test data
        train_positive_data_list = positive_data_list.sample(self.train_ratio, self.random_num)
        test_positive_data_list = positive_data_list.subtractByKey(train_positive_data_list)

        # split negative data into train and test data
        train_negative_data_list = negative_data_list.sample(self.train_ratio, self.random_num)
        test_negative_data_list = negative_data_list.subtractByKey(train_negative_data_list)

        train_data = train_positive_data_list.union(train_negative_data_list)
        test_data = test_positive_data_list.union(test_negative_data_list)

        promoter_train_data_with_header = self.set_header(train_data)
        promoter_test_data_with_header = self.set_header(test_data)

        return promoter_train_data_with_header, promoter_test_data_with_header

    def vert_segment_promoter(self, data_instances):
        """

        cut the data in vertical task when the role is promoter

        Parameters
        ----------
        data_instances: data list

        Returns
        -------
        promoter_train_data_with_header: data list
        promoter_test_data_with_header: data list

        """

        # determine if there is a label in dataset
        data_instances_take = data_instances.take()
        if data_instances_take[0][1].label is None:
            raise HasNoLabelError()

        # collect data that has positive label
        positive_data_list = data_instances.filter(lambda k, v: int(v.label) == 1)
        # collect data that has negative label
        negative_data_list = data_instances.filter(lambda k, v: int(v.label) != 1)

        # split positive data into train and test data
        train_positive_data_list = positive_data_list.sample(self.train_ratio, self.random_num)
        test_positive_data_list = positive_data_list.subtractByKey(train_positive_data_list)

        # split negative data into train and test data
        train_negative_data_list = negative_data_list.sample(self.train_ratio, self.random_num)
        test_negative_data_list = negative_data_list.subtractByKey(train_negative_data_list)

        promoter_train_data = train_positive_data_list.union(train_negative_data_list)
        promoter_test_data = test_positive_data_list.union(test_negative_data_list)

        promoter_test_data_ids = promoter_test_data.map(lambda k, v: (k, 1))

        # encrypt ID with dh =============================================== start
        # get provider_public_keys from providers
        provider_public_keys = self.transfer_variable.provider_key.get(-1)
        LOGGER.info("Get provider_public_keys:{} from Provider".format(provider_public_keys))
        # get p ,g from provider
        self.promoter_g = [int(provider_public_key["provider_g"]) for provider_public_key in provider_public_keys]
        self.promoter_p = [int(provider_public_key["provider_p"]) for provider_public_key in provider_public_keys]
        self.provider_r = [int(public_key["provider_encrypt_r"]) for public_key in provider_public_keys]
        # generate promoter_r list
        self.promoter_r = [random.SystemRandom().getrandbits(128) for i in range(len(provider_public_keys))]
        # generate multi promoter_r
        promoter_encrypt_r = [DiffieHellman.encrypt(self.promoter_g[i], self.promoter_r[i], self.promoter_p[i])
                              for i in range(len(self.promoter_g))]
        # send promoter_public_key to all providers
        for i, promoter_r in enumerate(promoter_encrypt_r):
            promoter_public_key = {"promoter_encrypt_r": promoter_r}
            self.transfer_variable.promoter_key.remote(promoter_public_key, role=consts.PROVIDER, idx=i)
            LOGGER.info("Remote promoter_key to Provider {}".format(i))
        self.promoter_key = [DiffieHellman.decrypt(self.provider_r[i], self.promoter_r[i], self.promoter_p[i])
                             for i in range(len(self.promoter_g))]
        # encrypt segment_Id with promoter_key
        for i in range(len(self.promoter_key)):
            encrypt_segment_ids = promoter_test_data_ids.map(
                lambda k, v: (self.encrypt_id_process(k, self.promoter_key[i]), 1)
                , need_send=True)
            self.transfer_variable.segment_ids.remote(encrypt_segment_ids, role=consts.PROVIDER, idx=i)
            LOGGER.info("Remote encrypt_segment_ids to Provider {}".format(i))
        # encrypt ID with dh ===============================================  end

        promoter_train_data_with_header = self.set_header(promoter_train_data)
        promoter_test_data_with_header = self.set_header(promoter_test_data)

        return promoter_train_data_with_header, promoter_test_data_with_header

    def vert_segment_provider(self, data_instances):

        """

        cut the data in vertical task when the role is provider

        Parameters
        ----------
        data_instances: data list

        Returns
        -------
        provider_train_data_with_header: data list
        provider_test_data_with_header: data list

        """

        LOGGER.info('start DH encrypt')
        # generate p, g, where p is a prime number, g is a positive number
        self.provider_p, self.provider_g = self.get_dh_key()
        LOGGER.info("Get dh key!")
        provider_encrypt_r = DiffieHellman.encrypt(self.provider_g, self.provider_r, self.provider_p)
        provider_public_key = {"provider_p": self.provider_p, "provider_g": self.provider_g,
                               "provider_encrypt_r": provider_encrypt_r}
        # send provider public key
        self.transfer_variable.provider_key.remote(provider_public_key, role=consts.PROMOTER, idx=0)
        LOGGER.info("Remote public key to Promoter.")
        promoter_public_key = self.transfer_variable.promoter_key.get(idx=0)
        LOGGER.info("get promoter_public_key from Promoter.")
        self.promoter_r = int(promoter_public_key['promoter_encrypt_r'])
        self.provider_key = DiffieHellman.decrypt(self.promoter_r, self.provider_r, self.provider_p)
        # get encrypt_segment_ids like (k,1) from promoter
        encrypt_segment_ids = self.transfer_variable.segment_ids.get(idx=0)
        LOGGER.info("get encrypt_segment_ids from Promoter.")
        # get data_ids from DSource
        provider_encrypt_segment_ids = data_instances.map(
            lambda k, v: (self.encrypt_id_process(k, self.provider_key), k))
        # get id when provider_encrypt_segment_ids match encrypt_segment_ids
        test_segment_ids_join = provider_encrypt_segment_ids.join(encrypt_segment_ids, lambda v1, v2: (v1, v2))
        # test_segment_ids_join -> [(('105c50471c307c5f4dbe48c2c9ddae953227e70eb782ad5a710a79de34637625', 1), ('565', 1))]
        test_segment_ids = test_segment_ids_join.map(lambda k, v: (v[0], 1))
        provider_train_data = data_instances.subtractByKey(test_segment_ids)
        provider_test_data = data_instances.join(test_segment_ids, lambda v1, v2: v1)

        provider_train_data_with_header = self.set_header(provider_train_data)
        provider_test_data_with_header = self.set_header(provider_test_data)

        return provider_train_data_with_header, provider_test_data_with_header

    def vert_segment(self, data_instances):
        if self.role == consts.PROMOTER:
            return self.vert_segment_promoter(data_instances)
        else:
            return self.vert_segment_provider(data_instances)

    def set_header(self, data_instances):
        if isinstance(self.feature_columns, str):
            data_instances.schema["header"] = self.feature_columns.split(self.delimitor)
        else:
            data_instances.schema["header"] = self.feature_columns
        data_instances.schema["label_name"] = self.label
        data_instances.schema["sid_name"] = self.sid_name
        return data_instances

    def base_fit(self, data_instances):
        final_input_data = self.filter_features(data_instances)
        return self.vert_segment(final_input_data) if self.is_vert() else self.horz_segment(final_input_data)

    @staticmethod
    def get_dh_key(dh_bit=2048):
        return DiffieHellman.key_pair(dh_bit)

    def encrypt_id_process(self, data_id, key):
        encrypt_id = self.hash(gmpy2.mpz(int(self.hash(data_id), 16) * key))
        return encrypt_id

    @staticmethod
    def hash(value):
        return hashlib.sha256(bytes(str(value), encoding='utf-8')).hexdigest()

    @staticmethod
    def to_instance(features, label=None):
        return Instance(features=np.array(features), label=label)

    def save_metric_data(self, train_data=None, eval_data=None):
        """

        Parameters
        ----------
        train_data: train data
        eval_data: eval data

        Returns
        -------

        """
        metric_data = {
            # boolean, whether with label
            'with_label': self.with_label,
            # int, train count
            'train_count': 0,
            # int, positive label count in train data
            'train_y_positive_example_count': 0,
            # float, positive label ratio in train data
            'train_y_positive_example_ratio': 0,
            # int, eval count
            'eval_count': 0,
            # int, positive label count in eval data
            'eval_y_positive_example_count': 0,
            # float, positive label ratio in eval data
            'eval_y_positive_example_ratio': 0
        }

        try:
            if train_data is not None:
                metric_data['train_count'] = train_data.count()
                if self.with_label:
                    metric_data['train_y_positive_example_count'] = train_data.filter(
                        lambda k, v: int(v.label) == 1).count()
                if metric_data['train_count'] > 0:
                    metric_data['train_y_positive_example_ratio'] = round(
                        metric_data['train_y_positive_example_count'] / metric_data['train_count'], 4)

            if eval_data is not None:
                metric_data['eval_count'] = eval_data.count()
                if self.with_label:
                    metric_data['eval_y_positive_example_count'] = eval_data.filter(
                        lambda k, v: int(v.label) == 1).count()
                if metric_data['eval_count'] > 0:
                    metric_data['eval_y_positive_example_ratio'] = round(
                        metric_data['eval_y_positive_example_count'] / metric_data['eval_count'], 4)

            metric_name = 'segment'
            metric_namespace = 'train_eval'
            metric_data_result = [("with_label", metric_data['with_label']),
                                  ("train_count", metric_data['train_count']),
                                  ("train_y_positive_example_count", metric_data['train_y_positive_example_count']),
                                  ("train_y_positive_example_ratio", metric_data['train_y_positive_example_ratio']),
                                  ("eval_count", metric_data['eval_count']),
                                  ("eval_y_positive_example_count", metric_data['eval_y_positive_example_count']),
                                  ("eval_y_positive_example_ratio", metric_data['eval_y_positive_example_ratio'])]

            print(f'segment metric_data: {metric_data_result}')
            self.tracker.saveMetricData(metric_name, metric_namespace, None, metric_data_result)
        except Exception as e:
            LOGGER.error("segment method save_metric_data call error: ", e)
