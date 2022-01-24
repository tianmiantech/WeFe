<template>

    <el-card class="page" shadow="never">

        <h2 class="title">开通服务</h2>

        <el-form :model="clientService" label-width="102px" :rules="rules" ref="clientService">
            <el-form-item label="服务名称：" prop="serviceName">
                <el-select v-model="clientService.serviceId" filterable clearable placeholder="请选择服务">
                    <el-option
                        v-for="item in services"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value">
                    </el-option>
                </el-select>
            </el-form-item>

            <el-form-item label="客户名称：" prop="clientName">
                <el-select v-model="clientService.clientId" filterable clearable placeholder="请选择客户">
                    <el-option
                        v-for="item in clients"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value">
                    </el-option>
                </el-select>
            </el-form-item>

            <!--            <el-form-item v-for="item in feeConfig" :label="item.key" :prop="item.rule_" :key="item.key">-->
            <!--                {{ item.value }}-->
            <!--            </el-form-item>-->

            <!--            <el-form-item>-->
            <!--                <el-button type="button" @click="dialogFormVisible = true">自定义计费规则</el-button>-->
            <!--            </el-form-item>-->

            <el-form-item label="单价(￥)：" prop="unitPrice" class="unit_price">
                <el-input v-model="clientService.unitPrice" maxlength="10"></el-input>
            </el-form-item>

            <el-form-item label="付费类型：" :label-width="formLabelWidth" prop="payType">
                <el-radio v-model="clientService.payType" label="0">后付费</el-radio>
                <el-radio v-model="clientService.payType" label="1">预付费</el-radio>
            </el-form-item>
            <!--            <el-dialog title="计费规则" :visible.sync="dialogFormVisible">-->
            <!--                <el-form :model="clientService" :rules="rules">-->
            <!--                    <el-form-item label="单价(￥)：" :label-width="formLabelWidth" prop="unitPrice">-->
            <!--                        <el-input v-model="clientService.unitPrice" maxlength="10"></el-input>-->
            <!--                    </el-form-item>-->
            <!--                    <el-form-item label="付费类型：" :label-width="formLabelWidth" prop="payType">-->
            <!--                        <el-radio v-model="clientService.payType" label="0">后付费</el-radio>-->
            <!--                        <el-radio v-model="clientService.payType" label="1">预付费</el-radio>-->
            <!--                    </el-form-item>-->
            <!--                </el-form>-->
            <!--                <div slot="footer" class="dialog-footer">-->
            <!--                    <el-button @click="dialogFormVisible = false">取 消</el-button>-->
            <!--                    <el-button type="primary" @click="saveFeeConfig()">确 定</el-button>-->
            <!--                </div>-->
            <!--            </el-dialog>-->

            <el-form-item>
                <el-button type="primary" @click="onSubmit">提交</el-button>
                <router-link
                    :to="{
                            name: 'client-service-list',
                        }"
                >
                    <el-button>返回</el-button>
                </router-link>
            </el-form-item>
        </el-form>

    </el-card>


</template>

<script>
import {mapGetters} from 'vuex';


