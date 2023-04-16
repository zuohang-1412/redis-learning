# Redis
## 文档
1) http://redis.cn/
2) https://redis.io/
3) https://spring.io/
## 关系数据库
### MySQL
1) 会根据 schema 进行 字段字节宽度的 创建， 并根据行进行存储， 就算字段值为null 也会进行开辟，这样更新起来就会比较方便
2) 当表中的数据体量越来越大时，性能就会下降，索引的建立能提高查询效率，但是数据体量特别大时，由于大并发时会收到硬盘带宽的影响，查询效率依旧会变慢。
3) 所以提出将一些数据提取出来在缓存中进行查询提高效率。
4) db 选型：https://db-engines.com/en/
### Redis
1) key-value
2) value 类型： string、hashes、lists、sets、sorted sets；string 中包括（字符类型、数值类型、bitmaps）
3) 和 同样是k，v 结构的 memcached 这个 db 相比， memcached 中value 是无类型的，redis value 是有类型的，
   有类型就意味着 可以在redis 中实现 value 的解析，而不需要把value 数据下载到client 再用代码进行解析操作。（本质就是计算向数据移动的问题）
4) 默认端口号：6379
5) 单进程、单线程、单实例。
6) 单实例如何做到高并发的请求？ 通过内核（kernel）中使用 epoll  达到同步，非阻塞，多路复用
7) 二进制安全：字节流
8) sorted set 是根据物理内存从小到大存储，不会根据命令进行变化，例：ZREVRANGE zk1 0 1 和 ZRANGE zk1 -2 -1 两个取出值的顺序是不一样的。
9) sorted set 是如何快速排序的？ 使用跳跃表（skip list）的形式进行查询，
   会在最开始时 生成一个链表1 -> 10 -> 20 -> 50, 在这个链表的基础上再生成一个向上的链表
   1 ------> 20 ------> null   
   ^
   |
   1 -> 10 -> 20 -> 50
   当要插入 40 这个数据是 会在1 的位置找最大层，查看 1 的next 是否大于 40 不大于就再next 发现下一个是null 这个时候 根据20 的层数进行减一，
   再判断 next 是否大于 40 发现next 为 50 所以就需要把40 插入到 20 的next 并让 40 的next 指向 50, 最后随机生成自己的层数，添加到相应的层中。
   1 ------> 20 ------> null   
   ^
   |
   1 -- -- ------> 40 --> null  
   ^                ^
   |                |
   1 -> 10 -> 20 -> 40 —> 50

