import ElDialog from './src/component.vue';

/* istanbul ignore next */
ElDialog.install = function(Vue) {
  Vue.component(ElDialog.name, ElDialog);
};

export default ElDialog;
