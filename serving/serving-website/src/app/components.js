import Element from 'element-ui';
import ElDialog from '../components/dialog';
import TableEmptyData from '../components/TableEmptyData';

export default {
    install (Vue) {
        Vue.use(Element);
        Vue.use(ElDialog);

        Vue.prototype.$ELEMENT = { size: 'small' };

        Vue.component('TableEmptyData', TableEmptyData);
    },
};
