package com.welab.wefe.board.service.test;

import java.io.FileNotFoundException;
import java.security.PrivateKey;

import org.bouncycastle.openssl.PEMKeyPair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.webank.cert.toolkit.utils.CertUtils;
import com.webank.cert.toolkit.utils.KeyUtils;
import com.welab.wefe.board.service.BoardService;
import com.welab.wefe.board.service.database.entity.cert.CertInfoMysqlModel;
import com.welab.wefe.board.service.database.entity.cert.CertKeyInfoMysqlModel;
import com.welab.wefe.board.service.service.CertOperationService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BoardService.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MyTest {

    @Autowired
    private CertOperationService certOperationService;

    // 导出证书
    @Test
    public void testExportCertToFile() throws Exception {
        String certId = "4aee053477fe4afcb363c613729d7aad";
        CertInfoMysqlModel certVO = certOperationService.queryCertInfoById(certId);
        System.out.println(certVO.getId());
//        CertUtils.writeDer(CertUtils.convertStrToCert(certVO.getCertContent()), "wefe.cer");
        CertUtils.writeCrt(CertUtils.convertStrToCert(certVO.getCertContent()), "tianmian.crt");
    }

    // 导出私钥
    @Test
    public void exportKey() throws Exception {
        // 私钥ID
        String userKeyId = "4554f03640bd4a4ab2966b93b9fde205";
        CertKeyInfoMysqlModel keyVo = certOperationService.queryCertKeyInfoById(userKeyId);
        System.out.println(keyVo.getId());
        PrivateKey privateKey = KeyUtils.getRSAPrivateKey(keyVo.getKeyPem());
//        PrivateKey privateKey = KeyUtils.getRSAKeyPair(keyVo.getKeyPem()).getPrivate();
        CertUtils.writeKey(privateKey, "tianmian.key");
//        CertUtils.writeToPKCS8File(privateKey, "tianmian.key");
    }

    @Test
    public void readKey() throws Exception {
        PEMKeyPair pemKeyPair = CertUtils.readKey("tianmian.key");
        System.out.println(pemKeyPair); // writeKey
        PrivateKey privateKey = (PrivateKey) CertUtils.readRSAPrivateKey("tianmian.key"); // writeToPKCS8File
        System.out.println(privateKey);
    }
}
