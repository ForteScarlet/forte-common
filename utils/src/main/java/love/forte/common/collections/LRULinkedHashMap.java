package love.forte.common.collections;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author ForteScarlet
 */
public class LRULinkedHashMap<K, V> extends LinkedHashMap<K, V> {

    private volatile int capacity;

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public LRULinkedHashMap(int initialCapacity, float loadFactor, int capacity) {
        super(initialCapacity, loadFactor);
        this.capacity = capacity;
    }

    public LRULinkedHashMap(int initialCapacity, int capacity) {
        super(initialCapacity);
        this.capacity = capacity;
    }

    public LRULinkedHashMap(int capacity) {
        this.capacity = capacity;
    }

    public LRULinkedHashMap(Map<? extends K, ? extends V> m, int capacity) {
        super(m);
        this.capacity = capacity;
    }

    public LRULinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder, int capacity) {
        super(initialCapacity, loadFactor, accessOrder);
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}
