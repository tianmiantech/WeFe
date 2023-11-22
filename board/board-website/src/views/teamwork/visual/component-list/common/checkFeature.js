import { reactive, nextTick, getCurrentInstance } from 'vue';

export default () => {
    return {
        emits: ['revertCheck', 'columnCheckChange', 'showColumnList', 'hideColumnList', 'autoCheck', 'checkAll'],
        mixin ({
            vData,
            props,
            context,
            methods,
            CheckFeatureDialogRef,
        }) {
            const { appContext } = getCurrentInstance();
            const {
                $http,
                $message,
            } = appContext.config.globalProperties;
            const $data = reactive({
                loading:          false,
                inited:           false,
                featureSelectTab: [],
            });
            const $methods = {
                async readData (model) {
                    if (vData.loading) return;
                    vData.loading = true;

                    // get all features
                    const { code, data } = await $http.post({
                        url:  '/flow/job/task/feature',
                        data: {
                            job_id:       props.jobId,
                            flow_id:      props.flowId,
                            flow_node_id: model.id,
                        },
                    });

                    nextTick(() => {
                        vData.loading = false;
                        if (code === 0) {
                            // flatten a 2D array
                            vData.featureSelectTab = [];
                            vData.total_column_count = 0;
                            if (data.members && data.members.length) {
                                data.members.forEach(row => {
                                    // cache display list fields
                                    const $feature_list = row.features.map(feature => {
                                        return {
                                            name:   feature.name,
                                            count:  1,
                                            method: '',
                                            id:     '',
                                        };
                                    });

                                    vData.total_column_count += $feature_list.length;
                                    vData.featureSelectTab.push({
                                        member_id:          row.member_id,
                                        member_name:        row.member_name,
                                        member_role:        row.member_role,
                                        $checkedAll:        false,
                                        $indeterminate:     false,
                                        $checkedColumnsArr: [],
                                        $checkedColumns:    '',
                                        $feature_list,
                                    });
                                });
                                methods.getNodeDetail(model);
                            }
                        }
                    });
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
                        if (code === 0 && data && data.params && Object.keys(data.params).length) {
                            const { strategies, members } = data.params;

                            vData.feature_column_count = 0;
                            if (strategies && strategies.length) {
                                vData.selectList = strategies;
                                strategies.forEach((row) => {
                                    vData.feature_column_count += row.feature_column_count;
                                });
                            }
                            members.forEach((row) => {
                                const member = vData.featureSelectTab.find(item => item.member_id === row.member_id && item.member_role === row.member_role);

                                if (member) {
                                    row.features.forEach((x) => {
                                        const item = member.$feature_list.find((m) => m.name === x.name);

                                        if (item) {
                                            item.id = x.id;
                                            item.count = x.count;
                                            item.method = x.method;
                                            // for binning
                                            item.points = x.points || '';
                                        }
                                    });
                                }
                            });
                            vData.inited = true;
                        }
                    });
                },

                changeMethod (item) {
                    vData.featureSelectTab.forEach(member => {
                        member.$feature_list.forEach(row => {
                            if (row.id === item.id) {
                                row.method = item.method;
                            }
                        });
                    });
                },

                removeRow ({ id }, index) {
                    vData.selectList.splice(index, 1);
                    vData.featureSelectTab.forEach(member => {
                        member.$feature_list.forEach(row => {
                            if (row.id === id) {
                                row.method = '';
                                row.count = 1;
                                row.points = '';
                                row.id = '';
                            }
                        });
                    });
                },

                showColumnListDialog (item, index) {
                    vData.selectListIndex = index;
                    vData.columnListType = item.method;
                    vData.featureSelectTab.forEach(row => {
                        if (row.$checkedColumnsArr.length === 0) {
                            row.$feature_list.forEach(x => {
                                if (x.method) {
                                    row.$checkedColumnsArr.push(x.name);
                                }
                            });
                        }
                    });
                    CheckFeatureDialogRef.value.methods.show();
                    context.emit('showColumnList');
                },

                hideColumnList () {
                    props.featureSelectTab && props.featureSelectTab.forEach(row => {
                        row.$checkedAll = false;
                        row.$indeterminate = false;
                        row.$checkedColumns = '';
                    });
                    context.emit('hideColumnList');
                },

                autoCheck (item) {
                    vData.columnListLoading = true;

                    setTimeout(() => {
                        const ids = item.$checkedColumns.length ? item.$checkedColumns.split(/,|，/) : [];

                        ids.forEach(name => {
                            const row = item.$feature_list.find(column => column.name === name);

                            if (row && !row.method) {
                                row.method = vData.columnListType;
                                item.$checkedColumnsArr.push(name);
                            }
                        });

                        if (item.$checkedColumnsArr.length === item.$feature_list.length) {
                            item.$indeterminate = false;
                            item.$checkedAll = true;
                        } else if (item.$checkedAll) {
                            item.$indeterminate = true;
                            item.$checkedAll = false;
                        }
                        context.emit('autoCheck');
                        setTimeout(() => {
                            vData.columnListLoading = false;
                        });
                    }, 300);
                },

                checkAll (item) {
                    if(!item) return;
                    item.$indeterminate = false;
                    vData.columnListLoading = true;

                    setTimeout(() => {
                        item.$feature_list.forEach(column => {
                            const disabled = Boolean(!!column.method && props.selectListId && column.id && column.id !== props.selectListId);

                            if (!disabled) {
                                if (item.$checkedAll) {
                                    // remove pre-existing items first
                                    const lastIndex = item.$checkedColumnsArr.findIndex(x => x === column.name);

                                    if (~lastIndex) {
                                        item.$checkedColumnsArr.splice(lastIndex, 1);
                                    }
                                    // add again
                                    item.$checkedColumnsArr.push(column.name);
                                } else {
                                    // cancel check all
                                    const index = item.$checkedColumnsArr.findIndex(x => x === column.name);

                                    if (~index) {
                                        item.$checkedColumnsArr.splice(index, 1);
                                    }
                                }
                            }
                        });
                        context.emit('checkAll');
                        setTimeout(_ => {
                            vData.columnListLoading = false;
                        });
                    }, 300);
                },

                revertCheck (item) {
                    vData.columnListLoading = true;

                    setTimeout(() => {
                        if (item.$checkedColumnsArr.length === item.$feature_list.length) {
                            item.$indeterminate = false;
                            item.$checkedAll = false;
                        }

                        if (props.revertCheckEmit) {
                            // emit to parent component
                            context.emit('revertCheck', item);
                        } else {
                            const lastIds = [...item.$checkedColumnsArr];

                            // Remove last selected result
                            for (let i = 0; i < lastIds.length; i++) {
                                const name = lastIds[i];
                                const column = item.$feature_list.find(x => name === x.name);
                                const disabled = Boolean(!!column.method && props.selectListId && column.id && column.id !== props.selectListId);

                                if (!disabled) {
                                    const index = item.$checkedColumnsArr.findIndex(x => x === column.name);

                                    item.$checkedColumnsArr.splice(index, 1);
                                }
                            }

                            // Add selected results
                            item.$feature_list.forEach(column => {
                                const name = lastIds.find(x => x === column.name);
                                const disabled = Boolean(!!column.method && props.selectListId && column.id && column.id !== props.selectListId);

                                if (!name && !disabled) {
                                    item.$checkedColumnsArr.push(column.name);
                                }
                            });
                        }

                        if (item.$checkedColumnsArr.length === item.$feature_list.length) {
                            item.$indeterminate = false;
                            item.$checkedAll = true;
                        }

                        setTimeout(_ => {
                            vData.columnListLoading = false;
                        });
                    }, 300);
                },

                columnCheckChange (item) {
                    if (item.$checkedColumnsArr.length === item.$feature_list.length) {
                        item.$indeterminate = false;
                        item.$checkedAll = true;
                    } else if (item.$checkedAll) {
                        item.$indeterminate = true;
                    }
                    context.emit('columnCheckChange', item);
                },

                confirmCheck () {
                    let num = 0;
                    const selected = vData.selectList[vData.selectListIndex];

                    vData.featureSelectTab.forEach(row => {
                        row.$feature_list.forEach(column => {
                            const item = row.$checkedColumnsArr.find(x => x === column.name);

                            if (!item) {
                                column.id = '';
                                column.method = '';
                                column.points = '';
                                column.count = 1;
                            } else if (column.id === '' || column.id === selected.id) {
                                num++;
                                column.method = vData.columnListType;
                                column.points = selected.points;
                                column.count = selected.count;
                                column.point = selected.point;
                                column.id = selected.id;
                            }
                        });
                    });

                    vData.selectList[vData.selectListIndex] = {
                        ...selected,
                        feature_column_count: num,
                    };

                    vData.feature_column_count = 0;
                    vData.selectList.forEach(column => {
                        vData.feature_column_count += column.feature_column_count;
                    });
                },

                changeMethodCount (item, index) {
                    if (!item.count) {
                        item.count = 1;
                    }

                    vData.featureSelectTab.forEach(member => {
                        member.$feature_list.forEach(row => {
                            if (row.id === item.id) {
                                row.count = item.count;
                            }
                        });
                    });
                },

                paramsCheck () {
                    return true;
                },

                checkParams () {
                    if (methods.paramsCheck()) {
                        const strategies = [];

                        vData.selectList.forEach(row => {
                            if (row.feature_column_count) {
                                strategies.push({
                                    ...row,
                                    strategy_id: row.id,
                                });
                            }
                        });

                        const members = [];

                        vData.featureSelectTab.forEach(member => {
                            const selected = {
                                member_id:   member.member_id,
                                member_role: member.member_role,
                                features:    [],
                            };

                            member.$feature_list.forEach(row => {
                                if (row.method) {
                                    selected.features.push({
                                        ...row,
                                        points: row.method === 'custom' ? row.points : '',
                                    });
                                }
                            });
                            if (selected.features.length) {
                                members.push(selected);
                            }
                        });

                        if (members.length === 0) {
                            $message.error('请先选择特征!');
                            return false;
                        }

                        return {
                            params: {
                                strategies,
                                members,
                            },
                        };
                    }
                },
            };

            // merge mixin
            vData = Object.assign($data, vData);
            methods = Object.assign($methods, methods);

            return {
                $methods,
                $data,
            };
        },
    };
};
