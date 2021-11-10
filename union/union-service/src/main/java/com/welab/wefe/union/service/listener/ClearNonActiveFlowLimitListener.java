package com.welab.wefe.union.service.listener;

import cn.hutool.core.thread.ThreadUtil;
import com.welab.wefe.common.data.mongodb.entity.common.FlowLimit;
import com.welab.wefe.common.data.mongodb.repo.FlowLimitRepo;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.union.service.UnionService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author aaron.li
 * @date 2021/11/9 10:53
 **/
@Component
public class ClearNonActiveFlowLimitListener implements ApplicationListener<ApplicationStartedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(ClearNonActiveFlowLimitListener.class);

    @Override
    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
        ThreadUtil.execAsync(() -> {
            while (true) {
                LOG.info("ClearNonActiveFlowLimitListener start.....");
                try {
                    FlowLimitRepo flowLimitRepo = UnionService.CONTEXT.getBean(FlowLimitRepo.class);
                    List<FlowLimit> flowLimitList = flowLimitRepo.findAll();
                    if (CollectionUtils.isNotEmpty(flowLimitList)) {
                        for (FlowLimit flowLimit : flowLimitList) {
                            if ((System.currentTimeMillis() - flowLimit.getLatestVisitTime()) > flowLimit.getActiveTime()) {
                                flowLimitRepo.removeByKey(flowLimit.getKey());
                            }
                        }
                    }
                    LOG.info("ClearNonActiveFlowLimitListener end.");
                } catch (Exception e) {
                    LOG.error("ClearNonActiveFlowLimitListener exception: ", e);
                } finally {
                    com.welab.wefe.common.util.ThreadUtil.sleepSeconds(10);
                }
            }
        });
    }
}
