package test;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author ForteScarlet
 */
public class Test2 {
    public static void main(String[] args) throws InterruptedException {

        Map<String, Integer> map = new PiecedConcurrentLruMap<>(1, 16);

        System.out.println(map.size());
        System.out.println(map);

        ThreadFactory fac = (r) -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        };

        // Executor
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                16, 16, 5000, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), fac
        );

        for (int i = 0; i < 10000; i++) {
            final int in = i;
            pool.execute(() -> {
                map.put(Integer.toHexString(in), in);
            });
            pool.execute(() -> {
                System.out.println(map.get(Integer.toHexString(in)));
            });
        }



        Thread.sleep(5000);

        System.out.println(map.size());
        System.out.println(map);



    }
}
