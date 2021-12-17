<template>
    <div
        v-loading="vData.loading"
        class="result"
    >
        <template v-if="vData.commonResultData.task">
            <el-collapse
                v-model="activeName"
                @change="methods.collapseChanged"
            >
                <el-collapse-item
                    title="基础信息"
                    name="1"
                >
                    <CommonResult
                        :result="vData.commonResultData"
                        :currentObj="currentObj"
                        :jobDetail="jobDetail"
                    />
                </el-collapse-item>

                <template v-if="myRole === 'promoter' && vData.promoterConfig.totalColumnCount">
                    <el-collapse-item title="皮尔逊热力图" name="2">
                        <el-button
                            size="mini"
                            :disabled="vData.promoterConfig.totalColumnCount === 0"
                            @click="methods.showColumnListDialog('promoter')"
                        >
                            选择特征（{{ vData.promoterConfig.featureColumnCount }}/{{ vData.promoterConfig.totalColumnCount }}）
                        </el-button>
                        <HeatmapChart
                            v-if="vData.showProviderChart"
                            ref="promoterChart"
                            :config="vData.promoterConfig"
                        />
                    </el-collapse-item>
                </template>

                <template v-else>
                    <template v-if="vData.localConfig.totalColumnCount">
                        <el-collapse-item title="本地热力图" name="3">
                            <el-button
                                size="mini"
                                :disabled="vData.localConfig.totalColumnCount === 0"
                                @click="methods.showColumnListDialog('local')"
                            >
                                选择特征（{{ vData.localConfig.featureColumnCount }}/{{ vData.localConfig.totalColumnCount }}）
                            </el-button>
                            <HeatmapChart
                                v-if="vData.showLocalChart"
                                ref="localChart"
                                :config="vData.localConfig"
                            />
                        </el-collapse-item>
                    </template>

                    <template v-if="vData.providerConfig.totalColumnCount">
                        <el-collapse-item title="联合热力图" name="4">
                            <el-button
                                size="mini"
                                :disabled="vData.providerConfig.totalColumnCount === 0"
                                @click="methods.showColumnListDialog('provider')"
                            >
                                选择特征（{{ vData.providerConfig.featureColumnCount }}/{{ vData.providerConfig.totalColumnCount }}）
                            </el-button>
                            <HeatmapChart
                                v-if="vData.showProviderChart"
                                ref="providerChart"
                                :config="vData.providerConfig"
                            />
                        </el-collapse-item>
                    </template>
                </template>
            </el-collapse>
        </template>

        <div
            v-else
            class="data-empty"
        >
            <p v-if="myRole === 'promoter'">查无结果!</p>
            <el-alert
                v-else
                title="!!! 协作方无法查看结果"
                style="width:250px;"
                :closable="false"
                type="warning"
                effect="dark"
                class="mb10"
                show-icon
            />
        </div>

        <CheckFeatureDialog
            ref="PromoterFeatureDialog"
            :feature-select-tab="vData.promoterFeatureSelectTab"
            :column-list-type="vData.columnListType"
            @confirmCheck="methods.confirmCheck"
        />
        <CheckFeatureDialog
            ref="ProviderFeatureDialog"
            :feature-select-tab="vData.providerFeatureSelectTab"
            :column-list-type="vData.columnListType"
            @confirmCheck="methods.confirmCheck"
        />
        <CheckFeatureDialog
            ref="LocalFeatureDialog"
            :feature-select-tab="vData.localFeatureSelectTab"
            :column-list-type="vData.columnListType"
            @confirmCheck="methods.confirmCheck"
        />
    </div>
</template>

