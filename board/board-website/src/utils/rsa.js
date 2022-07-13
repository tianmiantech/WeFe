import JSEncrypt from 'jsencrypt';
import Encrypt from 'encryptlong';
 
const publicKey = `MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDhgFTRBgcYsXqQpUVlVGn22TTv
SRHwNx1gsFWlp+/qzX4Jy6Qj0Q7pgxZjaFY7VgGxyRQH6cDCpUxon2R1ODQ0N6JH
hJltLiCRju5FPPFgYlQKiQyLbqwyE8o1lnLaY8TNFfgRBY2tfmWB/UWvZQ665ema
F+GCVrO/5pJI+d1k0wIDAQAB`;
const privateKey = `MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAOGAVNEGBxixepCl
RWVUafbZNO9JEfA3HWCwVaWn7+rNfgnLpCPRDumDFmNoVjtWAbHJFAfpwMKlTGif
ZHU4NDQ3okeEmW0uIJGO7kU88WBiVAqJDIturDITyjWWctpjxM0V+BEFja1+ZYH9
Ra9lDrrl6ZoX4YJWs7/mkkj53WTTAgMBAAECgYEAyGE8/TCaBbtP6lCyRcSpFI0W
mLsZkZeoJ08KDFYav08y/IlUpe8TjTTLJDKGzdszTkQb5Jw2icBREXbx0afL2h9y
dtK0cbtvB6BapyJaNZc0kB3xRxHstoj3Xc4Vlmgnq/2asUdTgTAEVObxExqQgiJL
yAi04c5RP1Rf5e4FpcECQQD5tRWobLCxzqMqxNqp640am6LrfjLYTuA+5B55S0tL
6wGYJDPVuNPHiEIbFSD9KzpNEt/2UfGRnkHHtUQRj0JpAkEA5y8WlSzgAU05ElXM
tD/5gDCA/sUP1G+IxejANZAya3gnAjhyf4RZsKaK+AUUn4rlNz3Bwp9utsHpExpy
O+ZN2wJAZ7OVkAG7i8xGJQ/lw6WITyGNkoExenWfWV8BbNwhJCEv41A9mqeMhBQJ
aBfpQFrAdpu9GR3E1fXEWbzkzOoS6QJBAJrDcinBVeBpMvisSZKtdTi0v9ZOU++S
Dwz2aJ4y/x3k3LBm23e0QVq50zSAKjM4B4S4qhrBIpvkKkToomqpHZsCQAGvDY0P
dVX1TwSfEGKGLbpCHpmi+M5AhV+ThVQIOBK6Kip5VpJNbC10OCZ5uZsMP5QQxK/5
ijajA7ZJeD2vYxQ=`;

export default {
  /* JSEncrypt加密 */
  rsaPublicData(data) {
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
  encrypt(data) {
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
