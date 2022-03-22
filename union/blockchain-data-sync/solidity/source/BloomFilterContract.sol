pragma solidity>=0.4.24 <0.6.11;
pragma experimental ABIEncoderV2;

import "./Table.sol";


contract BloomFilterContract{
    string constant TABLE_NAME = "bloom_filter";
    string constant FIX_ID = "fix_id_011";


    TableFactory tableFactory;


    event insertEvent(int256 ret_code,string[] params,string ext_json);
    event updateHashFunctionEvent(int256 ret_code,string data_resource_id,string hash_function,string updated_time);
    event updateExtJsonEvent(int256 ret_code,string data_resource_id, string ext_json,string updated_time);
    event deleteByDataResourceIdEvent(int256 ret_code,string data_resource_id);

    constructor() public {
        // 创建表
        tableFactory = TableFactory(0x1001);
        tableFactory.createTable(TABLE_NAME, "fix_id", "data_resource_id,hash_function,created_time,updated_time,ext_json");
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
        entry.set("hash_function", params[1]);
        entry.set("created_time", params[2]);
        entry.set("updated_time", params[3]);
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



    function updateHashFuntion(string data_resource_id,string hash_function,string updated_time) public returns (int) {
        int256 ret_code = 0;
        if (!isExist(data_resource_id)) {
            ret_code = -3;
            emit updateHashFunctionEvent(ret_code,data_resource_id,hash_function,updated_time);
            return ret_code;
        }

        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("data_resource_id", data_resource_id);
        condition.EQ("hash_function", hash_function);

        Entry entry = table.newEntry();
        entry.set("hash_function", hash_function);
        entry.set("updated_time", updated_time);


        int count = table.update(FIX_ID, entry, condition);

        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit updateHashFunctionEvent(ret_code,data_resource_id,hash_function,updated_time);
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

    function deleteByDataResourceId(string data_resource_id) public returns (int) {
        int256 ret_code = 0;
        if (!isExist(data_resource_id)) {
            emit deleteByDataResourceIdEvent(ret_code,data_resource_id);
            return ret_code;
        }
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



    function wrapReturnInfo(Entries entries) private view returns(string[]) {
        string[] memory data_list = new string[](uint256(entries.size()));
        for (int256 i = 0; i < entries.size(); ++i) {
            Entry entry = entries.get(i);


            string memory dataStr = strConcat(strEmptyToSpace(entry.getString("data_resource_id")), "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("updateHashFuntion")));
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