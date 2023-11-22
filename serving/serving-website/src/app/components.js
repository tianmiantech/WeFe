import Element from 'element-ui';
import uploader from 'vue-simple-uploader';
import ElDialog from '../components/dialog';
import PasswordStrength from '../components/PasswordStrength.vue';
import TableEmptyData from '../components/TableEmptyData.vue';
import JsonViewer from 'vue-json-viewer';
import 'vue-json-viewer/style.css';

export default {
    install (Vue) {
        Vue.use(Element);
        Vue.use(ElDialog);
        Vue.use(JsonViewer);
        Vue.use(uploader);

        Vue.prototype.$ELEMENT = { size: 'small' };

        Vue.component('PasswordStrength', PasswordStrength);
        Vue.component('TableEmptyData', TableEmptyData);
    },
};
