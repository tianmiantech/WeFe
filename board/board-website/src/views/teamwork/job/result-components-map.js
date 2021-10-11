/*
* component map
* component name map to the server interface
*/

const requireAll = context => context.keys().map(context);
const files = require.context('./components', false, /\.vue$/);
const components = requireAll(files);
const modules = {};

components.forEach(component => {
    modules[component.default.name] = component.default;
});

export default modules;
