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

package com.welab.wefe.common.data.mongodb.constant;

/**
 * @author yuxin.zhang
 **/
public class MongodbTable {

    public static final class Common {
        public static final String FLOW_LIMIT = "FlowLimit";
    }

    public static final class Union {
        public static final String DATASET = "DataSet";
        public static final String DATASET_MEMBER_PERMISSION = "DataSetMemberPermission";
        public static final String MEMBER = "Member";
        public static final String DATA_SET_DEFAULT_TAG = "DataSetDefaultTag";
        public static final String MEMBER_AUTH_TYPE = "MemberAuthType";
        public static final String BLOCK_SYNC_HEIGHT = "BlockSyncHeight";
        public static final String BLOCK_SYNC_DETAIL_INFO = "BlockSyncDetailInfo";
        public static final String BLOCK_SYNC_CONTRACT_HEIGHT = "BlockSyncContractHeight";
        public static final String UNION_NODE = "UnionNode";
        public static final String REALNAME_AUTH_AGREEMENT_TEMPLATE = "RealnameAuthAgreementTemplate";
        public static final String MEMBER_FILE_INFO = "MemberFileInfo";
        public static final String UNION_NODE_CONFIG = "UnionNodeConfig";
        public static final String IMAGE_DATASET = "ImageDataSet";
        public static final String IMAGE_DATASET_LABELEDCOUNT = "ImageDataSetLabeledCount";
        public static final String IMAGE_DATA_SET_DEFAULT_TAG = "ImageDataSetDefaultTag";
    }

    public static final class Sms {
        public static final String VERIFICATION_CODE = "SmsVerificationCode";
        public static final String DETAIL_INFO = "SmsDetailInfo";
    }

    public static final String USER = "User";
}
