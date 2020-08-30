package com.mgk.tau.persist;

import com.mgk.tau.utils.Validation;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MapperFactory {
    private static final Map<String, Mapper> mappers = new ConcurrentHashMap<String, Mapper>();

    static {
        Mapper<Integer> integerMapper = new IntegerMapper();
        Mapper<Long> longMapper = new LongMapper();
        Mapper<Boolean> booleanMapper = new BooleanMapper();

        mappers.put("int", integerMapper);
        mappers.put(Integer.class.getName(), integerMapper);
        mappers.put("long", integerMapper);
        mappers.put(Long.class.getName(), integerMapper);
        mappers.put("boolean", integerMapper);
        mappers.put(Boolean.class.getName(), integerMapper);
        mappers.put(String.class.getName(), integerMapper);
        mappers.put(Date.class.getName(), integerMapper);
    }

    private MapperFactory() {

    }

    public static Mapper getMapper(Field field) {
        Validation.assertTrueData(field != null, "Field must not be null");
        Mapper m = mappers.get(field.getType().getName());
        Validation.assertTrueData(m != null, String.format("Mapper must be registered for %s", field.getType()));
        return m;
    }

    public static Mapper getMapper(String typeName) {
        Mapper m = mappers.get(typeName);
        Validation.assertTrueData(m != null, String.format("Mapper must be registered for %s", typeName));
        return m;
    }
}

