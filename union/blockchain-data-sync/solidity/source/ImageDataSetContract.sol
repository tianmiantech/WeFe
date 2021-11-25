pragma solidity>=0.4.24 <0.6.11;
pragma experimental ABIEncoderV2;

import "./Table.sol";


contract ImageDataSetContract{
    string constant TABLE_NAME = "image_data_set";
    string constant FIX_ID = "fix_id_007";


    TableFactory tableFactory;


    event insertEvent(int256 ret_code,string[] params,string ext_json);
    event updateEvent(int256 ret_code,string[] params);
    event updateEnableEvent(int256 ret_code,string id,string enable,string updated_time);
    event deleteByDataSetIdEvent(int256 ret_code,string id);
    event updateExtJsonEvent(int256 ret_code,string id, string ext_json,string updated_time);
    event updateLabeledCountEvent(string id,string labeled_count,string sample_count,string label_list,string label_completed,string updated_time);

    constructor() public {
        // 创建表
        tableFactory = TableFactory(0x1001);
        tableFactory.createTable(TABLE_NAME, "fix_id", "id,member_id,name,tags,description,for_job_type,label_list,sample_count,labeled_count,label_completed,files_size,public_level,public_member_list,usage_count_in_job,usage_count_in_flow,usage_count_in_project,enable,created_time,updated_time,ext_json");
    }




    function insert(string[] params, string ext_json) public returns (int) {
        int256 ret_code = 0;
        if (isExist(params[0])) {
            ret_code = -1;
            emit insertEvent(ret_code,params,ext_json);
            return ret_code;
        }

        Table table = tableFactory.openTable(TABLE_NAME);
        Entry entry = table.newEntry();
        entry.set("fix_id", FIX_ID);
        entry.set("id", params[0]);
        entry.set("member_id", params[1]);
        entry.set("name", params[2]);
        entry.set("tags", params[3]);
        entry.set("description", params[4]);
        entry.set("for_job_type", params[5]);
        entry.set("label_list", params[6]);
        entry.set("sample_count", params[7]);
        entry.set("labeled_count", params[8]);
        entry.set("label_completed", params[9]);
        entry.set("files_size", params[10]);
        entry.set("public_level", params[11]);
        entry.set("public_member_list", params[12]);
        entry.set("usage_count_in_job", params[13]);
        entry.set("usage_count_in_flow", params[14]);
        entry.set("usage_count_in_project", params[15]);
        entry.set("enable", params[16]);
        entry.set("created_time", params[17]);
        entry.set("updated_time", params[18]);
        entry.set("ext_json", ext_json);


        int256 count = table.insert(FIX_ID, entry);

        if(count == 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit insertEvent(ret_code,params,ext_json);

        return ret_code;
    }



    function update(string[] params) public returns (int) {
        int256 ret_code = 0;
        if (!isExist(params[0])) {
            ret_code = -1;
            emit updateEvent(ret_code,params);
            return ret_code;
        }

        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("id", params[0]);

        Entry entry = table.newEntry();
        entry.set("member_id", params[1]);
        entry.set("name", params[2]);
        entry.set("tags", params[3]);
        entry.set("description", params[4]);
        entry.set("for_job_type", params[5]);
        entry.set("label_list", params[6]);
        entry.set("sample_count", params[7]);
        entry.set("labeled_count", params[8]);
        entry.set("label_completed", params[9]);
        entry.set("files_size", params[10]);
        entry.set("public_level", params[11]);
        entry.set("public_member_list", params[12]);
        entry.set("usage_count_in_job", params[13]);
        entry.set("usage_count_in_flow", params[14]);
        entry.set("usage_count_in_project", params[15]);
        entry.set("updated_time", params[16]);


        int count = table.update(FIX_ID, entry, condition);

        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit updateEvent(ret_code,params);
        return ret_code;
    }

    function deleteByDataSetId(string id) public returns (int) {
        int256 ret_code = 0;
        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        condition.EQ("id", id);
        int count = table.remove(FIX_ID,condition);

        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit deleteByDataSetIdEvent(ret_code,id);

        return ret_code;

    }


    function selectById(string id) public view returns (int256, string[]) {
        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        condition.EQ("id", id);
        Entries entries = table.select(FIX_ID, condition);
        if (0 == uint256(entries.size())) {
            return (-3, new string[](0));
        }

        return (0, wrapReturnMemberInfo(entries));
    }

    function updateEnable(string id,string enable,string updated_time) public returns (int) {
        int256 ret_code = 0;
        if (!isExist(id)) {
            ret_code = -1;
            emit updateEnableEvent(ret_code,id,enable,updated_time);
            return ret_code;
        }

        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("id", id);

        Entry entry = table.newEntry();
        entry.set("enable", enable);
        entry.set("updated_time", updated_time);


        int count = table.update(FIX_ID, entry, condition);

        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit updateEnableEvent(ret_code,id,enable,updated_time);
        return ret_code;
    }



    function updateExtJson(string id,string ext_json,string updated_time) public returns (int256) {
        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("id", id);

        Entry entry = table.newEntry();
        entry.set("ext_json", ext_json);
        entry.set("updated_time", updated_time);

        int count = table.update(FIX_ID, entry, condition);

        int256 ret_code = 0;
        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit updateExtJsonEvent(ret_code,id,ext_json,updated_time);
        return ret_code;
    }


    function updateLabeledCount(string id,string labeled_count,string sample_count,string label_list,string label_completed,string updated_time) public returns (int256) {
        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("id", id);

        Entry entry = table.newEntry();
        entry.set("labeled_count", labeled_count);
        entry.set("sample_count", sample_count);
        entry.set("label_list", label_list);
        entry.set("label_completed", label_completed);
        entry.set("updated_time", updated_time);

        int count = table.update(FIX_ID, entry, condition);

        int256 ret_code = 0;
        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit updateLabeledCountEvent(ret_code,id,labeled_count,sample_count,label_list,label_completed,updated_time);
        return ret_code;
    }



    function isExist(string id) public view returns(bool) {
        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        condition.EQ("id", id);
        Entries entries = table.select(FIX_ID, condition);
        if(uint256(entries.size()) > 0) {
            return true;
        }

        return false;
    }



    function wrapReturnMemberInfo(Entries entries) private view returns(string[]) {
        string[] memory data_list = new string[](uint256(entries.size()));
        for (int256 i = 0; i < entries.size(); ++i) {
            Entry entry = entries.get(i);


            string memory dataStr = strConcat(strEmptyToSpace(entry.getString("id")), "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("member_id")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("name")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("tags")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("description")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("for_job_type")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("label_list")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("sample_count")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("labeled_count")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("label_completed")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("files_size")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("public_level")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("public_member_list")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("usage_count_in_job")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("usage_count_in_flow")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("usage_count_in_project")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("enable")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("created_time")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("updated_time")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("ext_json")));

            data_list[uint256(i)] = dataStr;
        }

        return data_list;
    }


    function strConcat(string _a, string _b) private returns (string){
        bytes memory _ba = bytes(_a);
        bytes memory _bb = bytes(_b);
        string memory ret = new string(_ba.length + _bb.length);
        bytes memory bret = bytes(ret);
        uint k = 0;
        for (uint i = 0; i < _ba.length; i++) {
            bret[k++] = _ba[i];
        }
        for (i = 0; i < _bb.length; i++) {
            bret[k++] = _bb[i];
        }
        return string(ret);
    }



    function strLength(string str) private returns(uint256){
        return bytes(str).length;
    }



    function strEmptyToSpace(string str) private returns(string) {
        if(strLength(str) == 0) {
            return " ";
        }
        return str;
    }
}