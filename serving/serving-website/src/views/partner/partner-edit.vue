<template>
    <el-card
        v-loading="loading"
        class="page"
        shadow="never"
    >
        <h2 class="title">编辑合作者</h2>

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
                    placeholder="建议格式：[公司简称]-[手机号]-[日期]"
                    :maxlength="60"
                    :minlength="4"
                    show-word-limit
                />
            </el-form-item>
            <el-form-item
                label="Serving地址"
                prop="email"
            >
                <el-input v-model="client.servingBaseUrl" />
            </el-form-item>
            <el-form-item
                label="联邦成员："
                prop="isUnionMember"
            >
                <el-radio
                    v-model="client.isUnionMember"
                    :label="1"
                >
                    是
                </el-radio>
                <el-radio
                    v-model="client.isUnionMember"
                    :label="0"
                >
                    否
                </el-radio>
            </el-form-item>

            <el-form-item
                label="状态："
                prop="status"
            >
                <el-radio
                    v-model="client.status"
                    :label="1"
                >
                    正常
                </el-radio>
                <el-radio
                    v-model="client.status"
                    :label="0"
                >
                    禁用
                </el-radio>
            </el-form-item>

            <el-form-item label="备注">
                <el-input
                    v-model="client.remark"
                    type="textarea"
                    rows="5"
                    :maxlength="300"
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
    name: 'PartnerEdit',
    data() {
        const validateStatus = (rule, value, callback) => {
            if (this.client.status === '') {
                return callback(new Error('请选择合作者状态'));
            } else {
                callback();
            }
        };
        return {
            loading: false,
            client:  {
                id:     '',
                name:   '',
                email:  '',
                remark: '',
                code:   '',
                isUnionMember:'',
                servingBaseUrl:'',
                status: '',
            },
            rules: {
                name: [
                    { required: true, message: '请输入合作者名称', trigger: 'blur' },
                ],
                isUnionMember: [
                    { required: true, message: '是否是联邦成员', trigger: 'change' },
                ],
                status: [
                    { required: true, validator: validateStatus, trigger: 'change' },
                ],
            },
            clientId: '',
        };
    },

    computed: {
        ...mapGetters(['userInfo']),
    },
    async created() {

        this.loading = true;
        if (this.$route.query.id) {
            this.clientId = this.$route.query.id;
            await this.getPartnerById(this.$route.query.id);
        }
        this.loading = false;
    },
    methods: {


        onSubmit() {
            this.$refs.client.validate(async (valid) => {
                if (valid) {
                    const { code } = await this.$http.post({
                        url:  '/partner/update',
                        data: {
                            id:        this.client.id,
                            name:      this.client.name,
                            email:     this.client.email,
                            remark:    this.client.remark,
                            isUnionMember: this.client.isUnionMember,
                            servingBaseUrl : this.client.servingBaseUrl,
                            updatedBy: this.userInfo.nickname,
                            status:    this.client.status,
                        },
                    });

                    if (code === 0) {
                        setTimeout(() => {
                            this.$message('提交成功!');
                        }, 1000);
                    }
                }
            });

        },


        async getPartnerById(id) {
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
                this.client.code = data.code;
                this.client.isUnionMember = data.is_union_member?1:0;
                this.client.servingBaseUrl = data.serving_base_url;
                this.client.status = data.status;
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
