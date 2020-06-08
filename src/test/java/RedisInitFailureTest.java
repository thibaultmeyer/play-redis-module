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

import com.typesafe.config.ConfigException;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.HashMap;

/**
 * RedisInitFailureTest.
 *
 * @author Thibault Meyer
 * @version 17.08.24
 * @since 17.08.23
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RedisInitFailureTest {

    /**
     * @since 17.08.23
     */
    @Test
    public void redisInitFailureTest_001_host_not_empty() {
        try {
            new AbstractRedisTest(6379, new HashMap<String, Object>() {{
                put("redis.host", " ");
            }}) {{
            }}.initializeRedisModule();
            Assert.fail();
        } catch (final ConfigException ignore) {
        }
    }

    /**
     * @since 17.08.23
     */
    @Test
    public void redisInitFailureTest_002_port_range() {
        try {
            new AbstractRedisTest(80293) {{
            }}.initializeRedisModule();
            Assert.fail();
        } catch (final ConfigException ignore) {
        }

        try {
            new AbstractRedisTest(0) {{
            }}.initializeRedisModule();
            Assert.fail();
        } catch (final ConfigException ignore) {
        }
    }

    /**
     * @since 17.08.23
     */
    @Test
    public void redisInitFailureTest_003_timeout_only_positive() {
        try {
            new AbstractRedisTest(6379, new HashMap<String, Object>() {{
                put("redis.conn.timeout", -1);
            }}) {{
            }}.initializeRedisModule();
            Assert.fail();
        } catch (final ConfigException ignore) {
        }
    }

    /**
     * @since 17.08.23
     */
    @Test
    public void redisInitFailureTest_004_total_conn() {
        try {
            new AbstractRedisTest(6379, new HashMap<String, Object>() {{
                put("redis.conn.maxtotal", -1);
            }}) {{
            }}.initializeRedisModule();
            Assert.fail();
        } catch (final ConfigException ignore) {
        }
    }

    /**
     * @since 17.08.23
     */
    @Test
    public void redisInitFailureTest_005_minidle_cant_gt_totalconn() {
        try {
            new AbstractRedisTest(6379, new HashMap<String, Object>() {{
                put("redis.conn.maxtotal", 10);
                put("redis.conn.minidle", 12);
            }}) {{
            }}.initializeRedisModule();
            Assert.fail();
        } catch (final ConfigException ignore) {
        }
    }

    /**
     * @since 17.08.23
     */
    @Test
    public void redisInitFailureTest_006_minidle_gte_zero() {
        try {
            new AbstractRedisTest(6379, new HashMap<String, Object>() {{
                put("redis.conn.minidle", -1);
            }}) {{
            }}.initializeRedisModule();
            Assert.fail();
        } catch (final ConfigException ignore) {
        }
    }

    /**
     * @since 17.08.23
     */
    @Test
    public void redisInitFailureTest_007_maxidle_cant_lt_minidle() {
        try {
            new AbstractRedisTest(6379, new HashMap<String, Object>() {{
                put("redis.conn.minidle", 2);
                put("redis.conn.maxidle", 1);
            }}) {{
            }}.initializeRedisModule();
            Assert.fail();
        } catch (final ConfigException ignore) {
        }
    }

    /**
     * @since 17.08.23
     */
    @Test
    public void redisInitFailureTest_008_maxidle_cant_gt_totalconn() {
        try {
            new AbstractRedisTest(6379, new HashMap<String, Object>() {{
                put("redis.conn.maxtotal", 10);
                put("redis.conn.maxidle", 12);
            }}) {{
            }}.initializeRedisModule();
            Assert.fail();
        } catch (final ConfigException ignore) {
        }
    }

    /**
     * @since 17.08.23
     */
    @Test
    public void redisInitFailureTest_009_defaultdb_cant_lt_zero() {
        try {
            new AbstractRedisTest(6379, new HashMap<String, Object>() {{
                put("redis.defaultdb", -1);
            }}) {{
            }}.initializeRedisModule();
            Assert.fail();
        } catch (final ConfigException ignore) {
        }
    }

    /**
     * @since 17.08.23
     */
    @Test
    public void redisInitFailureTest_010_deprecated_warning() {
        try {
            new AbstractRedisTest(6379, new HashMap<String, Object>() {{
                put("redis.defaultdb", -1);
            }}) {{
            }}.initializeRedisModule();
            Assert.fail();
        } catch (final ConfigException ignore) {
        }
    }
}
