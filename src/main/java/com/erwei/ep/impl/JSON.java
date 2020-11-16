package com.erwei.ep.impl;

import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class JSON {
	final public static ObjectMapper mapper = new ObjectMapper();
	
	 /**
     * String类型的javaType
     */
    public static final JavaType StringJavaType = mapper.getTypeFactory().constructType(
            String.class);

    /**
     * 支持Map<String,String>
     */
    public static final JavaType StringStringMap = mapper.getTypeFactory().constructMapType(HashMap.class,
            String.class, String.class);
    
    /**
     * 支持Map<String,Integer>
     */
    public static final JavaType StringIntegerMap = mapper.getTypeFactory().constructMapType(HashMap.class,
            String.class, Integer.class);

    /**
     * 支持Map<String,Map<String,String>>
     */
    public static final JavaType StringStringStringMap = mapper.getTypeFactory()
            .constructMapType(HashMap.class, StringJavaType, StringStringMap);

    /**
     * 支持Map<String,Map<String,Map<String,String>>>
     */
    public static final JavaType StringStringStringStringMap = mapper.getTypeFactory()
            .constructMapType(HashMap.class, StringJavaType, StringStringStringMap);

    /**
     * 支持List<Object>
     */
    public static final JavaType ListObject = mapper.getTypeFactory().constructCollectionType(ArrayList.class, Object.class);

    /**
     * 支持List<List<Object>>
     */
    public static final JavaType ListListObject = mapper.getTypeFactory().constructCollectionType(ArrayList.class, ListObject);

	public static String toJSON2(Object obj) {
		try {
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("无法解析为JSON", e);
		}
	}

	public static String toJSON(Object obj) {
		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("无法解析为JSON", e);
		}
	}
	
	public static <T> T fromJSON(String json,Class<T> valueType){
		try {
			return mapper.readValue(json,valueType);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("无法从JSON读取", e);
		}
		
	}
	public static <T> T fromJSON(String json,JavaType valueType){
		try {
			return mapper.readValue(json,valueType);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("无法从JSON读取", e);
		}
		
	}
	
	public static <T> T fromJSON(String json,TypeReference<T> valueTypeRef){
		try {
			return mapper.readValue(json,valueTypeRef);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("无法从JSON读取", e);
		}
		
	}

}
