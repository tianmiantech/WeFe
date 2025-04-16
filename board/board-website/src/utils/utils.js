/**
 * 截取小数点
 */
export function turnDemical(str, number){
    try {
        if(/^-?[A-Za-z]+$/.test(str)){
            return str;
        }
        const reg = new RegExp(`^-?[1-9]?\\d+(\\.\\d{0,${number}})?`);
        const [result] = (str+'').match(reg) || [];

        return result;

    } catch (error) {
        return str;
    }
}

/**
 * 处理全局数字精度
 * 浮点型保留4位小数
 * 百分比保留2位小数
 */
export function dealNumPrecision(number, digit = 4) {
    try {
        if(/^-?[A-Za-z]+$/.test(number) || Number.isInteger(number)){
            return number;
        } else {
            const numStr = number.toString();
            if (numStr.indexOf('e') > -1) {
                const digitNum = Number('1'.padEnd(digit, '0'));
                const floorNum = Math.floor(number * digitNum) / digitNum;
                if (floorNum === Infinity) {
                    return number;
                }
                return Math.floor(number * digitNum) / digitNum;
            } else {
                const reg = new RegExp(`^-?[1-9]?\\d+(\\.\\d{0,${digit}})?`);
                const [result] = (number + '').match(reg) || [];
                return result;
            }
        }
    } catch (error) {
        return number;
    }
}
