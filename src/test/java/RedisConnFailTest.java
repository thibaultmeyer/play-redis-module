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

import com.zero_x_baadf00d.play.module.redis.PlayRedisImpl;
import org.junit.*;
import org.junit.runners.MethodSorters;
import play.Application;
import play.inject.ApplicationLifecycle;
import play.test.Helpers;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.mock;

/**
 * RedisConnFailTest.
 *
 * @author Thibault Meyer
 * @version 17.08.24
 * @since 17.06.26
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RedisConnFailTest {

    /**
     * Handle to the Redis module.
     *
     * @since 17.06.26
     */
    protected PlayRedisImpl playRedis;

    /**
     * Handle to the current application instance.
     *
     * @since 17.06.26
     */
    protected Application application;

    /**
     * Initialize Redis module.
     *
     * @since 17.06.26
     */
    @Before
    public void initializeRedisModule() {
        if (this.playRedis == null) {
            this.application = Helpers.
                fakeApplication(new HashMap<String, Object>() {{
                    put(
                        "play.modules.disabled",
                        Collections.singletonList(
                            "com.zero_x_baadf00d.play.module.redis.PlayRedisModule"
                        )
                    );
                    put("redis.default.host", "127.0.0.1");
                    put("redis.default.port", 6379);
                    put("redis.default.password", "false-password");
                }});
            Assert.assertEquals(
                "false-password",
                this.application.config().getString("redis.default.password")
            );
            this.playRedis = new PlayRedisImpl(
                mock(ApplicationLifecycle.class),
                this.application.config()
            );
            Assert.assertNotEquals(null, this.playRedis);
        }
    }

    /**
     * Destroy Redis module.
     *
     * @since 17.06.26
     */
    @After
    public void destroyRedis() {
        if (this.playRedis != null) {
            try {
                Assert.assertNull(
                    this.playRedis.stopHook()
                        .toCompletableFuture()
                        .get()
                );
            } catch (InterruptedException | ExecutionException e) {
                Assert.fail();
            }
        }
    }

    /**
     * @since 17.06.26
     */
    @Test
    public void redisConnTailTest_001_password_not_set() {
        try {
            this.playRedis.getConnection();
        } catch (JedisConnectionException ex) {
            if (!ex.getCause().getMessage().startsWith("ERR Client sent AUTH")) {
                Assert.fail();
            } else {
                return;
            }
        }
        Assert.fail();
    }
}
