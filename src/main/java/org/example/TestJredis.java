package org.example;

import redis.clients.jedis.Jedis;

public class TestJredis {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        jedis.set("test1", "hello world");
        String test1 = jedis.get("test1");
        System.out.println(test1);
    }
}
