import authClient from '../http/authClient';

export const getPermission = () => {
    return authClient().requestPermission();
};

export const getSystemLicense = () => {
    return authClient().requestSystemLicenseByAppCode();
};
