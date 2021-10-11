import Element from 'element-ui';
import TableEmptyData from '../components/TableEmptyData';

export default {
    install (Vue) {
        Vue.use(Element);

        Vue.prototype.$ELEMENT = { size: 'small' };

        Vue.component('TableEmptyData', TableEmptyData);
    },
};
