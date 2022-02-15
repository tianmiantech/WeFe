<template>

    <el-card class="page" shadow="never">

        <h2 class="title">新增客户</h2>

        <el-form :model="client" label-width="90px" :rules="rules" ref="client">
            <el-form-item label="客户名称" prop="name">
                <el-input v-model="client.name"
                          :maxlength="32"
                          :minlength="4"
                          show-word-limit
                ></el-input>
            </el-form-item>
            <el-form-item label="客户邮箱" prop="email">
                <el-input v-model="client.email"></el-input>
            </el-form-item>
            <el-form-item label="IP 白名单" prop="ipAdd">
                <el-input v-model="client.ipAdd" placeholder="支持多个，英文逗号分隔"></el-input>
            </el-form-item>
            <el-form-item label="客户 code" prop="code">
                <el-input v-model="client.code" :disabled="this.clientId !== ''"
                          placeholder="建议格式：公司简称+日期，如 WELAB20220118"
                          :maxlength="60"
                          :minlength="4"
                          show-word-limit
                ></el-input>
            </el-form-item>
            <el-form-item label="公钥" prop="pubKey">
                <el-input v-model="client.pubKey" type="textarea"
                          :minlength="128"
                          show-word-limit></el-input>
            </el-form-item>

<!--            <el-form-item label="状态：" prop="status">-->
<!--                <el-radio v-model="client.status" label="0">正常</el-radio>-->
<!--                <el-radio v-model="client.status" label="1">禁用</el-radio>-->
<!--            </el-form-item>-->


            <el-form-item label="备注">
                <el-input v-model="client.remark" type="textarea"
                          rows="5"
                          :maxlength="300"
                          :minlength="0"
                          show-word-limit
                ></el-input>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="onSubmit">提交</el-button>
                <router-link
                    :to="{
                            name: 'client-list',
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
import RoleTag from "../components/role-tag";


export default {
    name: "client-add",
    components: {
        RoleTag,
    },
    data() {

        let util = {
            isValidIp: function (e) {
                // 去除后面多余的 “,”
                let reg = /,+$/gi;
                const ip = e.replace(reg, "");
                return /^(?:(?:^|,)(?:[0-9]|[1-9]\d|1\d{2}|2[0-4]\d|25[0-5])(?:\.(?:[0-9]|[1-9]\d|1\d{2}|2[0-4]\d|25[0-5])){3})+$/.test(ip)
            }
        };

        let validateIpAdd = (rule, value, callback) => {
            if (!this.client.ipAdd) {
                return callback(new Error('IP 地址不能为空'));
            } else if (!util.isValidIp(this.client.ipAdd)) {
                return callback(new Error('请输入合法的IP'));
            } else {
                callback();
            }
        };

        let validatePubKey = (rule, value, callback) => {
            if (!this.client.pubKey) {
                return callback(new Error('公钥不能为空'));
            } else if (this.client.pubKey.length < 128) {
                return callback(new Error('公钥长度不符合规范，至少十六进制128长度'));
            } else {
                callback();
            }
        };

        // let validateStatus = (rule, value, callback) => {
        //     if (!this.client.status) {
        //         return callback(new Error('请选择客户状态'));
        //     } else {
        //         callback();
        //     }
        // };


        return {
            client: {
                id: '',
                name: '',
                pubKey: '',
                email: '',
                ipAdd: '',
                remark: '',
                code: '',
                status: '',
            },
            rules: {

                name: [
                    {required: true, message: '请输入客户名称', trigger: 'blur'}
                ],
                email: [
                    {required: true, message: '请输入邮箱', trigger: 'change'}
                ],
                ipAdd: [
                    // {required: true, message: '请输入IP白名单', trigger: 'change'}
                    {required: true, validator: validateIpAdd, trigger: 'blur'}
                ],
                // status: [
                //     {required: true, validator: validateStatus, trigger: 'change'}
                // ],
                code: [
                    {required: true, message: '请输入客户code', trigger: 'change'}
                ],
                pubKey: [
                    {required: true, validator: validatePubKey, trigger: 'change'}
                ],
            },
            clientId: '',
        }
    },

    computed: {
        ...mapGetters(['userInfo']),
    },
    created() {
        if (this.$route.query.id) {
            this.clientId = this.$route.query.id
            this.getClientById(this.$route.query.id)
        }

    },
    methods: {


        onSubmit() {
            this.$refs.client.validate(async (valid) => {
                if (valid) {
                    const {code, message} = await this.$http.post({
                        url: '/client/save',
                        data: {
                            id: this.client.id,
                            name: this.client.name,
                            email: this.client.email,
                            ipAdd: this.client.ipAdd,
                            pubKey: this.client.pubKey,
                            remark: this.client.remark,
                            createdBy: this.userInfo.nickname,
                            code: this.client.code,
                        },
                    });

                    if (code === 0) {
                        setTimeout(() => {
                            this.$message('提交成功!');
                        }, 1000)
                        this.$router.push({
                            name: 'client-list'
                        })
                    }
                }
            });

        },


        async getClientById(id) {
            const {code, data} = await this.$http.post({
                url: '/client/query-one',
                data: {
                    id: id,
                },

            });
            if (code === 0) {
                this.client.id = data.id
                this.client.name = data.name
                this.client.email = data.email
                this.client.ipAdd = data.ip_add
                this.client.pubKey = data.pub_key
                this.client.remark = data.remark
                this.client.code = data.code
            }
        }
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
