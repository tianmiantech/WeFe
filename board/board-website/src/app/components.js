/**
 * install 3rd party plugins
 */
import Konva from 'konva';
import bus from './eventHub';
import http from '@src/http/http';
import CGrid from 'vue-cheetah-grid';
import { VirtualList } from 'vue3-virtual-list';
import { elIcons, elComponents } from './element-import';
import locale from 'element-plus/lib/locale/lang/zh-cn';
import VueSimpleUploader from '@comp/VueSimpleUploader/install';
import CommonComponents from '@comp/components-install.js';
import VueDragResize from '@comp/DragResize/install.js';
import { dateFormat, dateLast } from '@src/utils/date';
import { timeFormat } from '@src/utils/timer';
import '@js/polyfill/requestAnimationFrame';
import '@styles/base.scss';

export default {
    install (app) {
        // global properties for date
        app.config.globalProperties.dateLast = dateLast;
        app.config.globalProperties.dateFormat = dateFormat;
        app.config.globalProperties.timeFormat = timeFormat;
        app.config.globalProperties.$http = http;
        app.config.globalProperties.$bus = bus;

        // register element-plus components on demand
        for (const component in elComponents) {
            app.use(elComponents[component]);
        }
        for (const component in elIcons) {
            app.component(`elicon${component}`, elIcons[component]);
        }

        // set default language & size
        app.config.globalProperties.$ELEMENT = {
            size: 'small',
            locale,
        };

        const messageBox = elComponents['ElMessageBox'];

        app.config.globalProperties.$message = elComponents['ElMessage'];
        app.config.globalProperties.$alert = messageBox.alert;
        app.config.globalProperties.$confirm = messageBox.confirm;
        app.config.globalProperties.$prompt = messageBox.prompt;
        app.config.globalProperties.$notify = elComponents['ElNotification'];

        CommonComponents.forEach(component => {
            app.component(component.name, component);
        });
        app.component('VirtualList', VirtualList);
        app.component('Konva', Konva);
        app.use(VueSimpleUploader);
        app.use(VueDragResize);
        app.use(CGrid);
    },
};
