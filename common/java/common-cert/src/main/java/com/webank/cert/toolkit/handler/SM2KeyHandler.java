/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.cert.toolkit.handler;

import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.crypto.keypair.SM2KeyPair;


/**
 * SM2KeyHandler
 *
 * @author tangxianjie
 * @date 2021/08/20
 */
public class SM2KeyHandler {
    public static CryptoKeyPair generateSM2KeyPair() {
        CryptoKeyPair cryptoKeyPair = new SM2KeyPair().generateKeyPair();
        return cryptoKeyPair.createKeyPair(cryptoKeyPair.getHexPrivateKey());
    }

    public static CryptoKeyPair generateSM2KeyPair(String hexPrivateKey) {
        return new SM2KeyPair().createKeyPair(hexPrivateKey);
    }

}














