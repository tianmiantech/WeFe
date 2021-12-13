package com.welab.wefe.parser;

import com.welab.wefe.common.OperationType;
import com.welab.wefe.dto.EventDto;

/**
 * @author aaron.li
 * @date 2021/12/13 11:44
 **/
public abstract class AbstractDataMoveParser {

    /**
     * 全量同步
     */
    public abstract boolean fullSync();

    /**
     * 单条同步
     */
    public void singleSync(EventDto eventDto) {
        if (OperationType.insert.name().equals(eventDto.getOperationType())) {
            insert(eventDto);
        } else if (OperationType.update.name().equals(eventDto.getOperationType())) {
            update(eventDto);
        } else if (OperationType.delete.name().equals(eventDto.getOperationType())) {
            delete(eventDto);
        }
    }

    protected abstract void insert(EventDto eventDto);

    protected abstract void update(EventDto eventDto);

    protected abstract void delete(EventDto eventDto);
}
