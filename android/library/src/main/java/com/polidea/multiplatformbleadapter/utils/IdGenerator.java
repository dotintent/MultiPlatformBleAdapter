package com.polidea.multiplatformbleadapter.utils;

import java.util.HashMap;

public class IdGenerator {
    private static final HashMap<IdGeneratorKey, Integer> idMap = new HashMap<>();
    private static int nextKey = 0;

    public static int getIdForKey(IdGeneratorKey idGeneratorKey) {
        Integer id = idMap.get(idGeneratorKey);
        if (id != null) {
            return id;
        }
        idMap.put(idGeneratorKey, ++nextKey);
        return nextKey;
    }

    public static void clear() {
        idMap.clear();
        nextKey = 0;
    }
}
