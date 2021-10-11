import { reactive, getCurrentInstance, nextTick } from 'vue';

export default {
    props: {
        jobId:      String,
        flowId:     String,
        flowNodeId: String,
    },
    emits: ['confirmCheck'],
    mixin ({
        vData,
        methods,
        props,
        context,
    }) {
        const { appContext } = getCurrentInstance();
        const {
            $http,
            $message,
        } = appContext.config.globalProperties;
        const $data = reactive({
            total:      0,
            list:       [],
            features:   [],
            showDialog: false,
        });
        const $methods = {
            show ({ features }) {
                vData.list = features.filter(feature => {
                    if (feature.id) {
                        return feature;
                    }
                });
                vData.total = features.length;
                vData.features = features;
                vData.showDialog = true;
            },
            async filter (e) {
                const { code, data, message } = await $http.post({
                    url:  '/flow/job/task/select',
                    data: {
                        job_id:       props.jobId,
                        flow_id:      props.flowId,
                        flow_node_id: props.flowNodeId,
                        members:      vData.features,
                        ...vData.params,
                    },
                    btnState: {
                        target: e,
                    },
                });

                nextTick(() => {
                    if (code === 0) {
                        if (data.featureNum) {
                            vData.list = data.members.map(row => {
                                // add id mark
                                row.id = true;
                                return row;
                            });
                        } else {
                            vData.list = [];
                            $message.error('没有符合条件的特征');
                        }
                    } else {
                        message && $message.error(message);
                    }
                });
            },
            confirm () {
                vData.showDialog = false;
                context.emit('confirmCheck', { list: vData.list });
            },
        };

        // merge mixin
        vData = Object.assign($data, vData);
        methods = Object.assign($methods, methods);

        return {
            $data,
            $methods,
        };
    },
};
