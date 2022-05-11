/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.welab.wefe.board.service.scheduled;

import com.welab.wefe.board.service.api.project.project.CloseProjectApi;
import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.board.service.database.entity.OperationLogMysqlModel;
import com.welab.wefe.board.service.database.entity.base.AbstractMySqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectMySqlModel;
import com.welab.wefe.board.service.database.repository.GlobalConfigRepository;
import com.welab.wefe.board.service.database.repository.ProjectRepository;
import com.welab.wefe.board.service.service.ProjectService;
import com.welab.wefe.common.TimeSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * Demo 环境的定时任务
 *
 * @author zane
 * @date 2021/12/13
 */
@Component
@Lazy(false)
public class OnlineDemoScheduledService {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Config config;
    @Autowired
    private GlobalConfigRepository globalConfigRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProjectService projectService;

    /**
     * 清理体验者产生的过多无效数据
     */
    @Scheduled(fixedDelay = 600_000, initialDelay = 60_000)
//    @Scheduled(fixedDelay = 5_000, initialDelay = 1_000)
    public void clean() {
        if (!config.isOnlineDemo()) {
            return;
        }
        LOG.info("开始 demo 环境数据清理...");

        /**
         * 清理 project
         * 1. 查询出我方非管理员创建的项目
         * 2. 检查 project 下的最后 job 启动时间，如果超过10天，则关闭。
         */
        for (ProjectMySqlModel project : projectRepository.findCreatedByThisMemberButNotAdminAccountBefore10DaysAgo()) {
            Date jobLastStartTime = projectRepository.getJobLastStartTime(project.getProjectId());
            if (jobLastStartTime != null) {
                long days = TimeSpan.fromMs(System.currentTimeMillis() - jobLastStartTime.getTime()).toDays();
                // 如果活动时间小于10天，则不关闭。
                if (days < 10) {
                    continue;
                }
            }

            CloseProjectApi.Input input = new CloseProjectApi.Input();
            input.setProjectId(project.getProjectId());
            try {
                projectService.closeProject(input, true);
            } catch (Exception e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            }

        }

        /**
         * 清理 project
         * 1. 太久没使用
         * 2. 无流程或已关闭的项目删掉
         *
         * 注意：这里不管项目是否是管理员创建的，都删。
         */
        delete(
                ProjectMySqlModel.class,
                "where DATEDIFF(now(), created_time)>10 "
                        + "and (updated_time is null or DATEDIFF(now(), updated_time)>10) "
                        + "and (project_id not in (select project_id from project_flow) or closed=true)"
        );


        /**
         * 公共前提：
         * 1. 太久没使用（编辑、启动）的数据要删掉
         * 2. 管理员创建的数据不删
         */
        String commonWhere = "where DATEDIFF(now(), created_time)>10 and"
                + "(updated_time is null or DATEDIFF(now(), updated_time)>10) and "
                + "created_by not in (select id from account where admin_role=true or super_admin_role=true)";

//        /**
//         * 清理 table_data_set
//         * 1. 无项目引用的删掉
//         */
//        delete(
//                TableDataSetMysqlModel.class,
//                commonWhere
//                        + "and id not in (select data_set_id from project_data_set)"
//        );
//
//        /**
//         * 清理 image_data_set
//         * 1. 无项目引用的删掉
//         */
//        delete(
//                ImageDataSetMysqlModel.class,
//                commonWhere
//                        + "and id not in (select data_set_id from project_data_set)"
//        );

        /**
         * 清理 operator_log
         * 1. 三个月以前的删掉
         */
        delete(
                OperationLogMysqlModel.class,
                "where DATEDIFF(now(), created_time)>90"
        );

        LOG.info("demo 环境数据清理执行完毕");
    }


    @Transactional(rollbackFor = Exception.class)
    public void delete(Class<? extends AbstractMySqlModel> clazz, String where) {

        String tableName = "";

        Entity entity = clazz.getAnnotation(Entity.class);
        if (entity != null) {
            tableName = entity.name();
        } else {
            Table annotation = clazz.getAnnotation(Table.class);
            tableName = annotation.name();
        }

        StringBuilder sql = new StringBuilder(1024);
        sql
                .append("select * from `")
                .append(tableName)
                .append("` ")
                .append(where)
                // jpa 删数据必须先查出来
                // 这里为了避免数据量巨大导致查询超时
                // 加上 limit 子句
                .append(" limit 1000");

        String sqlStr = sql.toString().replace(System.lineSeparator(), "");
        int count = globalConfigRepository.deleteByQuery(sqlStr, clazz);
        LOG.info("delete " + tableName + " count:" + count);
    }
}
