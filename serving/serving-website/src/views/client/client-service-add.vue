<template>

    <el-card class="page" shadow="never">

        <h2 class="title">新增客户服务</h2>

        <el-form ref="form" :model="clientService">
            <el-form-item label="服务名称：" label-width="100px">
                <el-select v-model="clientService.serviceId" filterable clearable placeholder="请选择服务">
                    <el-option
                        v-for="item in services"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value">
                    </el-option>
                </el-select>
            </el-form-item>

            <el-form-item label="客户名称：" label-width="100px">
                <el-select v-model="clientService.clientId" filterable clearable placeholder="请选择客户">
                    <el-option
                        v-for="item in clients"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value">
                    </el-option>
                </el-select>
            </el-form-item>

            <el-form-item v-for="item in feeConfig" :label="item.key" label-width="100px">
                {{ item.value }}
            </el-form-item>


            <el-form-item>
                <el-button type="button" @click="dialogFormVisible = true">自定义计费规则</el-button>
            </el-form-item>

            <el-dialog title="计费规则" :visible.sync="dialogFormVisible">
                <el-form :model="form">
                    <el-form-item label="单价：" :label-width="formLabelWidth">
                        <el-input v-model="form.unitPrice"></el-input>
                    </el-form-item>
                    <el-form-item label="付费类型：" :label-width="formLabelWidth">
                        <el-radio v-model="form.payType" label="0">后付费</el-radio>
                        <el-radio v-model="form.payType" label="1">预付费</el-radio>
                    </el-form-item>
                </el-form>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="dialogFormVisible = false">取 消</el-button>
                    <el-button type="primary" @click="saveFeeConfig()">确 定</el-button>
                </div>
            </el-dialog>

            <!--            <el-form-item label="活动名称">-->
            <!--                <el-input v-model="form.name" ></el-input>-->
            <!--            </el-form-item>-->

            <el-form-item>
                <el-radio v-model="clientService.status" label="1">启用</el-radio>
                <el-radio v-model="clientService.status" label="0">暂不启用</el-radio>

            </el-form-item>
            <el-form-item>
                <!--                <router-link-->
                <!--                    :to="{-->
                <!--                            name: 'client-service-list',-->
                <!--                        }"-->
                <!--                >-->
                <el-button type="primary" @click="onSubmit">提交</el-button>
                <!--                </router-link>-->
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
        return {
            clientService: {
                serviceId: '',
                clientId: '',
                status: '',
                feeConfigId: '',
                unitPrice: '',
                // 预留字段
                payType: '',
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
            feeConfig: [],
            payType: {
                0: "后付费",
                1: "预付费"
            }
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
        async saveFeeConfig() {
            const {code, data} = await this.$http.post({
                url: '/feeconfig/save',
                data: {
                    payType: this.form.payType,
                    unitPrice: this.form.unitPrice,
                },
            });

            if (code === 0) {
                this.clientService.feeConfigId = data.id
                this.dialogFormVisible = false
                this.feeVisible = true
                this.$message('保存成功！');
            }


            this.feeConfig = []
            this.feeConfig.push({
                key: '单价:',
                value: data.unit_price
            })
            this.feeConfig.push({
                key: '付费类型:',
                value: this.payType[data.pay_type]
            })

        },


        async onSubmit() {


            const {code} = await this.$http.post({
                url: '/clientservice/save',
                data: {
                    serviceId: this.clientService.serviceId,
                    clientId: this.clientService.clientId,
                    unitPrice: this.clientService.unitPrice,
                    feeConfigId: this.clientService.feeConfigId,
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
        },

        handleServices(data) {
            for (let i = 0; i < data.length; i++) {
                this.services.push({
                    label: data[i].name,
                    value: data[i].id
                })
            }
        },

        handleClients(data) {
            for (let i = 0; i < data.length; i++) {
                this.clients.push({
                    label: data[i].name,
                    value: data[i].id
                })
            }
        },

        async getServices() {
            const {code, data} = await this.$http.post({
                url: '/service/query',
            });

            if (code === 0) {
                this.handleServices(data.list)
            }
        },

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

}
</style>
