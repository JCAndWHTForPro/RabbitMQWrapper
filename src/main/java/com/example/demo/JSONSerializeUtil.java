package com.example.demo;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Type JSONSerializeUtil.java
 * @Desc json序列化相关帮助类
 * @author fengsibo
 * @date 下午2:51:47
 * @version 
 */
@SuppressWarnings("ALL")
public class JSONSerializeUtil {

    private static ConcurrentHashMap objWriterCache = new ConcurrentHashMap(); // 缓存class到ObjectWriter的

    // 映射关系,格式为Class - > ObjectWriter对象
    private static ConcurrentHashMap objWriterCacheNoType = new ConcurrentHashMap(); // 缓存class到

    // ObjectWriter的映射关系,格式为Class - > ObjectWriter对象
    private static ConcurrentHashMap objWriterCacheNoNull = new ConcurrentHashMap(); // 缓存class到

    // ObjectWriter的映射关系,格式为Class - > ObjectWriter对象
    private static ObjectMapper mapper = new ObjectMapper();

    private static ObjectMapper mapperNoType = new ObjectMapper();

    private static ObjectMapper mapperNoNull = new ObjectMapper();


    private static ObjectWriter getObjWriter(Class<?> serializationView) {
        if (objWriterCache.get(serializationView) != null) {
            return (ObjectWriter) objWriterCache.get(serializationView);
        } else {
            ObjectWriter temp = mapper.writerWithView(serializationView);
            objWriterCache.put(serializationView, temp);
            return temp;
        }
    }

    private static ObjectWriter getObjWriterNoType(Class<?> serializationView) {
        if (objWriterCacheNoType.get(serializationView) != null) {
            return (ObjectWriter) objWriterCacheNoType.get(serializationView);
        } else {
            ObjectWriter temp = mapperNoType.writerWithView(serializationView);
            objWriterCacheNoType.put(serializationView, temp);
            return temp;
        }
    }

    private static ObjectWriter getObjWriterNoNull(Class<?> serializationView) {
        if (objWriterCacheNoNull.get(serializationView) != null) {
            return (ObjectWriter) objWriterCacheNoNull.get(serializationView);
        } else {
            mapperNoNull.setSerializationInclusion(Include.NON_NULL);
            ObjectWriter temp = mapperNoNull.writerWithView(serializationView);
            objWriterCacheNoNull.put(serializationView, temp);
            return temp;
        }
    }

    public static String jsonSerializer(Object originalObject) {
        try {
            mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
            ObjectWriter objectWriter = getObjWriter(originalObject.getClass());
            String json = objectWriter.writeValueAsString(originalObject);
            return json;
        } catch (Exception e) {
            throw new RuntimeException(String.format("对象%s序列化失败。", originalObject.getClass().getName()), e);
        }
    }

    public static String jsonSerializerNoType(Object originalObject) {
        try {
            ObjectWriter objectWriter = getObjWriterNoType(originalObject.getClass());
            String json = objectWriter.writeValueAsString(originalObject);
            return json;
        } catch (Exception e) {
            throw new RuntimeException(String.format("对象%s序列化失败。", originalObject.getClass().getName()), e);
        }
    }

    public static String jsonSerializerNoNull(Object originalObject) {
        try {
            ObjectWriter objectWriter = getObjWriterNoNull(originalObject.getClass());
            String json = objectWriter.writeValueAsString(originalObject);
            return json;
        } catch (Exception e) {
            throw new RuntimeException(String.format("对象%s序列化失败。", originalObject.getClass().getName()), e);
        }
    }

    public static <T> T jsonReSerializer(String jsonStr, Class<T> calzz) {
        try {
            mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
            ObjectReader reader = mapper.reader(calzz);
            T object = reader.readValue(jsonStr);
            return object;
        } catch (Exception e) {
            throw new RuntimeException(String.format("字符串[%.20s]...反序列化失败。", jsonStr), e);
        }
    }

    public static <T> T jsonReSerializerNoType(String jsonStr, Class<T> calzz) {
        try {
            ObjectReader reader = mapperNoType.reader(calzz);
            T object = reader.readValue(jsonStr);
            return object;
        } catch (Exception e) {
            throw new RuntimeException(String.format("字符串[%.20s]...反序列化失败。", jsonStr), e);
        }
    }

    public static <T> T jsonReSerializerNoNull(String jsonStr, Class<T> calzz) {
        try {
            ObjectReader reader = mapperNoNull.reader(calzz);
            T object = reader.readValue(jsonStr);
            return object;
        } catch (Exception e) {
            throw new RuntimeException(String.format("字符串[%.20s]...反序列化失败。", jsonStr), e);
        }
    }

    public static <T> T jsonReSerializerNoType(String jsonStr, Type type) {
        try {
            ObjectReader reader = mapperNoType.reader(TypeFactory.defaultInstance().constructType(type));
            T object = reader.readValue(jsonStr);
            return object;
        } catch (Exception e) {
            throw new RuntimeException(String.format("字符串[%.20s]...反序列化失败。", jsonStr), e);
        }
    }

    public static <T> T jsonReSerializerNoType(String jsonStr, TypeReference<T> typeReference) {
        try {
            ObjectReader reader = mapperNoType.reader(typeReference);
            T object = reader.readValue(jsonStr);
            return object;
        } catch (Exception e) {
            throw new RuntimeException(String.format("字符串[%.20s]...反序列化失败。", jsonStr), e);
        }
    }

    public static <T, P> T jsonReSerializerNoType(String jsonStr, Class<T> type, Class<P> parametric) {
        try {
            JavaType t = mapperNoType.getTypeFactory().constructParametricType(type, parametric);
            return mapperNoType.readValue(jsonStr, t);
        } catch (Exception e) {
            throw new RuntimeException(String.format("字符串[%.20s]...反序列化失败。", jsonStr), e);
        }
    }

    public static <T, P> T jsonReSerializerNest(String jsonStr, Class<T> type, Class<P>[] parametric) {
        try {
            JavaType t = getJacaType(type, parametric);
            return mapperNoType.readValue(jsonStr, t);
        } catch (Exception e) {
            throw new RuntimeException(String.format("字符串[%.20s]...反序列化失败。", jsonStr), e);
        }
    }


    private static <T, P> JavaType getJacaType(Class<T> cls, Class<P>[] parametric) {
        if (parametric == null || parametric.length == 0) {
            throw new RuntimeException(String.format("序列化方法调用错误导致字符串反序列化失败，请确认！"));
        } else if (parametric.length == 1) {
            return mapperNoType.getTypeFactory().constructParametricType(cls, parametric[0]);
        } else {
            JavaType type = null;
            for (int i = parametric.length; i >= 2; i--) {
                if (type == null) {// 处理数组最后两个
                    type = mapperNoType.getTypeFactory().constructParametricType(parametric[i - 2], parametric[i - 1]);
                } else if (i <= parametric.length - 2) {
                    type = mapperNoType.getTypeFactory().constructParametricType(parametric[i - 1], type);
                }
            }
            return mapperNoType.getTypeFactory().constructParametricType(cls, type);
        }
    }
}
