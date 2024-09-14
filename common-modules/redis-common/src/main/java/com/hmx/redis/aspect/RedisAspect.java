package com.hmx.redis.aspect;


import com.hmx.redis.annotation.LockOperation;
import com.hmx.redis.service.RedisService;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class RedisAspect {


    private final RedisService redisService;

    private final String pointcutPath = "@annotation(com.hmx.redis.annotation.LockOperation)";


    @Pointcut(pointcutPath)
    public void lockPointCut(){};


    @Around("lockPointCut()")
    public Object  aroundLockFunction(ProceedingJoinPoint point){
        LockOperation annotation = point.getClass().getAnnotation(LockOperation.class);
        String lockName = annotation.lockName();
        String lockValue = redisService.getLock(lockName);

        if (StringUtils.isNotEmpty(lockValue)){

            try{
                return point.proceed(point.getArgs());
            }catch (Throwable e){
                throw new RuntimeException("com.hmx.redis.annotation.LockOperation上锁出现异常:{}"+e.getMessage());
            }finally {
                redisService.releaseLock(lockName,lockValue);
            }
        }

        throw new RuntimeException("执行超时");
    }



}
