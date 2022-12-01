/**
 * 组件内部公共的函数
 */
 const { $message } = window.$app.config.globalProperties;


/**
 * psi组件自定义方法，分割点验证函数
 */
export const psiCustomSplit = (array) => {
    try{
        if(!array.length){
            $message.error('分割点不能为空');
            return false;
        }
        if(new Set(array).size !== array.length){
            $message.error('分割点不能重复');
            return false;
        }
        const reg = /^-?\d+(\.\d+)?$/;


        for(const i of array){
            const v = parseFloat(i);

            if(!reg.test(i+'')){
                $message.error(`请输入正确的数字，错误项${i}`);
                return false;
            }

            if(v< 0 || v> 1){
                $message.error(`请输入0-1之间返回的数字，错误项${i}`);
                return false;
            }
        }
        return true;
    }catch{
        $message.error(`未知错误`);
        return false;
    }
};

/**
 * 将空格换行回车替换掉
 */
export const replace = (value,repalceStr='') => {
    try{
        return value.replace(/[\s\r\n]/g, repalceStr);
    } catch{
        return value;
    }
};
