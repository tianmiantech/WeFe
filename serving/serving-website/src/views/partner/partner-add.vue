<template>
    <el-card
        class="page"
        shadow="never"
    >
        <h2 class="title">新增合作者</h2>

        <el-form
            ref="client"
            :model="client"
            label-width="120px"
            :rules="rules"
        >
            <el-form-item
                label="合作者名称"
                prop="name"
            >
                <el-input
                    v-model="client.name"
                    :maxlength="32"
                    :minlength="4"
                    show-word-limit
                />
            </el-form-item>
            <el-form-item
                label="合作者邮箱"
                prop="email"
            >
                <el-input v-model="client.email" />
            </el-form-item>
            <el-form-item
                label="合作者 code"
                prop="code"
            >
                <el-input
                    v-model="client.code"
                    :disabled="clientId !== ''"
                    placeholder="建议格式：公司简称+日期，如 WELAB20220118"
                    :maxlength="60"
                    :minlength="4"
                    show-word-limit
                />
            </el-form-item>

            <el-form-item
                label="Serving服务地址"
                prop="servingBaseUrl"
            >
                <el-input v-model="client.servingBaseUrl" />
            </el-form-item>
            <el-form-item label="联邦成员：" prop="isUnionMember">
                <el-radio v-model="client.isUnionMember" label="1">是</el-radio>
                <el-radio v-model="client.isUnionMember" label="0">否</el-radio>
            </el-form-item>
            <el-form-item label="备注">
                <el-input
                    v-model="client.remark"
                    type="textarea"
                    rows="7"
                    :maxlength="200"
                    :minlength="0"
                    show-word-limit
                />
            </el-form-item>
            <el-form-item>
                <el-button
                    type="primary"
                    @click="onSubmit"
                >
                    提交
                </el-button>
                <router-link
                    class="ml10"
                    :to="{
                        name: 'partner-list',
                    }"
                >
                    <el-button>返回</el-button>
                </router-link>
            </el-form-item>
        </el-form>
    </el-card>
</template>

<script>
import { mapGetters } from 'vuex';

export default {
    name: 'PartnerAdd',
    data() {
        return {
            client: {
                id:     '',
                name:   '',
                email:  '',
                servingBaseUrl:'',
                isUnionMember:'0',
                remark: '',
                code:   '',
                status: '',
            },
            rules: {
                name: [
                    { required: true, message: '请输入合作者名称', trigger: 'blur' },
                ],
            },
            clientId: '',
        };
    },

    computed: {
        ...mapGetters(['userInfo']),
    },
    created() {
        if (this.$route.query.id) {
            this.clientId = this.$route.query.id;
            this.getClientById(this.$route.query.id);
        }

    },
    methods: {


        onSubmit() {
            this.$refs.client.validate(async (valid) => {
                if (valid) {
                    const { code } = await this.$http.post({
                        url:  '/partner/save',
                        data: {
                            id:        this.client.id,
                            name:      this.client.name,
                            email:     this.client.email,
                            remark:    this.client.remark,
                            createdBy: this.userInfo.nickname,
                            servingBaseUrl:this.client.servingBaseUrl,
                            isUnionMember:this.client.isUnionMember,
                            code:      this.client.code,
                        },
                    });

                    if (code === 0) {
                        setTimeout(() => {
                            this.$message('提交成功!');
                        }, 1000);
                        this.$router.push({
                            name: 'partner-list',
                        });
                    }
                }
            });

        },
        async getClientById(id) {
            const { code, data } = await this.$http.post({
                url:  '/partner/query-one',
                data: {
                    id,
                },
            });

            if (code === 0) {
                this.client.id = data.id;
                this.client.name = data.name;
                this.client.email = data.email;
                this.client.remark = data.remark;
                this.client.servingBaseUrl = data.servingBaseUrl;
                this.client.isUnionMember = data.isUnionMember;
                this.client.code = data.code;
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

.el-form {
    width: 600px;
}
</style>
