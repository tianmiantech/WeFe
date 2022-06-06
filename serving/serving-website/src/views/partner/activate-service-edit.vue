<template>

    <el-card class="page" shadow="never">

        <h2 class="title">更新激活服务</h2>

        <el-form :model="clientService" label-width="142px" :rules="rules" ref="clientService">
            <el-form-item label="服务ID：" prop="serviceId" class = "url">
                <el-input v-model="clientService.serviceId"></el-input>
            </el-form-item>

            <el-form-item label="服务名称：" prop="serviceName" class = "url">
                <el-input v-model="clientService.serviceName"></el-input>
            </el-form-item>

            <el-form-item label="服务提供商ID：" prop="clientId" class = "url">
                <el-input v-model="clientService.clientId"></el-input>
            </el-form-item>

            <el-form-item label="服务提供商名称：" prop="clientName" class = "url">
                <el-input v-model="clientService.clientName"></el-input>
            </el-form-item>
            <el-form-item label="服务访问URL：" prop="url" class = "url flex_box">
                <el-input v-model="clientService.url"></el-input>
                <el-link @click="testUrl" type="primary" :underline="false">测试连通性</el-link>
            </el-form-item>
            <el-form-item label="code：" prop="code" class = "url">
                <el-input v-model="clientService.code"></el-input>
            </el-form-item>
            <el-form-item label="公钥：" prop="publicKey" class="public_key">
                <el-input v-model="clientService.publicKey"
                          type="textarea"
                          rows="5"
                          :minlength="0"
                          show-word-limit>
                </el-input>
            </el-form-item>

            <el-form-item label="私钥：" prop="privateKey" class="public_key">
                <el-input v-model="clientService.privateKey"
                          type="textarea"
                          rows="5"
                          :minlength="0"
                          show-word-limit>
                </el-input>
                <a @click="getRsaKey">填充系统公私钥</a>
            </el-form-item>

            <el-form-item>
                <el-button type="primary" @click="onSubmit">提交</el-button>
                <router-link
                    :to="{
                            name: 'activation-service-list',
                        }"
                >
                    &nbsp;<el-button>返回</el-button>
                </router-link>
            </el-form-item>
        </el-form>

    </el-card>


</template>

<script>
import {mapGetters} from 'vuex';


export default {
    name: "activate-service-edit",
    data() {
        let validateServiceName = (rule, value, callback) => {
            if (!this.clientService.serviceName) {
                return callback(new Error('服务名称不能为空'));
            } else {
                callback();
            }
        };

        let validateClientName = (rule, value, callback) => {
            if (!this.clientService.clientName) {
                return callback(new Error('客户名称不能为空'));
            } else {
                callback();
            }
        };
        return {
            clientService: {
                serviceId: '',
                clientId: '',
                ipAdd:'',
                publicKey:'',
                privateKey:'',
                code:'',
                url:'',
                serviceName: '',
                clientName: '',
            },
            services: [],
            clients: [],
            dialogFormVisible: false,
            feeVisible: false,
            formLabelWidth: '110px',
            rules: {
                serviceId: [
                    {required: true, trigger: 'change'},
                ],
                clientId: [
                    {required: true, trigger: 'change'}
                ],
                serviceName: [
                    {required: true, validator: validateServiceName, trigger: 'change'},
                ],
                clientName: [
                    {required: true, validator: validateClientName, trigger: 'change'}
                ],
                url: [
                    {required: true, trigger: 'change'}
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
        this.loading = true;
        if (this.$route.query.clientId && this.$route.query.serviceId) {
            this.getClientService(this.$route.query.serviceId, this.$route.query.clientId)
        }
        this.loading = false;
    },

    methods: {
        async testUrl(){
            const { code, data } = await this.$http.post({
                url: '/clientservice/service_url_test',
                data:{
                    "url":this.clientService.url
                }
            });
            if (code === 0) {
                this.$message('连通成功,code=' + data.code);
            }
        },
        async getRsaKey(){
            this.clientService.privateKey='';
            this.clientService.publicKey='';
            const { code, data } = await this.$http.post({
                url: '/global_config/detail',
                data:{
                    "groups":['identity_info']
                }
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
                    const {code} = await this.$http.post({
                        url: '/clientservice/update_activate',
                        data: {
                            serviceId: this.clientService.serviceId,
                            clientId: this.clientService.clientId,
                            publicKey:this.clientService.publicKey,
                            privateKey:this.clientService.privateKey,
                            ipAdd:this.clientService.ipAdd,
                            url:this.clientService.url,
                            serviceName: this.clientService.serviceName,
                            clientName: this.clientService.clientName,
                            code:this.clientService.code,
                            createdBy: this.userInfo.nickname,
                        },
                    });

                    if (code === 0) {
                        setTimeout(() => {
                            this.$message('提交成功!');
                        }, 1000)
                        this.$router.push({
                            name: 'activation-service-list'
                        })
                    }
                } else {
                    return false;
                }
            });
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
                this.clientService.serviceId = data.service_id;
                this.clientService.serviceName = data.service_name;
                this.clientService.publicKey = data.public_key;
                this.clientService.clientId = data.client_id;
                this.clientService.clientName = data.client_name;
                this.clientService.privateKey = data.private_key;
                this.clientService.code = data.code;
                this.clientService.url = data.url;
            }
        },
    },
}
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