## Redis 使用
### 命令
1) redis 默认有16个库（0-15） 所以可以使用 redis> select 8 选择 8 号库，默认登录为0号库
2) redis> keys * 查询所有创建的key
3) redis> help @generic 关于key 的所有操作
4) redis> help @string 关于 value 为 string 的所有操作 
5) redis> set k1 hello 创建key 
6) redis> get k1 查询key
7) redis> set k1 hi nx （nx：代表 只能创建不能更新，所以常用于分布式锁抢夺时使用）
8) redis> set k2 hello xx (xx: 代表只能更新，不能创建) get k2 时显示为nil。
9) redis> mset k3 a k4 b redis> mget k3 k4 显示a和b
10) redis> append k1 " world" 显示："hello world"
11) redis> GETRANGE k1 6 -1 显示："world"
12) redis> SETRANGE k1 6 xx 显示："hello xxrld"
13) redis> STRLEN k1 显示：(integer) 11
14) redis> TYPE k1 显示：string
15) redis> set k2 99  get k2 显示：string 但是因为其在 redis>  object encoding k2 显示为 int，所以可以使用 数值加减操作
16) redis> INCR k2 显示： 100 表示数据加一
17) redis> INCRBY k2 50 显示： 50 表示 100+50 = 150
18) redis> DECR k2 显示：149 表示数据减一
19) redis> DECRBY k2 50 显示：99 表示 149-50 = 99
20) redis> INCRBYFLOAT k2 0.5 显示：99.5 表示 99+0.5=99.5
21) redis> GETSET k1 "hello world" 显示："hello xxrld" redis> get k1 显示："hello world"
22) redis> SETBIT k5 1 1 显示："@" 即 0100 0000 0000 0000 redis> STRLEN k5 显示：1
23) redis> SETBIT k5 7 1 显示："A" 即 0100 0001 0000 0000 redis> STRLEN k5 显示：1
24) redis> SETBIT k5 9 1 显示："A@" 即 0100 0001 0100 0000 redis> STRLEN k5 显示：2
25) redis> BITPOS k5 1 0 0 显示：1 即 0组：0100 0001 中第一个 1 的位置，所以返回下标1
26) redis> BITPOS k5 1 1 1 显示：9 即 1组：0100 0000 中第一个 1 的位置，所以返回下标9
27) redis> BITPOS k5 1 0 1 显示：1 即 0组到1组中：0100 0001 0100 0000 第一个 1 的位置，所以返回下标1
28) redis> BITCOUNT k5 0 1 显示：3 即 0组到1组中1出现的次数
29) redis> SETBIT k6 1 1  redis> SETBIT k6 7 1 显示为：A
30) redis> SETBIT k7 1 1  redis> SETBIT k7 6 1 显示为：B
31) redis> BITOP and andkey k6 k7 显示：@
32) redis> BITOP or orkey k6 k7 显示：C
33) redis> LPUSH lk1 a b c d e f ; redis> LRANGE lk1 0 -1 显示："f" "e" "d" "c" "b" "a"
34) redis> RPUSH lk2 a b c d e f ; redis> LRANGE lk2 0 -1 显示："a" "b" "c" "d" "e" "f"
35) redis> LPOP lk1 显示：f 说明L可以做为栈
36) redis> RPOP lk2 显示：f 说明R可以作为队列
37) redis> LINDEX lk1 -1 显示：a
38) redis> LSET lk1 3 xxx ; redis> LRANGE lk1 0 -1 显示："e" "d" "c" "xxx" "a"
39) redis> LPUSH lk3 1 a 2 b 3 a 4 c 5 d 6 a ; redis> LRANGE lk3 0 -1 显示： "a" "6" "d" "5" "c" "4" "a" "3" "b" "2" "a" "1"
40) redis> LREM lk3 2 a ; redis> LRANGE lk3 0 -1 显示："6" "d" "5" "c" "4" "3" "b" "2" "a" "1" 去除前两个a
41) redis> LINSERT lk3 before 6 a ; redis> LRANGE lk3 0 -1 显示："6" "d" "5" "c" "4" "3" "b" "2" "a" "1" 在6前面加上一个 a
42) redis> LINSERT lk3 after 4 a ; redis> LRANGE lk3 0 -1 显示： "a" "6" "d" "5" "c" "4" "a" "3" "b" "2" "a" "1" 在4后面加上一个a
43) redis> LREM lk3 -2 a ; redis> LRANGE lk3 0 -1 显示："a" "6" "d" "5" "c" "4" "3" "b" "2" "1"
44) redis> LLEN lk3; 显示：10
45) redis> BLPOP lk4; 因为k4 没有数据会一直阻塞， 
    这时候再起一个 redis> BLPOP lk4; 第二个也会因为没有数据而阻塞
    再起一个 redis> LPUSH lk4 hello， 第一个redis 显示：hello，第二个redis 还是阻塞状态
    再起一个 redis> LPUSH lk4 worle， 第二个redis 显示：world。
    这种方式常用于消息队列的形式
