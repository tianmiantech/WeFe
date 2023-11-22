from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import math

import paddle
import paddle.fluid as fluid

__all__ = ['CNN']


class CNN():
    def __init__(self):
        pass

    def net(self,input, class_dim=1000):
        conv_pool_1 = fluid.nets.simple_img_conv_pool(
            input=input,
            num_filters=20,
            filter_size=5,
            pool_size=2,
            pool_stride=2,
            act="relu",
        )
        conv_pool_2 = fluid.nets.simple_img_conv_pool(
            input=conv_pool_1,
            num_filters=50,
            filter_size=5,
            pool_size=2,
            pool_stride=2,
            act="relu",
        )

        out = fluid.layers.fc(
            input=conv_pool_2, size=class_dim
        )

        return out