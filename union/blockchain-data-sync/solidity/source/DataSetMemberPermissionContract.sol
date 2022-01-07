pragma solidity>=0.4.24 <0.6.11;
pragma experimental ABIEncoderV2;

import "./Table.sol";

/**
 * 数据集权限合约 
 */
contract DataSetMemberPermissionContract{
    // 表名
    string constant TABLE_NAME = "data_set_member_permission";
    string constant FIX_ID = "fix_id_003";


    TableFactory tableFactory;

    event insertEvent(int256 ret_code,string id, string data_set_id, string member_id, string created_time, string updated_time, int log_time,string ext_json);
    event updateEvent(int256 ret_code,string id, string data_set_id, string member_id, string created_time, string updated_time,int log_time,string ext_json);
    event deleteByDataSetIdEvent(int256 ret_code,string data_set_id);
    event updateExtJsonEvent(int256 ret_code,string data_set_id, string ext_json);

    constructor() public {
        // 创建表
        tableFactory = TableFactory(0x1001);
        tableFactory.createTable(TABLE_NAME, "fix_id", "id,data_set_id,member_id,created_time,updated_time,log_time,ext_json");
    }




    function insert(string id, string data_set_id, string member_id, string created_time, string updated_time, int log_time,string ext_json) public returns (int) {
        int256 ret_code = 0;
        if (isExist(id)) {
            ret_code = -1;
            emit insertEvent(ret_code,id,data_set_id,member_id,created_time,updated_time,log_time,ext_json);
            return ret_code;
        }

        Table table = tableFactory.openTable(TABLE_NAME);
        Entry entry = table.newEntry();
        entry.set("fix_id", FIX_ID);
        entry.set("id", id);
        entry.set("data_set_id", data_set_id);
        entry.set("member_id", member_id);
        entry.set("created_time", created_time);
        entry.set("updated_time", updated_time);
        entry.set("log_time", log_time);

        entry.set("ext_json", ext_json);


        int256 count = table.insert(FIX_ID, entry);

        if(count == 1){
            //成功
            ret_code = 0;
        } else {
            //失败
            ret_code = -2;
        }

        emit insertEvent(ret_code,id,data_set_id,member_id,created_time,updated_time,log_time,ext_json);

        return ret_code;
    }




    function update(string id, string data_set_id, string member_id, string created_time, string updated_time,int log_time,string ext_json) public returns (int) {
        int256 ret_code = 0;

        if (!isExist(id)) {
            ret_code = -3;
            emit updateEvent(ret_code,id,data_set_id,member_id,created_time,updated_time,log_time,ext_json);
            return ret_code;
        }

        Table table = tableFactory.openTable(TABLE_NAME);

        // where条件
        Condition condition = table.newCondition();
        condition.EQ("id", id);

        // 更新的字段
        Entry entry = table.newEntry();
        entry.set("data_set_id", data_set_id);
        entry.set("member_id", member_id);
        entry.set("created_time", created_time);
        entry.set("updated_time", updated_time);
        entry.set("log_time", log_time);

        entry.set("ext_json", ext_json);

        int count = table.update(FIX_ID, entry, condition);

        if(count >= 1){
            //成功
            ret_code = 0;
        } else {
            //失败
            ret_code = -2;
        }

        emit updateEvent(ret_code,id,data_set_id,member_id,created_time,updated_time,log_time,ext_json);
        // 更新
        return ret_code;
    }


    function deleteByDataSetId(string data_set_id) public returns (int) {
        int256 ret_code = 0;
        if (!isExist(id)) {
            ret_code = -3;
            emit deleteByDataSetIdEvent(ret_code,id);
            return ret_code;
        }
        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        condition.EQ("data_set_id", data_set_id);

        int count = table.remove(FIX_ID,condition);

        if(count >= 1){
            //成功
            ret_code = 0;
        } else {
            //失败
            ret_code = -2;
        }

        emit deleteByDataSetIdEvent(ret_code,data_set_id);

        return ret_code;

    }



    function selectById(string id) public view returns (int256, string[]) {
        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        condition.EQ("id", id);
        Entries entries = table.select(FIX_ID, condition);
        if (0 == uint256(entries.size())) {
            // 记录不存在
            return (-3, new string[](0));
        }

        return (0, wrapReturnMemberInfo(entries));
    }

    function selectByMemberId(string member_id) public view returns (int256, string[]) {
        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        condition.EQ("member_id", member_id);
        Entries entries = table.select(FIX_ID, condition);
        if (0 == uint256(entries.size())) {
            // 记录不存在
            return (-3, new string[](0));
        }

        return (0, wrapReturnMemberInfo(entries));
    }


    /**
     * 查询所有
     */
    function selectAll() public view returns(int256, string[]) {
        Table table = tableFactory.openTable(TABLE_NAME);
        Entries entries = table.select(FIX_ID, table.newCondition());
        if (0 == uint256(entries.size())) {
            // 查询为空
            return (-3, new string[](0));
        }
        return (0, wrapReturnMemberInfo(entries));
    }

    /**
     * 分页查询
     * startIndex 开始位置
     * endIndex 结束位置
     */
    function selectByPage(int256 startIndex, int256 endIndex) public view returns(int256, string[]) {
        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();


        condition.limit(startIndex, endIndex);
        Entries entries = table.select(FIX_ID, condition);
        if (0 == uint256(entries.size())) {
            // 查询为空
            return (-3, new string[](0));
        }
        return (0, wrapReturnMemberInfo(entries));
    }

    function updateExtJson(string data_set_id,string ext_json) public returns (int256) {
        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("data_set_id", data_set_id);

        Entry entry = table.newEntry();
        entry.set("ext_json", ext_json);

        int count = table.update(FIX_ID, entry, condition);

        int256 ret_code = 0;
        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit updateExtJsonEvent(ret_code,data_set_id,ext_json);
        return ret_code;
    }


    /**
     * 是否存在Id的信息
     */
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


    /**
     * 包装成员信息
     * 参数1：
     */
    function wrapReturnMemberInfo(Entries entries) private view returns(string[]) {
        string[] memory data_list = new string[](uint256(entries.size()));
        for (int256 i = 0; i < entries.size(); ++i) {
            Entry entry = entries.get(i);
            string memory dataStr = strConcat(strEmptyToSpace(entry.getString("id")), "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("data_set_id")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("member_id")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("created_time")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("updated_time")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("log_time")));
            dataStr = strConcat(dataStr, "|");
            dataStr = strConcat(dataStr, strEmptyToSpace(entry.getString("ext_json")));

            data_list[uint256(i)] = dataStr;
        }

        return data_list;
    }

    /**
     * 字符串拼接
     */
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


    /**
     * 返回字符串长度
     */
    function strLength(string str) private returns(uint256){
        return bytes(str).length;
    }

    /**
      * 空字符串转空格
     */
    function strEmptyToSpace(string str) private returns(string) {
        if(strLength(str) == 0) {
            return " ";
        }
        return str;
    }
}