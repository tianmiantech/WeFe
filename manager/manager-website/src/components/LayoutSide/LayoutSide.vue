<template>
    <div :class="['layout-sider', { isCollapsed: vData.isCollapsed }]">
        <div class="heading-logo">
            <img style="width:60px;" src="../../assets/images/x-logo.png" alt="">
            <p
                class="member-name mt10"
                :title="userInfo.realname"
            >
                {{ userInfo.realname }}
            </p>
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
            <i :class="['icon mr10', vData.isCollapsed ? 'el-icon-s-unfold' : 'el-icon-s-fold']" />
        </div>
    </div>
</template>

<script src="./LayoutSide.js"></script>

<style lang="scss">
    .layout-sider {
        height: 100vh;
        padding-bottom: 40px;
        background: $nav-background;
        position: relative;
        flex:none;
        &.isCollapsed{
            .member-name{
                font-size:0;
                height: 0;
                width: auto;
            }
            .member-avatar{
                width: 50px !important;
                height: 50px !important;
                line-height: 50px !important;
            }
            .el-menu--collapse{width: 70px;}
            .el-menu-item,
            .el-sub-menu__title,
            .el-sub-menu__icon-arrow{font-size:0;}
            .icon{margin-left: 5px;}
        }
        .heading-logo {
            color: #fff;
            text-align: center;
            padding:20px 10px 10px;
            background: $nav-background;
        }
        .collapse-btn{
            color: $nav-text-color;
            position: absolute;
            bottom: 0;
            width: 100%;
            font-size: 12px;
            text-align: right;
            padding:10px 20px;
            cursor: pointer;
            .icon{
                font-size: 14px;
                vertical-align:middle;
            }
            &:hover{color: #fff;}
        }
        .member-name{
            height: 16px;
            line-height: 16px;
            font-size: 14px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            font-weight: bold;
            width: 180px;
        }
        .el-menu {
            background: $nav-background;
            border: 0;
        }
        .menu-list{
            height: calc(100vh - 160px);
            overflow: auto;
        }
        .el-menu-item,
        .el-sub-menu__title {
            color: #a6aaae;
        }
        .el-menu-item-group__title {
            display: none;
        }
        .el-menu-item {
            &.is-active {
                color: #fff;
                background: #4c536d;
                &:after {
                    content: "";
                    position: absolute;
                    top: 0;
                    right: 0;
                    bottom: 0;
                    background: $nav-background-active;
                    width: 3px;
                }
                i {color: #fff;}
                .icon {
                    color: $nav-background-active;
                }
            }
        }
        .el-menu-item,
        .el-sub-menu__title {
            &:hover,
            &:focus {
                background: #4f566f;
                color: #fff;
                i{color: #fff;}
            }
            i {
                font-size: 16px;
            }
            .icon {
                margin-right: 10px;
                margin-top: -4px;
            }
        }
        .el-sub-menu {
            &.is-active {
                background: #464e6c !important;
                .el-sub-menu__title {
                    color: #fff;
                    &:after {
                        content: "";
                        position: absolute;
                        top: 0;
                        right: 0;
                        bottom: 0;
                        background: $nav-background-active;
                        width: 3px;
                    }
                    i {color: #fff;}
                }
                .sub-menu-list {
                    background: $sub-menu-list_bg;
                }
                .el-menu-item:after {
                    display: none;
                }
            }
        }
        .el-menu:not(.el-menu--collapse) {
            width: 200px;
        }
    }
    .sidebar-menu-popover{
        .el-menu--popup{
            background: $nav-background;
            color: $nav-text-color;
        }
        .el-menu-item-group__title{display: none;}
        .el-menu-item{
            color: $nav-text-color;
            &.is-active{color:#fff;}
            &:hover,
            &:focus {
                background: #464e6c;
                color: #fff;
            }
        }
    }
</style>
