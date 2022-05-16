<template>

    <el-card class="page" shadow="never">

        <h2 class="title">开通服务</h2>

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
                <el-radio v-model="clientService.payType" label="0">后付费</el-radio>
                <el-radio v-model="clientService.payType" label="1">预付费</el-radio>
            </el-form-item>

            <el-form-item>
                <el-button type="primary" @click="onSubmit">提交</el-button>
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
    name: "partner-service-add",
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
                ipAdd:'',
                publicKey:'',
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
            formLabelWidth: '110px',
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
                    {required: true, validator: validateUnitPrice, trigger: 'change'}
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


        if (this.$route.query.partnerId) {
            this.getPartnerById(this.$route.query.partnerId)
        }
        if (this.$route.query.serviceId) {
            this.getServiceById(this.$route.query.serviceId)
        }
        this.getServices();
        this.getPartners()


    },

    methods: {

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
                    this.clientService.serviceName = this.services.find(y => y.value === this.clientService.serviceId).label;
                    const {code} = await this.$http.post({
                        url: '/clientservice/save',
                        data: {
                            serviceId: this.clientService.serviceId,
                            clientId: this.clientService.clientId,
                            unitPrice: this.clientService.unitPrice,
                            payType: this.clientService.payType,
                            publicKey:this.clientService.publicKey,
                            ipAdd:this.clientService.ipAdd,
                            serviceName: this.clientService.serviceName,
                            clientName: this.clientService.clientName,
                            createdBy: this.userInfo.nickname,
                        },
                    });

                    if (code === 0) {
                        setTimeout(() => {
                            this.$message('提交成功!');
                        }, 1000)
                        this.$router.push({
                            name: 'partner-service-list'
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
        }
        ,

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
    width: 305px;
}

.ip_add{
    width: 500px;
}

.public_key{
    width: 800px;
}
</style>
