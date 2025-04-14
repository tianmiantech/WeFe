<template>
    <div class="layout-sider">
        <div
            v-if="!isInQianKun"
            class="heading-logo"
        >
            <img
                width="50"
                class="logo"
                src="../../assets/images/favicon.png"
            >
        </div>
        <div
            v-if="isInQianKun"
            class="qk-aside-header"
        >
            {{ appInfo.appName || '数据融合平台' }}
        </div>
        <el-menu
            router
            class="menu-list"
            :unique-opened="true"
            :default-active="defaultActive"
            :default-openeds="defaultOpens"
            :collapse="isCollapsed"
            @select="menuSelected"
        >
            <menu-temp :menus="menuList" />
        </el-menu>
    </div>
</template>

<script src="./LayoutSide.js"></script>

<style lang="scss">
    .layout-sider {
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
            .el-menu--collapse{width: 70px;}
            .el-menu-item,
            .el-sub-menu__title,
            .el-sub-menu__icon-arrow {font-size:0;}
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
        .el-menu {
            background: $nav-background;
            border: 0;
        }
        .menu-list{
            // height: calc(100vh - 160px);
            overflow-y: auto;
            overflow-x: hidden;
            flex: 1;
        }
        .el-menu-item,
        .el-submenu__title {color: $nav-text-color;}
        .el-menu-item-group__title {display: none;}
        .el-menu-item {
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
        .el-menu-item,
        .el-submenu .el-menu-item,
        .el-submenu__title {
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
        .el-sub-menu {
            &.is-active {
                .el-submenu__title {
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
                .el-menu-item:after {display: none;}
            }
        }
        .el-menu:not(.el-menu--collapse) {width: 200px;}
    }
    .sidebar-menu-popover{
        .el-menu--popup{
            background: $nav-background;
            color: $nav-text-color;
        }
        .el-menu-item-group__title{display: none;}
        .el-menu-item{
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
