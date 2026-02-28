package cn.lili.cache.impl;

import cn.lili.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * redis 缓存实现
 *
 * @author Chopepr
 */
@Slf4j
@Component
public class RedisCache implements Cache {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    public RedisCache() {

    }

    /**
     * 根据key获取缓存中的值
     *
     * @param key 缓存key
     * @return 缓存中对应的值，不存在则返回null
     */
    @Override
    public Object get(Object key) {

        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 根据key获取缓存中的字符串值
     *
     * @param key 缓存key
     * @return 缓存值的字符串形式，不存在或异常时返回null
     */
    @Override
    public String getString(Object key) {
        try {
            return redisTemplate.opsForValue().get(key).toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 批量获取缓存值
     *
     * @param keys 缓存key集合
     * @return 对应key的值列表，不存在的key对应位置为null
     */
    @Override
    public List multiGet(Collection keys) {
        return redisTemplate.opsForValue().multiGet(keys);

    }

    /**
     * 批量设置缓存键值对
     *
     * @param map 键值对集合
     */
    @Override
    public void multiSet(Map map) {
        redisTemplate.opsForValue().multiSet(map);
    }

    /**
     * 批量删除缓存
     *
     * @param keys 需要删除的key集合
     */
    @Override
    public void multiDel(Collection keys) {
        redisTemplate.delete(keys);
    }

    /**
     * 设置缓存，无过期时间
     *
     * @param key   缓存key
     * @param value 缓存值
     */
    @Override
    public void put(Object key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置缓存，指定过期时间（单位：秒）
     *
     * @param key   缓存key
     * @param value 缓存值
     * @param exp   过期时间，单位为秒
     */
    @Override
    public void put(Object key, Object value, Long exp) {
        put(key, value, exp, TimeUnit.SECONDS);
    }

    /**
     * 设置缓存，指定过期时间和时间单位
     *
     * @param key      缓存key
     * @param value    缓存值
     * @param exp      过期时间
     * @param timeUnit 时间单位
     */
    @Override
    public void put(Object key, Object value, Long exp, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, exp, timeUnit);
    }

    /**
     * 删除指定key的缓存
     *
     * @param key 缓存key
     * @return 删除是否成功
     */
    @Override
    public Boolean remove(Object key) {

        return redisTemplate.delete(key);
    }

    /**
     * 删除
     *
     * @param key 模糊删除key
     */
    @Override
    public void vagueDel(Object key) {
        List keys = this.keys(key + "*");
        redisTemplate.delete(keys);
    }

    /**
     * 清空所有缓存
     */
    @Override
    public void clear() {
        List keys = this.keys("*");
        redisTemplate.delete(keys);
    }

    /**
     * 向Hash结构中存入单个键值对
     *
     * @param key       缓存key
     * @param hashKey   Hash中的字段名
     * @param hashValue Hash中的字段值
     */
    @Override
    public void putHash(Object key, Object hashKey, Object hashValue) {
        redisTemplate.opsForHash().put(key, hashKey, hashValue);
    }

    /**
     * 向Hash结构中批量存入键值对
     *
     * @param key 缓存key
     * @param map 需要存入的键值对集合
     */
    @Override
    public void putAllHash(Object key, Map map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * 从Hash结构中获取指定字段的值
     *
     * @param key     缓存key
     * @param hashKey Hash中的字段名
     * @return Hash中对应字段的值
     */
    @Override
    public Object getHash(Object key, Object hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    /**
     * 获取Hash结构中所有的键值对
     *
     * @param key 缓存key
     * @return Hash中所有的键值对
     */
    @Override
    public Map<Object, Object> getHash(Object key) {
        return this.redisTemplate.opsForHash().entries(key);
    }

    /**
     * 判断指定key是否存在
     *
     * @param key 缓存key
     * @return 存在返回true，否则返回false
     */
    @Override
    public boolean hasKey(Object key) {
        return this.redisTemplate.opsForValue().get(key) != null;
    }

    /**
     * 获取符合条件的key
     *
     * @param pattern 表达式
     * @return 模糊匹配key
     */
    @Override
    public List<Object> keys(String pattern) {
        List<Object> keys = new ArrayList<>();
        this.scan(pattern, item -> {
            //符合条件的key
            String key = new String(item, StandardCharsets.UTF_8);
            keys.add(key);
        });
        return keys;
    }

    /**
     * 使用KEYS命令（阻塞式）获取符合条件的key列表
     * 注意：在数据量大的场景下可能导致Redis阻塞，建议使用 {@link #keys(String)} 替代
     *
     * @param pattern 匹配模式
     * @return 符合模式的key列表
     */
    @Override
    public List<Object> keysBlock(String pattern) {
        Set<Object> set = redisTemplate.keys(pattern);
        List<Object> list = new ArrayList<>();
        list.addAll(set);
        return list;
    }

    /**
     * scan 实现
     *
     * @param pattern  表达式
     * @param consumer 对迭代到的key进行操作
     */
    private void scan(String pattern, Consumer<byte[]> consumer) {
        this.redisTemplate.execute((RedisConnection connection) -> {
            try (Cursor<byte[]> cursor =
                         connection.scan(ScanOptions.scanOptions()
                                 .count(Long.MAX_VALUE)
                                 .match(pattern).build())) {
                cursor.forEachRemaining(consumer);
                return null;

            } catch (IOException e) {
                log.error("scan错误", e);
                throw new RuntimeException(e);
            }
        });
    }


    /**
     * 使用HyperLogLog进行累加计数
     * 利用PFADD命令将值添加到HyperLogLog结构中，用于基数统计（去重计数）
     *
     * @param key   缓存key
     * @param value 需要累加的值
     * @return 添加成功返回1，元素已存在返回0
     */
    @Override
    public Long cumulative(Object key, Object value) {
        HyperLogLogOperations<Object, Object> operations = redisTemplate.opsForHyperLogLog();
        //add 方法对应 PFADD 命令
        return operations.add(key, value);

    }

    /**
     * 获取HyperLogLog的基数估算值
     * 利用PFCOUNT命令返回去重后的近似计数
     *
     * @param key 缓存key
     * @return 基数估算值
     */
    @Override
    public Long counter(Object key) {
        HyperLogLogOperations<Object, Object> operations = redisTemplate.opsForHyperLogLog();

        //add 方法对应 PFCOUNT  命令
        return operations.size(key);
    }

    /**
     * 批量获取HyperLogLog的基数估算值
     *
     * @param keys 缓存key集合
     * @return 各key对应的基数估算值列表，keys为null时返回空列表
     */
    @Override
    public List multiCounter(Collection keys) {
        if (keys == null) {
            return new ArrayList();
        }
        List<Long> result = new ArrayList<>();
        for (Object key : keys) {
            result.add(counter(key));
        }
        return result;
    }

    /**
     * 合并多个HyperLogLog并累加计数
     * 利用PFMERGE命令将多个HyperLogLog合并到第一个key中
     *
     * @param key 第一个参数为目标key，后续为需要合并的源key
     * @return 合并后的基数估算值
     */
    @Override
    public Long mergeCounter(Object... key) {
        HyperLogLogOperations<Object, Object> operations = redisTemplate.opsForHyperLogLog();
        //计数器合并累加
        return operations.union(key[0], key);
    }

    /**
     * 原子自增操作，并设置过期时间（仅在首次创建时设置）
     * 常用于限流、计数器等场景
     *
     * @param key      缓存key
     * @param liveTime 过期时间，单位为秒，仅在计数器首次创建时生效
     * @return 自增前的值
     */
    @Override
    public Long incr(String key, long liveTime) {
        RedisAtomicLong entityIdCounter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        Long increment = entityIdCounter.getAndIncrement();
        //初始设置过期时间
        if (increment == 0 && liveTime > 0) {
            entityIdCounter.expire(liveTime, TimeUnit.SECONDS);
        }

        return increment;
    }

    /**
     * 原子自增操作，无过期时间
     *
     * @param key 缓存key
     * @return 自增前的值
     */
    @Override
    public Long incr(String key) {
        RedisAtomicLong entityIdCounter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        return entityIdCounter.getAndIncrement();
    }

    /**
     * 使用Sorted Set记录keyword
     * zincrby命令，对于一个Sorted Set，存在的就把分数加x(x可自行设定)，不存在就创建一个分数为1的成员
     *
     * @param sortedSetName sortedSetName的Sorted Set不用预先创建，不存在会自动创建，存在则向里添加数据
     * @param keyword       关键词
     */
    @Override
    public void incrementScore(String sortedSetName, String keyword) {
        //指向key名为KEY的zset元素
        redisTemplate.opsForZSet().incrementScore(sortedSetName, keyword, 1);
    }

    /**
     * 对Sorted Set中指定关键词的分数加1（默认步长）
     *
     * @param sortedSetName sortedSetName
     * @param keyword       关键词，不存在则自动创建，分数初始为1
     * @param score         增加的分数值
     */
    @Override
    public void incrementScore(String sortedSetName, String keyword, Integer score) {
        redisTemplate.opsForZSet().incrementScore(sortedSetName, keyword, score);
    }

    /**
     * zrevrange命令, 查询Sorted Set中指定范围的值
     * 返回的有序集合中，score大的在前面
     * zrevrange方法无需担心用于指定范围的start和end出现越界报错问题
     *
     * @param sortedSetName sortedSetName
     * @param start         查询范围开始位置
     * @param end           查询范围结束位置
     * @return 符合排序的集合
     */
    @Override
    public Set<ZSetOperations.TypedTuple<Object>> reverseRangeWithScores(String sortedSetName, Integer start, Integer end) {
        return this.redisTemplate.opsForZSet().reverseRangeWithScores(sortedSetName, start, end);
    }

    /**
     * zrevrange命令, 查询Sorted Set中指定范围的值
     * 返回的有序集合中，score大的在前面
     * zrevrange方法无需担心用于指定范围的start和end出现越界报错问题
     *
     * @param sortedSetName sortedSetName
     * @param count         获取数量
     * @return 符合排序的集合
     */
    @Override
    public Set<ZSetOperations.TypedTuple<Object>> reverseRangeWithScores(String sortedSetName, Integer count) {
        return this.redisTemplate.opsForZSet().reverseRangeWithScores(sortedSetName, 0, count);
    }


    /**
     * 向Zset里添加成员
     *
     * @param key   key值
     * @param score 分数，通常用于排序
     * @param value 值
     * @return 增加状态
     */
    @Override
    public boolean zAdd(String key, long score, String value) {
        return redisTemplate.opsForZSet().add(key, value, score);

    }


    /**
     * 获取 某key 下 某一分值区间的队列
     *
     * @param key  缓存key
     * @param from 开始时间
     * @param to   结束时间
     * @return 数据
     */
    @Override
    public Set<ZSetOperations.TypedTuple<Object>> zRangeByScore(String key, int from, long to) {
        Set<ZSetOperations.TypedTuple<Object>> set = redisTemplate.opsForZSet().rangeByScoreWithScores(key, from, to);
        return set;
    }

    /**
     * 移除 Zset队列值
     *
     * @param key   key值
     * @param value 删除的集合
     * @return 删除数量
     */
    @Override
    public Long zRemove(String key, String... value) {
        return redisTemplate.opsForZSet().remove(key, value);
    }
}