46) redis> LTRIM lk1 2 -2; LRANGE lk1 0 -1 显示："c" "xxx" 删除 下标 2 前 和 -2 后的值
47) redis> HSET hk1 name ZhangSan ; redis> HGET hk1 name 显示："ZhangSan"
48) redis> HMSET hk1 name ZhangSan age 18 ; redis> HMGET hk1 name age 显示："ZhangSan" "18"
49) redis> HKEYS hk1 显示："name" "age"
50) redis> HVALS hk1 显示："ZhangSan" "18"
51) redis> HGETALL hk1 显示："name" "ZhangSan" "age" "18"
52) redis> HINCRBY hk1 age 1 显示：19
53) redis> HINCRBYFLOAT hk1 age -0.5 显示: 18.5
54) redis> SADD sk1 a b c d a e ; redis> SMEMBERS sk1 显示："a" "d" "c" "e" "b" （无序去重）
55) redis> SREM sk1 a b ; redis> SMEMBERS sk1 显示："c" "d" "e"
56) redis> SADD sk2 1 2 3 4 5 ; redis> SADD sk3 3 4 5 6 7 ; redis> SINTER sk2 sk3 显示：3 4 5 (只交集计算不存储结果)
57) redis> SADD sk2 1 2 3 4 5 ; redis> SADD sk3 3 4 5 6 7 ; redis> SINTERSTORE destkey sk2 sk3 ; 
    redis> SMEMBERS destkey 显示：3 4 5 （交集计算后存储结果）
58) redis> SUNION sk2 sk3 显示：1 2 3 4 5 6 7
59) redis> SDIFF sk2 sk3 显示：1 2
60) redis> SDIFF sk3 sk2 显示：6 7
61) redis> SRANDMEMBER sk2 3 显示：5 2 4 （随机在sk2 中抽取三个数值，不重复）
62) redis> SRANDMEMBER sk2 -3 显示：4 2 4（随机在sk2 中抽取三个数值，可以重复）
63) redis> SRANDMEMBER sk2 7 显示：1 2 3 4 5 （因为只有5 个参数 所以全部取出）
64) redis> SRANDMEMBER sk2 -7 显示：2 1 1 5 3 3 3 （会重复 一定会出7个值）
65) redis> SPOP sk2 显示：4 （随机抽出一个值）
66) redis> ZADD zk1 8 a 2 b 3 c ; 
    redis> ZRANGE zk1 0 -1 显示： b c a
    redis> ZRANGE zk1 0 -1 WITHSCORES 显示：b 2 c 3 a 8
67) redis> ZRANGEBYSCORE zk1 3 10 显示：c a
68) redis> ZREVRANGE zk1 0 -1 显示：a c b （倒序）
69) redis> ZSCORE zk1 a 显示：8
70) redis> ZINCRBY zk1 7 b
    redis> ZRANGE zk1 0 -1 WITHSCORES 显示："c" "3" "a" "8" "b" "9" （根据改的数值实时排序 ）
71) redis> ZADD zk2 30 a 40 b 50 c
    redis> ZADD zk3 60 a 70 b 80 d
    redis> ZUNION 2 zk2 zk3 withScores 显示："c" "50" "d" "80" "a" "90" "b" "110" （加和）
    redis> ZUNION 2 zk2 zk3 WEIGHTS 1 0.1 withScores 显示："d" "8" "a" "36" "b" "47" "c" "50"
    redis> ZUNION 2 zk2 zk3 AGGREGATE max withScores 显示："c" "50" "a" "60" "b" "70" "d" "80"

### 管道 pipeline
1) echo "keys *\n get k1"| nc localhost 6379
   这样就可以通过管道一次性返回数据， 注意：\n 分割两条命令
2) cat data.txt |redis-cli --pipe 实现数据文件管道加载

### 消息订阅 help @pubsub
1) redis> SUBSCRIBE channel1 先监听
   redis> PUBLISH channel1 hello 后发消息才会被监听的人看到。

### 事务 help @transactions
1) redis> MULTI (开启事务)
   redis> set tmp1 aaa
   redis> set tmp2 bbb
   redis> EXEC (执行事务，如果开启了两个事务，哪一个事务先执行exec 那一个事务先执行)
2) redis> watch tmp1 先监控 tmp1 是否有变更，再执行开启事务，如果第二个事务变更了tmp1的value，第一个事务就不会执行 tmp1 之后的操作

