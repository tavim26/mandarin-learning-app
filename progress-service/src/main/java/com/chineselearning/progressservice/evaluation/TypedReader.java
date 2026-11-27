package com.chineselearning.progressservice.evaluation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


final class TypedReader {

    private static final int MAX_ITEMS = 200;
    private static final int MAX_TEXT_LENGTH = 2_000;

    private final Map<String, Object> source;
    private final Function<String, RuntimeException> error;

    TypedReader(Map<String, Object> source, Function<String, RuntimeException> error)
    {
        this.source = source;
        this.error = error;
    }

    String text(String key)
    {
        Object value = source.get(key);

        if (value == null)
        {
            return null;
        }
        return checkedText(value, key);
    }

    Integer integer(String key)
    {
        Object value = source.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number n)
        {
            double d = n.doubleValue();
            if (d == Math.rint(d) && Math.abs(d) <= Integer.MAX_VALUE) {
                return n.intValue();
            }
        }
        throw error.apply("Field '" + key + "' must be a whole number");
    }

    List<String> texts(String key)
    {
        Object value = source.get(key);

        if (value == null)
        {
            return null;
        }
        List<?> list = checkedList(value, key);
        List<String> result = new ArrayList<>(list.size());

        for (Object item : list)
        {
            result.add(item == null ? "" : checkedText(item, key));
        }
        return result;
    }

    Map<String, String> textMap(String key)
    {
        Object value = source.get(key);
        return value == null ? null : toTextMap(value, key);
    }

    List<Map<String, String>> textMaps(String key)
    {
        Object value = source.get(key);

        if (value == null)
        {
            return null;
        }
        List<Map<String, String>> result = new ArrayList<>();

        for (Object item : checkedList(value, key))
        {
            result.add(toTextMap(item, key));
        }
        return result;
    }

    private Map<String, String> toTextMap(Object value, String key)
    {
        if (!(value instanceof Map<?, ?> map))
        {
            throw error.apply("Field '" + key + "' must contain objects");
        }
        if (map.size() > MAX_ITEMS) {
            throw error.apply("Field '" + key + "' has too many entries");
        }

        Map<String, String> result = new LinkedHashMap<>();

        for (Map.Entry<?, ?> entry : map.entrySet())
        {
            if (!(entry.getKey() instanceof String k))
            {
                throw error.apply("Field '" + key + "' has a non-text key");
            }
            result.put(k, entry.getValue() == null ? "" : checkedText(entry.getValue(), key));
        }
        return result;
    }

    private List<?> checkedList(Object value, String key)
    {
        if (!(value instanceof List<?> list))
        {
            throw error.apply("Field '" + key + "' must be a list");
        }
        if (list.size() > MAX_ITEMS)
        {
            throw error.apply("Field '" + key + "' has too many items");
        }
        return list;
    }

    private String checkedText(Object value, String key)
    {
        if (!(value instanceof String s))
        {
            throw error.apply("Field '" + key + "' must contain text values");
        }
        if (s.length() > MAX_TEXT_LENGTH)
        {
            throw error.apply("Field '" + key + "' contains a value that is too long");
        }
        return s;
    }
}