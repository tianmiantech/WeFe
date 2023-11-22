#　业务背景：
　　由于手机号属于用户的隐私字段，因此为了加强保护用户个人隐私，Wefe的相关模块 （如Board、Fusion、Manager、Serving）已支持
以SM4方式加密手机号再保存到数据库；而相关模块对手机号的加密保存是默认打开的，如果想关闭此功能则在各个模块里相关的配置文件里
把配置项：encrypt.phone.number.open的值设置false即可，如encrypt.phone.number.open=false。而在打开的时，则要求各模块系统
要求提供SM4的密钥，而密钥通过各模块的配置项：sm4.secret.key指定。因此Wefe系统提供了SM4Util.jar的SM4小工具，方便用户快速生成
密钥、加密和解密。
##　SM4小工具使用方法如下：
```
生产密钥：
java -jar .\SM4Util.jar generateKey

加密:
java -jar .\SM4Util.jar encrypt 密钥 明文

解密：
java -jar .\SM4Util.jar decrypt 密钥 密文
```