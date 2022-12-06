<template>
    <div :class="['layout-sider', { isCollapsed: vData.isCollapsed }]">
        <div v-if="!vData.isInQianKun" class="heading-logo">
            <img style="width:60px;" src="../../assets/images/x-logo.png" alt="">
            <p
                class="member-name mt10"
                :title="userInfo.nickname"
            >
                {{ userInfo.nickname }}
            </p>
        </div>
        <div v-if="vData.isInQianKun" class="qk-aside-header">
            {{ appInfo?.appName || '联邦管理平台' }}
        </div>
        <el-menu
            router
            class="menu-list"
            :unique-opened="true"
            :default-active="vData.defaultActive"
            :default-openeds="vData.defaultOpens"
            :collapse="vData.isCollapsed"
        >
            <menu-temp :menus="vData.menuList" />
        </el-menu>
        <div
            class="collapse-btn"
            @click="changeCollapsed"
        >
            {{ vData.isCollapsed ? '' : '收起' }}
            <el-icon class="icon mr10">
                <elicon-expand v-if="vData.isCollapsed" />
                <elicon-fold v-else />
            </el-icon>
        </div>
    </div>
</template>

<script src="./LayoutSide.js"></script>

<style lang="scss">
    .layout-sider {
        // height: calc(100vh - 40px);
        // padding-bottom: 40px;
        background: $nav-background;
        position: relative;
        // flex:none;
        display: flex;
        flex-direction: column;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        border-right: 1px solid #f0f2f5;
        .member-name {
            color: #57638a;
        }
        .qk-aside-header {
            height: 48px;
            line-height: 48px;
            padding: 0px 12px;
            display: inline-block;
            align-items: center;
            font-size: 14px;
            border-bottom: 1px solid rgb(228, 235, 241);
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            width: 100%;
        }
        &.isCollapsed{
            // width: 70px;
            // .member-name{
            //     font-size:0;
            //     height: 0;
            //     width: auto;
            // }
            .member-avatar{
                width: 50px !important;
                height: 50px !important;
                line-height: 50px !important;
            }
            .qk-aside-header,
            .manager-menu--collapse{width: 70px;}
            .manager-menu-item,
            .manager-sub-menu__title,
            .manager-sub-menu__icon-arrow {font-size:0;}
            .icon {margin-left: 5px; font-size: 16px;}
        }
        .heading-logo {
            color: #fff;
            text-align: center;
            padding:20px 10px 10px;
            background: $nav-background;
        }
        .collapse-btn {
            color: $nav-text-color;
            // position: absolute;
            // bottom: 0;
            width: 100%;
            font-size: 12px;
            text-align: right;
            padding:10px 20px;
            cursor: pointer;
            .icon {
                font-size: 14px;
                vertical-align:middle;
                color: $nav-text-color;
            }
            &:hover{color: $nav-active-color;}
        }
        // .member-name{
        //     height: 16px;
        //     line-height: 16px;
        //     font-size: 14px;
        //     overflow: hidden;
        //     text-overflow: ellipsis;
        //     white-space: nowrap;
        //     font-weight: bold;
        //     width: 180px;
        // }
        .manager-menu {
            background: $nav-background;
            border: 0;
        }
        .menu-list{
            // height: calc(100vh - 160px);
            // overflow: scroll;
            overflow-y: auto;
            overflow-x: hidden;
            flex: 1;
        }
        .manager-menu-item,
        .manager-sub-menu__title {color: $nav-text-color;}
        .manager-menu-item-group__title {display: none;}
        .manager-menu-item {
            &.is-active {
                color: $nav-menu-item-active;
                &:after {
                    content: "";
                    position: absolute;
                    top: 0;
                    right: 0;
                    bottom: 0;
                    background: $nav-background-active;
                    width: 3px;
                }
            }
        }
        .manager-menu-item,
        .manager-sub-menu__title {
            height: var(--tm-menu-height);
            line-height: var(--tm-menu-height);
            font-size: 13px;
            &:hover,
            &:focus {
                color: $nav-menu-item-hover;
                background: $nav-background-active;
            }
            &.is-active {
                color: $nav-active-color;
                background: $nav-background-active;
            }
            .icon {
                font-size: 14px;
                margin-right: 10px;
                margin-top: -2px;
            }
        }
        .manager-sub-menu {
            &.is-active {
                .manager-sub-menu__title {
                    color: $nav-menu-item-active;
                    &:after {
                        content: "";
                        position: absolute;
                        top: 0;
                        right: 0;
                        bottom: 0;
                        background: $nav-background-active;
                        width: 3px;
                    }
                }
                .manager-menu-item:after {display: none;}
            }
        }
        .manager-menu:not(.manager-menu--collapse) {width: 200px;}
    }
    .sidebar-menu-popover{
        .manager-menu--popup{
            background: $nav-background;
            color: $nav-text-color;
        }
        .manager-menu-item-group__title{display: none;}
        .manager-menu-item{
            color: $nav-text-color;
            &.is-active{
                color: $nav-menu-item-active;
                background: $sub-menu-hover-bg;
            }
            &:hover,
            &:focus {
                background: $nav-background-active;
                color: $nav-menu-item-hover;
            }
        }
    }
</style>
