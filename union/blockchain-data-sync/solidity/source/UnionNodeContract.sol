pragma solidity>=0.4.24 <0.6.11;
pragma experimental ABIEncoderV2;

import "./Table.sol";


contract UnionNodeContract{
    string constant TABLE_NAME = "union_node";
    string constant FIX_ID = "fix_id_005";


    TableFactory tableFactory;


    event insertEvent(int256 ret_code,string[] params,string ext_json);
    event updateEvent(int256 ret_code,string union_node_id,string[] update_params);
    event updateEnableEvent(int256 ret_code,string union_node_id,string enable,string update_params);
    event deleteByUnionNodeIdEvent(int256 ret_code,string union_node_id);
    event updateExtJsonEvent(int256 ret_code,string union_node_id, string ext_json);

    constructor() public {
        tableFactory = TableFactory(0x1001);
        tableFactory.createTable(TABLE_NAME, "fix_id", "union_node_id,sign,union_base_url,organization_name,enable,created_time,updated_time,ext_json");
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
        entry.set("union_node_id", params[0]);
        entry.set("sign", params[1]);
        entry.set("union_base_url", params[2]);
        entry.set("organization_name", params[3]);
        entry.set("enable", params[4]);
        entry.set("created_time", params[5]);
        entry.set("updated_time", params[6]);
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

    function updateEnable(string union_node_id,string enable,string updated_time) public returns (int) {
        int256 ret_code = 0;
        if (!isExist(union_node_id)) {
            ret_code = -1;
            emit updateEnableEvent(ret_code,union_node_id,enable,updated_time);
            return ret_code;
        }

        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("union_node_id", union_node_id);

        Entry entry = table.newEntry();
        entry.set("enable", enable);
        entry.set("updated_time", updated_time);


        int count = table.update(FIX_ID, entry, condition);

        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit updateEnableEvent(ret_code,union_node_id,enable,updated_time);
        return count;
    }



    function update(string union_node_id,string[] params) public returns (int) {
        int256 ret_code = 0;
        if (!isExist(union_node_id)) {
            ret_code = -1;
            emit updateEvent(ret_code,union_node_id,params);
            return ret_code;
        }

        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("union_node_id", union_node_id);

        Entry entry = table.newEntry();
        entry.set("sign", update_params[0]);
        entry.set("union_base_url", update_params[1]);
        entry.set("organization_name", update_params[2]);
        entry.set("updated_time", update_params[3]);


        int count = table.update(FIX_ID, entry, condition);

        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit updateEvent(ret_code,union_node_id,params);
        return count;
    }

    function deleteByUnionNodeId(string union_node_id) public returns (int) {
        int256 ret_code = 0;
        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        condition.EQ("union_node_id", union_node_id);
        int count = table.remove(FIX_ID,condition);

        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit deleteByUnionNodeIdEvent(ret_code,union_node_id);

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

    function updateExtJson(string union_node_id,string ext_json) public returns (int256) {
        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("union_node_id", union_node_id);

        Entry entry = table.newEntry();
        entry.set("ext_json", ext_json);

        int count = table.update(FIX_ID, entry, condition);

        int256 ret_code = 0;
        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit updateExtJsonEvent(ret_code,union_node_id,ext_json);
        return ret_code;
    }

    function isExist(string union_node_id) public view returns(bool) {
        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        condition.EQ("union_node_id", union_node_id);
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

            string memory dataStr = strConcat(strEmptyToSpace(entry.getString("union_node_id")), "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("sign")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("union_base_url")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("organization_name")));
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