### 应用实例 （RedisBloom）
1) redis> BF.* 就是布隆过滤器可以添加的命令
2) 在redis 前使用 bloom 是为了防止缓存穿透，例如：618 每件商品都可以用三种映射函数在 bitmap 中插入1，
   当有用户访问相关商品时，根据商品的三个映射函数找到 bitmap 中是否存在相同一的位置， 如果存在就再去redis 中查看，如果没有就直接返回， 
   如果正好三个映射函数都符合但是 redis 中没有的话，这种情况出现概率极低。
3) 当数据更新后需要同步到redis 和 布隆过滤器。

### 过期
1) 因为内存大小是固定的 一般为1G-10G， 所以设置过期时间是非常有必要的
2) 在redis.conf 中 MAXMEMORY <bytes> 是可以设置内存大小的，MAXMEMORY-polic 设置过期规则
3) redis> set tk temp ; redis> get tk 显示 temp
4) redis> EXPIRE tk 10; redis> TTl tk （TTL 查询 tk 倒计时， 10 s 后就会消失）；redis> get tk 显示 (nil)
5) redis> set tk tmp EX 20; 这也是一种设置倒计时的方式
6) redis> set tk temp ; get tk 显示 temp ; redis> time 查询当前时间戳（1681471626）；
   redis>  EXPIREAT tmp 1681481626 ; redis> TTL tmp 显示：9981 （设置过期时间戳，到时间自动清除）
7) 注意：当设置完时间戳后 如果调用查询后 倒计时不会重制
8) MAXMEMORY-polic 过期规则, 一般使用 allkeys-lru 和 volatile-lru
   1) noeviction:返回错误当内存限制达到并且客户端尝试执行会让更多内存被使用的命令（大部分的写入指令，但DEL和几个例外）
   2) allkeys-lru: 尝试回收最少使用的键（LRU），使得新添加的数据有空间存放。
   3) volatile-lru: 尝试回收最少使用的键（LRU），但仅限于在过期集合的键,使得新添加的数据有空间存放。
   4) allkeys-random: 回收随机的键使得新添加的数据有空间存放。
   5) volatile-random: 回收随机的键使得新添加的数据有空间存放，但仅限于在过期集合的键。
   6) volatile-ttl: 回收在过期集合的键，并且优先回收存活时间（TTL）较短的键,使得新添加的数据有空间存放。
9) lru 和 lfu 的区别: lru 看的是时间（多久没有查询）；lfu 看的是查询次数
10) redis 如何淘汰过期keys？ 采用的是 每秒十次这样的操作：
    1) 测试随机的20个keys 并进行相关过期检测。
    2) 删除已经过期的keys
    3) 如果有多余25%的keys 过期，重复步骤1。

### 数据持久化 RDB 或 AOF
1) 第一种方式： 8：00 开始同步数据，然后开始阻塞，等到8：30 同步完成，释放阻塞。 这种不太符合生产环境
2) 第二种方式： 8：00 开始同步数据，采用非阻塞形式，当数据同步到8：10分时，发现没有同步的数据有过更新，这样会导致数据无法精确到8：00 这一时刻上，因为没同步的数据永远会存在可变可能。
3) 第三种方式：采用管道衔接的方式，前一个命令的输出作为后一个命令的输入。因为管道会创建子进程，子进程和父进程不会相互影响，例如：
   1) root> num=0; root>echo $num 显示：0；root>((num++); root>echo $num 显示：1
   2) root> ((num++))| echo ok  显示：ok  root> echo $num 显示：1 num没有变为2 说明 echo $num 和 ((num++)) 和 echo ok 是3个进程，所以不会相互影响
   3) 父子进程间不会影响就是完成持久化的第一步， 如果是把内存中10G的redis 运用子进程拷贝这10G 的数据也不现实，这样即出现了第二个技术：fork
   4) fork 实现的是在物理内存中找到redis 存储的位置，通过指针的方式进行标记，如果出现数据更新的情况 就让父进程重新开辟空间进行新值的记录，（copy on write）
   5) 子进程负责数据持久化，并且数据不会变化
   在 redis.conf 中配置 
   save 3600 1 300 100 60 10000 即可save to db on disk
   文件存储目录：dir /usr/local/var/db/redis/
   文件名称：dbfilename dump.rdb
   在redis> bgsave 就会持久化了。
   弊端： 因为不支持拉链存储，只有一个 dump.rdb 所有需要时节更名备份； 因为是一个小时一个小时备份的 如果宕机会出现大量数据丢失的可能
