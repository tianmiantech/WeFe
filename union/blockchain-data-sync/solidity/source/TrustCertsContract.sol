pragma solidity>=0.4.24 <0.6.11;
pragma experimental ABIEncoderV2;

import "./Table.sol";


contract TrustCertsContract{
    string constant TABLE_NAME = "trust_certs";
    string constant FIX_ID = "trust_certs";


    TableFactory tableFactory;


    event insertEvent(int256 ret_code,string[] params,string ext_json);
    event deleteByCertIdEvent(int256 ret_code,string cert_id);

    constructor() public {
        tableFactory = TableFactory(0x1001);
        tableFactory.createTable(TABLE_NAME, "fix_id", "cert_id,member_id,serial_number,cert_content,p_cert_id,issuer_org,issuer_cn,subject_org,subject_cn,is_ca_cert,is_root_cert,created_time,updated_time,ext_json");
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
        entry.set("cert_id", params[0]);
        entry.set("member_id", params[1]);
        entry.set("serial_number", params[2]);
        entry.set("cert_content", params[3]);
        entry.set("p_cert_id", params[4]);
        entry.set("issuer_org", params[5]);
        entry.set("issuer_cn", params[6]);
        entry.set("subject_org", params[7]);
        entry.set("subject_cn", params[8]);
        entry.set("is_ca_cert", params[9]);
        entry.set("is_root_cert", params[10]);
        entry.set("created_time", params[11]);
        entry.set("updated_time", params[12]);
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

    function deleteByCertId(string cert_id) public returns (int) {
        int256 ret_code = 0;
        if (!isExist(cert_id)) {
            ret_code = -3;
            emit deleteByCertIdEvent(ret_code,cert_id);
            return ret_code;
        }

        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        condition.EQ("cert_id", cert_id);
        int count = table.remove(FIX_ID,condition);

        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit deleteByCertIdEvent(ret_code,cert_id);
        return ret_code;

    }

    function isExist(string cert_id) public view returns(bool) {
        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        condition.EQ("cert_id", cert_id);
        Entries entries = table.select(FIX_ID, condition);
        if(uint256(entries.size()) > 0) {
            return true;
        }

        return false;
    }
}