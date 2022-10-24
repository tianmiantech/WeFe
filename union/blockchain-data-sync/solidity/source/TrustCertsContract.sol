pragma solidity>=0.4.24 <0.6.11;
pragma experimental ABIEncoderV2;

import "./Table.sol";


contract TrustCertsContract{
    string constant TABLE_NAME = "trust_certs";
    string constant FIX_ID = "trust_certs";


    TableFactory tableFactory;


    event insertEvent(int256 ret_code,string[] params,string ext_json);
    event deleteBySerialNumberEvent(int256 ret_code,string serial_number);

    constructor() public {
        tableFactory = TableFactory(0x1001);
        tableFactory.createTable(TABLE_NAME, "fix_id", "cert_id,serial_number,cert_content,p_cert_id,issuer_org,issuer_cn,subject_org,subject_cn,is_ca_cert,is_root_cert,created_time,updated_time,ext_json");
    }

    function insert(string[] params, string ext_json) public returns (int) {
        int256 ret_code = 0;
        if (isExistBySerialNumber(params[1])) {
            ret_code = -1;
            emit insertEvent(ret_code,params,ext_json);
            return -1;
        }
        Table table = tableFactory.openTable(TABLE_NAME);
        Entry entry = table.newEntry();
        entry.set("fix_id", FIX_ID);
        entry.set("cert_id", params[0]);
        entry.set("serial_number", params[1]);
        entry.set("cert_content", params[2]);
        entry.set("p_cert_id", params[3]);
        entry.set("issuer_org", params[4]);
        entry.set("issuer_cn", params[5]);
        entry.set("subject_org", params[6]);
        entry.set("subject_cn", params[7]);
        entry.set("is_ca_cert", params[8]);
        entry.set("is_root_cert", params[9]);
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

    function deleteBySerialNumber(string serial_number) public returns (int) {
        int256 ret_code = 0;
        if (!isExistBySerialNumber(serial_number)) {
            ret_code = -3;
            emit deleteBySerialNumberEvent(ret_code,serial_number);
            return ret_code;
        }

        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        condition.EQ("serial_number", serial_number);
        int count = table.remove(FIX_ID,condition);

        if(count >= 1){
            ret_code = 0;
        } else {
            ret_code = -2;
        }

        emit deleteBySerialNumberEvent(ret_code,serial_number);
        return ret_code;

    }

    function isExistBySerialNumber(string serial_number) public view returns(bool) {
        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        condition.EQ("serial_number", serial_number);
        Entries entries = table.select(FIX_ID, condition);
        if(uint256(entries.size()) > 0) {
            return true;
        }

        return false;
    }
}