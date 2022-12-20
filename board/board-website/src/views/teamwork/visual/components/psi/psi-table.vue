<template>
    <div class="psi-table">
        <div v-if="judge">
            特征过滤：<el-select 
                multiple 
                filterable
                collapse-tags 
                clearable
                v-model="data.searchParams.feature">
                <el-option
                    v-for="item in searchParams"
                    :key="item"
                    :label="item"
                    :value="item"
                ></el-option>
            </el-select>
        </div>
        <el-table 
            :data="showTableData" 
            :max-height="500"
            :sort-method="sort-method"
            @sort-change="sortChange"
            style="margin-top: 10px;"
            row-key="key"
            ref="tableExpand"
            stripe
            border
            fixed>
            <el-table-column
                prop="tmp"
                label=""
                align="center"
                width="60">
                <template v-slot="scope">
                    <div class="line">
                        <button v-if="data.expandKeys.includes(scope.row.key) || (scope.row.children && scope.row.children.length)" :class="data.expandKeys.includes(scope.row.key)? 'iconExpand icon-collage': 'iconExpand'" @click="editExpandKeys(scope.row.key,scope.row)"></button>
                    </div>
                </template>
            </el-table-column>

            <el-table-column
                v-for="item in columns"
                :key="item.key"
                :prop="item.dataKey"
                :label="item.title"
                :sortable="item.sortable"
            >
            </el-table-column>
        </el-table>
        <el-pagination
            v-if="judge"
            style="margin-top: 10px;"
            v-model:currentPage="data.current"
            v-model:page-size="data.pageSize"
            :page-sizes="[10, 15, 20, 30]"
            :total="showAllData.length"
            :small="true"
            layout="total, sizes, prev, pager, next"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
    </div>
</template>

