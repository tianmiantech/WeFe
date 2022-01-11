import os
import shutil
import urllib.request

from service.base_action import BaseAction


def replace():
    source = "/Users/zane/data/wefe_file_upload_dir/dir1"
    target = "/Users/zane/data/wefe_file_upload_dir/dir2"

    copydirs(source, target)


def copydirs(source_dir, target_dir):
    # 如不存在目标目录则创建
    if not os.path.exists(target_dir):
        os.makedirs(target_dir)

    # 获取文件夹中文件和目录列表
    files = os.listdir(source_dir)
    for f in files:
        # 判断是否是文件夹
        if os.path.isdir(source_dir + '/' + f):
            # 递归调用本函数
            copydirs(source_dir + '/' + f, target_dir + '/' + f)
        else:
            # 替换文件
            shutil.copy(source_dir + '/' + f, target_dir + '/' + f)


def unzip():
    action = BaseAction()
    action.unzip(
        "/Users/zane/data/wefe_file_upload_dir/hello.zip",
        "/Users/zane/data/wefe_file_upload_dir/unzip"
    )


if __name__ == '__main__':
    replace()