4) 第四种方式：AOF（append only file）根据写操作追加到文件， 
   优点：丢失数据少。
   缺点：体量会随着时间的变长，文件变大。 
   所以 4.0 前 会进行文件的重写，删除抵消掉的命令，合并重复的命令
   4.0 后 虽然也是文件重写，但是会先把RDB的数据导入到文件中，再以追加命令的形式补全文件。利用的RDB的快和AOF的全。
   在 redis.conf 中配置
   appendonly yes
   文件存储目录：dir /usr/local/var/db/redis/
   文件名称：appendfilename "appendonly.aof" 
   自动重写配置 
   auto-aof-rewrite-percentage 100
   auto-aof-rewrite-min-size 64mb
   重写操作 redis> BGREWRITEAOF
5) 写操作会触发IO，影响redis 正常运行，所以出现三种形式：
   appendfsync always ：每一次写入buffer 后都会flush ，这样数据是最可靠的。
   appendfsync everysec ：每一秒中调一次flush， 默认使用此参数
   appendfsync no ：等内核buffer 满了自动写入磁盘，所以会出现buffer大小的数据量丢失的可能

### 主从复制 help SLAVEOF/ help REPLICAOF
1) 单点、单实例会出现三个问题：1.单点故障 2.容量有限 3.压力
2) AKF 三轴体系：
   x轴 启动多个redis，全量镜像，一个用作更新删除，两个作为读，解决读写分离，单点故障
   y轴 启动多个redis，分别存储不同业务，解决容量有限问题
   z轴 启动多个redis，逻辑拆分，解决单一业务容量问题，比如第一个redis 存1-16 第二个存17-32
3) CAP原则: 一致性、可用性、分区容忍性
4) 人工控制主从复制方式：
   1) redis 主从复制命令：redis> REPLICAOF host port (host port:要追随的ip 和端口号)
   2) 如果主挂掉了，其他从节点想上位，在从节点输入命令 redis> REPLICAOF no one 就可以变为主， 其他节点再次输入 redis> REPLICAOF host port
5) 配置redis.conf 实现自动切换：
   1) 设置主 replicaof <masterip> <masterport>
   2) 密码 masterauth <master-password>
   3) 在同步完成前是否可以查询老数据，因为同步完成后老数据会被flush replica-serve-stale-data yes
   4) 磁盘传输还是网络传输 repl-diskless-sync yes
6) 解决容量问题的多种方法：
   1) 客户端逻辑拆分 redis，根据不同业务进行拆分，会导致数据不均匀的情况。
   2) 客户端通过hash取模(modula)的方式，把数据分散到多个redis 中，但是影响分布式扩展
   3) 客户端随机分发(random)数据到多个redis 中，这种不太好查询，这种情况一般适用于消费队列，一个client 负责分发数据 list， 另一个client 连接所有 redis 直接rpop 即可获得数据，类比于kafka
   4) 客户端通过一致性哈希(kemata)进行划分数据，利用算法（hash、md5、crc32、crc16）进行取值，再创建一个0-2^32大小的哈希环。
   让 data 通过算法生成的值映射到环的某一位置，让 node 通过算法也生成映射到环的某一位置，
   这样在环中就可以判断data值顺时针最近的node 点为当前data 存放node。
   如果想要新增node ，直接添加node 值，然后把原来node值的数据拆分给新node 即可。
   如果感觉node出现数据分布不均匀的情况，可以适当增加node 节点个数，同一个node 改成node+num的方式，同一个node 就可以生成多个值，放入到环中。
   5) 客户端因为要实现数据多node 获取，所以连接池个数会成倍增加，所以这个时候就需要用到 代理解决 连接成本高的问题
      1) proxy  可以在代理层实现数据分发策略（modula、random、kemata）,
      常见的三种代理：twemproxy、predixy、cluster
      通过查询路由的方式，随机连接一个redis 然后通过 mapping 获取key 对应的redis 去相应的redis获取数据

