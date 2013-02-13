package com.shntec.json2action;

import java.io.*;
import java.lang.reflect.Field;
import java.net.*;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonschema.SchemaAware;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;

public class Parser {
	public class GenerationException extends RuntimeException {

	    public GenerationException(String message, Throwable cause) {
	        super(message, cause);
	    }

	    public GenerationException(String message) {
	        super(message);
	    }

	    public GenerationException(Throwable cause) {
	        super(cause);
	    }

	    public GenerationException(String message, ClassNotFoundException cause) {
	        super(message, cause);
	    }
	};
	
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private JsonNode content = null;
	
	public Parser(URL json){
		try {
			content = OBJECT_MAPPER.readTree(new File(URI.create(json.toString())));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new GenerationException("Could not process JSON in source file", e);
		}
	};
	
	public Parser(){
		
	};
	
	public ObjectNode parse(){
		if (null != content)
			return schemaFromJson(content);
		else
			return null;
	};
	
	public ObjectNode parse(URL url){
		try {
			content = OBJECT_MAPPER.readTree(new File(URI.create(url.toString())));
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			throw new GenerationException("Could not process JSON in source file", e);
		}		
		return parse();
	};
	
	private ObjectNode schemaFromJson(JsonNode node){
		if (node.isObject()) 
		{
            return objectSchema(node);
		} 
		else if (node.isArray()) 
		{
            return arraySchema(node);
        } 
		else 
		{
            return simpleTypeSchema(node);
        }
	};
	
	private ObjectNode objectSchema(JsonNode jsonObject) {

        ObjectNode schema = OBJECT_MAPPER.createObjectNode();
        schema.put("type", "object");

        ObjectNode properties = OBJECT_MAPPER.createObjectNode();
        for (Iterator<String> iter = jsonObject.fieldNames(); iter.hasNext();) 
        {
            String property = iter.next();
            properties.put(property, schemaFromJson(jsonObject.get(property)));
        }
        schema.put("properties", properties);
        schema.put("required", true);

        return schema;
    };

    private ObjectNode arraySchema(JsonNode jsonArray) {
        ObjectNode schema = OBJECT_MAPPER.createObjectNode();

        schema.put("type", "array");

        if (jsonArray.size() > 0) {

            JsonNode arrayItem = jsonArray.get(0).isObject() ? mergeArrayItems(jsonArray) : jsonArray.get(0);

            schema.put("items", schemaFromJson(arrayItem));
        }
        schema.put("required", true);
        return schema;
    };

    private JsonNode mergeArrayItems(JsonNode jsonArray) {

        ObjectNode mergedItems = OBJECT_MAPPER.createObjectNode();

        for (JsonNode item : jsonArray) {
            if (item.isObject()) {
                mergedItems.putAll((ObjectNode) item);
            }
        }

        return mergedItems;
    };

    private ObjectNode simpleTypeSchema(JsonNode jsonValue) {

        try {

            Object valueAsJavaType = OBJECT_MAPPER.treeToValue(jsonValue, Object.class);

            SchemaAware valueSerializer = getValueSerializer(valueAsJavaType);

            ObjectNode n = (ObjectNode) valueSerializer.getSchema(OBJECT_MAPPER.getSerializerProvider(), null);
            n.put("required", true);
            return n;
        } catch (JsonMappingException e) {
            throw new GenerationException("Unable to generate a schema for this json example: " + jsonValue, e);
        } catch (JsonProcessingException e) {
            throw new GenerationException("Unable to generate a schema for this json example: " + jsonValue, e);
        }

    };
    
    private SchemaAware getValueSerializer(Object valueAsJavaType) throws JsonMappingException {

        SerializerProvider serializerProvider = new DefaultSerializerProvider.Impl().createInstance(OBJECT_MAPPER.getSerializationConfig(), BeanSerializerFactory.instance);

        if (valueAsJavaType == null) {
            return NullSerializer.instance;
        } else {
            Class<? extends Object> javaTypeForValue = valueAsJavaType.getClass();
            JsonSerializer<Object> valueSerializer = serializerProvider.findValueSerializer(javaTypeForValue, null);
            return (SchemaAware) valueSerializer;
        }
    }
}
