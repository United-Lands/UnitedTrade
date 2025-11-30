package org.unitedlands.trade.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.unitedlands.trade.utils.annotations.Info;
import org.unitedlands.utils.Logger;

public class FieldHelper {

    public static String getFieldValuesString(Class<?> clazz, Object o) {
        List<String> stringFields = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            var info = fields[i].getAnnotation(Info.class);
            if (info != null) {
                try {
                    var value = fields[i].get(o);
                    stringFields.add("<bold><gold>" + fields[i].getName() + ": </gold></bold>" + value);
                } catch (Exception ex) {
                    Logger.logError(ex.getMessage());
                    continue;
                }
            }
        }
        return String.join(" | ", stringFields);
    }
}
