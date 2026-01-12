package cn.lili.common.utils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池工具类
 *
 * @author Chopper
 */
public class ThreadPoolUtil {

    /**
     * 核心线程数，会一直存活，即使没有任务，线程池也会维护线程的最少数量
     */
    private static final int SIZE_CORE_POOL = 5;
    /**
     * 线程池维护线程的最大数量
     */
    private static final int SIZE_MAX_POOL = 10;
    /**
     * 线程池维护线程所允许的空闲时间（毫秒）
     */
    private static final long ALIVE_TIME = 2000;
    /**
     * 线程缓冲队列容量
     */
    private static final int QUEUE_CAPACITY = 100;

    /**
     * 线程池实例
     */
    private static final ThreadPoolExecutor POOL;

    static {
        POOL = new ThreadPoolExecutor(
                SIZE_CORE_POOL,
                SIZE_MAX_POOL,
                ALIVE_TIME,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                new NamedThreadFactory("lili-pool"),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        POOL.prestartAllCoreThreads();
    }

    private ThreadPoolUtil() {
    }

    /**
     * 执行任务
     *
     * @param runnable 任务
     */
    public static void execute(Runnable runnable) {
        POOL.execute(runnable);
    }

    /**
     * 提交有返回值的任务
     *
     * @param callable 任务
     * @return Future
     */
    public static <T> Future<T> submit(Callable<T> callable) {
        return POOL.submit(callable);
    }

    /**
     * 获取线程池
     *
     * @return 线程池对象
     */
    public static ThreadPoolExecutor getPool() {
        return POOL;
    }

    /**
     * 优雅关闭线程池
     */
    public static void shutdown() {
        POOL.shutdown();
        try {
            if (!POOL.awaitTermination(60, TimeUnit.SECONDS)) {
                POOL.shutdownNow();
            }
        } catch (InterruptedException e) {
            POOL.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 自定义线程工厂
     */
    private static class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        NamedThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}