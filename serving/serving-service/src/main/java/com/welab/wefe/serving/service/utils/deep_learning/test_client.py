import sys
import numpy as np
from paddle_serving_client import Client
from paddle_serving_app.reader import *
import cv2
preprocess = Sequential([
    File2Image(), BGR2RGB(), Resize(
        (608, 608), interpolation=cv2.INTER_LINEAR), Div(255.0), Transpose(
        (2, 0, 1))
])

postprocess = RCNNPostprocess(sys.argv[2], sys.argv[3], [608, 608])
client = Client()

client.load_client_config("serving_client/serving_client_conf.prototxt")
client.connect(['127.0.0.1:9393'])

im = preprocess(sys.argv[1])
fetch_map = client.predict(
    feed={
        "image": im,
        "im_size": np.array(list(im.shape[1:])),
    },
    fetch=["multiclass_nms_0.tmp_0"])
fetch_map["image"] = sys.argv[1]
postprocess(fetch_map)