pragma solidity>=0.4.24 <0.6.11;
pragma experimental ABIEncoderV2;

import "./Table.sol";


contract UnionNodeContract{
    string constant TABLE_NAME = "union_node";
    string constant FIX_ID = "union_node";


    TableFactory tableFactory;


    event insertEvent(int256 ret_code,string[] params,string ext_json);
    event updateEvent(int256 ret_code,string node_id,string[] params);
    event updateEnableEvent(int256 ret_code,string node_id,string enable,string updated_time);
    event updatePublicKeyEvent(int256 ret_code,string node_id,string public_key,string updated_time);
    event deleteByUnionNodeIdEvent(int256 ret_code,string node_id);
    event updateExtJsonEvent(int256 ret_code,string node_id, string ext_json,string updated_time);

    constructor() public {
        tableFactory = TableFactory(0x1001);
        tableFactory.createTable(TABLE_NAME, "fix_id", "node_id,blockchain_node_id,base_url,organization_name,lost_contact,contact_email,priority_level,enable,version,public_key,created_time,updated_time,ext_json");
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
        entry.set("node_id", params[0]);
        entry.set("blockchain_node_id", params[1]);
        entry.set("base_url", params[2]);
        entry.set("organization_name", params[3]);
        entry.set("lost_contact", params[4]);
        entry.set("contact_email", params[5]);
        entry.set("priority_level", params[6]);
        entry.set("enable", "0");
        entry.set("version", params[7]);
        entry.set("public_key", params[8]);
        entry.set("created_time", params[9]);
        entry.set("updated_time", params[10]);
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

    function updateEnable(string node_id,string enable,string updated_time) public returns (int) {
        int256 ret_code = 0;
        if (!isExist(node_id)) {
            ret_code = -3;
            emit updateEnableEvent(ret_code,node_id,enable,updated_time);
            return ret_code;
        }

        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("node_id", node_id);

        Entry entry = table.newEntry();
        entry.set("enable", enable);
        entry.set("updated_time", updated_time);


        int count = table.update(FIX_ID, entry, condition);

        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit updateEnableEvent(ret_code,node_id,enable,updated_time);
        return ret_code;
    }

    function updatePublicKey(string node_id,string public_key,string updated_time) public returns (int) {
        int256 ret_code = 0;
        if (!isExist(node_id)) {
            ret_code = -3;
            emit updatePublicKeyEvent(ret_code,node_id,public_key,updated_time);
            return ret_code;
        }

        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("node_id", node_id);

        Entry entry = table.newEntry();
        entry.set("public_key", public_key);
        entry.set("updated_time", updated_time);


        int count = table.update(FIX_ID, entry, condition);

        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit updatePublicKeyEvent(ret_code,node_id,public_key,updated_time);
        return ret_code;
    }



    function update(string node_id,string[] params) public returns (int) {
        int256 ret_code = 0;
        if (!isExist(node_id)) {
            ret_code = -3;
            emit updateEvent(ret_code,node_id,params);
            return ret_code;
        }

        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("node_id", node_id);

        Entry entry = table.newEntry();
        entry.set("base_url", params[0]);
        entry.set("organization_name", params[1]);
        entry.set("contact_email", params[2]);
        entry.set("updated_time", params[3]);

        int count = table.update(FIX_ID, entry, condition);

        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit updateEvent(ret_code,node_id,params);
        return ret_code;
    }

    function deleteByUnionNodeId(string node_id) public returns (int) {
        int256 ret_code = 0;
        if (!isExist(node_id)) {
            ret_code = -3;
            emit deleteByUnionNodeIdEvent(ret_code,node_id);
            return ret_code;
        }

        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        condition.EQ("node_id", node_id);
        int count = table.remove(FIX_ID,condition);

        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit deleteByUnionNodeIdEvent(ret_code,node_id);

        return ret_code;

    }



    function selectAll() public view returns(int256, string[]) {
        Table table = tableFactory.openTable(TABLE_NAME);
        Entries entries = table.select(FIX_ID, table.newCondition());
        if (0 == uint256(entries.size())) {

            return (-3, new string[](0));
        }
        return (0, wrapReturnMemberInfo(entries));
    }

    function updateExtJson(string node_id,string ext_json,string updated_time) public returns (int256) {
        int256 ret_code = 0;
        if (!isExist(node_id)) {
            ret_code = -3;
            emit updateExtJsonEvent(ret_code,node_id,ext_json,updated_time);
            return ret_code;
        }

        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("node_id", node_id);

        Entry entry = table.newEntry();
        entry.set("ext_json", ext_json);
        entry.set("updated_time", updated_time);

        int count = table.update(FIX_ID, entry, condition);

        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit updateExtJsonEvent(ret_code,node_id,ext_json,updated_time);
        return ret_code;
    }

    function isExist(string node_id) public view returns(bool) {
        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        condition.EQ("node_id", node_id);
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

            string memory dataStr = strConcat(strEmptyToSpace(entry.getString("node_id")), "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("blockchain_node_id")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("base_url")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("organization_name")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("lost_contact")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("contact_email")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("priority_level")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("enable")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("version")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("public_key")));
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