### 哨兵 sentinel
1) 监控（Monitoring）： Sentinel 会不断地检查你的主服务器和从服务器是否运作正常。
2) 提醒（Notification）： 当被监控的某个 Redis 服务器出现问题时， Sentinel 可以通过 API 向管理员或者其他应用程序发送通知。
3) 自动故障迁移（Automatic failover）： 当一个主服务器不能正常工作时， Sentinel 会开始一次自动故障迁移操作， 
   它会将失效主服务器的其中一个从服务器升级为新的主服务器， 并让失效主服务器的其他从服务器改为复制新的主服务器； 
   当客户端试图连接失效的主服务器时， 集群也会向客户端返回新主服务器的地址， 使得集群可以使用新主服务器代替失效服务器。
4) 启动哨兵 redis-server sentinel.conf --sentinel
5) 在sentinel.conf中配置 sentinel monitor mymaster 127.0.0.1 6379 
6) 主节点通过发布订阅消息发现其他哨兵


### 需求场景
1) 用redis 设置一个用户登录天数统计，随机某个日期区间登录多少天？
   解：可以设置 366 个字节， 1月1日登录了，第0个字节就为1 1月2日没登录，第二个字节为0，1月3 日登录了，第三个字节为1
   利用 setBit ZhangSan 0 1 ; setBit ZhangSan 2 1  bitcount ZhangSan 0 2  显示为2 即为2 天
2) 用redis 实现 活跃用户统计
   解：当key 为天数，value 为字节，每一位字节即为用户的userid，
   如果 20230101 userid为1 的用户登录了，setBit 20230101 1 1
   如果 20230102 userid为2 和 1 的用户都登录了， setBit 20230102 1 1 ; setBit 20230102 2 1
   求 20230101 至 20230102 的活跃用户数 bitop or actKey 20230101 20230102； bitcount actKey 0 -1 即为活跃用户数2
3) 用redis 存储 用户三天的消息
   解：可以利用 sorted set 按照 key 为用户名称 score 为时间戳，value 为消息内容 进行存储
   如果超过三天的数据 可以使用 ZREMRANGEBYRANK key start stop 设置删除范围

## 面试常见问题：
### 击穿：请求在高并发的情况下，当redis 因为过期或者LRU、LFU策略导致删除，大量访问数据库获取数据时。
1) 通过抢锁 setnx 只有第一个请求会查询数据库，获取结果后在redis 中添加数据，后返回并删除key ，其他请求没有获取到key 就先sleep 1秒后再次查看
2) 抢锁必然会发生死锁现象，当第一个请求获取到锁，但是在请求DB 时挂了， 无法删除锁，其他请求无法请求DB，解决办法是给 key 添加过期时间
3) 这是又会出现新的状况，当第一个请求在DB中查询缓慢时，会发生key消失， 后来的请求也会在db 中进行阻塞。所以解决办法是多线程，第一个线程去DB取数据，第二个线程监控数据是否取出，如果没有增加过期时间。

### 穿透：从业务中查询系统根本不存在的数据
1) 布隆过滤器，因为布隆过滤器 无法删除key，所以当DB 更新数据时，需要给布隆过滤器设置空key
2) 布谷鸟过滤器

### 雪崩：大量的key 同时失效，间接造成大量访问到达DB
1) 与时点性无关： 可以过期时间+随机值
2) 与时点性有关（每日0点要刷新新的key数据，所以要让key在0点全部过期）：
   1) 可以在业务层加入0点延时，让请求来的数量减少。
   2) 多线程---依赖击穿方案
3) 与时点性有关（618、双11）： 采用提前提供key的方式存储，当天直接使用即可。
