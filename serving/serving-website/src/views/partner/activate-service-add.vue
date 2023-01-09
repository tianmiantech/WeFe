<template>
    <el-card
        class="page"
        shadow="never"
    >
        <h2 class="title">激活服务</h2>

        <el-form
            ref="clientService"
            :model="clientService"
            label-width="142px"
            :rules="rules"
        >
            <el-form-item
                label="服务名称："
                prop="serviceName"
                class="url"
            >
                <el-input
                    v-model="clientService.serviceName"
                    placeholder="自定义"
                />
            </el-form-item>

            <el-form-item
                label="服务提供商名称："
                prop="clientName"
                class="url"
            >
                <el-input
                    v-model="clientService.clientName"
                    placeholder="自定义"
                />
            </el-form-item>
            <el-form-item
                label="服务访问URL："
                prop="url"
                class="url flex_box"
            >
                <el-input
                    v-model="clientService.url"
                    clearable
                />
                <el-link
                    type="primary"
                    :underline="false"
                    @click="testUrl"
                >
                    测试连通性
                </el-link>
            </el-form-item>
            <el-form-item
                label="我的code："
                prop="code"
                class="url"
            >
                <el-input v-model="clientService.code" />
            </el-form-item>
            <el-form-item
                label="加密方式："
                prop="secret_key_type"
                class="url"
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
                label="我的公钥："
                prop="publicKey"
                class="public_key"
            >
                <el-input
                    v-model="clientService.publicKey"
                    type="textarea"
                    rows="5"
                    :minlength="0"
                    show-word-limit
                />
            </el-form-item>

            <el-form-item
                label="我的私钥："
                prop="privateKey"
                class="public_key"
            >
                <el-input
                    v-model="clientService.privateKey"
                    type="textarea"
                    rows="5"
                    :minlength="0"
                    show-word-limit
                />
                <a @click="getRsaKey">点击自动填充系统公私钥</a>
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
                >
                    &nbsp;<el-button>返回</el-button>
                </router-link>
            </el-form-item>
        </el-form>
    </el-card>
</template>

<script>
import { mapGetters } from 'vuex';
import { secret_key_type_list } from './config.js';


export default {
    name: 'ActivateServiceAdd',
    data() {
        const validateServiceName = (rule, value, callback) => {
            if (!this.clientService.serviceName) {
                return callback(new Error('服务名称不能为空'));
            } else {
                callback();
            }
        };

        const validateClientName = (rule, value, callback) => {
            if (!this.clientService.clientName) {
                return callback(new Error('客户名称不能为空'));
            } else {
                callback();
            }
        };

        return {
            clientService: {
                clientId:        '',
                status:          '',
                ipAdd:           '',
                publicKey:       '',
                privateKey:      '',
                code:            '',
                url:             '',
                // 预留字段
                payType:         '',
                serviceName:     '',
                clientName:      '',
                secret_key_type: 'sm2',
            },
            services:          [],
            clients:           [],
            dialogFormVisible: false,
            feeVisible:        false,
            formLabelWidth:    '110px',
            rules:             {
                serviceId: [
                    { required: true, trigger: 'change' },
                ],
                clientId: [
                    { required: true, trigger: 'change' },
                ],
                url: [
                    { required: true, trigger: 'change' },
                ],
                serviceName: [
                    { required: true, validator: validateServiceName, trigger: 'change' },
                ],
                clientName: [
                    { required: true, validator: validateClientName, trigger: 'change' },
                ],
                secret_key_type: [
                    { required: true, message: '请选择加密方式', trigger: 'change' },
                ],
            },
            secret_key_type_list,
        };
    },

    computed: {
        ...
            mapGetters(['userInfo']),
    },    
    created() {
    },

    methods: {
        async getRsaKey(){
            this.clientService.privateKey='';
            this.clientService.publicKey='';
            const { code, data } = await this.$http.post({
                url:  '/global_config/detail',
                data: {
                    'groups': ['identity_info'],
                },
            });

            if (code === 0) {
                this.clientService.privateKey='******************';
                this.clientService.publicKey=data.identity_info.rsa_public_key;
                setTimeout(() => {
                    this.$message('填充成功!');
                }, 1000);
            }
        },
        async testUrl(){
            const { code, data } = await this.$http.post({
                url:  '/clientservice/service_url_test',
                data: {
                    'url': this.clientService.url,
                },
            });

            if (code === 0) {
                this.$message('连通成功,code=' + data.code);
            }
        },
        onSubmit() {
            this.$refs.clientService.validate(async (valid) => {
                if (valid) {
                    const { code } = await this.$http.post({
                        url:  '/clientservice/activate',
                        data: {
                            serviceId:     'tempserviceidvalue',
                            clientId:      'tempclientidvalue',
                            publicKey:     this.clientService.publicKey,
                            ipAdd:         this.clientService.ipAdd,
                            serviceName:   this.clientService.serviceName,
                            clientName:    this.clientService.clientName,
                            createdBy:     this.userInfo.nickname,
                            privateKey:    this.clientService.privateKey === '******************' ? '' : this.clientService.privateKey,
                            url:           this.clientService.url,
                            code:          this.clientService.code,
                            secretKeyType: this.clientService.secret_key_type,
                        },
                    });

                    if (code === 0) {
                        setTimeout(() => {
                            this.$message('提交成功!');
                        }, 1000);
                        this.$router.push({
                            name: 'activate-service-list',
                        });
                    }
                } else {
                    return false;
                }
            });


        },

        async getServices() {
            const { code, data } = await this.$http.post({
                url:  '/service/query',
                data: {
                    status: 1,
                },
            });

            if (code === 0) {
                for (let i = 0; i < data.list.length; i++) {
                    this.services.push({
                        label: data.list[i].name,
                        value: data.list[i].id,
                    });
                }
            }
        },

        async getPartners() {
            const { code, data } = await this.$http.post({
                url: '/partner/query-list',
            });

            if (code === 0) {
                for (let i = 0; i < data.list.length; i++) {
                    this.clients.push({
                        label: data.list[i].name,
                        value: data.list[i].id,
                    });
                }
            }
        },

        async getPartnerById(id) {
            const { code, data } = await this.$http.post({
                url:  '/partner/query-one',
                data: {
                    id,
                },

            });

            if (code === 0) {
                this.clientService.clientId = data.id;
                this.clientService.clientName = data.name;
            }
        },

        async getServiceById(id) {
            const { code, data } = await this.$http.post({
                url:  '/service/query-one',
                data: {
                    id,
                },

            });

            if (code === 0) {
                this.clientService.serviceId = data.id;
                this.clientService.serviceName = data.name;
            }
        },
    },
};
</script>

<style lang="scss" scoped>
.url{
    width:600px;
}
.public_key{
    width: 800px;
}
.flex_box {
    .el-form-item__content {
        .el-input {
            width: 80%;
            margin-right: 10px;
        }
    }
}
</style>
