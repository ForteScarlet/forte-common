package test;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.StampedLock;

/**
 * 分段记录的线程安全同步 `LRU` 实现。
 *
 * @author ForteScarlet
 */
public class PiecedConcurrentLruMap<K, V>
        implements ConcurrentMap<K, V>, Map<K, V> {

    /**
     * 每个 {@code 分段(piece)} map的最大容量。
     */
    private final int pieceCapacity;


    /**
     * 每个 {@code 分段(piece)} map的初始化容量。
     */
    private final int pieceInitCapacity;


    private static final int DEFAULT_PIECE_INIT_CAPACITY = 16;


    /**
     * 每个 {@code 分段(piece)} map的负载因子。
     */
    private final float pieceLoadFactor;


    /**
     * 分段数量。
     */
    private final int pieceLen;

    /**
     * 默认分段数量。
     */
    private static final int DEFAULT_PIECE_LEN = 16;

    /**
     * 分段map。
     */
    private final LRULinkedHashMap<K, V>[] piece;

    /**
     * 对应于每个分段map的同步锁。
     */
    private final StampedLock[] locks;


    /**
     * 默认负载因子。由于lru基于 {@link LRULinkedHashMap} 实现，
     * 其存在最大容量上限，负载因子在容量真正达到容量顶峰的时候再扩容。
     */
    private static final float DEFAULT_LOAD_FACTOR = 1.0f;


    private final int maxLength;


    /**
     * The maximum capacity, used if a higher value is implicitly specified
     * by either of the constructors with arguments.
     * MUST be a power of two <= 1<<30.
     *
     * @see HashMap #MAXIMUM_CAPACITY
     */
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * 构建一个分段lruMap。需要提供分段Map的初始化参数供其初始化。
     *
     * @param pieceCapacity     每个分段map的最大容量。
     * @param pieceInitCapacity 每个分段map的初始化容量。
     * @param pieceLoadFactor   每个分段map的负载因子。
     * @param pieceLength       分段数量。
     */
    public PiecedConcurrentLruMap(int pieceCapacity,
                                  int pieceInitCapacity,
                                  float pieceLoadFactor,
                                  int pieceLength) {
        this.pieceCapacity = pieceCapacity;
        this.pieceInitCapacity = pieceInitCapacity;
        this.pieceLoadFactor = pieceLoadFactor;
        this.pieceLen = tableSizeFor(pieceLength);
        //noinspection unchecked
        this.piece = (LRULinkedHashMap<K, V>[]) new LRULinkedHashMap[pieceLen];
        this.locks = new StampedLock[pieceLen];
        this.maxLength = pieceLen * pieceCapacity;
    }

    /**
     * 构建一个分段lruMap。需要提供分段Map的初始化参数供其初始化。
     * 每个分段的默认初始化容量为 16.
     *
     * @param pieceCapacity   每个分段map的最大容量。
     * @param pieceLoadFactor 每个分段map的负载因子。
     * @param pieceLength     分段数量。
     * @see #DEFAULT_PIECE_INIT_CAPACITY
     */
    public PiecedConcurrentLruMap(
            int pieceCapacity,
            float pieceLoadFactor,
            int pieceLength) {
        this(pieceCapacity, DEFAULT_PIECE_INIT_CAPACITY, pieceLoadFactor, pieceLength);
    }

    /**
     * 构建一个分段lruMap。需要提供分段Map的初始化参数供其初始化。
     * 每个分段的默认初始化容量为 {@code 16}, 负载因子默认值为 {@code 1.0f}
     *
     * @param pieceCapacity 每个分段map的最大容量。
     * @param pieceLength   分段数量。
     * @see #DEFAULT_PIECE_INIT_CAPACITY
     * @see #DEFAULT_LOAD_FACTOR
     */
    public PiecedConcurrentLruMap(
            int pieceCapacity,
            int pieceLength) {
        this(pieceCapacity, DEFAULT_PIECE_INIT_CAPACITY, DEFAULT_LOAD_FACTOR, pieceLength);
    }

    /**
     * 构建一个分段lruMap。需要提供分段Map的初始化参数供其初始化。
     * 每个分段的默认初始化容量为 {@code 16}, 负载因子默认值为 {@code 1.0f}
     *
     * @param pieceCapacity 每个分段map的最大容量。
     * @see #DEFAULT_PIECE_INIT_CAPACITY
     * @see #DEFAULT_LOAD_FACTOR
     * @see #DEFAULT_PIECE_LEN
     */
    public PiecedConcurrentLruMap(int pieceCapacity) {
        this(pieceCapacity, DEFAULT_PIECE_INIT_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_PIECE_LEN);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        boolean first, firstPiece = true;
        StampedLock lock;
        for (int i = 0; i < pieceLen; i++) {
            lock = getLock(i);
            long stamp = lock.readLock();
            try {
                LRULinkedHashMap<K, V> map = piece[i];
                if (map != null) {
                    Set<Entry<K, V>> entries = map.entrySet();
                    if (firstPiece) {
                        firstPiece = false;
                    } else {
                        sb.append(',');
                    }
                    sb.append("{(").append(i).append(')');
                    first = true;
                    for (Entry<K, V> e : entries) {
                        if (first) {
                            first = false;
                        } else {
                            sb.append(',');
                        }
                        sb.append(e.getKey()).append('=').append(e.getValue());
                    }
                    sb.append('}');
                }
            } finally {
                lock.unlockRead(stamp);
            }
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public V putIfAbsent(@NotNull K key, V value) {
        return putVal(hash(key), key, value, true);
    }

    @Override
    public boolean remove(@NotNull Object key, Object value) {
        return removeVal(hash(key), key, value);
    }

    @Override
    public boolean replace(@NotNull K key, @NotNull V oldValue, @NotNull V newValue) {
        return replaceVal(hash(key), key, oldValue, newValue);
    }

    @Override
    public V replace(@NotNull K key, @NotNull V value) {
        return replaceVal(hash(key), key, value);
    }

    @Override
    public int size() {
        int s = 0;
        Long stamp = null;
        StampedLock lock = null;
        try {
            for (int i = 0; i < piece.length; i++) {
                lock = getLock(i);
                stamp = lock.readLock();
                LRULinkedHashMap<K, V> map = piece[i];
                s += map == null ? 0 : map.size();
                lock.unlockRead(stamp);
                stamp = null;
                lock = null;
            }
        } catch (Exception e) {
            if (lock != null) {
                if (stamp != null) {
                    lock.unlockRead(stamp);
                }
            }
        }
        return s;
    }

    @Override
    public boolean isEmpty() {
        Long stamp = null;
        StampedLock lock = null;
        boolean empty = true;
        try {
            for (int i = 0; i < piece.length; i++) {
                lock = getLock(i);
                stamp = lock.readLock();
                LRULinkedHashMap<K, V> map = piece[i];
                empty = map == null || map.isEmpty();
                lock.unlockRead(stamp);
                stamp = null;
                lock = null;
                if (!empty) {
                    break;
                }
            }
        } catch (Exception e) {
            if (lock != null && stamp != null) {
                lock.unlockRead(stamp);
            }
        }
        return empty;
    }

    @Override
    public boolean containsKey(Object key) {
        return containsKey0(hash(key), key);
    }


    @Override
    public boolean containsValue(Object value) {
        return containsValue0(value);
    }

    @Override
    public V get(Object key) {
        return getVal(hash(key), key);
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        return putVal(hash(key), key, value, false);
    }

    @Override
    public V remove(Object key) {
        return removeVal(hash(key), key);
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        putAllVal(m);
    }

    @Override
    public void clear() {
        StampedLock lock;
        Long[] stamps = new Long[pieceLen];
        LRULinkedHashMap<K, V> map;
        try {
            for (int i = 0; i < pieceLen; i++) {
                lock = getLock(i);
                stamps[i] = lock.writeLock();
                map = piece[i];
                if (map != null) {
                    map.clear();
                }
            }
        } finally {
            Long stamp;
            for (int i = 0; i < stamps.length; i++) {
                if ((stamp = stamps[i]) != null) {
                    getLock(i).unlockWrite(stamp);
                }
            }
        }
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        Set<K> set = new LinkedHashSet<>(size(), 1.0f);
        StampedLock lock = null;
        Long stamp = null;
        try {
            for (int i = 0; i < piece.length; i++) {
                lock = getLock(i);
                stamp = lock.readLock();
                LRULinkedHashMap<K, V> map = piece[i];
                if (map != null) {
                    set.addAll(map.keySet());
                }
                lock.unlockRead(stamp);
                lock = null;
                stamp = null;
            }
        } catch (Exception e) {
            if (lock != null && stamp != null) {
                lock.unlockRead(stamp);
            }
        }
        return set;
    }



    @NotNull
    @Override
    public Collection<V> values() {
        List<V> list = new ArrayList<>(size());
        StampedLock lock = null;
        Long stamp = null;
        try {
            for (int i = 0; i < piece.length; i++) {
                lock = getLock(i);
                stamp = lock.readLock();
                LRULinkedHashMap<K, V> map = piece[i];
                if (map != null) {
                    list.addAll(map.values());
                }
                lock.unlockRead(stamp);
                lock = null;
                stamp = null;
            }
        } catch (Exception e) {
            if (lock != null && stamp != null) {
                lock.unlockRead(stamp);
            }
        }
        return list;
    }


    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> set = new LinkedHashSet<>(size(), 1.0f);
        StampedLock lock = null;
        Long stamp = null;
        try {
            for (int i = 0; i < piece.length; i++) {
                lock = getLock(i);
                stamp = lock.readLock();
                LRULinkedHashMap<K, V> map = piece[i];
                if (map != null) {
                    set.addAll(map.entrySet());
                }
                lock.unlockRead(stamp);
                lock = null;
                stamp = null;
            }
        } catch (Exception e) {
            if (lock != null && stamp != null) {
                lock.unlockRead(stamp);
            }
        }
        return set;
    }


    private boolean containsKey0(int hash, Object key) {
        int i = indexOf(hash, pieceLen);
        StampedLock lock = getLock(i);
        long stamp = lock.tryOptimisticRead();
        boolean contains;
        LRULinkedHashMap<K, V> map;
        do {
            map = piece[i];
            if (map != null) {
                contains = map.containsKey(key);
            } else {
                contains = false;
            }
        } while (!lock.validate(stamp));

        return contains;
    }



    private boolean containsValue0(Object value) {
        boolean contains;
        long stamp;
        StampedLock lock;
        LRULinkedHashMap<K, V> map;
        for (int i = 0; i < pieceLen; i++) {
            lock = getLock(i);
            stamp = lock.tryOptimisticRead();
            do {
                map = piece[i];
                if (map != null) {
                    contains = map.containsValue(value);
                } else {
                    contains = false;
                }
            } while (!lock.validate(stamp));

            if (contains) {
                return true;
            }
        }
        return false;
    }


    /**
     * 替换一个值。
     *
     * @param hash  hash
     * @param key   key
     * @param value value
     * @return 是否替换成功.
     */
    private V replaceVal(int hash, K key, V value) {
        int i = indexOf(hash, pieceLen);
        StampedLock lock = getLock(i);
        long stamp = lock.writeLock();
        try {
            LRULinkedHashMap<K, V> map = piece[i];
            if (map != null) {
                return map.replace(key, value);
            }
            return null;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /**
     * 替换一个值。
     *
     * @param hash  hash
     * @param key   key
     * @param value value
     * @return 是否替换成功.
     */
    private boolean replaceVal(int hash, K key, V value, V newValue) {
        int i = indexOf(hash, pieceLen);
        StampedLock lock = getLock(i);
        long stamp = lock.writeLock();
        try {
            LRULinkedHashMap<K, V> map = piece[i];
            if (map != null) {
                return map.replace(key, value, newValue);
            }
            return false;
        } finally {
            lock.unlockWrite(stamp);
        }
    }


    /**
     * 移除某个val。
     *
     * @param hash hash
     * @param key  key
     * @return 被移除的值。
     */
    private boolean removeVal(int hash, Object key, Object value) {
        int i = indexOf(hash, pieceLen);
        StampedLock lock = getLock(i);
        long stamp = lock.writeLock();
        try {
            LRULinkedHashMap<K, V> map = piece[i];
            if (map != null) {
                return map.remove(key, value);
            }
            return false;
        } finally {
            lock.unlockWrite(stamp);
        }
    }


    /**
     * 移除某个val。
     *
     * @param hash hash
     * @param key  key
     * @return 被移除的值。
     */
    private V removeVal(int hash, Object key) {
        int i = indexOf(hash, pieceLen);
        StampedLock lock = getLock(i);
        long stamp = lock.writeLock();
        try {
            LRULinkedHashMap<K, V> map = piece[i];
            if (map != null) {
                return map.remove(key);
            }
            return null;
        } finally {
            lock.unlockWrite(stamp);
        }
    }


    /**
     * 存入值。
     *
     * @param hash  hash
     * @param key   key
     * @param value value
     * @return old value
     */
    private V putVal(int hash, K key, V value, boolean putIfAbsent) {
        // 计算索引
        int i = indexOf(hash, pieceLen);
        // 取出同步锁
        StampedLock lock = getLock(i);
        // 取出map
        LRULinkedHashMap<K, V> map = getMapWithInit(i, lock);

        long stamp = lock.writeLock();
        try {
            if (putIfAbsent) {
                return map.putIfAbsent(key, value);
            } else {
                return map.put(key, value);
            }
        } finally {
            lock.unlockWrite(stamp);
        }
    }


    /**
     * 存入值。
     */
    private void putAllVal(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            K key = entry.getKey();
            putVal(hash(key), key, entry.getValue(), false);
        }
    }


    /**
     * 取出一个值。
     *
     * @param hash hash
     * @param key  key
     * @return 取到的值，或者null。
     */
    private V getVal(int hash, Object key) {
        int i = indexOf(hash, pieceLen);
        StampedLock lock = getLock(i);
        long stamp = lock.tryOptimisticRead();
        // get map
        V val = null;
        LRULinkedHashMap<K, V> map;
        if ((map = piece[i]) != null) {
            val = map.get(key);
        }
        // not write?
        if (!lock.validate(stamp)) {
            // upload lock
            stamp = lock.readLock();
            try {
                if ((map = piece[i]) != null) {
                    val = map.get(key);
                }
            } finally {
                lock.unlockRead(stamp);
            }
        }

        return val;
    }


    /**
     * 根据索引位取出同步锁。
     *
     * @param index 索引位。
     * @return 对应锁。
     */
    private StampedLock getLock(int index) {
        StampedLock lock = locks[index];
        if (lock == null) {
            synchronized (locks) {
                lock = locks[index];
                if (lock == null) {
                    lock = new StampedLock();
                    locks[index] = lock;
                }
            }
        }
        return lock;
    }


    /**
     * 根据索引取出map, 如果没有map，初始化。
     *
     * @param index 索引位。
     * @param lock  对应锁。
     * @return {@link LRULinkedHashMap}。
     */
    private LRULinkedHashMap<K, V> getMapWithInit(int index, StampedLock lock) {
        LRULinkedHashMap<K, V> map = piece[index];

        if (map == null) {
            long stamp = lock.writeLock();
            try {
                map = piece[index];
                if (map == null) {
                    map = new LRULinkedHashMap<>(pieceCapacity, pieceInitCapacity, pieceLoadFactor);
                }
                piece[index] = map;
            } finally {
                lock.unlockWrite(stamp);
            }
        }

        return map;
    }


    ////// static //////


    /**
     * Returns a power of two size for the given target capacity.
     *
     * @see HashMap #tableSizeFor(int)
     */
    private static int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    /**
     * 计算一个key的hash值。
     *
     * @see HashMap #hash(Object)
     */
    private static int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    /**
     * 取模计算索引位。
     *
     * @param hash   hash值
     * @param length 数组长度。值应为 <code>2<sup>n</sup></code>。
     * @return 索引位。
     */
    private static int indexOf(int hash, int length) {
        return hash & (length - 1);
    }


}
