package love.forte.common.collections


/**
 *
 * Lru map 最基础的实现。
 *
 */
@Deprecated("Use LRULinkedHashMap")
public class _LRULinkedHashMap<K, V> : LinkedHashMap<K, V> {
    @Volatile
    var capacity: Int

    constructor(capacity: Int, initialCapacity: Int, loadFactor: Float) : super(initialCapacity, loadFactor, true) {
        this.capacity = capacity
    }

    constructor(capacity: Int, initialCapacity: Int) : super(initialCapacity, 0.75f, true) {
        this.capacity = capacity
    }

    constructor(capacity: Int) : super() {
        this.capacity = capacity
    }

    constructor(capacity: Int, m: MutableMap<out K, out V>?) : super(m) {
        this.capacity = capacity
    }

    constructor(capacity: Int, initialCapacity: Int, loadFactor: Float, accessOrder: Boolean) : super(
        initialCapacity,
        loadFactor,
        accessOrder
    ) {
        this.capacity = capacity
    }

    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
        return size > capacity
    }

}