<script>
    import {
        ref,
        reactive,
    } from 'vue';
    import checkFeatureMixin from '../common/checkFeature';
    import CheckFeatureDialog from '../common/checkFeatureDialog';
    import CommonResult from '../common/CommonResult';
    import resultMixin from '../result-mixin';

    const resultMixins = resultMixin();
    const checkFeatureMixins = checkFeatureMixin();

    export default {
        name:       'VertPearson',
        components: {
            CommonResult,
            CheckFeatureDialog,
        },
        props: {
            ...resultMixins.props,
        },
        emits: [...checkFeatureMixins.emits],
        setup(props, context) {
            const activeName = ref('1');
            const PromoterFeatureDialog = ref();
            const ProviderFeatureDialog = ref();
            const LocalFeatureDialog = ref();
            const promoterChart = ref();
            const providerChart = ref();
            const localChart = ref();

            let vData = reactive({
                hasResult:                false,
                showPromoterChart:        false,
                showLocalChart:           false,
                showProviderChart:        false,
                checkDialogName:          'promoter',
                promoterFeatureSelectTab: [],
                providerFeatureSelectTab: [],
                localFeatureSelectTab:    [],
                columnListType:           'heatmap',
                promoterConfig:           {
                    xAxis:              [],
                    yAxis:              [],
                    series:             [],
                    width:              1000,
                    height:             700,
                    featureColumnCount: 0,
                    totalColumnCount:   0,
                    mixCorr:            {},
                },
                providerConfig: {
                    xAxis:              [],
                    yAxis:              [],
                    series:             [],
                    width:              1000,
                    height:             700,
                    featureColumnCount: 0,
                    totalColumnCount:   0,
                    mixCorr:            {},
                },
                localConfig: {
                    xAxis:              [],
                    yAxis:              [],
                    series:             [],
                    width:              1000,
                    height:             700,
                    featureColumnCount: 0,
                    totalColumnCount:   0,
                    mixCorr:            {},
                },
                corr:     [],
                mix_corr: [],
            });

            let methods = {
                showResult(data) {
                    vData.promoterConfig.totalColumnCount = 0;
                    vData.providerConfig.totalColumnCount = 0;
                    vData.localConfig.totalColumnCount = 0;
                    vData.promoterFeatureSelectTab = [];
                    vData.providerFeatureSelectTab = [];
                    vData.localFeatureSelectTab = [];

                    if(data[0].result) {
                        const maxFeatureNum = 20;
                        const {
                            corr,
                            features,
                            local_corr,
                            mix_corr,
                            mix_feature_names,
                            corr_feature_names,
                            remote_features_names,
                        } = data[0].result.statistics_pearson.data.corr.value;

                        if(props.myRole === 'promoter') {
                            // promoter Multidimensional matrix thermodynamic diagram
                            // ---> mix_corr array to object mapping
                            mix_feature_names.forEach((row, rowIndex) => {
                                if(!vData.promoterConfig.mixCorr[row]) {
                                    vData.promoterConfig.mixCorr[row] = {};
                                }
                                mix_feature_names.forEach((column, columnIndex) => {
                                    if(!vData.promoterConfig.mixCorr[row][column]) {
                                        vData.promoterConfig.mixCorr[row][column] = mix_corr[rowIndex][columnIndex];
                                    }
                                });
                            });
                            // <---

                            // promoter x,y
                            mix_feature_names.forEach((item, index) => {
                                if(index < maxFeatureNum) {
                                    vData.promoterConfig.xAxis.push(item);
                                    vData.promoterConfig.yAxis.unshift(item);
                                }
                            });
                            vData.promoterConfig.series = [];
                            mix_corr.forEach((rows, rowIndex) => {
                                if(rowIndex < maxFeatureNum) {
                                    rows.forEach((row, index) => {
                                        if(index < maxFeatureNum) {
                                            vData.promoterConfig.series.push([rowIndex, maxFeatureNum - index - 1, String(row).replace(/^(.*\..{4}).*$/,'$1')]);
                                        }
                                    });
                                }
                            });

                            vData.promoterConfig.featureColumnCount = vData.promoterConfig.xAxis.length;
                            vData.promoterConfig.width = vData.promoterConfig.xAxis.length * (vData.promoterConfig.xAxis.length > 10 ? 60 : 100);
                            vData.promoterConfig.height = vData.promoterConfig.yAxis.length * 34 + (vData.promoterConfig.yAxis.length > 10 ? 50: 100);

                            const promoterCheckedFeatures = [];
                            const providerCheckedFeatures = [];
                            const array = features.map((feature, index) => {
                                const key = `promoter_${feature}`;

                                if(index < maxFeatureNum) {
                                    promoterCheckedFeatures.push(key);
                                }
                                return key;
                            });

                            // promoter
                            methods.featureData(
                                'promoter',
                                array,
                                promoterCheckedFeatures,
                                vData.promoterFeatureSelectTab,
                                'promoter',
                            );

                            if(remote_features_names) {
                                for(const [index, feature] of remote_features_names.entries()) {
                                    if(index < (maxFeatureNum - promoterCheckedFeatures.length)) {
                                        providerCheckedFeatures.push(feature);
                                    } else {
                                        break;
                                    }
                                }
                                // provider
                                methods.featureData(
                                    'promoter',
                                    remote_features_names,
                                    providerCheckedFeatures,
                                    vData.promoterFeatureSelectTab,
                                    'provider',
                                );
                            }
                        } else {
                            // provider has two charts: local & provider
                            // ---> local_corr array to object mapping
                            features.forEach((row, rowIndex) => {
                                if(!vData.localConfig.mixCorr[row]) {
                                    vData.localConfig.mixCorr[row] = {};
                                }
                                features.forEach((column, columnIndex) => {
                                    if(!vData.localConfig.mixCorr[row][column]) {
                                        vData.localConfig.mixCorr[row][column] = local_corr[rowIndex][columnIndex];
                                    }
                                });
                            });
                            // <---

                            features.forEach((item, index) => {
                                if(index < maxFeatureNum) {
                                    vData.localConfig.xAxis.push(item);
                                    vData.localConfig.yAxis.push(item);
                                }
                            });
                            vData.localConfig.series = [];
                            local_corr.forEach((rows, rowIndex) => {
                                if(rowIndex < maxFeatureNum) {
                                    rows.forEach((row, index) => {
                                        if(index < maxFeatureNum) {
                                            vData.localConfig.series.push([rowIndex, maxFeatureNum - index - 1, String(row).replace(/^(.*\..{4}).*$/,'$1')]);
                                        }
                                    });
                                }
                            });

                            vData.localConfig.featureColumnCount = vData.localConfig.xAxis.length;
                            vData.localConfig.width = vData.localConfig.xAxis.length * (vData.localConfig.xAxis.length > 10 ? 60: 100);
                            vData.localConfig.height = vData.localConfig.yAxis.length * 34 + (vData.localConfig.yAxis.length > 10 ? 50 : 100);

                            let providerCheckedFeatures = [];
                            const array = features.map((feature, index) => {
                                if(index < maxFeatureNum) {
                                    providerCheckedFeatures.push(feature);
                                }
                                return feature;
                            });

                            methods.featureData(
                                'local',
                                array,
                                providerCheckedFeatures,
                                vData.localFeatureSelectTab,
                                'provider',
                            );

                            // provider chart
                            // ---> corr array to object mapping
                            corr_feature_names[0].forEach((row, rowIndex) => {
                                if(!vData.providerConfig.mixCorr[row]) {
                                    vData.providerConfig.mixCorr[row] = {};
                                }
                                corr_feature_names[1].forEach((column, columnIndex) => {
                                    if(!vData.providerConfig.mixCorr[row][column]) {
                                        vData.providerConfig.mixCorr[row][column] = corr[rowIndex][columnIndex];
                                    }
                                });
                            });
                            // <---

                            corr_feature_names[0].forEach((item, index) => {
                                if(index < maxFeatureNum) {
                                    vData.providerConfig.yAxis.push(item);
                                }
                            });
                            corr_feature_names[1].forEach((item, index) => {
                                if(index < maxFeatureNum) {
                                    vData.providerConfig.xAxis.push(item);
                                }
                            });
                            vData.providerConfig.series = [];
                            corr.forEach((rows, rowIndex) => {
                                if(rowIndex < maxFeatureNum) {
                                    rows.forEach((row, index) => {
                                        if(index < maxFeatureNum) {
                                            vData.providerConfig.series.push([rowIndex, maxFeatureNum - index - 1, row.toFixed(4)]);
                                        }
                                    });
                                }
                            });

                            vData.providerConfig.featureColumnCount = vData.providerConfig.xAxis.length + vData.providerConfig.yAxis.length;
                            vData.providerConfig.width = vData.providerConfig.xAxis.length * (vData.providerConfig.xAxis.length > 10 ? 60 : 100);
                            vData.providerConfig.height = vData.providerConfig.yAxis.length * 34 + (vData.providerConfig.yAxis.length > 10 ? 50: 100);

                            const promoterCheckedFeatures = [];

                            for(const [index, feature] of corr_feature_names[0].entries()) {
                                if(index < maxFeatureNum) {
                                    promoterCheckedFeatures.push(feature);
                                } else {
                                    break;
                                }
                            }

                            // provider
                            methods.featureData(
                                'provider',
                                corr_feature_names[0],
                                promoterCheckedFeatures,
                                vData.providerFeatureSelectTab,
                                'promoter',
                            );

                            if(corr_feature_names) {
                                providerCheckedFeatures = [];
                                for(const [index, feature] of corr_feature_names[1].entries()) {
                                    if(index < maxFeatureNum) {
                                        providerCheckedFeatures.push(feature);
                                    } else {
                                        break;
                                    }
                                }

                                // provider
                                methods.featureData(
                                    'provider',
                                    corr_feature_names[1],
                                    providerCheckedFeatures,
                                    vData.providerFeatureSelectTab,
                                    'provider',
                                );
                            }
                        }
                        vData.hasResult = true;
                    }
                },

                featureData(role, features, $checkedColumnsArr, arr, member_role) {
                    const $feature_list = features.map(name => {return { name };});

                    if(role === 'promoter') {
                        vData.promoterConfig.totalColumnCount += $feature_list.length;
                    } else if(role === 'provider') {
                        vData.providerConfig.totalColumnCount += $feature_list.length;
                    } else {
                        vData.localConfig.totalColumnCount += $feature_list.length;
                    }

                    arr.push({
                        member_id:       '',
                        member_name:     '',
                        member_role,
                        $checkedAll:     false,
                        $indeterminate:  false,
                        $checkedColumnsArr,
                        $checkedColumns: '',
                        $feature_list,
                    });
                },

                showColumnListDialog(role) {
                    vData.checkDialogName = role;

                    switch(role) {
                    case 'promoter':
                        PromoterFeatureDialog.value.methods.show();
                        break;
                    case 'provider':
                        ProviderFeatureDialog.value.methods.show();
                        break;
                    case 'local':
                        LocalFeatureDialog.value.methods.show();
                        break;
                    }
                },

                confirmCheck(list) {
                    switch(vData.checkDialogName) {
                    case 'promoter':
                        methods.chartRender(promoterChart.value, vData.promoterConfig, 'promoter', list);
                        break;
                    case 'provider':
                        methods.chartRender(providerChart.value, vData.providerConfig, 'provider', list);
                        break;
                    case 'local':
                        methods.chartRender(localChart.value, vData.localConfig, 'local', list);
                        break;
                    }
                },

                chartRender(chart, chartData, role, list) {
                    // set x,y
                    chartData.xAxis = [];
                    chartData.yAxis = [];
                    chartData.featureColumnCount = 0;

                    if(role === 'provider') {
                        chartData.xAxis.push(...list[1].$checkedColumnsArr);
                        list[0].$checkedColumnsArr.forEach(name => {
                            chartData.yAxis.push(name);
                        });
                        chartData.featureColumnCount += list[0].$checkedColumnsArr.length + list[1].$checkedColumnsArr.length;

                        // set rows
                        const { length } = chartData.yAxis;

                        chartData.series = [];
                        chartData.xAxis.forEach(($row, rowIndex) => {

                            chartData.yAxis.forEach((column, columnIndex) => {
                                const row = chartData.mixCorr[$row][column];

                                chartData.series.push([rowIndex, length - columnIndex - 1, String(row).replace(/^(.*\..{4}).*$/,'$1')]);
                            });
                        });
                    } else {

                        list.forEach(({ $checkedColumnsArr }) => {
                            chartData.xAxis.push(...$checkedColumnsArr);
                            $checkedColumnsArr.forEach(name => {
                                chartData.yAxis.push(name);
                            });

                            if(role !== 'local') {
                                chartData.featureColumnCount += $checkedColumnsArr.length;
                            }
                        });

                        if(role === 'local') {
                            chartData.featureColumnCount += chartData.xAxis.length;
                        }

                        // set rows
                        const { length } = chartData.yAxis;

                        chartData.series = [];
                        chartData.xAxis.forEach(($row, rowIndex) => {

                            chartData.yAxis.forEach((column, columnIndex) => {
                                const row = chartData.mixCorr[$row][column];

                                chartData.series.push([rowIndex, length - columnIndex - 1, String(row).replace(/^(.*\..{4}).*$/,'$1')]);
                            });
                        });
                    }

                    chartData.width = chartData.xAxis.length * (chartData.xAxis.length > 10 ? 60 : 100);
                    chartData.height = chartData.yAxis.length * 34 + (chartData.yAxis.length > 10 ? 50 : 100);

                    setTimeout(() => {
                        chart.changeData();
                    }, 500);
                },

                collapseChanged(val) {
                    if(val.includes('2')) {
                        vData.showProviderChart = true;
                    }
                    if (val.includes('3')) {
                        vData.showLocalChart = true;
                    }
                    if(val.includes('4')){
                        vData.showProviderChart = true;
                    }
                },
            };

            // merge mixin
            const { $data, $methods } = checkFeatureMixins.mixin({
                vData,
                props,
                context,
                methods,
            });
            const { $data: _data, $methods: _methods } = resultMixins.mixin({
                props,
                context,
                vData,
                methods,
            });

            vData = Object.assign($data, _data, vData);
            methods = {
                ...$methods,
                ..._methods,
                ...methods,
            };

            return {
                vData,
                activeName,
                promoterChart,
                providerChart,
                localChart,
                methods,
                PromoterFeatureDialog,
                ProviderFeatureDialog,
                LocalFeatureDialog,
            };
        },
    };
</script>
