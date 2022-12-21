package com.welab.wefe.mpc.psi.sdk.pir;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

import com.welab.wefe.mpc.commom.Constants;
import com.welab.wefe.mpc.config.CommunicationConfig;
import com.welab.wefe.mpc.pir.protocol.ro.hf.HashFunction;
import com.welab.wefe.mpc.pir.protocol.ro.hf.Sha256;
import com.welab.wefe.mpc.pir.request.QueryKeysRequest;
import com.welab.wefe.mpc.pir.request.naor.QueryNaorPinkasRandomResponse;
import com.welab.wefe.mpc.pir.request.naor.QueryNaorPinkasResultRequest;
import com.welab.wefe.mpc.pir.request.naor.QueryNaorPinkasResultResponse;
import com.welab.wefe.mpc.psi.sdk.service.PrivateSetIntersectionService;
import com.welab.wefe.mpc.util.DiffieHellmanUtil;
import com.welab.wefe.mpc.util.EncryptUtil;

public class PirQuery {

    public static String query(int targetIndex, List<Object> ids, CommunicationConfig communicationConfig)
            throws Exception {
        BigInteger k = DiffieHellmanUtil.generateRandomKey(1024);
        QueryKeysRequest randomRequest = new QueryKeysRequest();
        randomRequest.setIds(ids);
        randomRequest.setOtMethod(Constants.PIR.NAORPINKAS_OT);
        randomRequest.setRequestId(UUID.randomUUID().toString().replaceAll("-", ""));
        PrivateSetIntersectionService service = new PrivateSetIntersectionService();
        QueryNaorPinkasRandomResponse randomResponse = service.handlePirResult(communicationConfig, randomRequest);
        if (randomResponse.getCode() != 0) {
            throw new Exception(randomResponse.getMessage());
        }
        String uuid = randomResponse.getUuid();
        BigInteger g = DiffieHellmanUtil.hexStringToBigInteger(randomResponse.getG());
        BigInteger p = DiffieHellmanUtil.hexStringToBigInteger(randomResponse.getP());
        BigInteger secret = DiffieHellmanUtil.hexStringToBigInteger(randomResponse.getSecret());

        BigInteger pk = DiffieHellmanUtil.encrypt(g, k, p);
        if (targetIndex != 0) {
            BigInteger c = DiffieHellmanUtil.hexStringToBigInteger(randomResponse.getRandoms().get(targetIndex - 1));
            pk = DiffieHellmanUtil.modDivide(c, pk, p);
        }
        QueryNaorPinkasResultRequest request = new QueryNaorPinkasResultRequest();
        request.setUuid(uuid);
        request.setPk(DiffieHellmanUtil.bigIntegerToHexString(pk));
        QueryNaorPinkasResultResponse response = service.queryNaorPinkasResult(communicationConfig, request);
        if (response.getCode() != 0) {
            throw new Exception(response.getMessage());
        }
        HashFunction hash = new Sha256();
        byte[] key = hash.digest(DiffieHellmanUtil.encrypt(secret, k, p).toByteArray());
        return EncryptUtil.decryptByAES(response.getEncryptResults().get(targetIndex), key);
    }
}
