package com.welab.wefe.mpc.psi.sdk;

import java.util.List;

import com.welab.wefe.mpc.config.CommunicationConfig;

public interface Psi {

    public static final String ECDH_PSI = "ECDH_PSI";
    public static final String DH_PSI = "DH_PSI";

    List<String> query(CommunicationConfig config, List<String> clientIds) throws Exception;

}
