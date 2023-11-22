#   Copyright (c) 2019 PaddlePaddle Authors. All Rights Reserved.
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

from .fl_distribute_transpiler import FLDistributeTranspiler


class FLStrategyBase(object):
    """
    FLStrategyBase is federated learning algorithm container
    """

    def __init__(self):
        self._fed_avg = False
        self._dpsgd = False
        self._inner_step = 1
        pass

    def minimize(self, optimizer=None, losses=[]):
        """
        minmize can do minimization as paddle.fluid.Optimizer.minimize does
        this function can be overloaded so that for some FLStrategy, the
        program should be transpiled before minimize

        Args:
            optimizer(paddle.fluid.optimizer): the user defined optimizer
            losses(List(Variable)): list of loss variables in paddle.fluid
        """
        for loss in losses:
            optimizer.minimize(loss)

    def _build_trainer_program_for_job(
        self,
        trainer_id=0,
        program=None,
        ps_endpoints=[],
        trainers=0,
        sync_mode=True,
        startup_program=None,
        job=None,
    ):
        pass

    def _build_server_programs_for_job(
        self,
        program=None,
        ps_endpoints=[],
        trainers=0,
        sync_mode=True,
        startup_program=None,
        job=None,
    ):
        pass


class FedAvgStrategy(FLStrategyBase):
    """
    FedAvgStrategy: this is model averaging optimization proposed in
    H. Brendan McMahan, Eider Moore, Daniel Ramage, Blaise Aguera y Arcas. Federated Learning of Deep Networks using Model Averaging. 2017
    """

    def __init__(self):
        super(FedAvgStrategy, self).__init__()

    def minimize(self, optimizer=None, losses=[]):
        """
        minimize the first loss as in paddle.fluid
        """
        optimizer.minimize(losses[0])

    def _build_trainer_program_for_job(
        self,
        trainer_id=0,
        program=None,
        ps_endpoints=[],
        trainers=0,
        sync_mode=True,
        startup_program=None,
        job=None,
    ):
        transpiler = FLDistributeTranspiler()
        transpiler.transpile(
            trainer_id,
            program=program,
            pservers=",".join(ps_endpoints),
            trainers=trainers,
            sync_mode=sync_mode,
            startup_program=startup_program,
        )
        recv, main, send = transpiler.get_trainer_program()
        job._trainer_startup_programs.append(startup_program)
        job._trainer_main_programs.append(main)
        job._trainer_send_programs.append(send)
        job._trainer_recv_programs.append(recv)

    def _build_server_programs_for_job(
        self,
        program=None,
        ps_endpoints=[],
        trainers=0,
        sync_mode=True,
        startup_program=None,
        job=None,
    ):
        transpiler = FLDistributeTranspiler()
        trainer_id = 0
        transpiler.transpile(
            trainer_id,
            program=program,
            pservers=",".join(ps_endpoints),
            trainers=trainers,
            sync_mode=sync_mode,
            startup_program=startup_program,
        )
        job.set_server_endpoints(ps_endpoints)
        for endpoint in ps_endpoints:
            main_prog = transpiler.get_pserver_program(endpoint)
            startup_prog = transpiler.get_startup_program(endpoint, main_prog)
            job._server_startup_programs.append(startup_prog)
            job._server_main_programs.append(main_prog)
