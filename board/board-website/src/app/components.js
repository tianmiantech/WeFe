/**
 * install 3rd party plugins
 */
import bus from './eventHub';
import http from '@src/http/http';
import CGrid from 'vue-cheetah-grid';
import elComponents from './element-import';
import { VirtualList } from 'vue3-virtual-list';
import locale from 'element-plus/lib/locale/lang/zh-cn';
import VueSimpleUploader from '@comp/VueSimpleUploader/install';
import CommonComponents from '@comp/components-install.js';
import VueDragResize from '@comp/DragResize/install.js';
import { dateFormat, dateLast } from '@src/utils/date';
import { timeFormat } from '@src/utils/timer';
import '@js/polyfill/requestAnimationFrame';
import '@styles/base.scss';
import Konva from 'konva';

export default {
    install (Vue) {
        // global properties for date
        Vue.config.globalProperties.dateLast = dateLast;
        Vue.config.globalProperties.dateFormat = dateFormat;
        Vue.config.globalProperties.timeFormat = timeFormat;
        Vue.config.globalProperties.$http = http;
        Vue.config.globalProperties.$bus = bus;

        // register element-plus components on demand
        for (const component in elComponents) {
            Vue.use(elComponents[component]);
        }

        // set default language & size
        Vue.config.globalProperties.$ELEMENT = {
            size: 'small',
            locale,
        };

        const messageBox = elComponents['ElMessageBox'];

        Vue.config.globalProperties.$message =
            elComponents['ElMessage'];
        Vue.config.globalProperties.$alert = messageBox.alert;
        Vue.config.globalProperties.$confirm = messageBox.confirm;
        Vue.config.globalProperties.$prompt = messageBox.prompt;
        Vue.config.globalProperties.$notify =
            elComponents['ElNotification'];

        CommonComponents.forEach(component => {
            Vue.component(component.name, component);
        });
        Vue.component('VirtualList', VirtualList);
        Vue.use(VueSimpleUploader);
        Vue.use(VueDragResize);
        Vue.use(CGrid);
        Vue.use(Konva);
    },
};
