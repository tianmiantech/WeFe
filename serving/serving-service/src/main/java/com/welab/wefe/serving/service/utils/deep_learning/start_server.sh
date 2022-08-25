#!/bin/sh

nohup python3 -m paddle_serving_server.serve --model serving_server --port 9393 &