import os

path = "/Users/zane/Code/WeLab/Wefe/documents/数据库设计/board"
files = os.listdir(path)
files.sort()

for file in files:
    print(file)
    print("isfile:", os.path.isfile(path + "/" + file))
