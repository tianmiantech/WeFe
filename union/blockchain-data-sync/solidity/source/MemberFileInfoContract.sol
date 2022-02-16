pragma solidity>=0.4.24 <0.6.11;
pragma experimental ABIEncoderV2;

import "./Table.sol";


contract MemberFileInfoContract{
    string constant TABLE_NAME = "member_file_info_contract";
    string constant FIX_ID = "member_file_info_contract";


    TableFactory tableFactory;


    event insertEvent(int256 ret_code,string[] params,string ext_json);
    event updateEvent(int256 ret_code,string file_id,string[] params);
    event updateEnableEvent(int256 ret_code,string file_id,string enable,string updated_time);
    event updateExtJsonEvent(int256 ret_code,string file_id, string ext_json,string updated_time);

    constructor() public {
        tableFactory = TableFactory(0x1001);
        tableFactory.createTable(TABLE_NAME, "fix_id", "file_id,file_sign,file_name,file_size,member_id,blockchain_node_id,purpose,file_public_level,describe,enable,created_time,updated_time,ext_json");
    }




    function insert(string[] params, string ext_json) public returns (int) {
        int256 ret_code = 0;
        if (isExist(params[0])) {
            ret_code = -1;
            emit insertEvent(ret_code,params,ext_json);
            return -1;
        }

        Table table = tableFactory.openTable(TABLE_NAME);
        Entry entry = table.newEntry();
        entry.set("fix_id", FIX_ID);
        entry.set("file_id", params[0]);
        entry.set("file_sign", params[1]);
        entry.set("file_name", params[2]);
        entry.set("file_size", params[3]);
        entry.set("member_id", params[4]);
        entry.set("blockchain_node_id", params[5]);
        entry.set("purpose", params[6]);
        entry.set("file_public_level", params[7]);
        entry.set("describe", params[8]);
        entry.set("enable", params[9]);
        entry.set("created_time", params[10]);
        entry.set("updated_time", params[11]);
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

    function updateEnable(string file_id,string enable,string updated_time) public returns (int) {
        int256 ret_code = 0;
        if (!isExist(file_id)) {
            ret_code = -3;
            emit updateEnableEvent(ret_code,file_id,enable,updated_time);
            return ret_code;
        }

        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("file_id", file_id);

        Entry entry = table.newEntry();
        entry.set("enable", enable);
        entry.set("updated_time", updated_time);


        int count = table.update(FIX_ID, entry, condition);

        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit updateEnableEvent(ret_code,file_id,enable,updated_time);
        return ret_code;
    }

    function updateExtJson(string file_id,string ext_json,string updated_time) public returns (int256) {
        int256 ret_code = 0;
        if (!isExist(file_id)) {
            ret_code = -3;
            emit updateExtJsonEvent(ret_code,file_id,ext_json,updated_time);
            return ret_code;
        }

        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("file_id", file_id);

        Entry entry = table.newEntry();
        entry.set("ext_json", ext_json);
        entry.set("updated_time", updated_time);

        int count = table.update(FIX_ID, entry, condition);

        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit updateExtJsonEvent(ret_code,file_id,ext_json,updated_time);
        return ret_code;
    }




    function selectByFileId(string file_id) public view returns(int256, string[]) {
        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        condition.EQ("file_id", file_id);
        Entries entries = table.select(FIX_ID, condition);
        if (0 == uint256(entries.size())) {

            return (-3, new string[](0));
        }
        return (0, wrapReturnMemberInfo(entries));
    }



    function isExist(string file_id) public view returns(bool) {
        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        condition.EQ("file_id", file_id);
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

            string memory dataStr = strConcat(strEmptyToSpace(entry.getString("file_id")), "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("file_sign")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("file_name")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("file_size")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("member_id")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("blockchain_node_id")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("purpose")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("file_public_level")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("describe")));
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