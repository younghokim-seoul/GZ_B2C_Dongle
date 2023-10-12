package com.hoho.android.usbserial.examples;

import java.util.HashMap;
import java.util.Map;

public class ServiceLocator {
    private static Map<Class<?>,Object> objectMap = new HashMap<>();

    public static <T> T get(Class<T> c){
        return (T) objectMap.get(c);
    }

    public static void register(Class<?> c , Object obj){
        objectMap.put(c,obj);
    }
}
