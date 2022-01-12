from dto.main_input import MainInput
from dto.service_action_info import ActionInfo
from service.java_service_action import JavaServiceAction
from util import object_util

service_map = {
    "wefe-board-website": JavaServiceAction,
    "wefe-board-service": JavaServiceAction,
    "wefe-gateway": JavaServiceAction,
    "wefe-flow": JavaServiceAction,
}


class Launcher:

    @staticmethod
    def run(config_json, wefe_dir):
        main_input: MainInput = object_util.json_to_model(config_json, MainInput)

        for item in main_input.services:
            service_action_info: ActionInfo = object_util.dict_to_model(ActionInfo(), **item)

            service_action = service_map.get(service_action_info.service)
            service_action(service_action_info, wefe_dir).run()
