package org.example;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

public class TestLettuce {
    public static void main(String[] args) {
        // 步骤1：连接信息
        RedisURI redisURI = RedisURI.builder()
                .withHost("localhost")
                .withPort(6379)
                // .withPassword(new char[]{'a', 'b', 'c', '1', '2', '3'})
                .withTimeout(Duration.ofSeconds(10))
                .build();
        // 步骤2：创建Redis客户端
        RedisClient client = RedisClient.create(redisURI);

        // 步骤3：建立连接
        StatefulRedisConnection<String, String> connection = client.connect();

        System.out.println("--------------------同步调用 BEGIN --------------------");
        // 异步转同步
        RedisCommands<String, String> commands = connection.sync();
        // Redis命令：set hello world
        System.out.println("set hello world");
        String result = commands.set("hello", "world");
        System.out.println(result);

        System.out.println("get hello");
        result = commands.get("hello");
        System.out.println(result);
        System.out.println("--------------------同步调用 END --------------------");

        System.out.println("--------------------异步调用 BEGIN --------------------");
        RedisAsyncCommands<String, String> asyncCommands = connection.async();
        System.out.println("get hello");
        RedisFuture<String> future = asyncCommands.get("hello");

        try {
            result = future.get();
            System.out.println(result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            System.out.println(e);
        }

        System.out.println("--------------------异步调用 END --------------------");

        connection.close();
        client.shutdown();

    }
}
