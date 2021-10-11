
# 前台地址

## 运行环境

nodejs 10.x

```ssh
// mac
brew install node@10
// windows
官网下载 nodejs 10.x
```

## 安装依赖

```ssh
npm install
```

## 启动命令

```ssh
// 启动服务，执行多次会开启多个端口监听， 默认 8080端口
npm run dev
npm run build
npm run build -- dev // dev 对应了 .env 中的配置
```

## 开发配置

.env 用于不同环境打包配置
package.json 中 context 用于配置打包后的文件夹名称, 留空则不创建文件夹
proxy.js 用于开发时代理配置

## 其他

[前端框架](https://element.eleme.cn/#/zh-CN)

[皮肤样式](https://sleek.tafcoder.com/form-advance.html)

## vscode 编辑器(格式化)配置项

编辑器插件: prettier, eslint, vetur, JavaScript (ES6) code snippets, tabnine

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
