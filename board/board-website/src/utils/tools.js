import http from '@src/http/http';

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

const getTokenName = () => {
    const qiankun_env = window?.$app?._QIANKUN_ENV;
    const tokenName = `iam-${process.env.HOST_ENV || qiankun_env || 'dev'}-x-user-token`;

    return tokenName;
};

const getFileName = (disposition) => {
    let fileName = '';

    if (disposition) {
        const reg = /filename=(.*)/;

        fileName = reg.exec(disposition)[1].trim();
    }
    return fileName;
};

const downLoadFileTool = async(reqUrl, params) => {
    try {
        const resp = await http.get({
            url:          reqUrl,
            params,
            responseType: 'blob',
            getResponse:  true,
        });

        const binaryData = [];

        binaryData.push(resp.data);
        const url = window.URL.createObjectURL(new Blob(binaryData));
        const link = document.createElement('a');

        document.body.appendChild(link);
        link.download = getFileName(resp.response.headers['content-disposition']);
        link.href = url;
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);
    } catch(e) {
        console.log(e);
    }
};

export {
    throttle,
    getTokenName,
    downLoadFileTool,
};
