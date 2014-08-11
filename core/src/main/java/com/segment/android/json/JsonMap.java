package com.segment.android.json;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Map wrapper that exposes additional methods to coerce types lost during serialization.
 * This lets clients use pure Maps if they wish.
 * For instance, a float will be deserialized as a double. getFloat() will try to coerce that value
 * for you.
 *
 * The types on the left get mapped to the types on the right.
 * byte -> integer
 * short -> integer
 * integer -> integer
 * long -> long
 * float -> double
 * double -> double
 * char -> String
 * String -> String
 * boolean -> boolean
 *
 * In addition to these, Strings can be coerced to Numbers as well.
 */

/**
 * A wrapper around {@link Map} to expose Json functionality. Only the {@link #toString()} method
 * is modified to return a json formatted string. All other methods will be forwarded to another
 * map.
 * <p>
 * The purpose of this class is to not limit clients to a custom implementation of a Json type,
 * they
 * can use existing {@link Map} and {@link List} implementations as they see fit. It adds some
 * utility methods, including methods to coerce numeric types from Strings, and a {@link
 * #putValue(String, Object)} to be able to chain method calls.
 * <p>
 * To create an instance of this class, use one of the static factory methods.
 * <code>JsonMap<Object> map = JsonMap.create();</code>
 * <code>JsonMap<Object> map = JsonMap.create(json);</code>
 * <code>JsonMap<Object> map = JsonMap.wrap(new HashMap<String, Object>);</code>
 * <p>
 * Since it implements the {@link Map} interface, you could just as simply do:
 * <code>Map<String, Object> map = JsonMap.create();</code>
 * <code>Map<String, Object> map = JsonMap.create(json);</code>
 * <code>Map<String, Object> map = JsonMap.wrap(new HashMap<String, Object>);</code>
 * <p>
 * Although it lets you use custom objects for values, note that type information is lost during
 * serialization. For a custom class Person using the default <code>toString</code> implementation.
 * {@code
 * JsonMap<Object> map = JsonMap.create();
 * map.put("person", new Person("john", "doe", 32));
 * Person person = (Person) map.get("person"); // no serialization yet
 *
 * String json = map.toString();
 * JsonMap<Object> deserialized = JsonMap.create(map.toString());
 * Person person = (Person) deserialized.get("person"); // ClassCastException
 * }
 * <p>
 * Only String, Integer, Double, Long and Boolean types are supported.
 * Short, Byte, Float and char are deserialized to one of the above types.
 * Short -> Integer
 * Byte -> Integer
 * Float -> Double
 * Char -> String
 */
public class JsonMap<V> implements Map<String, V> {
  final Map<String, V> delegate;

  /** Create an empty map. */
  public static <P> JsonMap<P> create() {
    return new JsonMap<P>(new LinkedHashMap<String, P>());
  }

