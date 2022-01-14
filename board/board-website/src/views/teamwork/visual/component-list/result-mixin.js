/* node result mixins */

import {
    reactive,
    nextTick,
    getCurrentInstance,
    onBeforeMount,
    onBeforeUnmount,
} from 'vue';

export default () => {
    return {
        props: {
            autoReadResult: Boolean,
            flowId:         String,
            flowNodeId:     String,
            jobDetail:      Object,
            jobId:          String,
            myRole:         String,
            isCreator:      Boolean,
            currentObj:     Object,
        },
        mixin({ props, methods, vData, context }) {
            const { appContext } = getCurrentInstance();
            const { $http } = appContext.config.globalProperties;

            const $data = reactive({
                timer:            null,
                loading:          false,
                result:           null,
                commonResultData: {
                    task: null,
                },
                jobStatus: {
                    created:          '已创建',
                    wait_run:         '等待运行',
                    running:          '运行中',
                    stop:             '人为结束',
                    wait_stop:        '等待结束',
                    stop_on_running:  '人为关闭',
                    error_on_running: '程序异常关闭',
                    error:            '执行失败',
                    success:          '成功(正常结束)',
                },
                pollingOnJobRunning: false, // Polling the calling result interface when the task is not finished
                jobRunningStatus:    {
                    created:   true,
                    wait_run:  true,
                    running:   true,
                    wait_stop: true,
                },
            });
            const $methods = {
                async readData() {
                    if (vData.loading) return;
                    vData.loading = true;

                    let params = {
                        jobId:      props.jobId,
                        flowId:     props.flowId,
                        flowNodeId: props.flowNodeId,
                        ...vData.expandparams,
                    };

                    if (methods.initParams) {
                        params = Object.assign(params, methods.initParams());
                    }

                    const { code, data } = await $http.post({
                        url:  vData.api || '/flow/job/task/result',
                        data: params,
                    });

                    setTimeout(_ => {
                        vData.loading = false;
                    }, 300);

                    nextTick(() => {
                        if (code === 0) {
                            if (Array.isArray(data)) {
                                if (data[0].status) {
                                    vData.commonResultData.task = data[0];
                                }
                                methods.showResult(data);

                                if (vData.pollingOnJobRunning && vData.jobDetail && vData.jobRunningStatus[vData.jobDetail.status]) {
                                    clearTimeout(vData.timer);
                                    vData.timer = setTimeout(() => {
                                        methods.readData();
                                    }, 3000);
                                }
                            } else {
                                if (data.status) {
                                    vData.commonResultData.task = data;
                                }
                                methods.showResult(data);
                            }
                        }
                    });
                },
                showResult(data) {
                    if (data[0].result) {
                        vData.result = data[0].result;
                    }
                },
            };

            // merge mixin
            vData = Object.assign($data, vData);
            methods = Object.assign($methods, methods);

            onBeforeMount(_ => {
                if (props.autoReadResult) {
                    methods.readData();
                }
            });

            onBeforeUnmount(_ => {
                clearTimeout(vData.timer);
            });

            return {
                $data,
                $methods,
            };
        },
    };
};
