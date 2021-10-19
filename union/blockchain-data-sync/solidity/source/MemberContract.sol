pragma solidity>=0.4.24 <0.6.11;
pragma experimental ABIEncoderV2;

import "./Table.sol";


contract MemberContract{
    string constant TABLE_NAME = "member";
    string constant FIX_ID = "fix_id_001";

    event insertEvent(int256 ret_code,string[] params,string ext_json);
    event updateEvent(int256 ret_code,string[] params,string ext_json);
    event updateExcludePublicKeyEvent(int256 ret_code,string[] params,string ext_json);
    event updatePublicKeyEvent(int256 ret_code,string id,string public_key);
    event deleteByIdEvent(int256 ret_code,string id);
    event updateExcludeLogoEvent(int256 ret_code,string[] params,string ext_json);
    event updateLogoByIdEvent(int256 ret_code,string id, string logo);
    event updateLastActivityTimeByIdEvent(int256 ret_code,string id, string lastActivityTime);
    event updateExtJsonEvent(int256 ret_code,string id, string ext_json);


    TableFactory tableFactory;


    constructor() public {
        tableFactory = TableFactory(0x1001);
        tableFactory.createTable(TABLE_NAME, "fix_id", "id,name,mobile,allow_open_data_set,hidden,freezed,lost_contact,public_key,email,gateway_uri,logo,created_time,updated_time,last_activity_time,log_time,ext_json");
    }


    /**
     * params[0] id
     * params[1] name
     * params[2] mobile
     * params[3] allow_open_data_set
     * params[4] hidden
     * params[5] freezed
     * params[6] lost_contact
     * params[7] public_key
     * params[8] email
     * params[9] gateway_uri
     * params[10] logo
     * params[11] created_time
     * params[12] updated_time
     * params[13] last_activity_time
     * params[14] log_time
     */
    function insert(string[] params,string ext_json) public returns (int256) {
        int256 ret_code = 0;

        if (isExist(params[0])) {
            ret_code = -1;
            emit insertEvent(ret_code,params,ext_json);
            return -1;
        }

        Table table = tableFactory.openTable(TABLE_NAME);
        Entry entry = table.newEntry();
        entry.set("fix_id", FIX_ID);
        entry.set("id", params[0]);
        entry.set("name", params[1]);
        entry.set("mobile", params[2]);
        entry.set("allow_open_data_set", params[3]);
        entry.set("hidden", params[4]);
        entry.set("freezed", params[5]);
        entry.set("lost_contact", params[6]);
        entry.set("public_key", params[7]);
        entry.set("email", params[8]);
        entry.set("gateway_uri", params[9]);
        entry.set("logo", params[10]);
        entry.set("created_time", params[11]);
        entry.set("updated_time", params[12]);
        entry.set("last_activity_time", params[13]);
        entry.set("log_time", params[14]);
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


    /**
     * params[0] id
     * params[1] name
     * params[2] mobile
     * params[3] allow_open_data_set
     * params[4] hidden
     * params[5] freezed
     * params[6] lost_contact
     * params[7] public_key
     * params[8] email
     * params[9] gateway_uri
     * params[10] logo
     * params[11] created_time
     * params[12] updated_time
     * params[13] last_activity_time
     */
    function update(string[] params, string ext_json) public returns (int256) {
        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("id", params[0]);

        Entry entry = table.newEntry();
        entry.set("name", params[1]);
        entry.set("mobile", params[2]);
        entry.set("allow_open_data_set", params[3]);
        entry.set("hidden", params[4]);
        entry.set("freezed", params[5]);
        entry.set("lost_contact", params[6]);
        entry.set("public_key", params[7]);
        entry.set("email", params[8]);
        entry.set("gateway_uri", params[9]);
        entry.set("logo", params[10]);
        entry.set("created_time", params[11]);
        entry.set("updated_time", params[12]);
        entry.set("last_activity_time", params[13]);
        entry.set("log_time", params[14]);
        entry.set("ext_json", ext_json);

        int count = table.update(FIX_ID, entry, condition);

        int256 ret_code = 0;
        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit updateEvent(ret_code,params,ext_json);
        return ret_code;
    }


    /**
    * params[0] id
    * params[1] name
    * params[2] mobile
    * params[3] allow_open_data_set
    * params[4] hidden
    * params[5] freezed
    * params[6] lost_contact
    * params[7] public_key
    * params[8] email
    * params[9] gateway_uri
    * params[10] updated_time
    * params[11] last_activity_time
    */
    function updateExcludeLogo(string[] params, string ext_json) public returns (int256) {
        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("id", params[0]);

        Entry entry = table.newEntry();
        entry.set("name", params[1]);
        entry.set("mobile", params[2]);
        entry.set("allow_open_data_set", params[3]);
        entry.set("hidden", params[4]);
        entry.set("freezed", params[5]);
        entry.set("lost_contact", params[6]);
        entry.set("public_key", params[7]);
        entry.set("email", params[8]);
        entry.set("gateway_uri", params[9]);
        entry.set("updated_time", params[10]);
        entry.set("last_activity_time", params[11]);
        entry.set("log_time", params[12]);
        entry.set("ext_json", ext_json);

        int count = table.update(FIX_ID, entry, condition);

        int256 ret_code = 0;
        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit updateExcludeLogoEvent(ret_code,params,ext_json);
        return ret_code;
    }


    function updateLastActivityTimeById(string id, string lastActivityTime) public returns (int256) {
        int256 ret_code = 0;
        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("id", id);

        Entry entry = table.newEntry();
        entry.set("last_activity_time", lastActivityTime);

        int count = table.update(FIX_ID, entry, condition);

        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit updateLastActivityTimeByIdEvent(ret_code, id,lastActivityTime);
        return ret_code;
    }

    function updateLogoById(string id, string logo) public returns (int256) {

        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("id", id);

        Entry entry = table.newEntry();
        entry.set("logo", logo);

        int count = table.update(FIX_ID, entry, condition);

        int256 ret_code = 0;
        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit updateLogoByIdEvent(ret_code,id,logo);
        return ret_code;
    }



    function updateExcludePublicKey(string[] params, string ext_json) public returns (int256) {
        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("id", params[0]);

        Entry entry = table.newEntry();
        entry.set("name", params[1]);
        entry.set("mobile", params[2]);
        entry.set("allow_open_data_set", params[3]);
        entry.set("hidden", params[4]);
        entry.set("freezed", params[5]);
        entry.set("lost_contact", params[6]);
        entry.set("email", params[7]);
        entry.set("gateway_uri", params[8]);
        entry.set("logo", params[9]);
        entry.set("created_time", params[10]);
        entry.set("updated_time", params[11]);
        entry.set("last_activity_time", params[12]);
        entry.set("log_time", params[13]);
        entry.set("ext_json", ext_json);

        int count = table.update(FIX_ID, entry, condition);

        int256 ret_code = 0;
        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit updateExcludePublicKeyEvent(ret_code,params,ext_json);
        return ret_code;
    }



    function updatePublicKey(string id,string public_key) public returns (int256) {
        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("id", id);

        Entry entry = table.newEntry();
        entry.set("public_key", public_key);

        int count = table.update(FIX_ID, entry, condition);

        int256 ret_code = 0;
        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit updatePublicKeyEvent(ret_code,id,public_key);
        return ret_code;
    }


        function updateExtJson(string id,string ext_json) public returns (int256) {
            Table table = tableFactory.openTable(TABLE_NAME);

            Condition condition = table.newCondition();
            condition.EQ("id", id);

            Entry entry = table.newEntry();
            entry.set("ext_json", ext_json);

            int count = table.update(FIX_ID, entry, condition);

            int256 ret_code = 0;
            if(count >= 1){
                ret_code = 0;
            } else {
                ret_code = -2;
            }

            emit updateExtJsonEvent(ret_code,id,ext_json);
            return ret_code;
        }


    function deleteById(string id) public returns (int) {
        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        condition.EQ("id", id);
        int256 ret_code = 0;
        int count = table.remove(FIX_ID,condition);

        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit deleteByIdEvent(ret_code,id);

        return ret_code;

    }



    function selectById(string id) public view  returns (int256, string[]) {
        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        condition.EQ("id", id);
        Entries entries = table.select(FIX_ID, condition);
        if (0 == uint256(entries.size())) {
            return (-3, new string[](0));
        }

        return (0, wrapReturnMemberInfo(entries));
    }



    function selectAll() public view returns(int256, string[]) {
        Table table = tableFactory.openTable(TABLE_NAME);
        Entries entries = table.select(FIX_ID, table.newCondition());
        if (0 == uint256(entries.size())) {
            return (-3, new string[](0));
        }
        return (0, wrapReturnMemberInfo(entries));
    }


    function selectByPage(string id, string name, string hidden, string freezed, string lost_contact, int256 startIndex, int256 endIndex) public view returns(int256, string[]) {
        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        if(strLength(id) > 0) {
            condition.EQ("id", id);
        }
        if(strLength(name) > 0) {
            condition.EQ("name", name);
        }
        if(strLength(hidden) > 0) {
            condition.EQ("hidden", hidden);
        }
        if(strLength(freezed) > 0) {
            condition.EQ("freezed", freezed);
        }
        if(strLength(lost_contact) > 0) {
            condition.EQ("lost_contact", lost_contact);
        }
        condition.limit(startIndex, endIndex);
        Entries entries = table.select(FIX_ID, condition);
        if (0 == uint256(entries.size())) {
            return (-3, new string[](0));
        }
        return (0, wrapReturnMemberInfo(entries));
    }


    function count(string id, string name, string hidden, string freezed, string lost_contact) public view returns(int256){
        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        if(strLength(id) > 0) {
            condition.EQ("id", id);
        }
        if(strLength(name) > 0) {
            condition.EQ("name", name);
        }
        if(strLength(hidden) > 0) {
            condition.EQ("hidden", hidden);
        }
        if(strLength(freezed) > 0) {
            condition.EQ("freezed", freezed);
        }
        if(strLength(lost_contact) > 0) {
            condition.EQ("lost_contact", lost_contact);
        }
        Entries entries = table.select(FIX_ID, condition);
        return int256(entries.size());
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
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("name")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("mobile")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("allow_open_data_set")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("hidden")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("freezed")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("lost_contact")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("logo")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("public_key")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("email")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("gateway_uri")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("created_time")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("updated_time")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("last_activity_time")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("log_time")));
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
