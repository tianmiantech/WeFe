from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import math

import paddle
import paddle.fluid as fluid

__all__ = ['LeNet']


class LeNet():
    def __init__(self):
        pass

    def net(self,input, class_dim=1000):
        conv_pool_1 = fluid.nets.simple_img_conv_pool(
            input=input,
            num_filters=6,
            filter_size=5,
            pool_size=2,
            pool_stride=2,
            act="sigmoid",
        )
        conv_pool_2 = fluid.nets.simple_img_conv_pool(
            input=conv_pool_1,
            num_filters=16,
            filter_size=5,
            pool_size=2,
            pool_stride=2,
            act="sigmoid",
        )
        conv_pool_3 = fluid.nets.simple_img_conv_pool(
            input=conv_pool_2,
            num_filters=120,
            filter_size=4,
            pool_size=1,
            pool_stride=1,
            act="sigmoid",
        )
        fc1 = fluid.layers.fc(
            input=conv_pool_3, size=64, act="sigmoid"
        )
        out = fluid.layers.fc(
            input=fc1, size=class_dim
        )

        return out