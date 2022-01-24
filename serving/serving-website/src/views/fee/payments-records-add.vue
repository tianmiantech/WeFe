<template>

    <el-card class="page" shadow="never">

        <h2 class="title">新增收支记录</h2>

        <el-form :model="paymentsRecords" label-width="90px" :rules="rules" ref="paymentsRecords">
            <el-form-item label="服务：" prop="serviceId">
                <el-select v-model="paymentsRecords.serviceId" filterable clearable placeholder="请选择服务">
                    <el-option
                        v-for="item in services"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value">
                    </el-option>
                </el-select>
            </el-form-item>

            <el-form-item label="客户：" prop="serviceId">
                <el-select v-model="paymentsRecords.clientId" filterable clearable placeholder="请选择客户">
                    <el-option
                        v-for="item in clients"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value">
                    </el-option>
                </el-select>
            </el-form-item>

            <el-form-item label="收支类型" prop="payType">
                <el-select v-model="paymentsRecords.payType" filterable clearable placeholder="请选择类型">
                    <el-option
                        v-for="item in payTypes"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value">
                    </el-option>
                </el-select>
            </el-form-item>

            <!--            <el-form-item label="状态" prop="status">-->
            <!--                <el-select v-model="paymentsRecords.status" filterable clearable placeholder="请选择类型">-->
            <!--                    <el-option-->
            <!--                        v-for="item in statusMap"-->
            <!--                        :key="item.value"-->
            <!--                        :label="item.label"-->
            <!--                        :value="item.value">-->
            <!--                    </el-option>-->
            <!--                </el-select>-->
            <!--            </el-form-item>-->

            <el-form-item label="金额" prop="amount" class="amount">
                <el-input v-model="paymentsRecords.amount"></el-input>
            </el-form-item>


            <el-form-item label="备注">
                <el-input v-model="paymentsRecords.remark" type="textarea"
                          :maxlength="300"
                          :minlength="0"
                          show-word-limit
                ></el-input>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="onSubmit">提交</el-button>
                <router-link
                    :to="{
                            name: 'payments-records',
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
    name: "payments-records-add",
    data() {
        let validateAmount = (rule, value, callback) => {
            if (!this.paymentsRecords.amount) {
                return callback(new Error('请输入金额'));
            } else {
                let reg = /^\d+(\.\d+)?$/;
                if (reg.test(this.paymentsRecords.amount)) {
                    callback();
                } else {
                    return callback(new Error('金额要求输入数值'));
                }
            }
        };

        return {
            services: [],
            clients: [],
            paymentsRecords: {
                payType: '',
                clientId: '',
                serviceId: '',
                amount: '',
                status: '',
                remark: '',
            },
            payTypes: [
                {value: '1', label: '充值'},
                {value: '2', label: '支出'},
            ],
            statusMap: [
                {value: '1', label: '正常'},
                {value: '2', label: '冲正'},
            ],
            rules: {

                clientId: [
                    {required: true, message: '请选择客户', trigger: 'change'}
                ],
                serviceId: [
                    {required: true, message: '请选择服务', trigger: 'change'}
                ],
                amount: [
                    {required: true, validator: validateAmount, trigger: 'blur'}
                ],
                payType: [
                    {required: true, message: '请选择收支类型', trigger: 'change'}
                ],
                status: [
                    {required: true, message: '请选择状态', trigger: 'change'}
                ],
            },
        }
    },

    computed: {
        ...mapGetters(['userInfo']),
    },
    created() {
        this.getServices()
        this.getClients()


    },

    methods: {


        onSubmit() {

            this.$refs.paymentsRecords.validate(async (valid) => {
                if (valid) {
                    const {code} = await this.$http.post({
                        url: '/paymentsrecords/save',
                        data: {
                            payType: this.paymentsRecords.payType,
                            serviceId: this.paymentsRecords.serviceId,
                            clientId: this.paymentsRecords.clientId,
                            amount: this.paymentsRecords.amount,
                            status: this.paymentsRecords.status,
                            remark: this.paymentsRecords.remark,
                        },
                    });

                    if (code === 0) {
                        this.$router.push({
                            name: 'payments-records'
                        })
                        this.$message('提交成功!');
                    }
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

    },


}
</script>

<style lang="scss" scoped>
.title {
    padding: 15px;
    margin: 5px;
}

.el-form {
    width: 600px;
}

.amount {
    width: 285px;
}
</style>
