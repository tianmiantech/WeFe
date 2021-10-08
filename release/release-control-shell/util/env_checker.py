import sys

from util import config

print("in env_checker")
print(sys.argv)

print("加载配置文件...")
config.load(sys.argv[1])
port = config.get(config.Keys.DB_MYSQL_PORT)
print(port)
print("检查 IO 权限...")
print("创建文件夹成功")
print("删除文件夹成功")
print("创建文件成功")
print("删除文件成功")
