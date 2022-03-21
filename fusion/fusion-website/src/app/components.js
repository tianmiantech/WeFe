import Element from 'element-ui';
import TableEmptyData from '../components/TableEmptyData';
import PasswordStrength from '../components/PasswordStrength';
import uploader from 'vue-simple-uploader';
import CGrid from 'vue-cheetah-grid';

export default {
    install (Vue) {
        Vue.use(Element);

        Vue.prototype.$ELEMENT = { size: 'small' };

        Vue.component('PasswordStrength', PasswordStrength);
        Vue.component('TableEmptyData', TableEmptyData);
        Vue.use(uploader);
        Vue.use(CGrid);
    },
};
