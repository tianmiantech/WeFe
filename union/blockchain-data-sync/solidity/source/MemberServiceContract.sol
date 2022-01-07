pragma solidity>=0.4.24 <0.6.11;
pragma experimental ABIEncoderV2;

import "./Table.sol";


contract MemberServiceContract{
    string constant TABLE_NAME = "member_service";
    string constant FIX_ID = "member_service";


    TableFactory tableFactory;


    event insertEvent(int256 ret_code,string[] params,string ext_json);
    event updateEvent(int256 ret_code,string service_id,string[] params,string updated_time);
    event updateExtJsonEvent(int256 ret_code,string service_id,string ext_json,string updated_time);
    event updateServiceStatusEvent(int256 ret_code,string service_id,string service_status,string updated_time);
    event deleteByServiceIdEvent(int256 ret_code,string service_id);

    constructor() public {
        // 创建表
        tableFactory = TableFactory(0x1001);
        tableFactory.createTable(TABLE_NAME, "fix_id", "service_id,member_id,name,url,service_type,query_params,service_status,created_time,updated_time,ext_json");
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
        entry.set("service_id", params[0]);
        entry.set("member_id", params[1]);
        entry.set("name", params[2]);
        entry.set("url", params[3]);
        entry.set("service_type", params[4]);
        entry.set("query_params", params[5]);
        entry.set("service_status", params[6]);
        entry.set("created_time", params[7]);
        entry.set("updated_time", params[8]);
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



    function update(string service_id,string[] params,string updated_time) public returns (int) {
        int256 ret_code = 0;
        if (!isExist(service_id)) {
            ret_code = -3;
            emit updateEvent(ret_code,service_id,params,updated_time);
            return ret_code;
        }

        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("service_id", service_id);

        Entry entry = table.newEntry();
        entry.set("name", params[0]);
        entry.set("url", params[1]);
        entry.set("service_type", params[2]);
        entry.set("query_params", params[3]);
        entry.set("updated_time", updated_time);


        int count = table.update(FIX_ID, entry, condition);

        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit updateEvent(ret_code,service_id,params,updated_time);
        return ret_code;
    }

    function updateExtJson(string service_id,string ext_json,string updated_time) public returns (int256) {
        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("service_id", service_id);

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

        emit updateExtJsonEvent(ret_code,service_id,ext_json,updated_time);
        return ret_code;
    }

   function updateServiceStatus(string service_id,string service_status,string updated_time) public returns (int) {
        int256 ret_code = 0;
        if (!isExist(service_id)) {
            ret_code = -3;
            emit updateServiceStatusEvent(ret_code,service_id,service_status,updated_time);
            return ret_code;
        }

        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("service_id", service_id);

        Entry entry = table.newEntry();
        entry.set("service_status", service_status);
        entry.set("updated_time", updated_time);


        int count = table.update(FIX_ID, entry, condition);

        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit updateServiceStatusEvent(ret_code,service_id,service_status,updated_time);
        return ret_code;
    }

    function deleteByServiceId(string service_id) public returns (int) {
        int256 ret_code = 0;
        if (!isExist(service_id)) {
            ret_code = -3
            emit deleteByServiceIdEvent(ret_code,service_id);
            return ret_code;
        }
        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        condition.EQ("service_id", service_id);
        int count = table.remove(FIX_ID,condition);

        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit deleteByServiceIdEvent(ret_code,service_id);

        return ret_code;

    }

    function isExist(string service_id) public view returns(bool) {
        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        condition.EQ("service_id", service_id);
        Entries entries = table.select(FIX_ID, condition);
        if(uint256(entries.size()) > 0) {
            return true;
        }

        return false;
    }

    function selectById(string service_id) public view returns (int256, string[]) {
        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        condition.EQ("service_id", service_id);
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

            string memory dataStr = strConcat(strEmptyToSpace(entry.getString("service_id")), "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("member_id")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("name")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("url")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("service_type")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("query_params")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("service_status")));
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