package com.welab.wefe;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.webank.cert.mgr.model.vo.CertVO;
import com.webank.cert.mgr.service.CertOperationService;
import com.webank.cert.toolkit.utils.CertUtils;
import com.welab.wefe.manager.service.ManagerService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ManagerService.class })
public class CertOperationServiceTest {

    @Autowired
    private CertOperationService certOperationService;

    @Test
    public void initRoot() throws Exception {
        String commonName = "WeFe_ROOT";
        String organizationName = "Welab Inc.";
        String organizationUnitName = "IT";

        CertVO vo = certOperationService.initRootCert(commonName, organizationName, organizationUnitName);
        System.out.println(vo.getPkId()); // 6f1aeda83661478eab791d952a5bc0ab
        CertUtils.writeCrt(CertUtils.convertStrToCert(vo.getCertContent()), "root.crt");
    }

    @Test
    public void createIssuerCert() throws Exception {
        String rootCertId = "6f1aeda83661478eab791d952a5bc0ab";
        String commonName = "WeFe_CA";
        String organizationName = "Welab Inc.";
        String organizationUnitName = "IT";

        CertVO vo = certOperationService.createIssuerCert(rootCertId, commonName, organizationName,
                organizationUnitName);
        System.out.println(vo.getPkId()); // 13a26ee9df6a4116b946a1c7c11f3e07
        CertUtils.writeCrt(CertUtils.convertStrToCert(vo.getCertContent()), "ca.crt");
    }

    @Test
    public void createUserCert() throws Exception {
        String issuerCertId = "6f1aeda83661478eab791d952a5bc0ab";
        String memberId = "3";
        String certRequestContent = "-----BEGIN CERTIFICATE REQUEST-----\n"
                + "MIICiDCCAXACAQAwQzEQMA4GA1UEAwwHeWlubGlhbjEUMBIGA1UECgwLeWlubGlh\n"
                + "bl9vcmcxGTAXBgNVBAsMEHlpbmxpYW5fdW5pdF9vcmcwggEiMA0GCSqGSIb3DQEB\n"
                + "AQUAA4IBDwAwggEKAoIBAQC2vmfAZhUA4g6nGML2qa2FRU+k5My0EImJzxdi/xSo\n"
                + "lXuCpGcpON6K13aSM3EgbIVGYwdFQT3GoOGIQwqd/Qoq6v52Zi+wjPeBR+rt10VL\n"
                + "mWKo2gHcEmgDl40nF7xjpN65hem00cXtWl3EkEpiWe8AX4HW7DIvQz9+M1hq6RDj\n"
                + "9LMCEAeCkvYyeSGPeAMfnXkAng0eBoNIRruKk+Zq2+Zfu+miiuWx6RtfJAOcqnc3\n"
                + "EZY9XFxAs0C4ymlCTV3fBHGcye0dBnI1uYhqYumSjhH0gd+M7dlhyoq4z6dLKjhK\n"
                + "qmX0t6viV9S1xQMfcKImxDsTp/YJKgangXCFE5JhmsyHAgMBAAGgADANBgkqhkiG\n"
                + "9w0BAQsFAAOCAQEAi+e3BvJAEyLJ5x7tt50s4z+X/UhdrQl3WSpPg5cyAXJsrcZB\n"
                + "Rbmo6cCBoacGSOEYStvpxfUe0USQ1eyQ6NWb4oO1n9oWo7X1hTSGZr99u6cOPHGs\n"
                + "IEzvQ/zpW0Cals7eD1v//GjtbhbP3CBzWHH0gbv22eDEpsRRJ/3KkppTtDdiaFJw\n"
                + "zMdXUcuXz8IfwVRwRpmzOK4K6B+hTCc4jStQdVkBWhRD8LYdknb8ceHUY0CPeRDU\n"
                + "xFXuh+pA11+NBHfl8oHVoKYlur0COBey3qazVKkbzxfxnSsmMvRyvPj/7Vfg4Z0x\n"
                + "fEAdLdBkl8r2ykRU76o63dxR0MuaKbLnmNjoCA==\n"
                + "-----END CERTIFICATE REQUEST-----\n"
                + "";

        CertVO vo = certOperationService.createUserCert(issuerCertId, memberId, certRequestContent);
        System.out.println(vo.getPkId()); // f414d68cfd0c4ed5bd64406e83066b88
        CertUtils.writeCrt(CertUtils.convertStrToCert(vo.getCertContent()), "yinlian.crt");
    }
}
