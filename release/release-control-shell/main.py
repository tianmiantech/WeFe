from dto.main_input import ActionConfig
from dto.service_action_info import ServiceActionInfo
from service.replace_program_file_action import JavaServiceAction
from util import object_util

service_map = {
    "wefe-board-website": JavaServiceAction,
    "wefe-board-service": JavaServiceAction,
    "wefe-gateway": JavaServiceAction,
    "wefe-flow": JavaServiceAction,
}


class Main:

    @staticmethod
    def run(config_file_path, action_info_str):
        """

        """
        action_info: ServiceActionInfo = object_util.json_to_model(action_info_str, ServiceActionInfo)

        for item in action_info.services:
            service_action_info: ServiceActionInfo = object_util.dict_to_model(ServiceActionInfo(), **item)

            service_action = service_map.get(service_action_info.service)
            service_action(service_action_info, wefe_dir).run()
