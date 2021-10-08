/*!
 * @author claude
 */

import Vue from 'vue';
import Vuex from 'vuex';
import base from './modules/base';

Vue.use(Vuex);

export default _ => {
    return new Vuex.Store({
        // strict: true,
        modules: {
            base: base(),
        },
    });
};
