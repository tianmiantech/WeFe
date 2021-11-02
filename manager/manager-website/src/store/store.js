/*!
 * @author claude
 * global store
 */

import { createStore } from 'vuex';
import base from './modules/base';

export default _ => {
    return createStore({
        // strict: true,
        modules: {
            base: base(),
        },
    });
};
