package com.webank.cert.mgr.config;

import com.webank.cert.toolkit.service.CertService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wesleywang
 */
@Configuration
public class CertBeans {

    @Bean
    public CertService getCertService(){
        return new CertService();
    }


}
