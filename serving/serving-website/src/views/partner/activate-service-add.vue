<template>

    <el-card class="page" shadow="never">

        <h2 class="title">激活服务</h2>

        <el-form :model="clientService" label-width="142px" :rules="rules" ref="clientService">
            <el-form-item label="服务ID：" prop="serviceId" class = "url">
                <el-input v-model="clientService.serviceId" aria-placeholder="自行填写"></el-input>
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
            <el-form-item label="服务访问URL：" prop="url" class = "url">
                <el-input v-model="clientService.url"></el-input>
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
            </el-form-item>

            <el-form-item>
                <el-button type="primary" @click="getRsaKey">填充系统公私钥</el-button>
                <el-button type="primary" @click="onSubmit">提交</el-button>
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
import {mapGetters} from 'vuex';


export default {
    name: "activate-service-add",
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
                status: '',
                ipAdd:'',
                publicKey:'',
                privateKey:'',
                code: '',
                url:'',
                // 预留字段
                payType: '',
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
                url: [
                    {required: true, trigger: 'change'}
                ],
                serviceName: [
                    {required: true, validator: validateServiceName, trigger: 'change'},
                ],
                clientName: [
                    {required: true, validator: validateClientName, trigger: 'change'}
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
        await this.getRsaKey();
    },

    methods: {
        async getRsaKey(){
            const { code, data } = await this.$http.post({
                url: '/global_config/detail',
                data:{
                    "groups":['identity_info']
                }
            });

            if (code === 0) {
                this.clientService.privateKey=data.identity_info.rsa_private_key;
                this.clientService.publicKey=data.identity_info.rsa_public_key;
            }
        },
        onSubmit() {
            this.$refs.clientService.validate(async (valid) => {
                if (valid) {
                    const {code} = await this.$http.post({
                        url: '/clientservice/activate',
                        data: {
                            serviceId: this.clientService.serviceId,
                            clientId: this.clientService.clientId,
                            publicKey:this.clientService.publicKey,
                            ipAdd:this.clientService.ipAdd,
                            serviceName: this.clientService.serviceName,
                            clientName: this.clientService.clientName,
                            createdBy: this.userInfo.nickname,
                            privateKey:this.clientService.privateKey,
                            code: this.clientService.code,
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

        async getServices() {
            const {code, data} = await this.$http.post({
                url: '/service/query',
                data: {
                    status: 1,
                }
            });

            if (code === 0) {
                for (let i = 0; i < data.list.length; i++) {
                    this.services.push({
                        label: data.list[i].name,
                        value: data.list[i].id
                    })
                }
            }
        },

        async getPartners() {
            const {code, data} = await this.$http.post({
                url: '/partner/query-list',
            });

            if (code === 0) {
                for (let i = 0; i < data.list.length; i++) {
                    this.clients.push({
                        label: data.list[i].name,
                        value: data.list[i].id
                    })
                }
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
</style>
