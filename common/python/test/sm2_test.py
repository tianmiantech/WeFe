from common.python.utils import sm2_utils
from base64 import b64decode, b64encode

if __name__ == '__main__':
    # test
    public_key = '04cd230582da29f9f883f4b64bf3da717acc4918c9b1b7ab2e34f1d7863a6d00bd0b860eb7a3848dec4cdb6dc1ad87d8940a6ae84b5688c2685be40220ccf03750'
    private_key = 'b721c84fe6052c0c784bd6cb2278a95dc30e8374d6f8240ad5e0563c6e44c897'

    data = 'abc123'
    sign_value = sm2_utils.sign_with_sm3(data, private_key, public_key)
    print(f'sign_value:{sign_value}')
    print(b64encode(bytes.fromhex(sign_value)).decode('utf-8'))
    result = sm2_utils.verify_with_sm3(data, public_key, sign_value)
    print(result)
