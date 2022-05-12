<template>

    <el-card v-loading="loading" class="page" shadow="never">

        <h2 class="title">编辑服务</h2>

        <el-form :model="clientService" label-width="112px" :rules="rules" ref="clientService">
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

            <el-form-item label="合作者名称：" prop="clientName">
                <el-select v-model="clientService.clientId" filterable clearable placeholder="请选择合作者">
                    <el-option
                        v-for="item in clients"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value">
                    </el-option>
                </el-select>
            </el-form-item>


            <el-form-item label="单价(￥)：" prop="unitPrice" class="unit_price">
                <el-input v-model="clientService.unitPrice" maxlength="10"></el-input>
            </el-form-item>
            <el-form-item label="公钥：" prop="publicKey" class="public_key">
                <el-input v-model="clientService.publicKey"
                          type="textarea"
                          rows="5"
                          :maxlength="300"
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
    name: "partner-service-edit",
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
                clientId: '',
                status: '',
                unitPrice: '',
                // 预留字段
                payType: '',
                ipAdd:'',
                publicKey:'',
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
    async created() {

        this.loading = true;
        if (this.$route.query.clientId && this.$route.query.serviceId) {
            this.getPartnerById(this.$route.query.clientId)
            this.getServiceById(this.$route.query.serviceId)
            this.getClientService(this.$route.query.serviceId, this.$route.query.clientId)
        }

        await this.getServices()
        await this.getPartners()

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
                            unitPrice: this.clientService.unitPrice,
                            ipAdd : this.clientService.ipAdd,
                            publicKey:this.clientService.publicKey,
                            payType: this.clientService.payType,
                            status: this.$route.query.status === '未启用' ? 0 : 1,
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

        handleServices(data) {
            for (let i = 0; i < data.length; i++) {
                this.services.push({
                    label: data[i].name,
                    value: data[i].id
                })
            }
        }
        ,

        handlePartners(data) {
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
        },

        async getPartners() {
            const {code, data} = await this.$http.post({
                url: '/partner/query-list',
            });

            if (code === 0) {
                this.handlePartners(data.list)
            }
        },

        async getPartnerById(id) {
            const {code, data} = await this.$http.post({
                url: '/partner/query-one',
                data: {
                    id: id,
                },

            });
            if (code === 0) {
                this.clientService.clientId = data.id
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
                this.clientService.serviceId = data.id
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
            console.log(data);
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
