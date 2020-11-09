/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Thibault Meyer
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

import com.zero_x_baadf00d.play.module.redis.PlayRedis;
import com.zero_x_baadf00d.play.module.redis.cache.AsyncCacheRedisImpl;
import com.zero_x_baadf00d.play.module.redis.cache.AsyncCacheRedisModule;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import play.api.Environment;
import play.api.inject.Module;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.mockito.Mockito.mock;

/**
 * AsyncCacheRedisTest.
 *
 * @author Felipe Bonezi
 * @version 20.11
 * @since 20.11.05
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AsyncCacheRedisTest extends AbstractRedisTest {

    /**
     * Default constructor.
     *
     * @since 20.11.05
     */
    public AsyncCacheRedisTest() {
        super(6379);
    }

    /**
     * @since 20.11.05
     */
    @Test
    public void cacheRedisTest_000_binding() {
        final Module module = new AsyncCacheRedisModule();
        Assert.assertEquals(1,
            module.bindings(
                mock(Environment.class),
                this.application.asScala().configuration()
            ).length()
        );
    }

    /**
     * @since 20.11.05
     */
    @Test
    public void cacheRedisTest_001_playRedisImpl() {
        final PlayRedis playRedis = ((AsyncCacheRedisImpl) this.asyncCacheApi).asPlayRedis();
        Assert.assertNotNull(playRedis);
    }

    /**
     * @since 20.11.05
     */
    @Test
    public void cacheRedisTest_002_set_get() throws InterruptedException, ExecutionException, TimeoutException {
        this.asyncCacheApi.set("junit.item", "Hello World!")
            .toCompletableFuture()
            .get(1, TimeUnit.MINUTES);
        final Optional<String> opt0 = this.asyncCacheApi.<String>get("junit.item")
            .toCompletableFuture()
            .get(1, TimeUnit.MINUTES);
        Assert.assertTrue(opt0.isPresent());

        final String helloWorld = opt0.get();
        Assert.assertEquals("Hello World!", helloWorld);

        this.asyncCacheApi.set("junit.item", "Hello World! class")
            .toCompletableFuture()
            .get(1, TimeUnit.MINUTES);
        final Optional<String> opt1 = this.asyncCacheApi.<String>get("junit.item")
            .toCompletableFuture()
            .get(1, TimeUnit.MINUTES);
        Assert.assertTrue(opt1.isPresent());

        final String helloWorld2 = opt1.get();
        Assert.assertEquals("Hello World! class", helloWorld2);

        this.asyncCacheApi.set("junit.item", "Hello World! class")
            .toCompletableFuture()
            .get(1, TimeUnit.MINUTES);
        final Optional<String> opt2 = this.asyncCacheApi.<String>get("junit.item")
            .toCompletableFuture()
            .get(1, TimeUnit.MINUTES);
        Assert.assertTrue(opt2.isPresent());

        final String helloWorld3 = opt2.get();
        Assert.assertEquals("Hello World! class", helloWorld3);

        this.asyncCacheApi.set("junit.item", 42)
            .toCompletableFuture()
            .get(1, TimeUnit.MINUTES);
        final Optional<Integer> opt4 = this.asyncCacheApi.<Integer>get("junit.item")
            .toCompletableFuture()
            .get(1, TimeUnit.MINUTES);
        Assert.assertTrue(opt4.isPresent());

        final Integer number = opt4.get();
        Assert.assertEquals(42L, number.longValue());

        this.asyncCacheApi.set("junit.item", 1337L)
            .toCompletableFuture()
            .get(1, TimeUnit.MINUTES);
        final Optional<Integer> opt5 = this.asyncCacheApi.<Integer>get("junit.item")
            .toCompletableFuture()
            .get(1, TimeUnit.MINUTES);
        Assert.assertTrue(opt5.isPresent());

        final Integer number2 = opt5.get();
        Assert.assertEquals(1337L, number2.longValue());
    }

    /**
     * @since 20.11.05
     */
    @Test
    public void cacheRedisTest_003_remove() throws InterruptedException, ExecutionException, TimeoutException {
        this.asyncCacheApi.set("junit.item", "test")
            .toCompletableFuture()
            .get(1, TimeUnit.MINUTES);
        Assert.assertTrue(this.asyncCacheApi.get("junit.item")
            .toCompletableFuture()
            .get(1, TimeUnit.MINUTES)
            .isPresent());

        this.asyncCacheApi.remove("junit.item");
        Assert.assertFalse(this.asyncCacheApi.get("junit.item")
            .toCompletableFuture()
            .get(1, TimeUnit.MINUTES)
            .isPresent());
    }

    /**
     * @since 20.11.05
     */
    @Test
    public void cacheRedisTest_004_getOrElse() throws InterruptedException, ExecutionException, TimeoutException {
        String data = this.asyncCacheApi.getOrElseUpdate("junit.item", () -> completedFuture("getOrElse"))
            .toCompletableFuture()
            .get(1, TimeUnit.MINUTES);
        Assert.assertEquals("getOrElse", data);

        data = this.asyncCacheApi.getOrElseUpdate("junit.item", () -> completedFuture("getOrElse"))
            .toCompletableFuture()
            .get(1, TimeUnit.MINUTES);
        Assert.assertEquals("getOrElse", data);

        data = this.asyncCacheApi.getOrElseUpdate("junit.item", () -> completedFuture("getOrElse"))
            .toCompletableFuture()
            .get(1, TimeUnit.MINUTES);
        Assert.assertEquals("getOrElse", data);

        try {
            // This test will raise a Cast exception on the method "getOrElseUpdate"
            final Long l = this.asyncCacheApi.getOrElseUpdate("junit.item", () -> completedFuture(42L))
                .toCompletableFuture()
                .get(1, TimeUnit.MINUTES);
            Assert.assertEquals(42, l.longValue());
            Assert.fail();
        } catch (final Exception ignore) {
        }

        this.asyncCacheApi.getOrElseUpdate("junit.item18", () -> {
            try {
                // This test will raise a Cast exception on the method "getOrElseUpdate"
                final Object o = "cast-error";
                Long o1 = (Long) o;
                Assert.fail();
                return completedFuture(o1);
            } catch (final Exception ignore) {
                return completedFuture(null);
            }
        }).toCompletableFuture()
            .get(1, TimeUnit.MINUTES);

        try {
            // This test will raise exception on the method "getOrElseUpdate"
            final Long l = this.asyncCacheApi.<Long>getOrElseUpdate("junit.item19", () -> {
                throw new Exception();
            }).toCompletableFuture()
                .get(1, TimeUnit.MINUTES);
            Assert.assertEquals(42, l.longValue());
            Assert.fail();
        } catch (final Exception ignore) {
        }
    }

    /**
     * @since 20.11.05
     */
    @Test
    public void cacheRedisTest_005_removeAll() throws InterruptedException, ExecutionException, TimeoutException {
        this.asyncCacheApi.removeAll();

        String data = this.asyncCacheApi.getOrElseUpdate("junit.item", () -> completedFuture("getOrElse"))
            .toCompletableFuture()
            .get(1, TimeUnit.MINUTES);
        Assert.assertEquals("getOrElse", data);
    }

}
