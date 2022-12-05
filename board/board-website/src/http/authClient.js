import AuthClient from '@tianmiantech/auth';
import { appCode } from '../utils/constant';
import iamHttp from './iamHttp';


console.log('appCode()', appCode());
const authClient = () => {
    return new AuthClient(appCode(), iamHttp, undefined, undefined, 'iam');
};

export default authClient;
