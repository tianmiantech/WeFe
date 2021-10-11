import {
    reactive,
    nextTick,
    getCurrentInstance,
} from 'vue';

export default {
    mixin ({
        vData,
        methods,
        props,
    }) {
        const { appContext } = getCurrentInstance();
        const { $http } = appContext.config.globalProperties;
        const $data = reactive({
            inited:  false,
            loading: false,
        });
        const $methods = {
            readData (model) {
                if (methods.formatter) {
                    methods.formatter(vData.originForm);
                } else {
                    vData.form = {
                        ...vData.originForm,
                    };
                }
                methods.getNodeDetail(model);
            },
            async getNodeDetail (model) {
                if (vData.loading) return;
                vData.loading = true;

                const { code, data } = await $http.get({
                    url:    '/project/flow/node/detail',
                    params: {
                        nodeId:  model.id,
                        flow_id: props.flowId,
                    },
                });

                nextTick(() => {
                    vData.loading = false;

                    if (code === 0) {
                        const { params } = data || {};

                        if (params) {
                            if (methods.formatter) {
                                methods.formatter(params);
                            } else {
                                vData.form = params;
                            }
                        }

                        vData.inited = true;
                    }
                });
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
