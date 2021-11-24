# Copyright 2021 Tianmian Tech. All Rights Reserved.
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


import copy
import json
import multiprocessing
import multiprocessing.pool
import pickle
import time
from types import SimpleNamespace

from common.python.common.consts import TaskResultDataType
from common.python.utils import log_utils
from common.python.utils.store_type import DBTypes
from kernel.examples.handler.component.component_base import Component
from kernel.examples.handler.component.dataio import DataIO
from kernel.examples.handler.config import Backend, WorkMode, Role
from kernel.examples.handler.interface import Data
from kernel.examples.handler.interface import Model
from kernel.examples.handler.task_controller import TaskController
from kernel.examples.handler.utils import tools
from kernel.examples.handler.utils.tools import Upload
from kernel.examples.handler.utils.tools import remove_wefe_process
from kernel.tracker.tracking import Tracking

LOGGER = log_utils.get_logger()


class NoDaemonProcess(multiprocessing.Process):
    # make 'daemon' attribute always return False
    @property
    def daemon(self):
        return False

    @daemon.setter
    def daemon(self, val):
        pass


class NoDaemonProcessPool(multiprocessing.pool.Pool):

    def Process(self, *args, **kwds):
        proc = super(NoDaemonProcessPool, self).Process(*args, **kwds)
        proc.__class__ = NoDaemonProcess

        return proc


