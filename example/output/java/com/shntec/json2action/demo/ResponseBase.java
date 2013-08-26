
package com.shntec.json2action.demo;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class ResponseBase {


    public String ToJson()
        throws JsonGenerationException, JsonMappingException, IOException, IllegalAccessException, IllegalArgumentException, NoSuchMethodException, SecurityException, InvocationTargetException
    {
        Field field[] = (this.getClass().getDeclaredFields());
        ObjectMapper objectmap = new ObjectMapper();
        Map<String, Object> map = new HashMap<String, Object>();
        for (Field f: field) {
            if (("this$0" != f.getName())) {
                map.put(f.getName(), this.getClass().getMethod("get" + f.getName()).invoke(this));
            }
        }
        return (objectmap.writeValueAsString(map));
    }

}
