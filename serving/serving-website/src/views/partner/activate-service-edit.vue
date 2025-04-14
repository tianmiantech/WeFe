<template>
    <el-card
        class="page"
        shadow="never"
    >
        <h2 class="title">更新激活服务</h2>

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
                <el-input v-model="clientService.serviceName" />
            </el-form-item>

            <el-form-item
                label="服务提供商名称："
                prop="clientName"
                class="url"
            >
                <el-input v-model="clientService.clientName" />
            </el-form-item>
            <el-form-item
                label="服务访问URL："
                prop="url"
                class="url flex_box"
            >
                <el-input v-model="clientService.url" />
                <el-link
                    v-if="clientService.url.trim().startsWith('http')"
                    type="primary"
                    :underline="false"
                    @click="testUrl"
                >
                    测试连通性
                </el-link>
            </el-form-item>
            <el-form-item
                label="code："
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
                label="公钥："
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
                label="私钥："
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
                <a @click="getRsaKey">填充系统公私钥</a>
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
                        name: 'activate-service-list',
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
import { secret_key_type_list } from './config';


export default {
    name: 'ActivateServiceEdit',
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
                serviceId:       '',
                clientId:        '',
                ipAdd:           '',
                publicKey:       '',
                privateKey:      '',
                code:            '',
                url:             '',
                serviceName:     '',
                clientName:      '',
                secret_key_type: 'rsa',
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
                serviceName: [
                    { required: true, validator: validateServiceName, trigger: 'change' },
                ],
                clientName: [
                    { required: true, validator: validateClientName, trigger: 'change' },
                ],
                url: [
                    { required: true, trigger: 'change' },
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
        this.loading = true;
        if (this.$route.query.clientId && this.$route.query.serviceId) {
            this.getClientService(this.$route.query.serviceId, this.$route.query.clientId);
        }
        this.loading = false;
    },

    methods: {
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
                this.clientService.privateKey=data.identity_info.rsa_private_key;
                this.clientService.publicKey=data.identity_info.rsa_public_key;
                setTimeout(() => {
                    this.$message('填充成功!');
                }, 1000);
            }
        },

        onSubmit() {
            this.$refs.clientService.validate(async (valid) => {
                if (valid) {
                    const { serviceId,clientId,publicKey,privateKey,ipAdd,url,serviceName,clientName,secret_key_type } = this.clientService;
                    const { code } = await this.$http.post({
                        url:  '/clientservice/update_activate',
                        data: {
                            serviceId,
                            clientId,
                            publicKey,
                            privateKey,
                            ipAdd,
                            url,
                            serviceName,
                            clientName,
                            code:          this.clientService.code,
                            updatedBy:     this.userInfo.nickname,
                            secretKeyType: secret_key_type,
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
        async getClientService(serviceId, clientId) {
            const { code, data } = await this.$http.post({
                url:  '/clientservice/detail',
                data: {
                    serviceId,
                    clientId,
                },
            });

            if (code === 0) {
                this.clientService.serviceId = data.service_id;
                this.clientService.serviceName = data.service_name;
                this.clientService.publicKey = data.public_key;
                this.clientService.clientId = data.client_id;
                this.clientService.clientName = data.client_name;
                this.clientService.privateKey = data.private_key;
                this.clientService.code = data.code;
                this.clientService.url = data.url;
                this.clientService.secret_key_type = data.secret_key_type;
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
