# -*- coding:utf-8 -*-

from gmssl.sm4 import CryptSM4, SM4_ENCRYPT, SM4_DECRYPT
import binascii


class SM4CBC:
    """
     国密sm4加解密
    """

    def __init__(self):
        self.crypt_sm4 = CryptSM4()
        # 偏移量
        self.iv = b'\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00'

    def str_to_hex_str(self, hex_str):
        """
            字符串转hex
        Parameters
        ----------
        hex_str

        Returns
        -------

        """
        hex_data = hex_str.encode('utf-8')
        str_bin = binascii.unhexlify(hex_data)
        return str_bin.decode('utf-8')

    def encrypt(self, encrypt_key, value):
        """
            国密sm4加密
        Parameters
        ----------
        encrypt_key sm4加密key
        value 待加密的字符串

        Returns
        -------
            sm4加密后的hex值
        """
        crypt_sm4 = self.crypt_sm4
        crypt_sm4.set_key(encrypt_key, SM4_ENCRYPT)
        encrypt_value = crypt_sm4.crypt_cbc(self.iv, value.encode())  # bytes类型
        return encrypt_value.hex()

    def decrypt(self, decrypt_key, encrypt_value):
        """
            国密sm4解密
        Parameters
        ----------
        decrypt_key sm4加密key
        encrypt_value 待解密的hex值

        Returns
        -------
            原字符串
        """
        crypt_sm4 = self.crypt_sm4
        crypt_sm4.set_key(decrypt_key, SM4_DECRYPT)
        d_value = crypt_sm4.crypt_cbc(self.iv, bytes.fromhex(encrypt_value))  # bytes类型
        return self.str_to_hex_str(d_value.hex())