/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Thibault Meyer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import com.fasterxml.jackson.core.type.TypeReference;
import com.zero_x_baadf00d.play.module.redis.PlayRedisModule;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import play.api.Environment;
import play.api.inject.Module;
import redis.clients.jedis.Jedis;

import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * RedisTest.
 *
 * @author Thibault Meyer
 * @version 17.05.26
 * @since 16.11.13
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RedisTest extends AbstractRedisTest {

    /**
     * Default constructor.
     *
     * @since 17.03.26
     */
    public RedisTest() {
        super(6379);
    }

    /**
     * @since 16.11.13
     */
    @Test
    public void redisTest_000_binding() {
        final Module module = new PlayRedisModule();
        Assert.assertEquals(1,
            module.bindings(
                mock(Environment.class),
                this.application.configuration().getWrappedConfiguration()
            ).length()
        );
    }

    /**
     * @since 16.11.13
     */
    @Test
    public void redisTest_001_tryLock() {
        boolean isLockAcquired = this.playRedis.tryLock("junit.lock", 900);
        Assert.assertTrue(isLockAcquired);

        isLockAcquired = this.playRedis.tryLock("junit.lock", 900);
        Assert.assertFalse(isLockAcquired);

        this.playRedis.set("junit.item", new TypeReference<String>() {
        }, "Hello World!", 900);
        isLockAcquired = this.playRedis.tryLock("junit.item", 900);
        Assert.assertFalse(isLockAcquired);
    }

    /**
     * @since 16.11.13
     */
    @Test
    public void redisTest_002_set_get() {
        this.playRedis.set("junit.item", new TypeReference<String>() {
        }, "Hello World!");
        final String helloWorld = this.playRedis.get("junit.item", new TypeReference<String>() {
        });
        Assert.assertEquals("Hello World!", helloWorld);

        this.playRedis.set("junit.item", new TypeReference<Integer>() {
        }, 42);
        final Long number = this.playRedis.get("junit.item", new TypeReference<Long>() {
        });
        Assert.assertEquals((Long) 42L, number);
    }

    /**
     * @since 16.11.13
     */
    @Test
    public void redisTest_003_exists() throws InterruptedException {
        this.playRedis.set("junit.item", new TypeReference<String>() {
        }, "Hello World!", 2);
        Assert.assertTrue(this.playRedis.exists("junit.item"));
        Thread.sleep(2500);
        Assert.assertFalse(this.playRedis.exists("junit.item"));
    }

    /**
     * @since 16.11.13
     */
    @Test
    public void redisTest_004_addInList() {
        this.playRedis.addInList("junit.item", new TypeReference<Integer>() {
        }, 1);
        this.playRedis.addInList("junit.item", new TypeReference<Integer>() {
        }, 4);
        this.playRedis.addInList("junit.item", new TypeReference<Integer>() {
        }, 3);
        this.playRedis.addInList("junit.item", new TypeReference<Integer>() {
        }, 2);
        List<Integer> numbers = this.playRedis.getFromList("junit.item", new TypeReference<Integer>() {
        });
        Assert.assertArrayEquals(numbers.toArray(), new Integer[]{2, 3, 4, 1});

        this.playRedis.addInList("junit.item2", new TypeReference<Integer>() {
        }, 1, 3);
        this.playRedis.addInList("junit.item2", new TypeReference<Integer>() {
        }, 4, 3);
        this.playRedis.addInList("junit.item2", new TypeReference<Integer>() {
        }, 3, 3);
        this.playRedis.addInList("junit.item2", new TypeReference<Integer>() {
        }, 2, 3);
        numbers = this.playRedis.getFromList("junit.item2", new TypeReference<Integer>() {
        });
        Assert.assertArrayEquals(numbers.toArray(), new Integer[]{2, 3, 4});

        numbers = this.playRedis.getFromList("junit.item2", new TypeReference<Integer>() {
        }, 0, 2);
        Assert.assertArrayEquals(numbers.toArray(), new Integer[]{2, 3});
    }

    /**
     * @since 16.11.13
     */
    @Test
    public void redisTest_005_getConnection() {
        try (final Jedis conn = this.playRedis.getConnection()) {
            conn.set("junit.item", "test@domain.local");
            Assert.assertTrue(conn.exists("junit.item"));
            Assert.assertEquals(
                "test@domain.local",
                conn.get("junit.item")
            );
        }
        try (final Jedis conn = this.playRedis.getConnection(1)) {
            Assert.assertFalse(conn.exists("junit.item"));
        }
    }

    /**
     * @since 16.11.13
     */
    @Test
    public void redisTest_006_remove() {
        this.playRedis.set("junit.item", new TypeReference<String>() {
        }, "test");
        Assert.assertTrue(this.playRedis.exists("junit.item"));
        this.playRedis.remove("junit.item");
        Assert.assertFalse(this.playRedis.exists("junit.item"));
    }

    /**
     * @since 16.11.13
     */
    @Test
    public void redisTest_007_getOrElse() {
        final String data = this.playRedis.getOrElse("junit.item", new TypeReference<String>() {
        }, () -> "getOrElse");
        Assert.assertEquals("getOrElse", data);

        // This test will raise a Cast exception on the method "get"
        final Long l = this.playRedis.getOrElse("junit.item", new TypeReference<Long>() {
        }, () -> 42L);
        Assert.assertEquals(42, l.longValue());
    }

    /**
     * @since 17.02.14
     */
    @Test
    public void redisTest_008_increment() throws InterruptedException {
        Long counter = this.playRedis.increment("junit.counter", 2);
        Assert.assertEquals(1, counter.longValue());
        Thread.sleep(2500);
        counter = this.playRedis.increment("junit.counter", 2);
        Assert.assertEquals(1, counter.longValue());
        this.playRedis.remove("junit.counter");

        counter = this.playRedis.increment("junit.counter");
        Assert.assertEquals(1, counter.longValue());
        counter = this.playRedis.increment("junit.counter");
        Assert.assertEquals(2, counter.longValue());

        counter = this.playRedis.increment("junit.counter", 2);
        Assert.assertEquals(3, counter.longValue());
        Thread.sleep(2500);
        counter = this.playRedis.increment("junit.counter", 2);
        Assert.assertEquals(4, counter.longValue());
    }
}
