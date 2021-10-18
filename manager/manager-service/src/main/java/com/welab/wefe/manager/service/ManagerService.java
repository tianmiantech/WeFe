package com.welab.wefe.manager.service;

import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.config.ApiBeanNameGenerator;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Author Jervis
 * @Date 2020-05-18
 **/
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableScheduling
@ComponentScan(
        nameGenerator = ApiBeanNameGenerator.class,
        basePackageClasses = {
                Launcher.class,
                ManagerService.class
        }
)
public class ManagerService implements ApplicationContextAware {

    /**
     * spring上下文环境
     */
    public static ApplicationContext CONTEXT = null;


    public static void main(String[] args) {

        Launcher.instance()
                .apiPackageClass(ManagerService.class)
                .checkSessionTokenFunction((api, annotation, token) -> CurrentAccount.get() != null)
                .launch(ManagerService.class, args);

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        CONTEXT = applicationContext;
    }
}
