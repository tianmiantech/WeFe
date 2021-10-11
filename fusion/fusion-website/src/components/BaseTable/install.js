import BaseTable from './BaseTable.vue';

BaseTable.install = (Vue) => {
    Vue.component(BaseTable.name, BaseTable);
};

export default BaseTable;

