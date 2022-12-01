import gridSearchParams from '../assets/js/const/gridSearchParams';
export const toCamelCase = (str) =>
    str
        .split('_')
        .reduce((acc, cur, index) =>
            index === 0
                ? cur
                : acc + String.prototype.toUpperCase.call(cur[0]) + cur.slice(1),
        );
export const mapGridName = (key) =>
    gridSearchParams.xgboost
        .concat(gridSearchParams.lr)
        .find((each) => each.key === key || each.key === toCamelCase(key))
        .label;
