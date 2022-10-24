from kernel.components.binning.vertfeaturebinning.param import TransformParam
from kernel.base.params.base_param import BaseParam
from kernel.utils import consts


class VertFeaturePSIParam(BaseParam):
    def __init__(self, bin_num= 6, method=consts.BUCKET, bin_names=None, bin_indexes = -1,
                 compress_thres=consts.DEFAULT_COMPRESS_THRESHOLD, category_indexes = None, category_names = None,
                 adjustment_factor=0.5,transform_param=TransformParam(),head_size=consts.DEFAULT_HEAD_SIZE,
                 error=consts.DEFAULT_RELATIVE_ERROR):
        super(VertFeaturePSIParam, self).__init__()
        self.bin_num = bin_num
        self.method = method
        self.bin_names = bin_names
        self.bin_indexs = bin_indexes
        self.adjustment_factor = adjustment_factor
        self.category_indexes = category_indexes
        self.category_names = category_names
        self.transform_param = transform_param
        self.compress_thres = compress_thres
        self.head_size = head_size
        self.error = error



    def check(self):
        pass

