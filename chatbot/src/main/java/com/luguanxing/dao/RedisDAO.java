package com.luguanxing.dao;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Log4j2
@Service
public class RedisDAO {

    @Value("${ip.daily.limit}")
    private int ipDailyLimit;

    @Value("${ip.ttl.seconds}")
    private long ipTtlSeconds;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public boolean checkAndUpdateIpCnt(String ip) {
        String key = "chatIpMap::" + ip;
        String value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            int cnt;
            try {
                cnt = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                log.error("Failed to parse Redis value to int for IP: " + ip + ", value = " + value, e);
                return false;
            }
            // 更新剩余次数
            if (cnt <= 0) {
                return false;
            } else {
                // 获取当前 TTL
                Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                if (ttl != null && ttl > 0) {
                    // 更新值并保留 TTL
                    redisTemplate.opsForValue().set(key, String.valueOf(cnt - 1), ttl, TimeUnit.SECONDS);
                } else {
                    // 如果没有 TTL，则重新设置值和默认 TTL
                    redisTemplate.opsForValue().set(key, String.valueOf(cnt - 1), ipTtlSeconds, TimeUnit.SECONDS);
                }
                return true;
            }
        } else {
            // 初始化值和 TTL
            redisTemplate.opsForValue().set(key, String.valueOf(ipDailyLimit), ipTtlSeconds, TimeUnit.SECONDS);
            return true;
        }
    }

}