<template>
    <el-card
        class="page"
        shadow="never"
    >
        <h2 class="title">开通服务</h2>

        <el-form
            ref="clientService"
            :model="clientService"
            :label-width="formLabelWidth"
            :rules="rules"
        >
            <el-form-item
                label="服务名称："
                prop="serviceName"
            >
                <el-select
                    v-model="clientService.serviceId"
                    filterable
                    clearable
                    placeholder="请选择服务"
                >
                    <el-option
                        v-for="item in services"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                    />
                </el-select>
            </el-form-item>

            <el-form-item
                label="合作者名称："
                prop="clientName"
            >
                <el-select
                    v-model="clientService.clientId"
                    filterable
                    clearable
                    placeholder="请选择合作者"
                >
                    <el-option
                        v-for="item in clients"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                    />
                </el-select>
            </el-form-item>
            <el-form-item
                label="服务调用单价(￥)："
                prop="unitPrice"
                class="unit_price"
            >
                <el-input
                    v-model="clientService.unitPrice"
                    maxlength="10"
                />
            </el-form-item>
            <el-form-item
                label="加密方式："
                prop="secret_key_type"
            >
                <el-select
                    v-model="clientService.secret_key_type"
                    filterable
                    placeholder="请选择加密方式"
                >
                    <el-option
                        v-for="item in secret_key_type_list"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                    />
                </el-select>
            </el-form-item>
            <el-form-item
                label="合作者公钥："
                prop="publicKey"
                class="public_key"
            >
                <el-input
                    v-model="clientService.publicKey"
                    type="textarea"
                    rows="5"
                    :maxlength="1000"
                    :minlength="0"
                    show-word-limit
                />
            </el-form-item>

            <el-form-item
                label="合作者出口IP："
                prop="ipAdd"
                class="ip_add"
            >
                <el-input v-model="clientService.ipAdd" />
            </el-form-item>

            <el-form-item
                label="合作者付费类型："
                prop="payType"
            >
                <el-radio
                    v-model="clientService.payType"
                    label="0"
                >
                    后付费
                </el-radio>
                <el-radio
                    v-model="clientService.payType"
                    label="1"
                >
                    预付费
                </el-radio>
            </el-form-item>

            <el-form-item>
                <el-button
                    type="primary"
                    @click="onSubmit"
                >
                    提交
                </el-button>
                <router-link
                    :to="{
                        name: 'partner-service-list',
                    }"
                    style="margin-left: 3px"
                >
                    <el-button>返回</el-button>
                </router-link>
            </el-form-item>
        </el-form>
    </el-card>
</template>

<script>
import { mapGetters } from 'vuex';
import { secret_key_type_list } from './config.js';


export default {
    name: 'PartnerServiceAdd',
    data() {
        const validateServiceName = (rule, value, callback) => {
            if (!this.clientService.serviceId) {
                return callback(new Error('服务名称不能为空'));
            } else {
                callback();
            }
        };

        const validateClientName = (rule, value, callback) => {
            if (!this.clientService.clientId) {
                return callback(new Error('客户名称不能为空'));
            } else {
                callback();
            }
        };

        const validateUnitPrice = (rule, value, callback) => {
            if (!this.clientService.unitPrice) {
                return callback(new Error('请输入单价'));
            } else {
                const reg = /^\d+(\.\d+)?$/;

                if (reg.test(this.clientService.unitPrice)) {
                    callback();
                } else {
                    return callback(new Error('单价要求输入数值'));
                }
            }
        };

        const validatePayType = (rule, value, callback) => {
            if (!this.clientService.payType) {
                return callback(new Error('请选择计费类型'));
            } else {
                callback();
            }
        };

        return {
            clientService: {
                serviceId:       '',
                clientId:        '',
                status:          '',
                unitPrice:       '',
                ipAdd:           '',
                publicKey:       '',
                // 预留字段
                payType:         '',
                serviceName:     '',
                clientName:      '',
                secret_key_type: 'rsa',
            },
            services:          [],
            clients:           [],
            dialogFormVisible: false,
            feeVisible:        false,
            form:              {
                unitPrice: '',
                payType:   '',
            },
            formLabelWidth: '142px',
            payType:        {
                0: '后付费',
                1: '预付费',
            },
            secret_key_type_list,
            rules: {
                serviceName: [
                    { required: true, validator: validateServiceName, trigger: 'change' },
                    // {min: 3, max: 5, message: '长度在 3 到 5 个字符', trigger: 'blur'}
                ],
                clientName: [
                    { required: true, validator: validateClientName, trigger: 'change' },
                ],
                unitPrice: [
                    { required: true, validator: validateUnitPrice, trigger: 'change' },
                ],
                payType: [
                    { required: true, validator: validatePayType, trigger: 'change' },
                ],
                secret_key_type: [
                    { required: true, message: '请选择加密方式', trigger: 'change' },
                ],
            },
        };
    },

    computed: {
        ...
            mapGetters(['userInfo']),
    },
    created() {
        if (this.$route.query.partnerId) {
            this.getPartnerById(this.$route.query.partnerId);
        }
        this.getServices();
        this.getPartners();
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

                    if(!this.clientService.secret_key_type){
                        this.$message.error('请选择加密方式');
                        return false;
                    }
                    this.clientService.serviceName = this.services.find(y => y.value === this.clientService.serviceId).label;
                    const { code } = await this.$http.post({
                        url:  '/clientservice/save',
                        data: {
                            serviceId:     this.clientService.serviceId,
                            clientId:      this.clientService.clientId,
                            unitPrice:     this.clientService.unitPrice,
                            payType:       this.clientService.payType,
                            publicKey:     this.clientService.publicKey,
                            ipAdd:         this.clientService.ipAdd,
                            serviceName:   this.clientService.serviceName,
                            clientName:    this.clientService.clientName,
                            secretKeyType: this.clientService.secret_key_type,
                            createdBy:     this.userInfo.nickname,
                        },
                    });

                    if (code === 0) {
                        setTimeout(() => {
                            this.$message('提交成功!');
                        }, 1000);
                        this.$router.push({
                            name: 'partner-service-list',
                        });
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
                    value: data[i].service_id,
                });
            }
        },

        handlePartners(data) {
            for (let i = 0; i < data.length; i++) {
                this.clients.push({
                    label: data[i].name,
                    value: data[i].id,
                });
            }
        },

        async getServices() {
            const { code, data } = await this.$http.post({
                url:  '/service/query',
                data: {
                    status: 1,
                },
            });

            if (code === 0) {
                this.handleServices(data.list);
            }
        },

        async getPartners() {
            const { code, data } = await this.$http.post({
                url: '/partner/query-list',
            });

            if (code === 0) {
                this.handlePartners(data.list);
            }
        },

        async getPartnerById(id) {
            const { code, data } = await this.$http.post({
                url:  '/partner/detail',
                data: {
                    id,
                },

            });

            if (code === 0) {
                this.clientService.clientId = data.id;
                this.clientService.clientName = data.name;
            }
        },
    },
};
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
