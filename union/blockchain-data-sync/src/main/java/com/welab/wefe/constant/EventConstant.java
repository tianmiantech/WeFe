/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

    public static final class DataSet {
        public static final String INSERT_EVENT = "INSERTEVENT";
        public static final String UPDATE_EVENT = "UPDATEEVENT";
        public static final String DELETE_BY_DATASETID_EVENT = "DELETEBYDATASETIDEVENT";
    }

    public static final class Member {
        public static final String INSERT_EVENT = "INSERTEVENT";
        public static final String UPDATE_EVENT = "UPDATEEVENT";
        public static final String UPDATE_EXCLUDE_PUBLICKEY_EVENT = "UPDATEEXCLUDEPUBLICKEYEVENT";
        public static final String UPDATE_PUBLICKEY_EVENT = "UPDATEPUBLICKEYEVENT";
        public static final String DELETE_BY_ID_EVENT = "DELETEBYIDEVENT";
        public static final String UPDATE_EXCLUDE_LOGO_EVENT = "UPDATEEXCLUDELOGOEVENT";
        public static final String UPDATE_LOGO_BY_ID_EVENT = "UPDATELOGOBYIDEVENT";
        public static final String UPDATE_LAST_ACTIVITY_TIME_BY_ID_EVENT = "UPDATELASTACTIVITYTIMEBYIDEVENT";
    }

    public static final class DataSetMemberPermission {
        public static final String INSERT_EVENT = "INSERTEVENT";
        public static final String UPDATE_EVENT = "UPDATEEVENT";
        public static final String DELETE_BY_DATASETID_EVENT = "DELETEBYDATASETIDEVENT";
    }

    public static final class DataSetDefaultTag {
        public static final String INSERT_EVENT = "INSERTEVENT";
        public static final String UPDATE_EVENT = "UPDATEEVENT";
        public static final String DELETE_BY_TAGID_EVENT = "DELETEBYTAGIDEVENT";
    }
}
