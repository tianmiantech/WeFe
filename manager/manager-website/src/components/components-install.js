/**
 * install components automatically
*/

const requireAll = context => context.keys().map(context);
const files = require.context('./Common', false, /.vue$/);
const components = [...requireAll(files)];

export default components.map(component => component.default);
