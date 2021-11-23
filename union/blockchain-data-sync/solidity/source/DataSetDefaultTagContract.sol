pragma solidity>=0.4.24 <0.6.11;
pragma experimental ABIEncoderV2;

import "./Table.sol";


contract DataSetDefaultTagContract{
    string constant TABLE_NAME = "data_set_default_tag";
    string constant FIX_ID = "fix_id_004";


    TableFactory tableFactory;


    event insertEvent(int256 ret_code,string[] params,string ext_json);
    event updateEvent(int256 ret_code,string tag_id,string tag_name,string ext_json,string updated_time);
    event deleteByTagIdEvent(int256 ret_code,string tag_id);
    event updateExtJsonEvent(int256 ret_code,string tag_id, string ext_json);

    constructor() public {
        tableFactory = TableFactory(0x1001);
        tableFactory.createTable(TABLE_NAME, "fix_id", "tag_id,tag_name,created_time,updated_time,ext_json");
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
        entry.set("tag_id", params[0]);
        entry.set("tag_name", params[1]);
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

        return count;
    }



    function update(string tag_id,string tag_name,string ext_json,string updated_time) public returns (int) {
        int256 ret_code = 0;
        if (!isExist(tag_id)) {
            ret_code = -1;
            emit updateEvent(ret_code,tag_id,tag_name,ext_json,updated_time);
            return ret_code;
        }

        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("tag_id", tag_id);

        Entry entry = table.newEntry();
        entry.set("tag_name", tag_name);
        entry.set("updated_time", updated_time);
        entry.set("ext_json", ext_json);


        int count = table.update(FIX_ID, entry, condition);

        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit updateEvent(ret_code,tag_id,tag_name,ext_json,updated_time);
        return count;
    }

    function deleteByTagId(string tag_id) public returns (int) {
        int256 ret_code = 0;
        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        condition.EQ("tag_id", tag_id);
        int count = table.remove(FIX_ID,condition);

        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit deleteByTagIdEvent(ret_code,tag_id);

        return count;

    }



    function selectAll() public view returns(int256, string[]) {
        Table table = tableFactory.openTable(TABLE_NAME);
        Entries entries = table.select(FIX_ID, table.newCondition());
        if (0 == uint256(entries.size())) {

            return (-3, new string[](0));
        }
        return (0, wrapReturnMemberInfo(entries));
    }

    function updateExtJson(string tag_id,string ext_json) public returns (int256) {
        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("tag_id", tag_id);

        Entry entry = table.newEntry();
        entry.set("ext_json", ext_json);

        int count = table.update(FIX_ID, entry, condition);

        int256 ret_code = 0;
        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit updateExtJsonEvent(ret_code,tag_id,ext_json);
        return ret_code;
    }

    function isExist(string tag_id) public view returns(bool) {
        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        condition.EQ("tag_id", tag_id);
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


            string memory dataStr = strConcat(strEmptyToSpace(entry.getString("tag_id")), "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("tag_name")));
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