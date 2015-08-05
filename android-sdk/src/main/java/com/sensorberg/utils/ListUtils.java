package com.sensorberg.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ListUtils {

    public static interface Filter<A> {
        boolean matches(A object);
    }

    /**
     * do somthing with the key of the map on the value (store it in the object)
     * @param <K>
     * @param <S>
     */
    public static interface MapKeyConverter<K,S> {
        void useMapKey(S Key, K object);
    }

    public static interface Mapper<INPUT, OUTPUT>{
        OUTPUT map(INPUT input);
    }

    public static interface KeyProvider<OBJECT, KEY_TYPE> {
        KEY_TYPE map(OBJECT resolveAction);
    }

    public static <INPUT, OUTPUT> List<OUTPUT> map(List<INPUT> inputs, Mapper<INPUT, OUTPUT> mapper){
        ArrayList<OUTPUT> value = new ArrayList<OUTPUT>();
        for (INPUT object : inputs) {
            OUTPUT mappedObject  = mapper.map(object);
            if (mappedObject != null) {
                value.add(mappedObject);
            }
        }
        return value;
    }

    public static <MAP_VALUE_TYPE, MAP_KEY_TYPE> List<MAP_VALUE_TYPE> filter(Map<MAP_KEY_TYPE, MAP_VALUE_TYPE> map, Filter<MAP_VALUE_TYPE> filter, MapKeyConverter<MAP_VALUE_TYPE, MAP_KEY_TYPE> mapKeyConverter) {
        ArrayList<MAP_VALUE_TYPE> value = new ArrayList<MAP_VALUE_TYPE>();
        Set<MAP_KEY_TYPE> keySet = map.keySet();
        for (MAP_KEY_TYPE key : keySet) {
            MAP_VALUE_TYPE object = map.get(key);
            if (filter.matches(object)){
                value.add(object);
                mapKeyConverter.useMapKey(key, object);
            }
        }
        return value;
    }



    public static <K> List<K> filter(List<K> list, Filter<K> filter){
        ArrayList<K> value = new ArrayList<K>();
        for(K object : list){
            if (filter.matches(object)){
                value.add(object);
            }
        }
        return value;
    }

    public static <OBJECT, KEY_TYPE> Map<KEY_TYPE, OBJECT> toMap(List<OBJECT> instantActions, KeyProvider<OBJECT, KEY_TYPE> keyProvider) {
        Map<KEY_TYPE, OBJECT> value = new HashMap<KEY_TYPE, OBJECT>();
        for (OBJECT instantAction : instantActions) {
            value.put(keyProvider.map(instantAction), instantAction);
        }
        return value;
    }
}
