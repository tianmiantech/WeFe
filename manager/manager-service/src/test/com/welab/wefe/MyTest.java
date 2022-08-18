package com.welab.wefe;

import java.security.PrivateKey;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.fastjson.JSONObject;
import com.webank.cert.mgr.db.dao.CertDao;
import com.webank.cert.toolkit.utils.CertUtils;
import com.webank.cert.toolkit.utils.KeyUtils;
import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.entity.manager.CertInfo;
import com.welab.wefe.common.data.mongodb.entity.manager.CertKeyInfo;
import com.welab.wefe.common.data.mongodb.repo.CertInfoRepo;
import com.welab.wefe.manager.service.ManagerService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ManagerService.class })
public class MyTest {

    @Autowired
    private CertInfoRepo repo;
    @Autowired
    private CertDao certDao;

//    @Test
    public void test() {
        PageOutput<CertInfo> result = repo.findCertList(null, null, true, false, 2, 0, 10);
        System.out.println(JSONObject.toJSONString(result));
    }

//    @Test
    public void test1() {
        CertInfo issuerCertInfo = certDao.findCertById("68a3248030a148468470e020e90bb33d");
        System.out.println(issuerCertInfo.getPkId());
    }

    // 导出证书
    @Test
    public void testExportCertToFile() throws Exception {
        String certId = "8400440d668d4c228dce2e0d3fd13674";
        CertInfo certVO = certDao.findCertById(certId);
        System.out.println(certVO.getPkId());
//        CertUtils.writeDer(CertUtils.convertStrToCert(certVO.getCertContent()), "root.cer");
        CertUtils.writeCrt(CertUtils.convertStrToCert(certVO.getCertContent()), "ca.crt");
    }

    // 导出私钥
//    @Test
    public void exportKey() throws Exception {
        // 私钥ID
        String userKeyId = "120c5633b404497690b33a8aa5143b5b";
        CertKeyInfo keyVo = certDao.findCertKeyById(userKeyId);
//        PrivateKey privateKey = KeyUtils.getRSAPrivateKey(keyVo.getKeyPem());
        PrivateKey privateKey = KeyUtils.getRSAKeyPair(keyVo.getKeyPem()).getPrivate();
        CertUtils.writeToPKCS8File(privateKey, "welab1_pri.key");
    }

}
