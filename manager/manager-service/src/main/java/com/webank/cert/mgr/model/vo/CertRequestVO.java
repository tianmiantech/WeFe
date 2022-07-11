package com.webank.cert.mgr.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author wesleywang
 */
@Data
public class CertRequestVO implements Serializable {

    private Long pkId;

    private String userId;

    private Long subjectKeyId;

    private String certRequestContent;

    private Long pCertId;

    private String subjectOrg;

    private String subjectCN;

    private Boolean issue;

    private String pCertUserId;

}
