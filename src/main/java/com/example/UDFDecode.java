package com.example;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;

/**
 * Created by liuhb on 2014/12/5.
 */

@Description(name = "decode,string",
        value = "_FUNC_(str, [fKey, sKey, tKey]) - returns the decode of str that"
                + " decode by  fKey, sKey, tKey ",
        extended = "Example:\n "
                + "  > SELECT _FUNC_('1CC7376126B8AE1DE343E4C20EAE9ADA') FROM src LIMIT 1;\n"
                + "  'Facebook'\n"
                + "  > SELECT _FUNC_('5BB6A40B0CEA149B0A1645E74C7E460C', 'fkey', 'skey', 'tkey') FROM src LIMIT 1;\n"
                + "  'Facebook'\n' b'\"")

public class UDFDecode extends UDF {
        private final Text reValue;

        public UDFDecode() {
                reValue = new Text();
        }

        public  Text evaluate(Text value) {
                return evaluate(value, null, null, null);
        }
        public Text evaluate(Text value,Text tfkey,Text tskey, Text ttkey  ) {
                if (value == null) {
                        return null;
                }

                reValue.clear();

                String strfkey = tfkey == null ? Encrypt.getfKey() : tfkey.toString();
                String strskey = tskey == null ? Encrypt.getsKey() : tskey.toString();
                String strtkey = ttkey == null ? Encrypt.getfKey() : ttkey.toString();

                String data = value.toString();

                String strValue = Encrypt.strDecode(data, strfkey, strskey, strtkey);

                reValue.set(strValue);
                return reValue;
        }
}
