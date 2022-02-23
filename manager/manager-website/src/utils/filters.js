/**
 * global filters
 */

import { dateFormat, dateLast } from '@src/utils/date';
import { timeFormat } from '@src/utils/timer';

export default app => {
    app.filter('dateFormat', dateFormat);
    app.filter('dateLast', dateLast);
    app.filter('timeFormat', timeFormat);
};
