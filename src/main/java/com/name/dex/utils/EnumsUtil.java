package com.name.dex.utils;

import java.util.function.Predicate;

public class EnumsUtil {

    public static <T extends Enum<T>> T safeValueOf(String name, Class<T> enumClass) {
        return safeValueOf(name, enumClass, false);
    }

    public static <T extends Enum<T>> T safeValueIgnoreCaseOf(String name, Class<T> enumClass) {
        return safeValueOf(name, enumClass, true);
    }

    public static <T extends Enum<T>> T safeValueOf(String name, Class<T> enumClass, boolean ignoreCase) {
        T[] values = enumClass.getEnumConstants();

        T value = null;

        Predicate<String> equalsPredicate = ignoreCase
            ? valueName -> valueName.equalsIgnoreCase(name)
            : valueName -> valueName.equals(name);

        for(int i = 0; i < values.length && value == null; i++) {
            T v = values[i];
            if(equalsPredicate.test(v.name())) {
                value = v;
            }
        }

        return value;
    }

}
