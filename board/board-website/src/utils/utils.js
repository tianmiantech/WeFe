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
