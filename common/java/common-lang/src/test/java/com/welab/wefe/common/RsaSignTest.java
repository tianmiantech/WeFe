/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.welab.wefe.common;

import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.common.util.UrlUtil;

import java.nio.charset.StandardCharsets;

/**
 * @author zane
 * @date 2022/6/1
 */
public class RsaSignTest {
    public static void main(String[] args) throws Exception {
        String encode = UrlUtil.encode("LimU4NQz492xhkTl6XQi6XCEdB2GkD4Cvr6wzcMEz0Xk+LbeEOTVSDs23PhhmSAtQSJkfewyutEbL6xg+vfoyN2GBCZ+gMA8wCA4bKoO6E2WMzehjGPdl3/I2IcQG1Lr13yFfgg1MQFXCQPqs8VCD4+taZecztf8CRIt4PXDVBQNc8XXIostUjip6UDwaZ3nCNxK66Y2YBiig1XcPw2pTQ+wWK9q5ERq7sgG2wsKCXZktqiNdn23GadegqejPEaFHzKSDg1zF9siALrak/8SgZQFjw3nEj6tb78PT/fcBAqi9YHOalei6JrdlDhHZPBGWf1Kkf+mp03mXzAwlXRZog==");
        System.out.println(encode);
        String data = UrlUtil.decode("%7B%22data_set_id%22%3A%20%22a3dce2feacc94db097f51d418e952c6e%22%2C%20%22job_id%22%3A%20%22b5158f6e932f484ab4e34e5a5a21cd26%22%2C%20%22version%22%3A%20%2270-103-1645934216379%22%7D");
        System.out.println(data);
        String sign = "LimU4NQz492xhkTl6XQi6XCEdB2GkD4Cvr6wzcMEz0Xk+LbeEOTVSDs23PhhmSAtQSJkfewyutEbL6xg+vfoyN2GBCZ+gMA8wCA4bKoO6E2WMzehjGPdl3/I2IcQG1Lr13yFfgg1MQFXCQPqs8VCD4+taZecztf8CRIt4PXDVBQNc8XXIostUjip6UDwaZ3nCNxK66Y2YBiig1XcPw2pTQ+wWK9q5ERq7sgG2wsKCXZktqiNdn23GadegqejPEaFHzKSDg1zF9siALrak/8SgZQFjw3nEj6tb78PT/fcBAqi9YHOalei6JrdlDhHZPBGWf1Kkf+mp03mXzAwlXRZog==";
        String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhgf8lafU7qcd2xaLHmogxwMg93cS1Z1rrohBFVinkvyE/fOSOj6ghSe7rpG/H/XLdLOq1hBnd+fyOELC3qEqlko5/q+/DYsH81Ld6Nj/p9X/zKS4cIbVBKJ9D7W2RLNhocrAiraw8n0TdaaCohhJQxX627gz6dEkI791D3mxCSOALkD9wBwVifJ4h7EfAByfqdh85tyjV02QNiv0WgHcnGBNSYs6Ny26tsoSMBaN1Q4hMmpZEPc34XAScl5c6/uWResI1xjFj7RxQ0MpAOlJckNRV/uCphg7e9bxZtSK/9uCI8Uzw1eimIlR23kgBCdQU+98Z2FKjdnqcNoAPObZ4QIDAQAB";
        boolean success = RSAUtil.verify(
                data.getBytes(StandardCharsets.UTF_8),
                RSAUtil.getPublicKey(publicKey),
                sign
        );
        System.out.println(success);
    }
}
