
/**
 * timeFormat
 * @param {Number} interval seconds
 * @param {String} format
 */
export const timeFormat = (interval, format = 'D天 HH小时MM分钟SS秒') => {
    // const noSpace = /(^\s+)|(\s+$)/g;
    if (interval) {
        const seconds = interval;
        const minutes = Math.floor(seconds / 60);
        const hours = Math.floor(minutes / 60);
        const day = Math.floor(hours / 60);

        const map = {
            D: day,
            H: hours % 24,
            M: minutes % 60,
            S: Math.floor(seconds % 60),
        };

        let regExp = 'DHMS';

        if (map.D === 0) {
            regExp = 'HMS';
            format = format.replace('D天', '');
            if (map.H === 0) {
                regExp = 'MS';
                format = format.replace('HH小时', '');
                if (map.M === 0) {
                    format = format.replace('MM分钟', '');
                }
            }
        }
        return format.replace(new RegExp(`(([${regExp}])+)`, 'g'), (all, t1, t2) => {
            return t1.length > 1 ? `0${map[t2] || 0}`.substr(-2) : (map[t2] || 0);
        });
    } else {
        return '0 分 0 秒';
    }
};