<script setup>
    import { reactive, computed,toRaw,watch,ref,nextTick } from 'vue';
    import { turnDemical } from '@src/utils/utils';

    // eslint-disable-next-line no-undef
    const props = defineProps({
        type: {
            type:    String,
            default: '',
        },
        tableData: {
            type:    Array,
            default: () => {},
        },
        judge: {
            type:    Boolean,
            default: false,
        },
    });

    const tableExpand = ref();
    const baseData = computed(() => {
        const tableData = [];
        const data = toRaw(props.tableData);

        Reflect.ownKeys(data).forEach(feature => {
            const { 
                train_feature_static ={},
                test_feature_static= {},
                feature_psi='',
                bin_cal_results,
                split_point = [],
            } = data[feature] || {};
            const { 
                bin_ln_test_base_value = [],
                bin_psi = [],
                bin_sub_test_base_value = [],
            } = bin_cal_results || {};

            const { 
                count_rate: testRate = [],
            } = test_feature_static;
            const { count_rate: trainRate = [] } = train_feature_static;

            let tmp = ['[0,1]'];

            if(split_point.length > 1){
                split_point.forEach((item, index) => {
                    if(index === 0){
                        tmp = [`<=${turnDemical(item, 2)}`];
                    } else if(index === split_point.length-1) {
                        tmp.push(`>${turnDemical(split_point[index-1], 2)}`);
                    } else {
                        tmp.push(`(${turnDemical(split_point[index-1],  2)} , ${turnDemical(item, 2)}]`);
                    }
                });
            }

            function judge(value){
                try {
                    return value > 0.0001;
                } catch (error) {
                    return false;
                }
            }
            
            tableData.push({
                feature,
                psi:         judge(feature_psi) ? turnDemical((feature_psi || 0), 4) : 0,
                key:         feature,
                test:        '-',
                train:       '-',
                testSubBase: '-',
                lntestbase:  '-',
                bin:         '-',
                children:    bin_psi.map((bin,index) => {
                    return {
                        key:         `${feature}-${index}`,
                        bin:         tmp[index] ,
                        test:        `${turnDemical((testRate[index] || 0) * 100, 2)}`,
                        train:       `${turnDemical((trainRate[index] || 0) * 100, 2)}`,
                        testSubBase: `${turnDemical((bin_sub_test_base_value[index] || 0) * 100, 2)}%`,
                        psi:         judge(bin_psi[index]) ? turnDemical((bin_psi[index] || 0), 4) : 0,
                        lntestbase:  turnDemical((bin_ln_test_base_value[index] || 0), 4),
                    };
                }),
            });
        });
        return tableData;
    });
    const columns = [
        { title: 'Feature', key: 'feature', dataKey: 'feature', sortable: props.judge },
        { title: 'Bin', key: 'bin', dataKey: 'bin' },
        { title: 'Test%', key: 'test', dataKey: 'test',filter: /^(?!Oot).+$/ },
        { title: 'Oot%', key: 'test', dataKey: 'test', filter: /^Oot$/ },
        { title: 'Train%', key: 'train', dataKey: 'train' },
        { title: 'Test - Train', key: 'testSubBase', dataKey: 'testSubBase',filter: /^(?!Oot).+$/ },
        { title: 'Oot - Train', key: 'testSubBase', dataKey: 'testSubBase',filter: /^Oot$/ },
        { title: 'ln(Test/Train)', key: 'lntestbase', dataKey: 'lntestbase',filter: /^(?!Oot).+$/ },
        { title: 'ln(Oot/Train)', key: 'lntestbase', dataKey: 'lntestbase',filter: /^Oot$/ },
        { title: 'PSI', key: 'psi', dataKey: 'psi',sortable: props.judge },
    ].filter(item => !item.filter || item.filter.test(props.type));

    const data = reactive({
        current:      1,
        pageSize:     10,
        searchParams: {
            feature: [],
        },
        tableData:  JSON.parse(JSON.stringify(baseData.value)),
        expandKeys: [],
    });

    watch(()=> baseData.value,
          (current, pre) => {
              data.tableData = current;
          });

    /**
     * 过滤条件后所有的数据
     */
    const showAllData = computed(() => {        
        return toRaw(data.tableData)
            .filter(item => {
                return Reflect.ownKeys(data.searchParams).every(key => {
                    if(Array.isArray(data.searchParams[key]) && data.searchParams[key].length){
                        return data.searchParams[key].includes(item[key]);
                    }
                    return true;
                });
            });
    });
    
    /**
     * 前端分页，显示在表格中的数据
     */
    const showTableData = computed(() => {
        return showAllData.value.slice((data.current - 1)*data.pageSize, data.current *data.pageSize);
    });

    // 排序
    const sortChange = ({ prop, column, order }) => {
        if(order){
            const tempData = toRaw(data.tableData);

            tempData.sort((a,b) => {
                return (a[prop] > b[prop] ? 1 : -1) * (order === 'ascending'? 1 : -1 );
            });
            data.tableData = tempData;
        } else {
            data.tableData = baseData;
        }
    };
    const handleSizeChange = (val) => {
        toggleRowAllExpand();

    };
    const handleCurrentChange = (val) => {
        toggleRowAllExpand();

    };
    const editExpandKeys = (key, row) => {
        const index = data.expandKeys.indexOf(key);

        if (index > -1) {
            data.expandKeys.splice(index, 1);
            tableExpand.value.toggleRowExpansion(row, false);
        } else {
            data.expandKeys.push(key);
            tableExpand.value.toggleRowExpansion(row, true);
        }
    };
    /**
     * 跳页返回保持原来的状态
     */
    const toggleRowAllExpand = () => {
        nextTick(() => {
            data.expandKeys.forEach(item => {
                tableExpand.value.toggleRowExpansion({ key: item }, true);
            });
        });


    };


    const searchParams = [
        ...new Set(baseData.value.map((item) => item.feature)),
    ];

</script>

<style>
  .notShowExpand .cell .board-table__expand-icon{display: none;}

  .iconExpand{cursor: pointer;height: 16px;width: 16px;background-color: #fff;position: relative;display: inline-block;}
  .iconExpand::before{top: 3px;bottom: 3px;height: 9px;width: 1px;content:'';background: currentColor;position: absolute;transform: rotate(-180deg);transition: transform .3s ease-out;}
  .iconExpand::after{left: 3px;right: 3px;width: 9px;height: 1px;content:'';background: currentColor;position: absolute;transform: rotate(0deg);transition: transform .3s ease-out;}
  button{border: 1px solid #c4c4c4}
  .icon-collage::before{transform: rotate(90deg);}
  .icon-collage::after{transform: rotate(180deg);}
  
  .psi-table .board-table .board-table__expand-icon{
    display: none;
  }
  .psi-table .line{
    /* text-align: center; */
    display: flex;
    align-items: center;
    justify-content: center;
  }
</style>
