# board-website

board-website 是一个基于 vue 开发的用户友好的可视化操作界面。

# 项目的打包与启动

**安装依赖**

基于 nodejs 10+ 环境安装项目依赖，[nodejs 官方下载地址](http://nodejs.cn/download/)。

```ssh
# 在 board-website 目录下运行
npm install
```

**开发配置**

.env 用于不同环境打包配置
package.json 中 context 用于配置打包后的文件夹名称, 留空则不创建文件夹
vue.config.js 用于开发时代理配置, target 为接口代理地址

**项目启动**

```
// 启动服务，执行多次会开启多个端口监听， 默认从 8080 端口累加
npm run dev
```

**项目打包**

```
# default 表示使用 /.env 文件中对应的上下文地址
npm run build -- default
```

# 参考文档

[前端框架 ElementPlus](https://element-plus.gitee.io/#/zh-CN)

# 开发规范

Visual Studio Code 代码格式化配置项。

编辑器插件: prettier, eslint, vetur, JavaScript (ES6) code snippets, tabnine。

```json
{
    "editor.formatOnSave": false, // 必选
    "editor.codeActionsOnSave": {
        "source.fixAll.eslint": true
    },
    "editor.defaultFormatter": "esbenp.prettier-vscode",
    "eslint.validate": [
        "javascript",
        "javascriptreact",
        "react",
        "html",
        "vue",
    ],
    "emmet.syntaxProfiles": {
        "postcss": "css",
        "vue-html": "html",
        "vue": "html",
    },
    "vetur.format.defaultFormatter.html": "js-beautify-html",
    "vetur.format.defaultFormatter.ts": "vscode-typescript",
    "vetur.format.defaultFormatter.js": "vscode-typescript",
    "vetur.format.options.tabSize": 4,
    "vetur.format.options.useTabs": false,
    "vetur.format.scriptInitialIndent": true,
    "vetur.format.styleInitialIndent": true,
    "[javascript]": {
        "editor.defaultFormatter": "esbenp.prettier-vscode"
    },
    "[json]": {
        "editor.defaultFormatter": "vscode.json-language-features"
    },
    "[html]": {
        "editor.defaultFormatter": "vscode.html-language-features"
    },
    "[css]": {
        "editor.defaultFormatter": "esbenp.prettier-vscode"
    },
    "[less]": {
        "editor.defaultFormatter": "esbenp.prettier-vscode"
    },
    "[scss]": {
        "editor.defaultFormatter": "esbenp.prettier-vscode"
    },
}
```