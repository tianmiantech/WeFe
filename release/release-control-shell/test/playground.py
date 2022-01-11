import os
import shutil
import urllib.request

dir = os.path.join(
    "/Users/zane/data/wefe_file_upload_dir",
    "download"
)

if not os.path.exists(dir):
    os.makedirs(dir)

full_path = os.path.join(
    dir,
    "test.zip"
)

url = "https://www.google.com.hk/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png"
urllib.request.urlretrieve(url, full_path)