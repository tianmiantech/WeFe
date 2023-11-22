/*!
 * @author claude
 * data transform
 */

/**
 * is empty (empty Object/array/string)
 * @param {*} (obj / array / string)
 */
export const isEmpty = obj => {
    if (!obj || obj === '' || obj == null) return true;

    if (obj.constructor === Array) {
        if (obj.length === 0) return true;
    } else {
        if (Object.keys(obj).length === 0) return true;
    }
    return false;
};

/**
 * deep clone
 * @param {Object} obj
 */
export const deepClone = (obj) => {
    if (obj == null) return obj;
    if (obj instanceof Date) return new Date(obj);
    if (obj instanceof RegExp) return new RegExp(obj);
    if (typeof obj !== 'object') return obj;

    let target = new obj.constructor();

    Object.keys(target).forEach((key) => {
        if (obj.hasOwnProperty(key)) {
            target = deepClone(obj[key]);
        }
    });

    return target;
};

/**
 * val is or not an array
 *
 * @param {Object} val
 * @returns {boolean} true/false
 */
export const isArray = (val) => {
    return toString.call(val) === '[object Array]';
};

function forEach(obj, fn) {
    // Don't bother if no value provided
    if (obj === null || typeof obj === 'undefined') {
        return;
    }

    // Force an array if not already something iterable
    if (typeof obj !== 'object') {
        /*eslint no-param-reassign:0*/
        obj = [obj];
    }

    if (isArray(obj)) {
        // Iterate over array values
        for (let i = 0, l = obj.length; i < l; i++) {
            fn.call(null, obj[i], i, obj);
        }
    } else {
        // Iterate over object keys
        for (const key in obj) {
            if (Object.prototype.hasOwnProperty.call(obj, key)) {
                fn.call(null, obj[key], key, obj);
            }
        }
    }
}

/**
 * deep merge two objects (deep coverage)
 * @param {target} obj
 * @param {origin} obj
 */
export const deepMerge = (/* obj1, obj2, obj3, ... */...args) => {
    /* for (const key in origin) {
        target[key] = target[key] && origin[key] !== undefined && origin[key].toString() === '[object Object]' ? deepMerge(target[key], origin[key]) : target[key] = origin[key];
    }
    return target; */
    const result = {};

    function assignValue(val, key) {
        if (typeof result[key] === 'object' && typeof val === 'object') {
            result[key] = deepMerge(result[key], val);
        } else {
            result[key] = val;
        }
    }

    for (let i = 0, l = args.length; i < l; i++) {
        forEach(args[i], assignValue);
    }
    return result;
};
