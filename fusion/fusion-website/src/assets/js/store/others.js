/*!
 * @author claude
 */

const files = require.context('../../../modules', true, /store\/store\.js$/);

const modules = {};

files.keys().forEach(key => {
    const moduleName = key.replace('/store/store.js', '').substring(2);

    modules[moduleName] = files(key).default;
});

export default modules;
