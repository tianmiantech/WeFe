# Copyright 2021 The WeFe Authors. All Rights Reserved.
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



import importlib
import json

from common.python import federation
from common.python import session
from common.python.common.consts import ComponentName, DataSetType, TaskResultDataType
from common.python.utils import log_utils
from kernel.components.oot.param import OotParam
from kernel.model_base import ModelBase
from kernel.task_executor import TaskExecutor
from kernel.tracker.tracking import Tracking
from kernel.utils import consts

LOGGER = log_utils.get_logger()


class Oot(ModelBase):
    """
    Class description：OOT component
    """

    # RunTaskAction
    def __init__(self):
        super().__init__()
        self.role = consts.PROMOTER
        self.oot_data_output = None
        self.model_param = OotParam()

    def fit(self, data_instances):
        LOGGER.info("Start Oot")
        self.tracker.init_task_progress(100)
        # List of subcomponent names (i.e. list of subcomponents to run)
        sub_component_name_list = self.model_param.sub_component_name_list
        # Task parameter dictionary for sub component operation
        sub_component_task_config_dick = self.model_param.sub_component_task_config_dick
        if sub_component_name_list is None or len(sub_component_name_list) == 0:
            return

        sub_component_model_task_config = None
        sub_component_model_output_data = None
        # Task result output of evaluation sub component
        sub_component_evaluation_task_result = None

        # Previous sub component task configuration
        pre_sub_component_task_config = None
        # Previous subcomponent result output
        pre_sub_component_output_data = []
        progress_step = 100 // len(sub_component_name_list)
        for index, sub_component_name in enumerate(sub_component_name_list):
            sub_component_task_config = sub_component_task_config_dick[sub_component_name]
            need_run = sub_component_task_config['oot_params']['need_run']
            if need_run is None or need_run is False:
                continue
            sub_parameters = self.build_sub_component_parameters(sub_component_task_config)
            federation_session_id = self.build_federation_session_id(sub_component_task_config)
            # Recreate federated objects to prevent reuse of overrides
            federation.init(job_id=federation_session_id, runtime_conf=sub_parameters)
            # Recreate the result tracking object for the sub component
            tracker = self.build_tracking(sub_component_task_config)

            # Save the task configuration of the previous subcomponent
            if index > 0:
                pre_sub_component_task_config = sub_component_task_config_dick[sub_component_name_list[index - 1]]

            # Create run parameters (get the output of the previous component)
            sub_component_task_run_args = self.build_sub_component_task_run_args(sub_component_task_config,
                                                                                 pre_sub_component_task_config,
                                                                                 pre_sub_component_output_data)
            # Run subcomponents
            sub_component_output_data_tmp = self.sub_component_run(sub_parameters, sub_component_task_run_args, tracker)

            if not isinstance(sub_component_output_data_tmp, list):
                sub_component_output_data_tmp_list = [sub_component_output_data_tmp]
            else:
                sub_component_output_data_tmp_list = sub_component_output_data_tmp

            # the output of the current sub component as the input of the next sub component
            if self.is_need_save_sub_component_output_data(sub_component_task_config):
                pre_sub_component_output_data = sub_component_output_data_tmp_list

            module_name = sub_component_task_config['module']
            # Is it a model component
            is_model = sub_component_task_config['oot_params']['is_model']
            if is_model:
                # If it is a modeling component, record relevant information
                sub_component_model_task_config = sub_component_task_config
                sub_component_model_output_data = sub_component_output_data_tmp

            # If it is an evaluation component, you need to query its output KS, AUC and other information
            if module_name == ComponentName.EVALUATION:
                # Query the output of subcomponent evaluation
                data_type = TaskResultDataType.METRIC + '_predict_' + tracker.component_name + '_oot'
                sub_component_evaluation_task_result = tracker.get_task_result(data_type, self.tracker.task_id)

            self.tracker.add_task_progress(progress_step)

        # Calculate the output result of OOT component and save it in the task result table
        self.save_oot_task_result(sub_component_model_task_config,
                                  sub_component_model_output_data,
                                  sub_component_evaluation_task_result)

        return None

    def sub_component_run(self, sub_component_parameters=None, sub_component_task_run_args=None, tracker=None):
        """
        describe：Run the subcomponent according to the configuration parameters of the subcomponent and the output results of the previous subcomponent
        :param sub_component_parameters: Sub component parameter configuration
        :param pre_sub_component_out_put: Output of the previous subcomponent
        :return: The output of this subcomponent
        """
        if sub_component_parameters is None:
            raise ValueError("sub_parameters is None")

        run_sub_component_class_paths = sub_component_parameters.get('CodePath').replace('\\', '/')
        run_sub_component_class_paths = run_sub_component_class_paths.split('/')
        run_sub_component_class_package = '.'.join(run_sub_component_class_paths[:-2]) + '.' + \
                                          run_sub_component_class_paths[-2].replace('.py', '')

        run_sub_component_class_name = run_sub_component_class_paths[-1]

        run_sub_component_object = getattr(importlib.import_module(run_sub_component_class_package),
                                           run_sub_component_class_name)()

        run_sub_component_object.set_tracker(tracker=tracker)
        # Run subcomponents
        run_sub_component_object.run(sub_component_parameters, sub_component_task_run_args)

        return run_sub_component_object.output_data()

    def build_sub_component_parameters(self, sub_component_task_config=None):
        """
         describe：Build subcomponent parameter configuration
         :param sub_component_task_config: subcomponent parameter configuration
        """
        if sub_component_task_config is None:
            raise ValueError("sub_component_task_config is None")
        role = self.tracker.role
        member_id = self.tracker.member_id
        sub_module_name = sub_component_task_config['module']
        sub_component_name = sub_component_task_config['oot_params']['component_name']
        parameters = TaskExecutor.get_parameters(role, member_id, sub_module_name, sub_component_name,
                                                 sub_component_task_config)
        return parameters

    def build_sub_component_task_run_args(self, sub_component_task_config=None,
                                          pre_sub_component_task_config=None,
                                          pre_sub_comonent_output_data=[]):
        """
        describe：Build subcomponent runtime parameters
        :param sub_component_task_config:Sub component parameter configuration
        :pre_sub_comonent_output_data:Output of the most recent subcomponent
        :pre_sub_component_output_model:Model output of the previous subcomponent
        :return:
        """
        params = sub_component_task_config.get('params', {})
        module_name = sub_component_task_config['module']
        task_input_dsl = sub_component_task_config['input']
        project_id = self.tracker.project_id
        # job_id = self.tracker.job_id
        job_id = sub_component_task_config['oot_params']['job_id']
        role = self.tracker.role
        member_id = self.tracker.member_id
        task_id = sub_component_task_config['oot_params']['task_id']

        # Is it a modeling component
        is_model = sub_component_task_config['oot_params']['is_model']

        # Output results of front sub components
        pre_task_output_dsl = None
        if pre_sub_component_task_config is not None:
            pre_task_output_dsl = pre_sub_component_task_config['output']
        pre_output_data_mapping_dick = self.pre_sub_comonent_output_data_mapping(pre_task_output_dsl,
                                                                                 pre_sub_comonent_output_data)
        task_run_args = {}
        # input_dsl => {'data': {'': ['']}, 'model': {'': ['']}}
        for input_type, input_detail in task_input_dsl.items():
            if input_type == 'data':
                this_type_args = task_run_args[input_type] = task_run_args.get(input_type, {})
                if len(input_detail.keys()) == 0 and module_name == ComponentName.DATA_IO:
                    data_table = session.table(
                        namespace=params['namespace'],
                        name=params['name']
                    )
                    args_from_component = this_type_args["upload"] = this_type_args.get(
                        "upload", {})
                    args_from_component[DataSetType.NORMAL_DATA_SET] = data_table
                else:
                    for data_type, data_list in input_detail.items():
                        for index, data_key in enumerate(data_list):
                            # data_key_item = data_key.split('.')
                            search_component_name, search_data_name = data_key, data_type
                            # Get the output of the component. If it is a modeling component,
                            # get the first value of the previous component
                            if is_model is not None and is_model is True:
                                data_table = list(pre_output_data_mapping_dick.values())[0]
                            else:
                                data_table = pre_output_data_mapping_dick[data_type]
                            args_from_component = this_type_args[search_component_name] = this_type_args.get(
                                search_component_name, {})
                            args_from_component[data_type] = data_table

            elif input_type == "model":
                # input_detail = "binning": ["Binning_1623318506974663"]
                this_type_args = task_run_args[input_type] = task_run_args.get(input_type, {})
                for dsl_model_key in input_detail:
                    # search_component_name = "Binning_1623318506974663"
                    # search_model_name = "binning"
                    search_component_name, search_model_name = input_detail[dsl_model_key][0], dsl_model_key
                    models = Tracking(project_id=project_id, job_id=job_id, role=role, member_id=member_id,
                                      component_name=search_component_name, task_id=task_id,
                                      model_id=task_id, model_version=job_id).get_output_model(
                        model_name=search_model_name)
                    this_type_args[search_model_name] = [models]
                    # return {"model":{"binning":[models]}}
        return task_run_args

    def pre_sub_comonent_output_data_mapping(self, pre_task_output_dsl, pre_sub_comonent_output_data):
        """
        describe：The output configuration of the previous sub component is mapped to the output entity, which is used to assemble the next sub component run_ task
        :param pre_task_output_dsl: Output of the previous subcomponent
        :param sub_comonent_output_data: The output entity of the previous subcomponent
        :return:
        """
        pre_output_data_mapping_dick = {}
        if pre_task_output_dsl is not None:
            for index, value in enumerate(pre_sub_comonent_output_data):
                data_name = pre_task_output_dsl.get('data')[index] if pre_task_output_dsl.get('data') else 'component'
                pre_output_data_mapping_dick[data_name] = value
        return pre_output_data_mapping_dick

    def build_federation_session_id(self, sub_component_task_config):
        """
        describe：Build a gateway session id for federated interaction
        :param sub_component_task_config: Parameter configuration of sub components
        :return:
        """
        job_id = self.tracker.job_id
        task_type = sub_component_task_config['oot_params']['task_type']
        flow_node_id = sub_component_task_config['oot_params']['flow_node_id']
        oot_flow_node_id = self.model_param.flow_node_id
        module_name = sub_component_task_config['module']
        return f'{job_id}_{task_type}_{flow_node_id}_oot_{module_name}_{oot_flow_node_id}'

    def build_tracking(self, sub_component_task_config):
        """
        describe：Build tasks for operating MySQL_ Result table object
        :param sub_component_task_config: Task configuration of subcomponents
        :return:
        """
        project_id = self.tracker.project_id
        # job_id = self.tracker.job_id
        # Use sub component original job id, because some subcomponents will be based on the original job id to query
        # the original result
        job_id = sub_component_task_config['oot_params']['job_id']
        role = self.tracker.role
        member_id = self.tracker.member_id
        component_name = sub_component_task_config['oot_params']['component_name']
        module_name = sub_component_task_config['module']
        task_id = self.tracker.task_id
        return Tracking(project_id=project_id, job_id=job_id, role=role, member_id=member_id,
                        model_id=task_id, model_version=job_id,
                        component_name=component_name, module_name=module_name, task_id=task_id, oot=True)

    def is_need_save_sub_component_output_data(self, sub_component_task_config=None):
        """
        describe：need save the output of the subcomponent
        :param sub_component_task_config Task configuration of subcomponents
        :return
        """
        if sub_component_task_config is None:
            return False

        task_output_dsl = sub_component_task_config['output']
        if task_output_dsl:
            for key, value in task_output_dsl.items():
                if key in ['data', 'model']:
                    return True
        return False

    def save_oot_task_result(self, sub_component_model_task_config=None, sub_component_model_output_data=None,
                             sub_component_evaluation_task_result=None):
        """
        describe：Save the output of the OOT component
        :param sub_component_model_task_config: Task configuration of modeling subcomponent
         # (1, [1, 1, 0.8075654935352979, {'0': 0.19243450646470206, '1': 0.8075654935352979}, 'predict'])
        :param sub_component_model_output_data: Prediction results of modeling sub components
        :param sub_component_evaluation_task_result: Evaluate task results for subcomponents
        :return:
        """
        oot_task_result = {}

        # Total output of the model
        sub_component_model_static = {}
        # Task configuration of model
        sub_component_model_static['task_config'] = sub_component_model_task_config
        # Total quantity and positive example proportion of forecast data
        predict_metric_data = {
            'with_label': self.model_param.with_label,
            'total_count': 0,
            'y_positive_example_count': 0,
            'y_positive_example_ratio': 0
        }
        # Total quantity of forecast data
        predict_data_total_count = 0
        # Number of positive examples of forecast data
        predict_data_y_positive_example_count = 0
        if sub_component_model_output_data and self.model_param.with_label is True:
            predict_data_total_count = sub_component_model_output_data.count()
            predict_data_y_positive_example_count = sub_component_model_output_data.filter(
                lambda k, v: int(v[0]) == 1).count()

        if predict_data_total_count > 0 and self.model_param.with_label is True:
            predict_metric_data['total_count'] = predict_data_total_count
            predict_metric_data['y_positive_example_count'] = predict_data_y_positive_example_count
            predict_metric_data['y_positive_example_ratio'] = round(
                predict_data_y_positive_example_count / predict_data_total_count, 4)

        sub_component_model_static['predict_metric_data'] = predict_metric_data

        # Evaluation results
        evaluation_task_result = sub_component_evaluation_task_result.result if sub_component_evaluation_task_result else None
        oot_task_result['model'] = sub_component_model_static
        oot_task_result['evaluation'] = json.loads(evaluation_task_result) if evaluation_task_result else {}

        print(f'segment metric_data: {oot_task_result}')

        # Save to task result table
        self.tracker.save_task_result(oot_task_result, 'metric_predict', self.tracker.component_name)
