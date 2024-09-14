package com.hmx.redis.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RedisService {

    private RedisTemplate redisTemplate;

    RedisService(RedisTemplate redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    private final Duration defaultDuration = Duration.ofMinutes(10);

    private final long lockingLatency = 1500L;

    private final Duration lockValid = Duration.ofMinutes(2);

//    private final Long limitLatency = 30000L;

//    private final Integer limitThreshold = 5;

    public boolean saveElement(Object object, String objectKey){
        return saveElement(object,objectKey,defaultDuration);
    }

    /**
     * <h2>数据元数缓存</h2>
     * 缓存数据
     */
    public boolean saveElement(Object object, String objectKey, Duration duration){
        ValueOperations opsForValue = redisTemplate.opsForValue();
        Boolean setAbsent = opsForValue.setIfAbsent(objectKey, object, duration);
        return setAbsent;
    }

    /**
     * 获取缓存的数据
     * @param objectKey
     * @return
     */
    public Object getElement(String objectKey){
        if (!redisTemplate.hasKey(objectKey)){
            return null;
        }
        ValueOperations opsForValue = redisTemplate.opsForValue();
        return opsForValue.get(objectKey);
    }

    /**
     * <h2>数据元数缓存</h2>
     * 删除缓存中的值，并返回缓存中的值
     */
    public Object removeElement(String objectKey){
        if (redisTemplate.hasKey(objectKey)){
            ValueOperations opsForValue = redisTemplate.opsForValue();
            return opsForValue.getAndDelete(objectKey);
        }
        return null;
    }


    /**
     * <h2>分布式锁</h2>
     * 尝试上锁
     * @param lockKey 锁键，应该保持细粒度
     * @return 返回锁值，如果为""表明上锁异常
     */
    public String getLock(String lockKey){
        long now = new Date().getTime();
        String lockValue = UUID.randomUUID().toString();
        ValueOperations opsForValue = redisTemplate.opsForValue();

        while (now+lockingLatency>=new Date().getTime()){
            if (opsForValue.setIfAbsent(lockKey,lockValue,lockValid)){
                log.info("成功上锁，锁名:{},锁值:{}",lockKey,lockValue);
                return lockValue;
            }
        }
        if (redisTemplate.getExpire(lockKey)<0){
            log.info("检测到锁名[{}] 未设置过期时间，现设置过期时间:{}",lockKey,lockValid);
            redisTemplate.delete(lockKey);
        }
        log.error("上锁失败，锁名[{}]",lockKey);
        return Strings.EMPTY;
    }

    /**
     * <h2>分布式锁</h2>
     * 尝试解锁
     * @param lockKey 锁名
     * @param lockValue 锁值
     * @return 解锁是否成功
     */
    public boolean releaseLock(String lockKey, String lockValue){
        log.info("尝试释放分布式锁[{}]",lockKey);

        while (true){
            String currentLockValue = redisTemplate.opsForValue().get(lockKey).toString();

            if (!lockValue.equals(currentLockValue)){
                log.error("释放分布式锁[{}]异常，当前锁已经更改[{}]",lockKey,currentLockValue);
                return false;
            }
            List<Boolean> executePipelined = redisTemplate.executePipelined(new SessionCallback<Object>() {
                @Override
                public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                    operations.watch((K) lockKey);
                    operations.multi();
                    operations.delete((K) lockKey);
                    operations.exec();
                    return null;
                }
            });

            /*if (executePipelined.stream().allMatch(exec-> exec)){
                log.info("释放分布式锁[{}]成功",lockKey);
                return true;
            }*/
            if (!redisTemplate.hasKey(lockKey)){
                log.info("释放分布式锁[{}]成功",lockKey);
                return true;
            }

            if (redisTemplate.getExpire(lockKey)<0){
                redisTemplate.expire(lockKey,lockValid);
            }

            log.info("本轮释放锁[{}]失败， 开始下一轮",lockKey);
        }

    }

    /**
     * <h2>限流</h2>
     * @param limitKey 限流标识key
     * @return 是否超过限流阈值
     */
    public boolean limitVisit(String limitKey, long limitLatency, int limitThreshold){
        long now = new Date().getTime();
        String valueLess = UUID.randomUUID().toString();
        ZSetOperations opsForZSet = redisTemplate.opsForZSet();

        if (!redisTemplate.hasKey(limitKey)){
            log.info("开始记录[{}]访问次数",limitKey);
            opsForZSet.add(limitKey,valueLess,now);
            redisTemplate.expire(limitKey,limitLatency+2000, TimeUnit.MICROSECONDS);
            return true;
        }

        Long aLong = opsForZSet.removeRangeByScore(limitKey, now-limitLatency, now);
        Long count = opsForZSet.count(limitKey, -1, now);
        log.info("限流[{}]，当前次数:{}",limitKey,count);

        if (Objects.nonNull(count)&&count>=limitThreshold){
            log.error("访问受限：访问过于频繁");
            return false;
        }

        log.info("允许成功访问[{}]",limitKey);
        opsForZSet.add(limitKey,valueLess,now);
        redisTemplate.expire(limitKey,limitLatency+2000, TimeUnit.MICROSECONDS);
        return true;
    }






}
