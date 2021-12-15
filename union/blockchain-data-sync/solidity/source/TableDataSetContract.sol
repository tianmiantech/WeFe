pragma solidity>=0.4.24 <0.6.11;
pragma experimental ABIEncoderV2;

import "./Table.sol";


contract TableDataSetContract{
    string constant TABLE_NAME = "table_data_set";
    string constant FIX_ID = "fix_id_010";


    TableFactory tableFactory;


    event insertEvent(int256 ret_code,string[] params,string ext_json);
    event updateEvent(int256 ret_code,string data_resource_id,string[] params,string updated_time);
    event updateExtJsonEvent(int256 ret_code,string data_resource_id, string ext_json,string updated_time);

    constructor() public {
        // 创建表
        tableFactory = TableFactory(0x1001);
        tableFactory.createTable(TABLE_NAME, "fix_id", "data_resource_id,contains_y,column_count,column_name_list,feature_count,feature_name_list,created_time,updated_time,ext_json");
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
        entry.set("contains_y", params[1]);
        entry.set("column_count", params[2]);
        entry.set("column_name_list", params[3]);
        entry.set("feature_count", params[4]);
        entry.set("feature_name_list", params[5]);
        entry.set("created_time", params[6]);
        entry.set("updated_time", params[7]);
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
        entry.set("contains_y", params[0]);
        entry.set("column_count", params[1]);
        entry.set("column_name_list", params[2]);
        entry.set("feature_count", params[3]);
        entry.set("feature_name_list", params[4]);
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

    function wrapReturnInfo(Entries entries) private view returns(string[]) {
        string[] memory data_list = new string[](uint256(entries.size()));
        for (int256 i = 0; i < entries.size(); ++i) {
            Entry entry = entries.get(i);

            string memory dataStr = strConcat(strEmptyToSpace(entry.getString("data_resource_id")), "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("contains_y")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("column_count")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("column_name_list")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("feature_count")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("feature_name_list")));
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