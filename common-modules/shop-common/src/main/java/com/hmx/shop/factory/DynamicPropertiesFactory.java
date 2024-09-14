package com.hmx.shop.factory;

import org.apache.logging.log4j.util.Strings;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public interface DynamicPropertiesFactory {

    default public Map defaultMap(){
        return generateMap(Strings.EMPTY);
    }

    public Map generateMap(String flag);


    default Map<String, String> notFoundFlag(){
        HashMap<String, String> stringMap = new HashMap<>();
        stringMap.put("msg","not found relation flag");
        return stringMap;
    }

}