class Handler(object):
    def __init__(self, **kwargs):
        if "job_id" in kwargs:
            self.job_id = kwargs["job_id"]
        if "work_mode" in kwargs:
            self.work_mode = kwargs["work_mode"]
        if "backend" in kwargs:
            self.backend = kwargs["backend"]
        if "db_type" in kwargs:
            self.db_type = kwargs["db_type"]
        if 'fl_type' in kwargs:
            self.fl_type = kwargs["fl_type"]

        self.project_id = '001'
        self.model_id = "examples_0001"
        self.model_version = "1"
        self.db_type = DBTypes.LMDB
        self._create_time = time.asctime(time.localtime(time.time()))
        self._initiator = None
        self._roles = {}
        self._members = []
        self._components = {}
        self._components_input = {}
        self._train_dsl = {}
        self._train_conf = {}
        self._upload_conf = []
        self._cur_state = None

    def set_initiator(self, role, member_id):
        self._initiator = SimpleNamespace(role=role, member_id=member_id)

        return self

    def get_train_dsl(self):
        return copy.deepcopy(self._train_dsl)

    def get_train_conf(self):
        return copy.deepcopy(self._train_conf)

    def get_upload_conf(self):
        return copy.deepcopy(self._upload_conf)

    #
    def _get_initiator_conf(self):
        if self._initiator is None:
            raise ValueError("Please set initiator of Handler")

        initiator_conf = {"role": self._initiator.role,
                          "member_id": self._initiator.member_id}

        return initiator_conf

    def set_roles(self, promoter=None, provider=None, arbiter=None, **kwargs):
        local_parameters = locals()
        support_roles = Role.support_roles()
        self._members = []
        for role, member_id in list(local_parameters.items()):
            if role == "self":
                continue

            if not local_parameters.get(role):
                continue

            if role not in support_roles:
                continue
                # raise ValueError("Current role not support {}, support role list {}".format(role, support_roles))
            member_id = local_parameters.get(role)
            member = {'member_role': role}
            self._roles[role] = []
            if isinstance(member_id, int):
                self._roles[role].append(member_id)
                member['member_id'] = member_id
                self._members.append(member)
            elif isinstance(member_id, list):
                self._roles[role].extend(member_id)
                for id in member_id:
                    member['member_id'] = id
                    self._members.append({'member_role': role, 'member_id': id})
            else:
                raise ValueError("role: {}'s member_id should be an integer or a list of integer".format(role))

        return self

    def _get_role_conf(self):
        return self._roles

    def _get_member_index(self, role, member_id):
        if role not in self._roles:
            raise ValueError("role {} does not setting".format(role))

        if member_id not in self._roles[role]:
            raise ValueError("role {} does not init setting with the member_id {}".format(role, member_id))

        return self._roles[role].index(member_id)

    def add_component(self, component, data=None, model=None, output_data_type=None):
        if not isinstance(component, Component):
            raise ValueError(
                "To add a component to handler, component {} should be a Component object".format(component))

        if component.name in self._components:
            raise Warning("component {} is added before".format(component.name))

        self._components[component.name] = component
        if output_data_type:
            self._components[component.name].output.data_output = output_data_type

        if data is not None:
            if not isinstance(data, Data):
                raise ValueError("data input of component {} should be passed by data object".format(component.name))

            attrs_dict = vars(data)
            self._components_input[component.name] = {"data": {}}
            for attr, val in attrs_dict.items():
                # if not attr.endswith("data_set"):
                #     continue

                if val is None:
                    continue

                data_key = attr.strip("_")

                if isinstance(val, list):
                    self._components_input[component.name]["data"][data_key] = val
                else:
                    self._components_input[component.name]["data"][data_key] = [val]

        if model is not None:
            if not isinstance(model, Model):
                raise ValueError("model input of component {} should be passed by model object".format(component.name))

            attrs_dict = vars(model)
            for attr, val in attrs_dict.items():
                if not attr.endswith("model"):
                    continue

                if val is None:
                    continue

                if isinstance(val, list):
                    self._components_input[component.name][attr.strip("_")] = val
                else:
                    self._components_input[component.name][attr.strip("_")] = [val]
        return self

    def add_upload_data(self, file, table_name, namespace, head=1, partition=16,
                        id_delimiter=",", backend=Backend.LOCAL, work_mode=WorkMode.STANDALONE):
        data_conf = {"file": file,
                     "table_name": table_name,
                     "namespace": namespace,
                     "head": head,
                     "partition": partition,
                     "id_delimiter": id_delimiter,
                     "backend": backend,
                     "work_mode": work_mode}
        self._upload_conf.append(data_conf)

    def get_metric_summary(self, name="evaluation_0", component_name=None, job_type="train"):
        role = self._initiator.role
        member_id = self._initiator.member_id

        tracker = Tracking(project_id=self.project_id, job_id=self.job_id, role=role, member_id=member_id,
                           model_id=self.model_id, model_version=self.model_version, component_name=name)

        result_type = tracker._get_task_result_type(TaskResultDataType.METRIC, job_type)
        metric_task_result = tracker.get_task_result(result_type)
        result = json.loads(metric_task_result.result)[f"{job_type}_{component_name}"]['data']
        metric_summary = {}
        for metric_name, summary in result.items():
            metric_summary[metric_name] = summary['value']

        return metric_summary

    def _construct_train_dsl(self):
        self._train_dsl["components"] = {}
        for name, component in self._components.items():
            component_dsl = {"module": component.module}
            component_dsl['param_name'] = component.param_name
            if name in self._components_input:
                component_dsl["input"] = self._components_input[name]

            if hasattr(component, "output"):
                component_dsl["output"] = {}
                if hasattr(component.output, "data_output"):
                    component_dsl["output"]["data"] = component.output.data_output

                if hasattr(component.output, "model"):
                    component_dsl["output"]["model"] = component.output.model_output

            self._train_dsl["components"][name] = component_dsl

        if not self._train_dsl:
            raise ValueError("there are no components to train")

        LOGGER.debug(f"train_dsl: {self._train_dsl}")

    def _construct_train_conf(self):
        # self._train_conf["job_initiator"] = self._get_initiator_conf()
        self._train_conf["role"] = self._roles
        self._train_conf["job"] = {}
        self._train_conf["job"]['env'] = {'name': 'test', 'db_type': self.db_type, 'work_mode': self.work_mode,
                                          "backend": self.backend}
        for name, component in self._components.items():
            param_conf = component.get_config(roles=self._roles)
            if "common" in param_conf:
                common_param_conf = param_conf["common"]
                if "component_parameters" not in self._train_conf:
                    self._train_conf["component_parameters"] = {}
                if "common" not in self._train_conf["component_parameters"]:
                    self._train_conf["component_parameters"]["common"] = {}

                self._train_conf["component_parameters"]["common"].update(common_param_conf)

            if "role" in param_conf:
                role_param_conf = param_conf["role"]
                if "component_parameters" not in self._train_conf:
                    self._train_conf["component_parameters"] = {}
                if "role" not in self._train_conf["component_parameters"]:
                    self._train_conf["component_parameters"]["role"] = {}
                self._train_conf["component_parameters"]["role"] = tools.merge_dict(role_param_conf,
                                                                                    self._train_conf[
                                                                                        "component_parameters"]["role"])

        # LOGGER.debug(f"self._train_conf: \n {json.dumps(self._train_conf, indent=4, ensure_ascii=False)}")
        return self._train_conf

    def _construct_upload_conf(self, data_conf, backend, work_mode):
        upload_conf = copy.deepcopy(data_conf)
        upload_conf["backend"] = backend
        upload_conf["work_mode"] = work_mode
        return upload_conf

    def get_train_job_id(self):
        return self._train_job_id

    def _set_state(self, state):
        self._cur_state = state

    def compile(self):
        self._construct_train_dsl()
        self._train_conf = self._construct_train_conf()
        return self

    def fit(self):
        for component_name, component in self._train_dsl['components'].items():
            pool = multiprocessing.pool.Pool(10)
            for role, members in self._train_conf['role'].items():
                for member_id in members:
                    task_config = {}
                    # task_config['project'] = {}
                    # task_config['job_args'] = {}
                    task_config['job'] = {}
                    task_config['task'] = {}
                    task_config['job']['project'] = {}
                    task_config['job']['project']['project_id'] = self.project_id
                    task_config['job']['env'] = self._train_conf['job']['env']
                    task_config['job']['members'] = self._members
                    task_config['task']['members'] = self._members
                    task_config['module'] = component['module']
                    task_config['job']['federated_learning_type'] = self.fl_type
                    # param_name = component['param_name']
                    # task_config['role'] = self._train_conf['role']
                    # task_config['job_parameters'] = self._train_conf['job_parameters']
                    component_role = self._train_conf['component_parameters']['role']
                    component_common = self._train_conf['component_parameters']['common']
                    if component_name in component_common:
                        task_config['params'] = component_common[component_name]
                    else:
                        if role not in component_role:
                            continue
                        task_config['params'] = component_role[role][component_name]

                    if 'input' not in component:
                        task_config['params']['namespace'] = task_config['params']['table']['namespace']
                        task_config['params']['name'] = task_config['params']['table']['name']
                        task_config['input'] = {"data": {}}
                    else:
                        task_config['input'] = component['input']
                    task_config['output'] = component['output']
                    LOGGER.debug(f"in fit, task_config is: \n {json.dumps(task_config)}")
                    pool.apply_async(TaskController.run_task,
                                     (self.backend, self.job_id, role, str(member_id), component_name, task_config,))
            pool.close()
            pool.join()
            print(f"{component_name} done!!!")

            if self.db_type == DBTypes.LMDB:
                remove_wefe_process()

        # pool = multiprocessing.pool.Pool(3)
        # for role, members in self._train_conf['role'].items():
        #     for member_id in members:
        #         task_configs = {}
        #         for component_name, component in self._train_dsl['components'].items():
        #             task_config = {}
        #             task_config['project'] = {}
        #             task_config['job_args'] = {}
        #             task_config['project']['project_id'] = self.project_id
        #             task_config['module'] = component['module']
        #             param_name = component['param_name']
        #             task_config['role'] = self._train_conf['role']
        #             task_config['job_parameters'] = self._train_conf['job_parameters']
        #             component_role = {}
        #             if role in self._train_conf['component_parameters']['role']:
        #                 component_role = self._train_conf['component_parameters']['role'][role]
        #             component_common = self._train_conf['component_parameters']['common']
        #             task_config[param_name] = {}
        #             if component_name in component_role:
        #                 task_config[param_name] = component_role[component_name]
        #
        #             else:
        #                 task_config[param_name] = component_common[component_name]
        #             if 'input' not in component:
        #                 namespace = task_config[param_name]['table']['namespace']
        #                 name = task_config[param_name]['table']['name']
        #                 task_config['job_args'] = {"data": {"data": {"name":name, "namespace":namespace}}}
        #                 task_config['input'] = {"data": {"data": ["args.data"]}}
        #             else:
        #                 task_config['input'] = component['input']
        #             task_config['output'] = component['output']
        #             LOGGER.debug(f"in fit, task_config is: \n {json.dumps(task_config)}")
        #             task_configs[component_name] = task_config
        #
        #         pool.apply_async(TaskController.run_job, (self.job_id,role,str(member_id),task_configs,))
        #         # TaskController.run_job(self.job_id,role,str(member_id),task_configs)
        #
        # pool.close()
        # pool.join()
        # print("all components done!")

    def upload(self, backend=Backend.LOCAL, work_mode=WorkMode.STANDALONE, db_type=DBTypes.LMDB):
        for data_conf in self._upload_conf:
            upload_conf = self._construct_upload_conf(data_conf, backend, work_mode)
            LOGGER.debug(f"upload_conf is {json.dumps(upload_conf)}")
            table_name = upload_conf['table_name']
            namespace = upload_conf['namespace']
            partition = upload_conf['partition']
            head = upload_conf['head'] == 1
            file = upload_conf['file']
            upload = Upload(backend, work_mode, db_type)
            upload.prevent_repeat_upload(table_name, namespace, partition)
            data_table_count = upload.save_data_table(file, table_name, namespace, partition, head)

            LOGGER.debug(f"upload_data_count is {data_table_count}")

    def dump(self, file_path=None):
        pkl = pickle.dumps(self)

        if file_path is not None:
            with open(file_path, "wb") as fout:
                fout.write(pkl)

        return pkl

    @classmethod
    def load(cls, handler_bytes):
        return pickle.loads(handler_bytes)

    @classmethod
    def load_model_from_file(cls, file_path):
        with open(file_path, "rb") as fin:
            return pickle.loads(fin.read())

    def get_component_input_msg(self):
        need_input = {}
        for cpn_name, config in self._predict_dsl["components"].items():
            if "input" not in config:
                continue

            if "data" not in config["input"]:
                continue

            data_config = config["input"]["data"]
            for data_type, dataset_list in data_config.items():
                for data_set in dataset_list:
                    input_cpn = data_set.split(".", -1)[0]
                    input_inst = self._components[input_cpn]
                    if isinstance(input_inst, DataIO):
                        if cpn_name not in need_input:
                            need_input[cpn_name] = {}

                        need_input[cpn_name][data_type] = []
                        need_input[cpn_name][data_type].append(input_cpn)

        return need_input

    def get_input_reader_placeholder(self):
        input_info = self.get_component_input_msg()
        input_placeholder = set()
        for cpn_name, data_dict in input_info.items():
            for data_type, dataset_list in data_dict.items():
                for dataset in dataset_list:
                    input_placeholder.add(dataset)

        return input_placeholder

    def __getattr__(self, attr):
        if attr in self._components:
            return self._components[attr]

        return self.__getattribute__(attr)

    def __getitem__(self, item):
        if item not in self._components:
            raise ValueError("Pipeline does not has component }{}".format(item))

        return self._components[item]

    def __getstate__(self):
        return vars(self)

    def __setstate__(self, state):
        vars(self).update(state)
