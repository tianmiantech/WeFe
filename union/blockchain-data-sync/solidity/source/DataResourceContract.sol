pragma solidity>=0.4.24 <0.6.11;
pragma experimental ABIEncoderV2;

import "./Table.sol";


contract DataResourceContract{
    string constant TABLE_NAME = "data_resource";
    string constant FIX_ID = "fix_id_009";


    TableFactory tableFactory;


    event insertEvent(int256 ret_code,string[] params,string ext_json);
    event updateEvent(int256 ret_code,string data_resource_id,string[] params,string updated_time);
    event updateExtJsonEvent(int256 ret_code,string data_resource_id,string ext_json,string updated_time);
    event updateEnableEvent(int256 ret_code,string data_resource_id,string enable,string updated_time);
    event deleteByDataResourceIdEvent(int256 ret_code,string data_resource_id);

    constructor() public {
        // 创建表
        tableFactory = TableFactory(0x1001);
        tableFactory.createTable(TABLE_NAME, "fix_id", "data_resource_id,member_id,name,description,tags,total_data_count,public_level,public_member_list,usage_count_in_job,usage_count_in_flow,usage_count_in_project,usage_count_in_member,enable,data_resource_type,created_time,updated_time,ext_json");
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
        entry.set("data_resource_id", params[0]);
        entry.set("member_id", params[1]);
        entry.set("name", params[2]);
        entry.set("description", params[3]);
        entry.set("tags", params[4]);
        entry.set("total_data_count", params[5]);
        entry.set("public_level", params[6]);
        entry.set("public_member_list", params[7]);
        entry.set("usage_count_in_job", params[8]);
        entry.set("usage_count_in_flow", params[9]);
        entry.set("usage_count_in_project", params[10]);
        entry.set("usage_count_in_member", params[11]);
        entry.set("data_resource_type", params[12]);
        entry.set("created_time", params[13]);
        entry.set("updated_time", params[14]);

        entry.set("ext_json", ext_json);
        entry.set("enable", "1");

        int256 count = table.insert(FIX_ID, entry);

        if(count == 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit insertEvent(ret_code,params,ext_json);

        return ret_code;
    }



    function update(string data_resource_id,string[] params,string updated_time) public returns (int) {
        int256 ret_code = 0;
        if (!isExist(data_resource_id)) {
            ret_code = -1;
            emit updateEvent(ret_code,data_resource_id,params,updated_time);
            return ret_code;
        }

        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("data_resource_id", data_resource_id);

        Entry entry = table.newEntry();
        entry.set("name", params[0]);
        entry.set("description", params[1]);
        entry.set("tags", params[2]);
        entry.set("total_data_count", params[3]);
        entry.set("public_level", params[4]);
        entry.set("public_member_list", params[5]);
        entry.set("usage_count_in_job", params[6]);
        entry.set("usage_count_in_flow", params[7]);
        entry.set("usage_count_in_project", params[8]);
        entry.set("usage_count_in_member", params[9]);
        entry.set("updated_time", updated_time);


        int count = table.update(FIX_ID, entry, condition);

        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit updateEvent(ret_code,data_resource_id,params,updated_time);
        return ret_code;
    }

    function updateExtJson(string data_resource_id,string ext_json,string updated_time) public returns (int256) {
        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("data_resource_id", data_resource_id);

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

        emit updateExtJsonEvent(ret_code,data_resource_id,ext_json,updated_time);
        return ret_code;
    }

   function updateEnable(string data_resource_id,string enable,string updated_time) public returns (int) {
        int256 ret_code = 0;
        if (!isExist(data_resource_id)) {
            ret_code = -3;
            emit updateEnableEvent(ret_code,data_resource_id,enable,updated_time);
            return ret_code;
        }

        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("data_resource_id", data_resource_id);

        Entry entry = table.newEntry();
        entry.set("enable", enable);
        entry.set("updated_time", updated_time);


        int count = table.update(FIX_ID, entry, condition);

        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit updateEnableEvent(ret_code,data_resource_id,enable,updated_time);
        return ret_code;
    }

    function deleteByDataResourceId(string data_resource_id) public returns (int) {
        int256 ret_code = 0;
        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        condition.EQ("data_resource_id", data_resource_id);
        int count = table.remove(FIX_ID,condition);

        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit deleteByDataResourceIdEvent(ret_code,data_resource_id);

        return ret_code;

    }


    function isExist(string data_resource_id) public view returns(bool) {
        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        condition.EQ("data_resource_id", data_resource_id);
        Entries entries = table.select(FIX_ID, condition);
        if(uint256(entries.size()) > 0) {
            return true;
        }

        return false;
    }

    function selectById(string data_resource_id) public view returns (int256, string[]) {
        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        condition.EQ("data_resource_id", data_resource_id);
        Entries entries = table.select(FIX_ID, condition);
        if (0 == uint256(entries.size())) {
            return (-3, new string[](0));
        }

        return (0, wrapReturnInfo(entries));
    }

    function wrapReturnInfo(Entries entries) private view returns(string[]) {
        string[] memory data_list = new string[](uint256(entries.size()));
        for (int256 i = 0; i < entries.size(); ++i) {
            Entry entry = entries.get(i);


            string memory dataStr = strConcat(strEmptyToSpace(entry.getString("data_resource_id")), "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("member_id")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("name")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("description")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("tags")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("total_data_count")));
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
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("usage_count_in_member")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("enable")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("data_resource_type")));
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