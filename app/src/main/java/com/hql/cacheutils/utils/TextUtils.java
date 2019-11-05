package com.hql.cacheutils.utils;

import java.util.List;
import java.util.Map;

/**
 * 对象处理工具类
 */
public class TextUtils {

    /**
     * 判断对象处理为空
     * @param obj
     * @return
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        } else if (obj instanceof String) {
            String string = (String) obj;
            if (string.length() <= 0) {
                return true;
            } else if ("".equals(string)) {
                return true;
            } else return "".equals(string.trim());
        } else if (obj instanceof List) {
            List list = (List) obj;
            if (list.size() <= 0) {
                return true;
            } else return list.isEmpty();
        } else if (obj instanceof Map) {
            Map map = (Map) obj;
            if (map.size() <= 0) {
                return true;
            } else return map.isEmpty();
        } else if (obj instanceof Object[]) {
            Object[] objs = (Object[]) obj;
            return objs.length <= 0;
        }
        return false;
    }
}