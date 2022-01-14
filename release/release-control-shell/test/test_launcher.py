from main import Launcher

with open('case1.json', 'r') as f:
    json_str = f.read()

Launcher().run(json_str, "/Users/zane/data/wefe")
