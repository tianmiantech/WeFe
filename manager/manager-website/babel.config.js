module.exports = {
    presets: [
        [
            '@babel/preset-env',
            {
                useBuiltIns: 'entry',
                modules: false,
                corejs: 3,
            },
        ],
    ],
    plugins: [
        '@babel/plugin-transform-runtime',
        [
            'import',
            {
                libraryName: 'element-plus',
                customName(name) {
                    name = name.slice(3);
                    return `element-plus/lib/components/${name}`;
                },
                customStyleName(name) {
                    name = name.slice(3);
                    // return `element-plus/lib/components/${name}/style` for scss
                    return `element-plus/lib/components/${name}/style`;
                },
            },
        ],
    ],
};
