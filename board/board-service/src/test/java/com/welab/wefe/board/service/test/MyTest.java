package com.welab.wefe.board.service.test;

import java.security.PrivateKey;

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
@SpringBootTest(classes = { BoardService.class })
public class MyTest {

    @Autowired
    private CertOperationService certOperationService;

    // 导出证书
    @Test
    public void testExportCertToFile() throws Exception {
        String certId = "9c29d8f181b34d3f8eb5cb07ca6e43c6";
        CertInfoMysqlModel certVO = certOperationService.queryCertInfoById(certId);
        CertUtils.writeDer(CertUtils.convertStrToCert(certVO.getCertContent()), "yinlian2.cer");
        CertUtils.writeCrt(CertUtils.convertStrToCert(certVO.getCertContent()), "xxx.crt");
    }

    // 导出私钥
    @Test
    public void exportKey() throws Exception {
        // 私钥ID
        String userKeyId = "120c5633b404497690b33a8aa5143b5b";
        CertKeyInfoMysqlModel keyVo = certOperationService.queryCertKeyInfoById(userKeyId);
        PrivateKey privateKey = KeyUtils.getRSAPrivateKey(keyVo.getKeyPem());
        CertUtils.writeToPKCS8File(privateKey, "welab1_pri.key");
    }
}
