package love.forte.common.collections;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 *
 * 一个有 key 的弱引用实例。
 *
 * @author ForteScarlet
 */
public class KeyWeakReference<D, T> extends WeakReference<T> {
    private final D key;
    public KeyWeakReference(D key, T referent) {
        super(referent);
        this.key = key;
    }
    public KeyWeakReference(D key, T referent, ReferenceQueue<? super T> q) {
        super(referent, q);
        this.key = key;
    }

    public D getKey() {
        return key;
    }
}
