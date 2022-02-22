<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form :inline="true" class="demo-form-inline">
            <el-form-item label="资源类型：">
                <el-select v-model="vData.search.dataResourceType" placeholder="请选择资源类型" clearable>
                    <el-option v-for="item in vData.dataResourceTypeList" :key="item.value" :label="item.label" :value="item.value"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="methods.getTagsList">
                    查询
                </el-button>
            </el-form-item>
            <el-form-item>
                <el-button
                    plain
                    type="primary"
                    native-type="button"
                    @click="methods.addKeywords"
                    class="mb10"
                >
                    +新增关键词
                </el-button>
            </el-form-item>
        </el-form>

        <el-table
            v-loading="vData.loading"
            style="width: 700px;"
            :data="vData.list"
            stripe
            border
        >
            <el-table-column label="关键词" prop="tag_name" />
            <el-table-column label="资源类型" prop="data_resource_type" />
            <el-table-column label="操作">
                <template v-slot="scope">
                    <el-button
                        type="primary"
                        @click="methods.updateTag($event, scope.row)"
                    >
                        编辑
                    </el-button>
                    <el-button
                        type="danger"
                        @click="methods.deleteTag($event, scope.row)"
                    >
                        删除
                    </el-button>
                </template>
            </el-table-column>
        </el-table>

        <el-dialog
            :title="vData.tagId ? '编辑' : '新增'"
            v-model="vData.dialogKeywords"
            custom-class="card-dialog"
            destroy-on-close
            width="400px"
            top="30vh"
        >
            <el-form class="flex-form" @submit.prevent>
                <el-form-item label="标签名称" style="width: 330px;" required>
                    <el-input v-model.trim="vData.tagName"></el-input>
                </el-form-item>
                <el-form-item v-if="!vData.tagId" label="资源类型" style="width: 330px;" required>
                    <el-select v-model="vData.dataResourceType" placeholder="请选择资源类型" clearable>
                        <el-option v-for="item in vData.dataResourceTypeList" :key="item.value" :label="item.label" :value="item.value"></el-option>
                    </el-select>
                </el-form-item>
                <el-button
                    class="mt10"
                    type="primary"
                    :disabled="!vData.tagName"
                    style="margin-left: 80px;width:100px;"
                    @click="methods.submitKeywords"
                >
                    提交
                </el-button>
            </el-form>
        </el-dialog>
    </el-card>
</template>

<script>
    import {
        reactive,
        onMounted,
        nextTick,
        getCurrentInstance,
    } from 'vue';
    import table from '@src/mixins/table.js';

    export default {
        inject: ['refresh'],
        mixins: [table],
        setup() {
            const { ctx, appContext } = getCurrentInstance();
            const { $http, $confirm } = appContext.config.globalProperties;
            const vData = reactive({
                loading:       true,
                getListApi:    '/data_resource/default_tag/query',
                requestMethod: 'post',
                search:        {
                    dataResourceType: '',
                },
                list:                 [],
                dialogKeywords:       false,
                tagName:              '',
                tagId:                '',
                dataResourceType:     'TableDataSet',
                dataResourceTypeList: [
                    {
                        label: 'TableDataSet',
                        value: 'TableDataSet',
                    },
                    {
                        label: 'ImageDataSet',
                        value: 'ImageDataSet',
                    },
                ],
            });

            const methods = {
                async getTagsList() {
                    await ctx.getList();
                },
                addKeywords() {
                    vData.dialogKeywords = true;
                },
                async submitKeywords($event) {
                    const params = {
                        tagName:          vData.tagName,
                        dataResourceType: vData.dataResourceType,
                    };

                    if(vData.tagId) {
                        params.tagId = vData.tagId;
                    }
                    vData.loading = true;
                    const { code } = await $http.post({
                        url:      vData.tagId ? '/data_resource/default_tag/update' : '/data_resource/default_tag/add',
                        data:     params,
                        btnState: {
                            target: $event,
                        },
                    });

                    nextTick(() => {
                        if(code === 0) {
                            vData.dialogKeywords = false;
                            vData.tagName = '';
                            vData.tagId = '';
                            ctx.refresh();
                        }
                        vData.loading = false;
                    });
                },
                async updateTag($event, tag) {
                    vData.dialogKeywords = true;
                    vData.tagName = tag.tag_name;
                    vData.tagId = tag.id;
                },
                async deleteTag($event, tag) {
                    $confirm('确定要删除该标签吗?', '警告', {
                        type:              'warning',
                        cancelButtonText:  '取消',
                        confirmButtonText: '确定',
                    })
                        .then(async _ => {
                            const { code } = await $http.post({
                                url:  '/data_resource/default_tag/delete',
                                data: {
                                    tagId: tag.id,
                                },
                                btnState: {
                                    target: $event,
                                },
                            });

                            nextTick(() => {
                                if(code === 0) {
                                    setTimeout(() => {
                                        ctx.getList();
                                    }, 1000);
                                }
                            });
                        });
                },
            };

            onMounted(async () => {
                await ctx.getList();
                vData.loading = false;
            });

            return {
                vData,
                methods,
            };
        },
    };
</script>

<style lang="scss" scoped>
    .tag-list{
        li{
            float:left;
            width: 20%;
            min-width: 100px;
            margin:20px 20px 0 0;
        }
    }
    .el-tag{cursor: pointer;}
</style>
