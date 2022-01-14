<template>

    <el-card class="page" shadow="never">

        <h2 class="title">新增收支记录</h2>

        <el-form :model="paymentsRecords" label-width="90px" :rules="rules" ref="paymentsRecords">
            <el-form-item label="客户" prop="client">
                <el-input v-model="paymentsRecords.clientId"
                ></el-input>
            </el-form-item>
            <el-form-item label="服务" prop="service">
                <el-input v-model="paymentsRecords.serviceId"
                ></el-input>
            </el-form-item>

            <el-form-item label="收支类型" prop="payType">
                <el-input v-model="paymentsRecords.payType"></el-input>
            </el-form-item>

            <el-form-item label="金额" prop="amount">
                <el-input v-model="paymentsRecords.amount"></el-input>
            </el-form-item>

            <el-form-item label="状态" prop="status">
                <el-input v-model="paymentsRecords.status"></el-input>
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
            rules: {

                // name: [
                //     {required: true, message: '请输入客户名称', trigger: 'blur'}
                // ],
                // email: [
                //     {required: true, message: '请输入邮箱', trigger: 'change'}
                // ],
                // ipAdd: [
                //     // {required: true, message: '请输入IP白名单', trigger: 'change'}
                //     { required: true, validator: validateIpAdd, trigger: 'blur'}
                // ],
                // code: [
                //     {required: true, message: '请输入客户code', trigger: 'change'}
                // ],
                // pubKey: [
                //     {required: true, message: '请输入公钥', trigger: 'change'}
                // ],
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
                        setTimeout(() => {
                            this.$message('提交成功!');
                        }, 1000)
                        this.$router.push({
                            name: 'payments-records'
                        })
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
</style>
