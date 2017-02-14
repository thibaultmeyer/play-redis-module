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
import com.zero_x_baadf00d.play.module.redis.RedisModule;
import com.zero_x_baadf00d.play.module.redis.RedisModuleBinder;
import com.zero_x_baadf00d.play.module.redis.RedisModuleImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import play.Application;
import play.api.Environment;
import play.api.inject.Module;
import play.inject.ApplicationLifecycle;
import play.test.Helpers;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * RedisTest.
 *
 * @author Thibault Meyer
 * @version 17.02.14
 * @since 16.11.13
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RedisTest {

    /**
     * Handle to the Redis module
     *
     * @since 16.11.13
     */
    private RedisModule redisModule;

    /**
     * Initialize Redis module.
     *
     * @since 16.11.13
     */
    @Before
    public void initializeRedisModule() {
        if (this.redisModule == null) {
            final Application application = Helpers.
                    fakeApplication(new HashMap<String, Object>() {{
                        put("redis.default.db.default", 0);
                        put("redis.default.host", "127.0.0.1");
                        put("redis.default.port", 6379);
                    }});
            Assert.assertEquals(
                    (Long) 0L,
                    application.configuration().getLong("redis.default.db.default")
            );
            Assert.assertEquals(
                    "127.0.0.1",
                    application.configuration().getString("redis.default.host")
            );
            Assert.assertEquals(
                    (Long) 6379L,
                    application.configuration().getLong("redis.default.port")
            );
            this.redisModule = new RedisModuleImpl(
                    mock(ApplicationLifecycle.class),
                    application.configuration()
            );
            Assert.assertNotEquals(null, this.redisModule);
            this.redisModule.remove(
                    "junit.lock",
                    "junit.item",
                    "junit.item2",
                    "junit.counter"
            );
        }
    }

    /**
     * @since 16.11.13
     */
    @Test
    public void redisTest_000_binding() {
        final Module module = new RedisModuleBinder();
        Assert.assertEquals(1,
                module.bindings(
                        mock(Environment.class),
                        Helpers.fakeApplication().configuration().getWrappedConfiguration()
                ).length()
        );
    }

    /**
     * @since 16.11.13
     */
    @Test
    public void redisTest_001_tryLock() {
        boolean isLockAcquired = this.redisModule.tryLock("junit.lock", 900);
        Assert.assertTrue(isLockAcquired);

        isLockAcquired = this.redisModule.tryLock("junit.lock", 900);
        Assert.assertFalse(isLockAcquired);

        this.redisModule.set("junit.item", new TypeReference<String>() {
        }, "Hello World!", 900);
        isLockAcquired = this.redisModule.tryLock("junit.item", 900);
        Assert.assertFalse(isLockAcquired);
    }

    /**
     * @since 16.11.13
     */
    @Test
    public void redisTest_002_set_get() {
        this.redisModule.set("junit.item", new TypeReference<String>() {
        }, "Hello World!");
        final String helloWorld = this.redisModule.get("junit.item", new TypeReference<String>() {
        });
        Assert.assertEquals("Hello World!", helloWorld);

        this.redisModule.set("junit.item", new TypeReference<Integer>() {
        }, 42);
        final Long number = this.redisModule.get("junit.item", new TypeReference<Long>() {
        });
        Assert.assertEquals((Long) 42L, number);
    }

    /**
     * @since 16.11.13
     */
    @Test
    public void redisTest_003_exists() throws InterruptedException {
        this.redisModule.set("junit.item", new TypeReference<String>() {
        }, "Hello World!", 2);
        Assert.assertTrue(this.redisModule.exists("junit.item"));
        Thread.sleep(2500);
        Assert.assertFalse(this.redisModule.exists("junit.item"));
    }

    /**
     * @since 16.11.13
     */
    @Test
    public void redisTest_004_addInList() {
        this.redisModule.addInList("junit.item", new TypeReference<Integer>() {
        }, 1);
        this.redisModule.addInList("junit.item", new TypeReference<Integer>() {
        }, 4);
        this.redisModule.addInList("junit.item", new TypeReference<Integer>() {
        }, 3);
        this.redisModule.addInList("junit.item", new TypeReference<Integer>() {
        }, 2);
        List<Integer> numbers = this.redisModule.getFromList("junit.item", new TypeReference<Integer>() {
        });
        Assert.assertArrayEquals(numbers.toArray(), new Integer[]{2, 3, 4, 1});

        this.redisModule.addInList("junit.item2", new TypeReference<Integer>() {
        }, 1, 3);
        this.redisModule.addInList("junit.item2", new TypeReference<Integer>() {
        }, 4, 3);
        this.redisModule.addInList("junit.item2", new TypeReference<Integer>() {
        }, 3, 3);
        this.redisModule.addInList("junit.item2", new TypeReference<Integer>() {
        }, 2, 3);
        numbers = this.redisModule.getFromList("junit.item2", new TypeReference<Integer>() {
        });
        Assert.assertArrayEquals(numbers.toArray(), new Integer[]{2, 3, 4});

        numbers = this.redisModule.getFromList("junit.item2", new TypeReference<Integer>() {
        }, 0, 2);
        Assert.assertArrayEquals(numbers.toArray(), new Integer[]{2, 3});
    }

    /**
     * @since 16.11.13
     */
    @Test
    public void redisTest_005_getConnection() {
        try (final Jedis conn = this.redisModule.getConnection()) {
            conn.set("junit.item", "test@domain.local");
            Assert.assertTrue(conn.exists("junit.item"));
            Assert.assertEquals(
                    "test@domain.local",
                    conn.get("junit.item")
            );
        }
        try (final Jedis conn = this.redisModule.getConnection(1)) {
            Assert.assertFalse(conn.exists("junit.item"));
        }
    }

    /**
     * @since 16.11.13
     */
    @Test
    public void redisTest_006_remove() {
        this.redisModule.set("junit.item", new TypeReference<String>() {
        }, "test");
        Assert.assertTrue(this.redisModule.exists("junit.item"));
        this.redisModule.remove("junit.item");
        Assert.assertFalse(this.redisModule.exists("junit.item"));
    }

    /**
     * @since 16.11.13
     */
    @Test
    public void redisTest_007_getOrElse() {
        final String data = this.redisModule.getOrElse("junit.item", new TypeReference<String>() {
        }, () -> "getOrElse");
        Assert.assertEquals("getOrElse", data);

        // This test will raise a Cast exception on the method "get"
        final Long l = this.redisModule.getOrElse("junit.item", new TypeReference<Long>() {
        }, () -> 42L);
        Assert.assertEquals(42, l.longValue());
    }

    /**
     * @since 17.02.14
     */
    @Test
    public void redisTest_008_increment() throws InterruptedException {
        Long counter = this.redisModule.increment("junit.counter", 2);
        Assert.assertEquals(1, counter.longValue());
        Thread.sleep(2500);
        counter = this.redisModule.increment("junit.counter", 2);
        Assert.assertEquals(1, counter.longValue());
        this.redisModule.remove("junit.counter");

        counter = this.redisModule.increment("junit.counter");
        Assert.assertEquals(1, counter.longValue());
        counter = this.redisModule.increment("junit.counter");
        Assert.assertEquals(2, counter.longValue());

        counter = this.redisModule.increment("junit.counter", 2);
        Assert.assertEquals(3, counter.longValue());
        Thread.sleep(2500);
        counter = this.redisModule.increment("junit.counter", 2);
        Assert.assertEquals(4, counter.longValue());
    }
}
