<template>

    <el-card class="page" shadow="never">

        <h2 class="title">新增客户</h2>

        <el-form ref="form" :model="client" label-width="88px" >
            <el-form-item label="客户名称">
                <el-input v-model="client.name"></el-input>
            </el-form-item>
            <el-form-item label="客户邮箱">
                <el-input v-model="client.email"></el-input>
            </el-form-item>
            <el-form-item label="IP 地址">
                <el-input v-model="client.ip_add"></el-input>
            </el-form-item>
            <el-form-item label="公钥">
                <el-input v-model="client.pub_key" type="textarea"></el-input>
            </el-form-item>
            <el-form-item label="备注">
                <el-input v-model="client.remark" type="textarea"></el-input>
            </el-form-item>

            <el-form-item>
                <router-link
                    :to="{
                            name: 'client-list',
                        }"
                >

                    <el-button type="primary" @click="onSubmit">提交</el-button>
                </router-link>
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


export default {
    name: "client-add",
    data() {
        return {
            client: {
                name: '',
                pubKey: '',
                email: '',
                ipAdd: '',
                remark: '',
            },
        }
    },

    computed: {
        ...mapGetters(['userInfo']),
    },
    created() {
        if (this.$route.query.id) {
            this.getClientById(this.$route.query.id)
        }

    },
    methods: {
        async onSubmit() {
            const {code} = await this.$http.post({
                url: '/client/save',
                data: {
                    name: this.client.name,
                    email: this.client.email,
                    ipAdd: this.client.ip_add,
                    pubKey: this.client.pub_key,
                    remark: this.client.remark,
                    createdBy: this.userInfo.nickname,
                },
            });

            if (code === 0) {
                this.$message('提交成功!');
            }
        },


        async getClientById(id) {
            const {code, data} = await this.$http.post({
                url: '/client/query-one',
                data: {
                    id: id,
                },

            });
            if (code === 0) {
                this.client = {
                    ...data,
                };
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

.el-form-item__content{
    margin: 5px;
}
</style>
