package com.welab.wefe.gateway.test;

import com.welab.wefe.common.wefe.enums.GatewayProcessorType;

public class BoardHttpProcessorTest {
    public static void main(String[] args) throws Exception {
        String response = Client.callLocalGateway(
                GatewayProcessorType.boardHttpProcessor,
                "{'a':'b'}"
        );
        System.out.println("response====" + response);
    }
}
