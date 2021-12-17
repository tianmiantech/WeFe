/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.constant;

/**
 * @author yuxin.zhang
 **/
public class EventConstant {
    public static final String RUN_SUCCESS_CODE = "0";


    public static final String UPDATE_EXTJSON_EVENT = "UPDATEEXTJSONEVENT";
    public static final String DELETE_BY_DATA_RESOURCE_ID_EVENT = "DELETEBYDATARESOURCEIDEVENT";
    public static final class DataSetEvent {
        public static final String INSERT_EVENT = "INSERTEVENT";
        public static final String UPDATE_EVENT = "UPDATEEVENT";
        public static final String DELETE_BY_DATASETID_EVENT = "DELETEBYDATASETIDEVENT";
    }

    public static final class ImageDataSetEvent {
        public static final String INSERT_EVENT = "INSERTEVENT";
        public static final String UPDATE_EVENT = "UPDATEEVENT";
        public static final String DELETE_BY_DATA_RESOURCE_ID_EVENT = "DELETEBYDATARESOURCEIDEVENT";
        public static final String UPDATE_ENABLE_EVENT = "UPDATEENABLEEVENT";
    }

    public static final class MemberEvent {
        public static final String INSERT_EVENT = "INSERTEVENT";
        public static final String UPDATE_EVENT = "UPDATEEVENT";
        public static final String UPDATE_EXCLUDE_PUBLICKEY_EVENT = "UPDATEEXCLUDEPUBLICKEYEVENT";
        public static final String UPDATE_PUBLICKEY_EVENT = "UPDATEPUBLICKEYEVENT";
        public static final String DELETE_BY_ID_EVENT = "DELETEBYIDEVENT";
        public static final String UPDATE_EXCLUDE_LOGO_EVENT = "UPDATEEXCLUDELOGOEVENT";
        public static final String UPDATE_LOGO_BY_ID_EVENT = "UPDATELOGOBYIDEVENT";
        public static final String UPDATE_LAST_ACTIVITY_TIME_BY_ID_EVENT = "UPDATELASTACTIVITYTIMEBYIDEVENT";
    }

    public static final class DataSetMemberPermissionEvent {
        public static final String INSERT_EVENT = "INSERTEVENT";
        public static final String UPDATE_EVENT = "UPDATEEVENT";
        public static final String DELETE_BY_DATASETID_EVENT = "DELETEBYDATASETIDEVENT";
    }

    public static final class DataSetDefaultTagEvent {
        public static final String INSERT_EVENT = "INSERTEVENT";
        public static final String UPDATE_EVENT = "UPDATEEVENT";
        public static final String DELETE_BY_TAGID_EVENT = "DELETEBYTAGIDEVENT";
    }

    public static final class MemberAuthTypeEvent {
        public static final String INSERT_EVENT = "INSERTEVENT";
        public static final String UPDATE_EVENT = "UPDATEEVENT";
        public static final String DELETE_BY_TYPEID_EVENT = "DELETEBYTYPEIDEVENT";
    }

    public static final class UnionNodeEvent {
        public static final String INSERT_EVENT = "INSERTEVENT";
        public static final String UPDATE_EVENT = "UPDATEEVENT";
        public static final String UPDATE_ENABLE_EVENT = "UPDATEENABLEEVENT";
        public static final String UPDATE_PUBLIC_KEY_EVENT = "UPDATEPUBLICKEYEVENT";
        public static final String DELETE_BY_UNIONNODEID_EVENT = "DELETEBYUNIONNODEIDEVENT";
    }

    public static final class DataResourceEvent {
        public static final String INSERT_EVENT = "INSERTEVENT";
        public static final String UPDATE_EVENT = "UPDATEEVENT";
        public static final String UPDATE_ENABLE_EVENT = "UPDATEENABLEEVENT";

    }

    public static final class TableDataSetEvent {
        public static final String INSERT_EVENT = "INSERTEVENT";
        public static final String UPDATE_EVENT = "UPDATEEVENT";
    }

    public static final class BloomFilterEvent {
        public static final String INSERT_EVENT = "INSERTEVENT";
        public static final String UPDATE_HASH_FUNCTION_EVENT = "UPDATEHASHFUNCTIONEVENT";
    }
}
