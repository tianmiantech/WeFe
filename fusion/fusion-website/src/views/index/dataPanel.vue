<template>
    <div class="layer">
        <el-card class="box-card1">
            <div
                slot="header"
                class="clearfix"
            >
                <span>任务总览</span>
            </div>
            <div class="box-content">
                <div
                    class="pointer"
                    @click="jumpToTask('')"
                >
                    <p class="count">{{ taskOverInfo.all_count }}</p>
                    <p class="text">全部任务</p>
                </div>
                <div
                    class="pointer"
                    @click="jumpToTask('')"
                >
                    <p class="count">{{ taskOverInfo.promoter_count }}</p>
                    <p class="text">我发起的</p>
                </div>
                <div
                    class="pointer"
                    @click="jumpToTask('')"
                >
                    <p class="count">{{ taskOverInfo.provider_count }}</p>
                    <p class="text">我协同的</p>
                </div>
                <div
                    class="pointer"
                    @click="jumpToTask('')"
                >
                    <p class="count">{{ taskOverInfo.pending_count }}</p>
                    <p class="text">待审核</p>
                </div>
                <div
                    class="pointer"
                    @click="jumpToTask('')"
                >
                    <p class="count">{{ taskOverInfo.running_count }}</p>
                    <p class="text">运行中</p>
                </div>
            </div>
        </el-card>
        <div class="bottom-area">
            <el-card class="box-card2">
                <div
                    slot="header"
                    class="box-header"
                >
                    <span>数据样本</span>
                    <router-link to="data-set-list">
                        去添加
                    </router-link>
                </div>
                <div class="box-content">
                    <div
                        class="pointer"
                        @click="jumpTo('filter-list')"
                    >
                        <p class="count">{{ dataOverviewInfo.bloom_filter_count }}</p>
                        <p class="text">过滤器</p>
                    </div>
                    <div
                        class="pointer"
                        @click="jumpTo('data-set-list')"
                    >
                        <p class="count">{{ dataOverviewInfo.data_set_count }}</p>
                        <p class="text">数据集</p>
                    </div>
                </div>
            </el-card>
            <el-card class="box-card3">
                <div
                    slot="header"
                    class="box-header"
                >
                    <span>合作伙伴</span>
                    <router-link to="partner-list">
                        去添加
                    </router-link>
                </div>
                <div class="box-list">
                    <div
                        v-for="item in partnerList"
                        :key="item.id"
                    >
                        <p class="name">{{ item.name }}</p>
                        <p class="time">{{ item.created_time | dateFormat }}加入</p>
                    </div>
                </div>
            </el-card>
        </div>
    </div>
</template>

<script>
export default {
    data() {
        return {
            taskOverInfo:     {},
            dataOverviewInfo: {},
            partnerList:      [],
        };
    },
    created() {
        this.getTaskOverviewData();
        this.getDataOverviewData();
        this.getPartnerData();
    },
    methods: {
        async getTaskOverviewData() {
            const { code, data } = await this.$http.get('/task/overview');

            if(code === 0) {
                this.taskOverInfo = data;
            }
        },
        async getDataOverviewData() {
            const { code, data } = await this.$http.get('/data_source/overview');

            if(code === 0) {
                this.dataOverviewInfo = data;
            }
        },
        async getPartnerData() {
            const { code, data } = await this.$http.get('/partner/paging');

            if(code === 0) {
                this.partnerList = data.list;
            }
        },
        jumpTo(routeName) {
            this.$router.replace({
                name: routeName,
            });
        },
    },
};
</script>

<style lang="scss">
@mixin flexItem() {
    display: flex;
    justify-content: space-between;
}
@mixin flexBox() {
    div {
        flex: 1;
        padding: 30px 0;
        border: 1px solid #f5f5f5;
        text-align: center;
        box-shadow: 1px 1px 5px 1px #f1f1f1;
        p.text {
            margin-top: 10px;
            color: #555;
            font-size: 14px;
        }
        p.count {
            font-size: 18px;
            font-weight: bold;
            color: #5088fc;
        }
    }
    div:first-child {
        margin-right: 10px;
    }
}
.box-card1 {
    .box-content {
        display: flex;
        justify-content: space-around;
        @include flexBox;
        div:nth-child(2), div:nth-child(3), div:nth-child(4) {
            margin-right: 10px;
        }
        .pointer {
            cursor: pointer;
        }
    }
}
.bottom-area {
    margin-top: 10px;
    @include flexItem;
    .pointer {
        cursor: pointer;
    }
    .el-card {
        flex: 1;
        .box-content {
            display: flex;
            justify-content: space-around;
            @include flexBox;
        }
    }
    .el-card:first-child {
        margin-right: 10px;
    }
    .box-header {
        @include flexItem;
        align-items: center;
        a {
            font-size: 12px;
        }
    }
    .box-card3 {
        height: 300px;
        .el-card__body {
            height: 100%;
            padding-bottom: 70px;
        }
        .box-list {
            overflow-y: auto;
            width: 100%;
            height: 100%;
            div {
                @include flexItem;
                font-size: 14px;
                margin-bottom: 5px;
                border-bottom: 1px solid #ebeef5;
                padding: 7px 0;
                .name {
                    color: #1b233b;
                }
                .time {
                    color: #999;
                }
            }
        }
    }
}
</style>
