package com.example;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;

/**
 * Created by sponge on 15-3-18.
 */

@Description(name = "Encode,string",
        value = "_FUNC_(str, [fKey, sKey, tKey]) - returns the Encode of str that"
                + " Encode by fKey, sKey, tKey ",
        extended = "Example:\n "
                + " > SELECT _FUNC_('Facebook') FROM src LIMIT 1;\n"
                + " '1CC7376126B8AE1DE343E4C20EAE9ADA'\n"
                + " > SELECT _FUNC_('Facebook', 'fkey', 'skey', 'tkey') FROM src LIMIT 1;\n"
                + " '5BB6A40B0CEA149B0A1645E74C7E460C'\n' b'\"")
public class UDFEncode extends UDF {
    private final Text reValue;
    public UDFEncode() {
        reValue = new Text();
    }
    public Text evaluate(Text value) {
        return evaluate(value, null, null, null);
    }
    public Text evaluate(Text value,Text tfkey,Text tskey, Text ttkey ) {
        if (value == null) {
            return null;
        }
        reValue.clear();
        String strfkey = tfkey == null ? Encrypt.getfKey() : tfkey.toString();
        String strskey = tskey == null ? Encrypt.getsKey() : tskey.toString();
        String strtkey = ttkey == null ? Encrypt.getfKey() : ttkey.toString();
        String data = value.toString();
        String strValue = Encrypt.strEncode(data, strfkey, strskey, strtkey);
        reValue.set(strValue);
        return reValue;
    }
}
