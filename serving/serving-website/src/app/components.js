import Element from 'element-ui';
import ElDialog from '../components/dialog';
import PasswordStrength from '../components/PasswordStrength.vue';
import TableEmptyData from '../components/TableEmptyData.vue';

export default {
    install (Vue) {
        Vue.use(Element);
        Vue.use(ElDialog);

        Vue.prototype.$ELEMENT = { size: 'small' };

        Vue.component('PasswordStrength', PasswordStrength);
        Vue.component('TableEmptyData', TableEmptyData);
    },
};
