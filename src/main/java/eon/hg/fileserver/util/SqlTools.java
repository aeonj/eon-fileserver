package eon.hg.fileserver.util;

import cn.hutool.core.date.DateUtil;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

public class SqlTools {
    public static String getInsertSql(String table_name, Map inMap) {
        Map map = inMap;
        StringBuffer field = new StringBuffer();
        StringBuffer value = new StringBuffer();
        Iterator keyIt = map.keySet().iterator();
        while (keyIt.hasNext()) {
            final String fieldInMap = (String) keyIt.next();
            final Object valueInMap = map.get(fieldInMap);
            field.append(fieldInMap).append(",");
            if (valueInMap != null) {
                if (valueInMap instanceof String) {
                    value.append("'").append(valueInMap.toString()).append("',");
                } else if (valueInMap instanceof Date) {
                    value.append("'").append(DateUtil.formatDateTime((Date) valueInMap)).append("',");
                } else {
                    value.append(valueInMap).append(",");
                }
            } else {
                value.append("null,");
            }
        }
        field.deleteCharAt(field.length() - 1);
        value.deleteCharAt(value.length() - 1);
        StringBuffer strSQL = new StringBuffer();
        strSQL.append("insert into ").append(table_name).append(" (").append(field).append(") ")
                .append("values (").append(value).append(")");
        return strSQL.toString();
    }

    public static String getUpdateSql(String table_name, Map inMap, String where) {
        Map map = inMap;
        StringBuffer strSQL = new StringBuffer();
        Iterator it = map.keySet().iterator();
        final String strRounder = "'";
        final String breaker = " ";
        final String equal = "=";
        final String seperator = ",";
        strSQL.append("update ").append(table_name).append(" set ");
        while (it.hasNext()) {
            final String field = (String) it.next();
            final Object valueInMap = map.get(field);
            if (valueInMap != null) {
                if (valueInMap instanceof String) {
                    strSQL.append(field).append(equal)
                            .append(strRounder).append(valueInMap).append(strRounder)
                            .append(seperator).append(breaker);
                } else if (valueInMap instanceof Date) {
                    strSQL.append(field).append(equal)
                            .append(strRounder).append(DateUtil.formatDateTime((Date) valueInMap)).append(strRounder)
                            .append(seperator).append(breaker);
                } else {
                    strSQL.append(field).append(equal)
                            .append(valueInMap)
                            .append(seperator).append(breaker);
                }
            } else {
                strSQL.append(field).append(equal)
                        .append("null")
                        .append(seperator).append(breaker);
            }

        }
        strSQL.deleteCharAt(strSQL.length() - 2);
        strSQL.append("where ").append(where);
        return strSQL.toString();
    }

    public static String getDeleteSql(String table_name, String where) {
        StringBuffer strSQL = new StringBuffer();
        strSQL.append("delete from ").append(table_name);
        strSQL.append(" where ").append(where);
        return strSQL.toString();
    }
}
