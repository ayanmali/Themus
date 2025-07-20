package com.delphi.delphi;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.delphi.delphi.components.RedisService;
import com.delphi.delphi.entities.User;
import com.delphi.delphi.utils.git.GithubAccountType;

@SpringBootTest
public class RedisTests {
    private final RedisService redisService;

    public RedisTests(RedisService redisService) {
        this.redisService = redisService;
    }

    @Test
    public void test() throws Exception {
        redisService.set("aaa", "111");
        Assertions.assertEquals("111", redisService.get("aaa").toString());
    }

    @Test
    public void testObjTimeout() throws Exception {
        User user = new User("userKey0", "email", "password", "name", "pat", "ghusername", GithubAccountType.USER);

        redisService.setWithExpiration("userKey1", user, 1, TimeUnit.SECONDS);

        Thread.sleep(1000);

        boolean exists = redisService.hasKey("userKey1");
        Assertions.assertFalse(exists);
    }
    
}