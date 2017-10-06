<style scoped>
    @import 'styles/common.css';
</style>
<style scoped>
    @menu-bg-color: #464c5b;
    .layout{
        border: 1px solid #d7dde4;
        background: #f5f7f9;
        position: relative;
        border-radius: 4px;
        overflow: hidden;
    }
    .layout-breadcrumb{
        padding: 10px 15px 0;
    }
    .layout-content{
        height: 100%;
        margin: 15px;
        overflow: hidden;
        background: #fff;
        border-radius: 4px;
    }
    .layout-content-main{
        padding: 10px;
        height: 100%;
        overflow-y: auto;
    }
    .layout-menu-left{
        background: @menu-bg-color;
        position: fixed;
        height: 100%;
    }
    .layout-header{
        height: 45px;
        background: #fff;
        box-shadow: 0 1px 1px rgba(0,0,0,.1);
    }
    .layout-ceiling-main a{
        color: #9ba7b5;
    }
    .layout-hide-text .layout-text{
        display: none;
    }
    .ivu-col{
        transition: width .2s ease-in-out;
    }
    .ivu-menu-dark.ivu-menu-vertical {
        .ivu-menu-item,.ivu-menu-submenu-title {
            color: #9ea7b4;
            &:hover {
                background: @menu-bg-color;
            }
        }
    }
    .ivu-menu-dark {
        background: @menu-bg-color;
    }
</style>
<template>
    <div class="layout" :class="{'layout-hide-text': leftMenuWidth < 180}">
        <div>
            <div :style="{width: leftMenuWidth + 'px'}" class="layout-menu-left">
                <Menu :active-name="activeName" theme="dark" width="auto" @on-select="toPage">
                    <Menu-item name="/metrics">
                        <Icon type="flash" :size="iconSize"></Icon>
                        <span class="layout-text">接口监控</span>
                    </Menu-item>
                    <Menu-item name="/report">
                        <Icon type="bug" :size="iconSize"></Icon>
                        <span class="layout-text">问题报警</span>
                    </Menu-item>
                </Menu>
            </div>
            <div :style="{'margin-left': leftMenuWidth + 'px', height: mainHeight + 'px'}" class="layout-main">
                <div class="layout-header">
                    <i-button type="text" @click="toggleClick">
                        <Icon type="navicon" size="32"></Icon>
                    </i-button>
                    <h1 :style="{display: 'inline', 'vertical-align': 'sub'}">Web监控系统原型</h1>
                </div>
                <div class="layout-content">
                    <div class="layout-content-main">
                        <router-view></router-view>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script>
    const EXPENDED_MENU_WIDTH = 180;
    const COLLAPSED_MENU_WIDTH = 70;
    export default {
        data () {
            return {
                leftMenuWidth: EXPENDED_MENU_WIDTH,
                mainHeight: 800
            }
        },
        computed: {
            activeName() {
                return this.$route.path;
            },
            iconSize () {
                return this.leftMenuWidth === EXPENDED_MENU_WIDTH ? 16 : 24;
            },
            showMenuText () {
                return this.leftMenuWidth === EXPENDED_MENU_WIDTH;
            }
        },
        mounted() {
            window.onresize = () => {
                this.mainHeight = window.innerHeight;
            };
            window.onresize();
        },
        methods: {
            toggleClick () {
                if (this.leftMenuWidth === EXPENDED_MENU_WIDTH) {
                    this.leftMenuWidth = COLLAPSED_MENU_WIDTH;
                } else {
                    this.leftMenuWidth = EXPENDED_MENU_WIDTH;
                }
            },
            toPage(path) {
                this.$router.push(path);
            }
        }
    }
</script>
