/*
* component map
* component name map to the server interface
* component must has name property
*/

const paramComponents = {};
const resultComponents = {};
const helpComponents = {};
const requireAll = context => context.keys().map(context);
const formFiles = require.context('./', true, /params\.vue$/);
const resultFiles = require.context('./', true, /result\.vue$/);
const helpFiles = require.context('./', true, /help\.vue$/);
const formKeyArr = formFiles.keys().map(item => item.match(/.\/(\S*)\/.*vue/)[1]);
const resultKeyArr = resultFiles.keys().map(item => item.match(/.\/(\S*)\/.*vue/)[1]);
const helpKeyArr = helpFiles.keys().map(item => item.match(/.\/(\S*)\/.*vue/)[1]);
const formFilesArr = requireAll(formFiles);
const resultFilesArr = requireAll(resultFiles);
const helpFilesArr = requireAll(helpFiles);
const componentsList = {};

for (const index in formKeyArr) {

    if (formKeyArr.hasOwnProperty(index)) {
        const file = formFilesArr[index];
        const { name } = file.default;

        if (!componentsList[name]) {
            componentsList[name] = {
                params: true,
                result: false,
            };
        }
        paramComponents[`${name}-params`] = file.default;
    }
}

for (const index in resultKeyArr) {

    if (resultKeyArr.hasOwnProperty(index)) {
        const file = resultFilesArr[index];
        const { name } = file.default;

        if (!componentsList[name]) {
            componentsList[name] = {
                params: false,
                result: true,
            };
        } else {
            componentsList[name].result = true;
        }
        resultComponents[`${name}-result`] = file.default;
    }
}

for (const index in helpKeyArr) {

    if (helpKeyArr.hasOwnProperty(index)) {
        const file = helpFilesArr[index];
        const { name } = file.default;

        helpComponents[`${name}-help`] = file.default;
    }
}

export {
    componentsList,
    paramComponents,
    resultComponents,
    helpComponents,
};
