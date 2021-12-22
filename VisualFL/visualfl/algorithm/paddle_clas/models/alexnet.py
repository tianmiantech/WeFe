#copyright (c) 2019 PaddlePaddle Authors. All Rights Reserve.
#
#Licensed under the Apache License, Version 2.0 (the "License");
#you may not use this file except in compliance with the License.
#You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
#Unless required by applicable law or agreed to in writing, software
#distributed under the License is distributed on an "AS IS" BASIS,
#WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#See the License for the specific language governing permissions and
#limitations under the License.

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import math

import paddle
import paddle.fluid as fluid

__all__ = ['AlexNet']


class AlexNet():
    def __init__(self):
        pass

    def net(self, input, class_dim=1000):

        conv_pool_1 = fluid.nets.simple_img_conv_pool(
            input=input,
            num_filters=96,
            filter_size=11,
            conv_stride=4,
            conv_padding=5,
            pool_size=2,
            pool_stride=2,
            act="relu",
        )
        conv_pool_2 = fluid.nets.simple_img_conv_pool(
            input=conv_pool_1,
            num_filters=256,
            filter_size=5,
            conv_stride=1,
            conv_padding=2,
            pool_size=2,
            pool_stride=2,
            act="relu",
        )
        conv_pool_3 = fluid.nets.simple_img_conv_pool(
            input=conv_pool_2,
            num_filters=384,
            filter_size=3,
            conv_stride=1,
            conv_padding=1,
            pool_size=1,
            pool_stride=1,
            act="relu",
        )
        conv_pool_4 = fluid.nets.simple_img_conv_pool(
            input=conv_pool_3,
            num_filters=384,
            filter_size=3,
            conv_stride=1,
            conv_padding=1,
            pool_size=1,
            pool_stride=1,
            act="relu",
        )
        conv_pool_5 = fluid.nets.simple_img_conv_pool(
            input=conv_pool_4,
            num_filters=256,
            filter_size=3,
            conv_stride=1,
            conv_padding=1,
            pool_size=2,
            pool_stride=2,
            act="relu",
        )
        fc1 = fluid.layers.fc(
            input=conv_pool_5, size=4096, act="relu"
        )
        fc1 = fluid.layers.dropout(fc1, 0.5)
        fc2 = fluid.layers.fc(
            input=fc1, size=4096, act="relu"
        )
        fc2 = fluid.layers.dropout(fc2, 0.5)
        out = fluid.layers.fc(
            input=fc2, size=class_dim
        )

        return out
