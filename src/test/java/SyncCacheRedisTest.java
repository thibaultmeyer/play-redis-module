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
import com.zero_x_baadf00d.play.module.redis.cache.SyncCacheRedisImpl;
import com.zero_x_baadf00d.play.module.redis.cache.SyncCacheRedisModule;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import play.api.Environment;
import play.api.inject.Module;

import java.util.Optional;

import static org.mockito.Mockito.mock;

/**
 * SyncCacheRedisTest.
 *
 * @author Felipe Bonezi
 * @version 20.11
 * @since 20.11.05
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SyncCacheRedisTest extends AbstractRedisTest {

    /**
     * Default constructor.
     *
     * @since 20.11.05
     */
    public SyncCacheRedisTest() {
        super(6379);
    }

    /**
     * @since 20.11.05
     */
    @Test
    public void cacheRedisTest_000_binding() {
        final Module module = new SyncCacheRedisModule();
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
        final PlayRedis playRedis = ((SyncCacheRedisImpl) this.cacheApi).asPlayRedis();
        Assert.assertNotNull(playRedis);
    }

    /**
     * @since 20.11.05
     */
    @Test
    public void cacheRedisTest_002_set_get() {
        this.cacheApi.set("junit.item", "Hello World!");
        final Optional<String> opt0 = this.cacheApi.get("junit.item");
        Assert.assertTrue(opt0.isPresent());

        final String helloWorld = opt0.get();
        Assert.assertEquals("Hello World!", helloWorld);

        this.cacheApi.set("junit.item", "Hello World! class");
        final Optional<String> opt1 = this.cacheApi.get("junit.item");
        Assert.assertTrue(opt1.isPresent());

        final String helloWorld2 = opt1.get();
        Assert.assertEquals("Hello World! class", helloWorld2);

        this.cacheApi.set("junit.item", "Hello World! class");
        final Optional<String> opt2 = this.cacheApi.get("junit.item");
        Assert.assertTrue(opt2.isPresent());

        final String helloWorld3 = opt2.get();
        Assert.assertEquals("Hello World! class", helloWorld3);

        this.cacheApi.set("junit.item", 42);
        final Optional<Integer> opt4 = this.cacheApi.get("junit.item");
        Assert.assertTrue(opt4.isPresent());

        final Integer number = opt4.get();
        Assert.assertEquals(42L, number.longValue());

        this.cacheApi.set("junit.item", 1337L);
        final Optional<Integer> opt5 = this.cacheApi.get("junit.item");
        Assert.assertTrue(opt5.isPresent());

        final Integer number2 = opt5.get();
        Assert.assertEquals(1337L, number2.longValue());
    }

    /**
     * @since 20.11.05
     */
    @Test
    public void cacheRedisTest_003_remove() {
        this.cacheApi.set("junit.item", "test");
        Assert.assertTrue(this.cacheApi.get("junit.item").isPresent());
        this.cacheApi.remove("junit.item");
        Assert.assertFalse(this.cacheApi.get("junit.item").isPresent());
    }

    /**
     * @since 20.11.05
     */
    @Test
    public void cacheRedisTest_004_getOrElse() {
        String data = this.cacheApi.getOrElseUpdate("junit.item", () -> "getOrElse");
        Assert.assertEquals("getOrElse", data);

        data = this.cacheApi.getOrElseUpdate("junit.item", () -> "getOrElse");
        Assert.assertEquals("getOrElse", data);

        data = this.cacheApi.getOrElseUpdate("junit.item", () -> "getOrElse");
        Assert.assertEquals("getOrElse", data);

        try {
            // This test will raise a Cast exception on the method "getOrElseUpdate"
            final Long l = this.cacheApi.getOrElseUpdate("junit.item", () -> 42L);
            Assert.assertEquals(42, l.longValue());
            Assert.fail();
        } catch (final Exception ignore) {
        }

        this.cacheApi.getOrElseUpdate("junit.item18", () -> {
            try {
                // This test will raise a Cast exception on the method "getOrElseUpdate"
                final Object o = "cast-error";
                Long o1 = (Long) o;
                Assert.fail();
                return o1;
            } catch (final Exception ignore) {
                return null;
            }
        });

        try {
            // This test will raise exception on the method "getOrElseUpdate"
            final Long l = this.cacheApi.getOrElseUpdate("junit.item19", () -> {
                throw new Exception();
            });
            Assert.assertEquals(42, l.longValue());
            Assert.fail();
        } catch (final Exception ignore) {
        }
    }

}
