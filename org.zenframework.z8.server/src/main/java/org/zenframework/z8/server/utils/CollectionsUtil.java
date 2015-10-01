package org.zenframework.z8.server.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CollectionsUtil {

    private CollectionsUtil() {}

    public static boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null)
            return true;
        if (o1 != null && o2 == null || o1 == null && o2 != null)
            return false;
        if (o1 instanceof Map && o2 instanceof Map && !equals((Map<?, ?>) o1, (Map<?, ?>) o2))
            return false;
        else if (o1 instanceof Collection && o2 instanceof Collection && !equals((List<?>) o1, (List<?>) o2))
            return false;
        else
            return o1.equals(o2);
    }

    public static boolean equals(Map<?, ?> o1, Map<?, ?> o2) {
        if (o1 == null && o2 == null)
            return true;
        if (o1 != null && o2 == null || o1 == null && o2 != null || o1.size() != o2.size())
            return false;
        for (Map.Entry<?, ?> entry : o1.entrySet()) {
            if (!equals(entry.getValue(), o2.get(entry.getKey())))
                return false;
        }
        return true;
    }

    public static boolean equals(List<?> o1, List<?> o2) {
        if (o1 == null && o2 == null)
            return true;
        if (o1 != null && o2 == null || o1 == null && o2 != null || o1.size() != o2.size())
            return false;
        for (int i = 0; i < o1.size(); i++) {
            if (!equals(o1.get(i), o2.get(i)))
                return false;
        }
        return true;
    }

}
