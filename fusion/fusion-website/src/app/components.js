import Element from 'element-ui';
import TableEmptyData from '../components/TableEmptyData';
import uploader from 'vue-simple-uploader';
import CGrid from 'vue-cheetah-grid';

export default {
    install (Vue) {
        Vue.use(Element);

        Vue.prototype.$ELEMENT = { size: 'small' };

        Vue.component('TableEmptyData', TableEmptyData);
        Vue.use(uploader);
        Vue.use(CGrid);
    },
};
