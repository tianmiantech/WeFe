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

package com.welab.wefe.serving.service.feature.sql.mysql;

import com.welab.wefe.serving.service.feature.sql.AbstractDruidTemplate;

/**
 * @author hunter.zhao
 */
public class MySqlTemplate extends AbstractDruidTemplate {

    public MySqlTemplate(String url, String username, String password, String sql, String userId) {
        super(url, username, password, sql, userId);
    }

    @Override
    protected String driver() {
        return "com.mysql.jdbc.Driver";
    }
}
