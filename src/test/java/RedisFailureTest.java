/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 - 2017 Thibault Meyer
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
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * RedisFailureTest.
 *
 * @author Thibault Meyer
 * @version 17.03.26
 * @since 17.02.14
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RedisFailureTest extends AbstractRedisTest {

    /**
     * Default constructor.
     *
     * @since 17.03.26
     */
    public RedisFailureTest() {
        super(12345);
    }

    /**
     * @since 17.02.14
     */
    @Test
    public void redisFailureTest_001_tryLock() {
        boolean isLockAcquired = this.playRedis.tryLock("junit.lock", 900);
        Assert.assertFalse(isLockAcquired);
    }

    /**
     * @since 17.02.14
     */
    @Test
    public void redisFailureTest_002_set_get() {
        try {
            this.playRedis.set("junit.item", new TypeReference<String>() {
            }, "Hello World!");
            Assert.fail();
        } catch (final JedisConnectionException ignore) {
        }
    }
}
