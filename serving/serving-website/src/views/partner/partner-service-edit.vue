<template>

    <el-card v-loading="loading" class="page" shadow="never">

        <h2 class="title">编辑客户服务</h2>

        <el-form :model="clientService" label-width="112px" :rules="rules" ref="clientService">
            <el-form-item label="服务名称：">
                {{clientService.serviceName}}
            </el-form-item>

            <el-form-item label="合作者名称：">
                {{clientService.clientName}}
            </el-form-item>

            <el-form-item label="单价(￥)：" prop="unitPrice" class="unit_price">
                <el-input v-model="clientService.unitPrice" maxlength="10"></el-input>
            </el-form-item>
            <el-form-item label="公钥：" prop="publicKey" class="public_key">
                <el-input v-model="clientService.publicKey"
                          type="textarea"
                          rows="5"
                          :maxlength="1000"
                          :minlength="0"
                          show-word-limit>
                </el-input>
            </el-form-item>

            <el-form-item label="IP白名单：" prop="ipAdd" class="ip_add">
                <el-input v-model="clientService.ipAdd"></el-input>
            </el-form-item>

            <el-form-item label="付费类型：" :label-width="formLabelWidth" prop="payType">
                <el-radio v-model="clientService.payType" :label="0">后付费</el-radio>
                <el-radio v-model="clientService.payType" :label="1">预付费</el-radio>
            </el-form-item>

            <el-form-item>
                <el-button type="primary" @click="onSubmit">提交</el-button>
                &nbsp;
                <router-link
                    :to="{
                            name: 'partner-service-list',
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
    name: "partner-service-edit",
    data() {
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
            if (this.clientService.payType === '') {
                return callback(new Error('请选择计费类型'));
            } else {
                callback();
            }
        };

        return {
            loading: false,
            clientService: {
                serviceId: '',
                serviceName:'',
                clientId: '',
                clientName:'',
                status: '',
                unitPrice: '',
                // 预留字段
                payType: '',
                ipAdd:'',
                publicKey:'',
            },
            dialogFormVisible: false,
            feeVisible: false,
            form: {
                unitPrice: '',
                payType: '',
            },
            formLabelWidth: '100px',
            payType: {
                0: "后付费",
                1: "预付费"
            },
            rules: {
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
    async created() {
        this.loading = true;
        if (this.$route.query.clientId && this.$route.query.serviceId) {
            this.clientService.serviceId = this.$route.query.serviceId;
            await this.getPartnerById(this.$route.query.clientId)
            await this.getServiceById(this.$route.query.serviceId)
            await this.getClientService(this.$route.query.serviceId, this.$route.query.clientId)
        }
        this.loading = false;
    },

    methods: {

        onSubmit() {
            this.$refs.clientService.validate(async (valid) => {
                if (valid) {

                    if (!this.clientService.unitPrice) {
                        this.$message.error('请输入单价');
                        return false;
                    }
                    if (this.clientService.payType === '') {
                        this.$message.error('请选择付费类型');
                        return false;
                    }
                    const {code} = await this.$http.post({
                        url: '/clientservice/update',
                        data: {
                            serviceId: this.clientService.serviceId,
                            clientId: this.clientService.clientId,
                            ipAdd : this.clientService.ipAdd,
                            publicKey:this.clientService.publicKey,
                            unitPrice:this.clientService.unitPrice,
                            updatedBy: this.userInfo.nickname,
                        },
                    });

                    if (code === 0) {
                        setTimeout(() => {
                            this.$message('提交成功!');
                        }, 1000)
                    }
                } else {
                    return false;
                }
            });


        },
        async getPartnerById(id) {
            const {code, data} = await this.$http.post({
                url: '/partner/detail',
                data: {
                    id: id,
                },

            });
            if (code === 0) {
                this.clientService.clientId = data.id;
                this.clientService.clientName = data.name;
            }
        },

        async getServiceById(id) {
            const {code, data} = await this.$http.post({
                url: '/service/query-one',
                data: {
                    id: id,
                },

            });
            if (code === 0) {
                // this.clientService.serviceId = data.id;
                this.clientService.serviceName = data.name;
            }
        },

        async getClientService(serviceId, clientId) {
            const {code, data} = await this.$http.post({
                url: '/clientservice/detail',
                data: {
                    serviceId: serviceId,
                    clientId: clientId,
                }
            });
            if (code === 0) {
                this.clientService.payType = data.pay_type;
                this.clientService.unitPrice = data.unit_price;
                this.clientService.publicKey = data.public_key;
                this.clientService.ipAdd = data.ip_add;
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
    width: 305px;
}

.ip_add{
    width: 500px;
}

.public_key{
    width: 800px;
}
</style>