  /** Parse a json string into a map. */
  public static JsonMap<Object> create(String json) {
    try {
      return wrap(JsonUtils.toMap(json));
    } catch (JsonUtils.JsonConversionException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Wrap an existing map as a JsonMap. Use this to take advantage of the extra coercion methods
   * exposed by this class.
   *
   * @throws IllegalArgumentException if the map is null
   */
  public static <P> JsonMap<P> wrap(Map<String, ? extends P> map) {
    if (map == null) {
      throw new IllegalArgumentException("Map must not be null.");
    }
    if (map instanceof JsonMap) {
      return (JsonMap) map;
    }
    return new JsonMap<P>((Map<String, P>) map);
  }

  JsonMap(Map<String, V> delegate) {
    this.delegate = delegate;
  }

  @Override public void clear() {
    delegate.clear();
  }

  @Override public boolean containsKey(Object key) {
    return delegate.containsKey(key);
  }

  @Override public boolean containsValue(Object value) {
    return delegate.containsValue(value);
  }

  @Override public Set<Entry<String, V>> entrySet() {
    return delegate.entrySet();
  }

  @Override public V get(Object key) {
    return delegate.get(key);
  }

  @Override public boolean isEmpty() {
    return delegate.isEmpty();
  }

  @Override public Set<String> keySet() {
    return delegate.keySet();
  }

  @Override public V put(String key, V value) {
    return delegate.put(key, value);
  }

  @Override public void putAll(Map<? extends String, ? extends V> map) {
    for (Map.Entry<? extends String, ? extends V> entry : map.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  @Override public V remove(Object key) {
    return delegate.remove(key);
  }

  @Override public int size() {
    return delegate.size();
  }

  @Override public Collection<V> values() {
    return delegate.values();
  }

  @Override public boolean equals(Object object) {
    return delegate.equals(object);
  }

  @Override public int hashCode() {
    return delegate.hashCode();
  }

  @Override public String toString() {
    try {
      return JsonUtils.fromMap(delegate);
    } catch (JsonUtils.JsonConversionException e) {
      throw new RuntimeException(e);
    }
  }

  /** Helper method to be able to chain put methods. */
  public JsonMap<V> putValue(String key, V value) {
    delegate.put(key, value);
    return this;
  }

  // Coercion Methods
  /* The methods return boxed primitives to be able to return null and keep parity with Map. */

  /**
   * Returns the value mapped by {@code key} if it exists and is a byte or
   * can be coerced to a byte. Returns null otherwise.
   */
  public Byte getByte(Object key) {
    V value = get(key);
    if (value instanceof Byte) {
      return (Byte) value;
    } else if (value instanceof Number) {
      return ((Number) value).byteValue();
    } else if (value instanceof String) {
      try {
        return Byte.valueOf((String) value);
      } catch (NumberFormatException ignored) {
        // Ignore
      }
    }
    return null;
  }

  /**
   * Returns the value mapped by {@code key} if it exists and is a short or
   * can be coerced to a short. Returns null otherwise.
   */
  public Short getShort(Object key) {
    V value = get(key);
    if (value != null) {
      if (value instanceof Short) {
        return (Short) value;
      } else if (value instanceof Number) {
        return ((Number) value).shortValue();
      } else if (value instanceof String) {
        try {
          return Short.valueOf((String) value);
        } catch (NumberFormatException ignored) {

        }
      }
    }
    return null;
  }

  /**
   * Returns the value mapped by {@code key} if it exists and is a integer or
   * can be coerced to a integer. Returns null otherwise.
   */
  public Integer getInteger(Object key) {
    V value = get(key);
    if (value instanceof Integer) {
      return (Integer) value;
    } else if (value instanceof Number) {
      return ((Number) value).intValue();
    } else if (value instanceof String) {
      try {
        return Integer.valueOf((String) value);
      } catch (NumberFormatException ignored) {
        // ignore
      }
    }
    return null;
  }

  /**
   * Returns the value mapped by {@code key} if it exists and is a long or
   * can be coerced to a long. Returns null otherwise.
   */
  public Long getLong(Object key) {
    V value = get(key);
    if (value instanceof Long) {
      return (Long) value;
    } else if (value instanceof Number) {
      return ((Number) value).longValue();
    } else if (value instanceof String) {
      try {
        return Long.valueOf((String) value);
      } catch (NumberFormatException ignored) {
        // ignore
      }
    }
    return null;
  }

  /**
   * Returns the value mapped by {@code key} if it exists and is a integer or
   * can be coerced to a integer. Returns null otherwise.
   */
  public Float getFloat(Object key) {
    V value = get(key);
    if (value instanceof Float) {
      return (Float) value;
    } else if (value instanceof Number) {
      return ((Number) value).floatValue();
    } else if (value instanceof String) {
      try {
        return Float.valueOf((String) value);
      } catch (NumberFormatException ignored) {
        // ignore
      }
    }
    return null;
  }

  /**
   * Returns the value mapped by {@code key} if it exists and is a double or
   * can be coerced to a double. Returns null otherwise.
   */
  public Double getDouble(Object key) {
    V value = get(key);
    if (value instanceof Double) {
      return (Double) value;
    } else if (value instanceof Number) {
      return ((Number) value).doubleValue();
    } else if (value instanceof String) {
      try {
        return Double.valueOf((String) value);
      } catch (NumberFormatException ignored) {
        // ignore
      }
    }
    return null;
  }

  /**
   * Returns the value mapped by {@code key} if it exists and is a char or
   * can be coerced to a char. Returns null otherwise.
   */
  public Character getChar(Object key) {
    V value = get(key);
    if (value instanceof Character) {
      return (Character) value;
    } else if (value != null && value instanceof String) {
      if (((String) value).length() == 1) {
        return ((String) value).charAt(0);
      }
    }
    return null;
  }

  /**
   * Returns the value mapped by {@code key} if it exists and is a string or
   * can be coerced to a string. Returns null otherwise.
   *
   * This will return null only if the value does not exist, since all types can have a String
   * representation.
   */
  public String getString(Object key) {
    V value = get(key);
    if (value instanceof String) {
      return (String) value;
    } else if (value != null) {
      return String.valueOf(value);
    }
    return null;
  }

  /**
   * Returns the value mapped by {@code key} if it exists and is a boolean or
   * can be coerced to a boolean. Returns null otherwise.
   */
  public Boolean getBoolean(Object key) {
    V value = get(key);
    if (value instanceof Boolean) {
      return (Boolean) value;
    } else if (value instanceof String) {
      String stringValue = (String) value;
      if ("false".equalsIgnoreCase(stringValue)) {
        return false;
      } else if ("true".equalsIgnoreCase(stringValue)) {
        return true;
      }
    }
    return null;
  }
}