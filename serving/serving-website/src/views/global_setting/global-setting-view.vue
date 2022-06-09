<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form :model="form">
            <el-row :gutter="100">
                <el-col :span="12">
                    <el-form-item label="系统ID：">
                        {{ form.identity_info.member_id }}
                    </el-form-item>
                    <el-form-item
                        :rules="[{required: true, message: '名称必填!'}]"
                        label="成员名称："
                    >
                        <el-input
                            v-model="form.identity_info.member_name"
                            placeholder="仅支持中文名称"
                            :disabled="is_update"
                        />
                    </el-form-item>

<!--                    <el-form-item-->
<!--                        v-if="is_display"-->
<!--                        label="公钥："-->
<!--                    >-->
<!--                        <el-input-->
<!--                            v-model="form.identity_info.rsa_public_key"-->
<!--                            type="textarea"-->
<!--                            :disabled="is_update"-->
<!--                            autosize-->
<!--                        />-->
<!--                    </el-form-item>-->

                    <el-form-item
                        label="Serving服务地址："
                    >
                        <el-input
                            v-model="form.identity_info.serving_base_url"
                            placeholder=""
                            :disabled="is_update"
                        />
                    </el-form-item>

                    <el-form-item
                        label="Union服务地址："
                    >
                        <el-input
                            v-model="form.wefe_union.intranet_base_uri"
                            placeholder=""
                            :disabled="is_update"
                        />
                    </el-form-item>

                    <el-form-item label="运行模式：">
                        <el-radio v-model="mode" :label="0" @change="modeChange">独立模式</el-radio>
                        <el-radio v-model="mode" :label="1" @change="modeChange">联邦模式</el-radio>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row :gutter="100">
                <el-col :span="12">
                    <el-button
                        v-loading="loading"
                        class="save-btn"
                        type="primary"
                        size="medium"
                        :disabled="is_update"
                        @click="update"
                    >
                        更新
                    </el-button>

                    <el-button
                        v-loading="is_reset"
                        class="save-btn"
                        type="primary"
                        size="medium"
                        @click="resetKey"
                    >
                        重置公私钥
                    </el-button>
                </el-col>
            </el-row>
        </el-form>
    </el-card>
</template>

<script>
    import { mapGetters } from 'vuex';

    export default {
        data() {
            return {
                loading:    false,
                is_update:  false,
                is_display: false,
                is_reset : false,
                mode:'',
                // model
                form:       {
                    identity_info:{
                        member_id:       '',
                        member_name:     '',
                        serving_base_url:'',
                        mode:'',
                    },
                    wefe_union:{
                        intranet_base_uri:'',
                    }
                },

            };
        },
        computed: {
            ...mapGetters(['userInfo']),
        },
        created() {
            this.getData();
        },
        methods: {
            async modeChange(){
                if(this.mode === 1){
                    this.form.identity_info.mode = 'union';
                }
                else{
                    this.form.identity_info.mode = 'standalone';
                }
            },
            async getData() {
                this.loading = true;
                const { code, data } = await this.$http.post({
                    url: '/global_config/detail',
                    data:{
                        "groups":['identity_info','wefe_union']
                    }
                });

                this.is_update = !this.userInfo.admin_role;
                this.is_display = this.userInfo.admin_role;

                if (code === 0) {
                    this.form = data;
                    if(data.identity_info.mode === 'union'){
                        this.mode = 1;
                    }
                    else{
                        this.mode = 0;
                    }
                }
                this.loading = false;
            },
            async update() {
                this.loading = true;
                const { code } = await this.$http.post({
                    url:  '/global_config/update',
                    data: {
                        "groups":this.form
                    },
                });
                if (code === 0) {
                    this.$message.success('保存成功!');
                    this.$router.push({ name: 'global-setting-view' });
                }
                this.loading = false;
            },
            async resetKey(){
                this.is_reset = true;
                const { code } = await this.$http.get({
                    url:  '/system/reset_rsa_key',
                });

                if (code === 0) {
                    this.$message.success('操作成功!');
                }
                this.is_reset = false;
            },
        },
    };
</script>

<style lang="scss" scoped>

    .save-btn {
        width: 100px;
    }
</style>
