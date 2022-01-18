/**
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

import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.board.service.database.entity.OperationLogMysqlModel;
import com.welab.wefe.board.service.database.entity.base.AbstractMySqlModel;
import com.welab.wefe.board.service.database.entity.data_set.DataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.job.JobMySqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectDataSetMySqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectFlowMySqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectMySqlModel;
import com.welab.wefe.board.service.database.repository.GlobalConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Entity;
import javax.persistence.Table;

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

    /**
     * 清理体验者产生的过多无效数据
     */
    @Scheduled(fixedDelay = 600_000, initialDelay = 60_000)
    //@Scheduled(fixedDelay = 5_000, initialDelay = 1_000)
    public void clean() {
        if (!config.isOnlineDemo()) {
            return;
        }
        LOG.info("开始 demo 环境数据清理...");
        /**
         * 公共前提：
         * 1. 太久没使用（编辑、启动）的数据要删掉
         * 2. 管理员创建的数据不删
         */
        String commonWhere = "where DATEDIFF(now(), created_time)>20 and"
                + "(updated_time is null or DATEDIFF(now(), updated_time)>20) and "
                + "created_by not in (select id from account where admin_role=true or super_admin_role=true)";

        /**
         * 清理 job
         * 无条件删除所有满足公共前提的 job 记录
         */
        delete(
                JobMySqlModel.class,
                commonWhere
        );

        /**
         * 清理 project
         * 1. 无流程或已关闭的项目删掉
         */
        delete(
                ProjectMySqlModel.class,
                commonWhere
                        + "and (project_id not in (select project_id from project_flow) or closed=true)"
        );


        /**
         * 清理 project_flow
         * 1. 从来没启动过的流程删掉(无关联 job)
         * 2. project 已被删的删掉
         */
        delete(
                ProjectFlowMySqlModel.class,
                commonWhere
                        + "and flow_id not in (select flow_id from job)"
        );
        delete(
                ProjectFlowMySqlModel.class,
                commonWhere
                        + "and project_id not in (select project_id from project)"
        );

        /**
         * 清理 project_data_set
         * 1. project 已被删的删掉
         */
        delete(
                ProjectDataSetMySqlModel.class,
                commonWhere
                        + "and project_id not in (select project_id from project)"
        );

        /**
         * 清理 data_set
         * 1. 无项目引用的删掉
         */
        delete(
                DataSetMysqlModel.class,
                commonWhere
                        + "and id not in (select data_set_id from project_data_set)"
        );

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
