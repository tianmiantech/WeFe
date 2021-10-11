/**
 * forked from https://github.com/simple-uploader/vue-uploader for vue3
 */

import Uploader from './components/uploader.vue';
import UploaderBtn from './components/btn.vue';
import UploaderDrop from './components/drop.vue';
import UploaderUnsupport from './components/unsupport.vue';
import UploaderList from './components/list.vue';
import UploaderFiles from './components/files.vue';
import UploaderFile from './components/file.vue';

function install (Vue) {
    if (install.installed) {
        return;
    }
    Vue.component(Uploader.name, Uploader);
    Vue.component(UploaderBtn.name, UploaderBtn);
    Vue.component(UploaderDrop.name, UploaderDrop);
    Vue.component(UploaderUnsupport.name, UploaderUnsupport);
    Vue.component(UploaderList.name, UploaderList);
    Vue.component(UploaderFiles.name, UploaderFiles);
    Vue.component(UploaderFile.name, UploaderFile);
}

const uploader = {
    version: '0.7.6',
    install,
    Uploader,
    UploaderBtn,
    UploaderDrop,
    UploaderUnsupport,
    UploaderList,
    UploaderFiles,
    UploaderFile,
};

export default uploader;
