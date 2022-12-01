import $http from '@src/http/http';

/**
 * 获取流程节点特征相关
 */
export const getFlowNodeFeature = ({
    jobId,flowId,flowNodeId,
}) => {
    return new Promise((resolve,reject) => {
        $http.post({
            url:  '/flow/job/task/feature',
            data: {
                job_id:       jobId,
                flow_id:      flowId,
                flow_node_id: flowNodeId,
            },
        }).then(res => {
            const { data } = res || {};

            resolve(data || {});
        });
    });
};

/**
 * 获取节点信息，已保存的参数
 */
export const getNodeDetail = ({ id, flowId }) => {
    return new Promise((resolve, reject) => {
        $http.post({
            url:  '/project/flow/node/detail',
            data: {
                nodeId:  id,
                flow_id: flowId,
            },
        }).then(res => {
            const { data } = res || {};
            const { params } = data || {};

            resolve(params || {});
        });
    });
};

/**
 * 检查当前节点所在流程分支是否存在纵向建模组件
 */
 export const checkExitVertModelComponet = ({ nodeId, flowId, modelNodeId, jobId }) => {
    return new Promise((resolve, reject) => {
        $http.post({
            url:  'project/flow/node/check_exist_vert_model_component',
            data: {
                flowId,
                nodeId,
                jobId,
                modelNodeId,
            },
        }).then(res => {
            const { data } = res || {};
            const { check_result } = data || {};

            resolve(check_result || false);
        });
    });
};

/**
 * 获取结果
 */
 export const getDataResult = ({ jobId,flowId, flowNodeId,type='' }) => {
    return new Promise((resolve, reject) => {
        $http.get({
            url:    '/flow/job/task/result',
            params: {
                jobId,
                flowId,
                flowNodeId,
                type,
            },
        }).then(res => {
            const { data = [] } = res || {};
            const { result = {} } = data[0] || {};

            resolve(result || {});
        });
    });
};

/**
 * 获取流程详情中的特征类型
 */

export const getFeatureType = ({ flow_id }) => {
    return new Promise((resolve, reject) => {
        $http.get({
                url:    '/project/flow/table_data_set/list',
                params: {
                    flow_id,
                },
            }).then(res => {
                const { code, data } = res;

                let { list } = data || {};

    
                if(code !== 0) {
                    list = [];
                    console.log('获取特征类型失败');
                }

                const obj = {};

                list.forEach(item => {
                    obj[item.data_set_id] = {};
        
                    (item.features || []).forEach(items => {
                        obj[item.data_set_id][items.name] = items.data_type;
                    });
                });
        
                // window.localStorage.setItem(`${window.api.baseUrl}_featureType`, JSON.stringify(list));
                resolve(obj);
            });

        
    });
};

/**
 * 获取某个数据集的特征类型
 */

export const getDataSetFeatureType = ({ projectId,memberId,dataSetId }) => {
    return new Promise((resolve, reject) => {
        $http.get({
                url:    '/project/table_data_set/feature/list',
                params: {
                    projectId,memberId,dataSetId,
                },
            }).then(res => {
                const { code, data } = res;

                let { list } = data || {};

    
                if(code !== 0) {
                    list = [];
                    console.log('获取特征类型失败');
                }
        
        
                const obj = {};
    
                list.forEach(item => {
                    obj[item.name] = item.data_type;
                });
                
                resolve(obj);
            });

        
    });
};

/**
 * 数据预览时调用获取特征类型
 * @param {string} data_set_id 
 */
export const getDataFeatureType = (data_set_id)=>{
    return new Promise((resolve, reject) => {
        $http.get({
                url:    '/table_data_set/column/list',
                params: {
                    data_set_id,
                },
            }).then(res => {
                const { code, data } = res;

                let { list } = data || {};

    
                if(code !== 0) {
                    list = [];
                    console.log('获取特征类型失败');
                }
    
                resolve(list);
            });

        
    });
}
