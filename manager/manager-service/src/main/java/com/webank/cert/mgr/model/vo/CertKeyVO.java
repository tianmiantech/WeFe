package com.webank.cert.mgr.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author wesleywang
 */
@Data
public class CertKeyVO implements Serializable {

    private Long pkId;

    private String userId;

    private String keyAlg;

    private String keyPem;
}
