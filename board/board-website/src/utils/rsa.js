import JSEncrypt from 'jsencrypt';
import Encrypt from 'encryptlong';
 
// const publicKey = '';
const privateKey = '';

export default {
  /* JSEncrypt加密 */
  rsaPublicData(publicKey, data) {
    const jsencrypt = new JSEncrypt();

    jsencrypt.setPublicKey(publicKey);
    // 如果是对象/数组的话，需要先JSON.stringify转换成字符串
    const result = jsencrypt.encrypt(data);

    return result;
  },
  /* JSEncrypt解密 */
  rsaPrivateData(data) {
    const jsencrypt = new JSEncrypt();

    jsencrypt.setPrivateKey(privateKey);
    // 如果是对象/数组的话，需要先JSON.stringify转换成字符串
    const result = jsencrypt.encrypt(data);

    return result;
  },

  /* 加密 */
  encrypt(publicKey, data) {
    const PUBLIC_KEY = publicKey;

    const encryptor = new Encrypt();

    encryptor.setPublicKey(PUBLIC_KEY);
    // 如果是对象/数组的话，需要先JSON.stringify转换成字符串
    const result = encryptor.encryptLong(data);

    return result;
  },
  /* 解密 - PRIVATE_KEY - 验证 */
  decrypt(data) {
    const PRIVATE_KEY = privateKey;

    const encryptor = new Encrypt();

    encryptor.setPrivateKey(PRIVATE_KEY);
    // 如果是对象/数组的话，需要先JSON.stringify转换成字符串
    const result = encryptor.decryptLong(data);

    return result;
  },
};
