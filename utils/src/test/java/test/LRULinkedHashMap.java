package test;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 基于 {@link LinkedHashMap} 的 简易 {@code LRU} 算法实现。
 * 构建的时候至少要提供一个 {@link #capacity} 最大容量上限。
 * @author ForteScarlet
 */
public class LRULinkedHashMap<K, V> extends LinkedHashMap<K, V> {

    /**
     * 最大容量上限。
     */
    private final int capacity;

    public LRULinkedHashMap(int capacity, int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor, true);
        this.capacity = capacity;
    }
    public LRULinkedHashMap(int capacity, int initialCapacity) {
        super(initialCapacity, 0.75f, true);
        this.capacity = capacity;
    }
    public LRULinkedHashMap(int capacity) {
        super(16, 0.75f, true);
        this.capacity = capacity;
    }
    public LRULinkedHashMap(Map<? extends K, ? extends V> m) {
        super(m);
        this.capacity = m.size();
    }
    public LRULinkedHashMap(int capacity, int initialCapacity, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor, accessOrder);
        this.capacity = capacity;
    }


    /**
     * 根据容量大小判断是否需要移除最后一个元素。
     * @param eldest 最近最久未使用元素。
     * @return 是否移除。
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }

    /**
     * 获取最大容量上限。
     */
    public int getCapacity() {
        return capacity;
    }
}
