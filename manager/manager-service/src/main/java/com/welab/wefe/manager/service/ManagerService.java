package com.welab.wefe.manager.service;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.welab.wefe.common.util.Md5;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.config.ApiBeanNameGenerator;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author yuxin.zhang
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DruidDataSourceAutoConfigure.class,
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class})
@EnableScheduling
@ComponentScan(
        basePackages = {"com.welab.wefe.common.data.mongodb"},
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
