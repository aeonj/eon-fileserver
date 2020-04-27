package eon.hg.fileserver.util;

import java.util.Iterator;
import java.util.Map;

public class SqlTools {
    public static String getInsertSql(String table_name,Map inMap) {
        Map map = inMap;
        StringBuffer field = new StringBuffer();
        StringBuffer value = new StringBuffer();
        Iterator keyIt = map.keySet().iterator();
        while(keyIt.hasNext()){
            final String fieldInMap = (String) keyIt.next();
            final Object valueInMap = map.get(fieldInMap);
            field.append(fieldInMap).append(",");
            if(valueInMap != null)
            {
                value.append("'").append(valueInMap.toString()).append("',");
            }
            else
            {
                value.append("null,");
            }
        }
        field.deleteCharAt(field.length()-1);
        value.deleteCharAt(value.length()-1);
        StringBuffer strSQL = new StringBuffer();
        strSQL.append("insert into ").append(table_name).append(" (").append(field).append(") ")
                .append("values (").append(value).append(")");
        return strSQL.toString();
    }

    public static String getUpdateSql(String table_name,Map inMap, String where) {
        Map map = inMap;
        StringBuffer strSQL = new StringBuffer();
        Iterator it = map.keySet().iterator();
        final String strRounder = "'";
        final String breaker = " ";
        final String equal = "=";
        final String seperator = ",";
        strSQL.append("update ").append(table_name).append(" set ");
        while(it.hasNext()){
            final String field = (String) it.next();
            strSQL.append(field).append(equal)
                    .append(strRounder).append(map.get(field) == null ? "" : map.get(field)).append(strRounder)
                    .append(seperator).append(breaker);
        }
        strSQL.deleteCharAt(strSQL.length()-2);
        strSQL.append("where ").append(where);
        return strSQL.toString();
    }

    public static String getDeleteSql(String table_name,String where) {
        StringBuffer strSQL = new StringBuffer();
        strSQL.append("delete from ").append(table_name);
        strSQL.append(" where ").append(where);
        return strSQL.toString();
    }
}
