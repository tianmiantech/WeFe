/**
 * install components automatically
*/

const requireAll = context => context.keys().map(context);
const files = require.context('./Common', false, /.vue$/);
const charts = require.context('./Charts', false, /.vue$/);
const components = [...requireAll(files), ...requireAll(charts)];

export default components.map(component => component.default);