export default {
    name: "client-service-add",
    data() {
        let validateServiceName = (rule, value, callback) => {
            if (!this.clientService.serviceId) {
                return callback(new Error('服务名称不能为空'));
            } else {
                callback();
            }
        };

        let validateClientName = (rule, value, callback) => {
            if (!this.clientService.clientId) {
                return callback(new Error('客户名称不能为空'));
            } else {
                callback();
            }
        };

        let validateUnitPrice = (rule, value, callback) => {
            if (!this.clientService.unitPrice) {
                return callback(new Error('请输入单价'));
            } else {
                let reg = /^\d+(\.\d+)?$/;
                if (reg.test(this.clientService.unitPrice)) {
                    callback();
                } else {
                    return callback(new Error('单价要求输入数值'));
                }
            }
        };

        let validatePayType = (rule, value, callback) => {
            if (!this.clientService.payType) {
                return callback(new Error('请选择计费类型'));
            } else {
                callback();
            }
        };

        return {
            clientService: {
                serviceId: '',
                clientId: '',
                status: '',
                unitPrice: '',
                // 预留字段
                payType: '',
                serviceName: '',
                clientName: '',
            },
            services: [],
            clients: [],
            dialogFormVisible: false,
            feeVisible: false,
            form: {
                unitPrice: '',
                payType: '',

            },
            formLabelWidth: '100px',
            // feeConfig: [],
            payType: {
                0: "后付费",
                1: "预付费"
            },
            rules: {
                serviceName: [
                    {required: true, validator: validateServiceName, trigger: 'change'},
                    // {min: 3, max: 5, message: '长度在 3 到 5 个字符', trigger: 'blur'}
                ],
                clientName: [
                    {required: true, validator: validateClientName, trigger: 'change'}
                ],
                unitPrice: [
                    {required: true, validator: validateUnitPrice, trigger: 'blur'}
                ],
                payType: [
                    {required: true, validator: validatePayType, trigger: 'change'}
                ],
            },
        }
    },

    computed: {
        ...
            mapGetters(['userInfo']),
    }
    ,
    created() {


        if (this.$route.query.clientId) {
            this.getClientById(this.$route.query.clientId)
        }
        if (this.$route.query.serviceId) {
            this.getServiceById(this.$route.query.serviceId)
        }
        this.getServices();
        this.getClients()


    },

    methods: {

        // saveFeeConfig() {
        //
        //     if (!this.clientService.unitPrice) {
        //         this.$message('请输入单价')
        //         return false;
        //     }
        //
        //
        //     if (!this.clientService.payType) {
        //         this.$message.error('请选择计费类型')
        //         return false;
        //     }
        //
        //     // 重新清空 fee config
        //     this.feeConfig = []
        //     this.feeConfig.push({
        //         key: '单价:',
        //         value: this.clientService.unitPrice,
        //         rule_: 'unitPrice'
        //     })
        //     this.feeConfig.push({
        //         key: '付费类型:',
        //         value: this.payType[this.clientService.payType],
        //         rule_: 'payType'
        //     })
        //
        //     this.dialogFormVisible = false
        //     this.feeVisible = true
        // },

        onSubmit() {
            this.$refs.clientService.validate(async (valid) => {
                if (valid) {

                    if (!this.clientService.unitPrice) {
                        this.$message.error('请输入单价');
                        return false;
                    }
                    if (!this.clientService.payType) {
                        this.$message.error('请选择付费类型');
                        return false;
                    }

                    const {code} = await this.$http.post({
                        url: '/clientservice/save',
                        data: {
                            serviceId: this.clientService.serviceId,
                            clientId: this.clientService.clientId,
                            unitPrice: this.clientService.unitPrice,
                            payType: this.clientService.payType,
                            serviceName: this.clientService.serviceName,
                            clientName: this.clientService.clientName,
                        },
                    });

                    if (code === 0) {
                        setTimeout(() => {
                            this.$message('提交成功!');
                        }, 1000)
                        this.$router.push({
                            name: 'client-service-list'
                        })
                    }
                } else {
                    return false;
                }
            });


        },

        handleServices(data) {
            for (let i = 0; i < data.length; i++) {
                this.services.push({
                    label: data[i].name,
                    value: data[i].id
                })
            }
        }
        ,

        handleClients(data) {
            for (let i = 0; i < data.length; i++) {
                this.clients.push({
                    label: data[i].name,
                    value: data[i].id
                })
            }
        }
        ,

        async getServices() {
            const {code, data} = await this.$http.post({
                url: '/service/query',
                data: {
                    status: 1,
                }
            });

            if (code === 0) {
                this.handleServices(data.list)
            }
        }
        ,

        async getClients() {
            const {code, data} = await this.$http.post({
                url: '/client/query-list',
            });

            if (code === 0) {
                this.handleClients(data.list)
            }
        },

        async getClientById(id) {
            const {code, data} = await this.$http.post({
                url: '/client/query-one',
                data: {
                    id: id,
                },

            });
            console.log(data, 'client')
            if (code === 0) {
                this.clientService.clientId = data.id
                this.clientService.clientName = data.name
            }
        },

        async getServiceById(id) {
            const {code, data} = await this.$http.post({
                url: '/service/query-one',
                data: {
                    id: id,
                },

            });

            console.log(data, 'service')
            if (code === 0) {
                this.clientService.serviceId = data.id
                this.clientService.serviceName = data.name
            }
        },

    }
    ,


}
</script>

<style lang="scss" scoped>
.title {
    padding: 15px;
    margin: 5px;
}

.unit_price {
    width: 295px;
}
</style>
