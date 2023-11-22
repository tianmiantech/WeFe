/* throttle */

function throttle(callback, delay = 300, duration = 200) {
    const _this = this;
    const timer = this.timer;

    let begin = new Date().getTime();

    return function() {
        const current = new Date().getTime();

        clearTimeout(timer);
        if (current - begin >= duration) {
            callback();
            begin = current;
        } else {
            _this.timer = setTimeout(function () {
                callback();
            }, delay);
        }
    };
}

export {
    throttle,
};
