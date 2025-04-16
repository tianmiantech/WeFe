import http from '@src/http/http';

const getTokenName = () => {
    const { _QIANKUN_ENV: qiankun_env } = window.$app || {};
    const tokenName = `iam-${qiankun_env || process.env.HOST_ENV || 'dev'}-x-user-token`;

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
    getTokenName,
    downLoadFileTool,
};
