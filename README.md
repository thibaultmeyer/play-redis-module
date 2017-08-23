# Play Redis Module


[![Latest release](https://img.shields.io/badge/latest_release-17.08-orange.svg)](https://github.com/0xbaadf00d/play-redis-module/releases)
[![JitPack](https://jitpack.io/v/0xbaadf00d/play-redis-module.svg)](https://jitpack.io/#0xbaadf00d/play-redis-module)
[![Build](https://img.shields.io/travis-ci/0xbaadf00d/play-redis-module.svg?branch=master&style=flat)](https://travis-ci.org/0xbaadf00d/play-redis-module)
[![codecov](https://codecov.io/gh/0xbaadf00d/play-redis-module/branch/develop/graph/badge.svg)](https://codecov.io/gh/0xbaadf00d/play-redis-module)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/0xbaadf00d/play-redis-module/master/LICENSE)

Redis module for Play Framework 2
*****

## Add play-redis-module to your project

#### build.sbt

     resolvers += "jitpack" at "https://jitpack.io"

     libraryDependencies += "com.github.0xbaadf00d" % "play-redis-module" % "release~YY.MM"

#### application.conf

    # Play Redis Module
    # ~~~~~
    redis {
      default {
        host = "127.0.0.1"
        port = 6379
        password = "your-password"  # Optional
        db {
          default = 0
        }
        conn {
          timeout = 2000
          maxtotal = 256
          maxidle  = 32
          minidle  = 8
        }
      }
    }



## Usage

#### Example 1

```java
    public class MyController extends Controller {

        private final PlayRedis playRedis;

        @Inject
        public MyController(final PlayRedis playRedis) {
            this.playRedis = playRedis;
        }

        public Result index() {
            final String token = this.playRedis.getOrElse("key", new TypeReference<String>() {}, () -> {
                return "new-token";
            }, 60);
            return ok(token);
        }
    }
```


#### Example 2

```java
    public class MyController extends Controller {

        @Inject
        private final PlayRedis playRedis;

        public Result index() {
            final List<String> tokens = this.playRedis.getOrElse("key", new TypeReference<List<String>>() {}, () -> {
                final List<String> tokens = new ArrayList<>();
                tokens.add("token 1");
                tokens.add("token 2");
                return tokens;
            }, 60);
            return ok(token);
        }
    }
```


#### Example 3

```java
    final PlayRedis playRedis = Play.application().injector().instanceOf(PlayRedis.class);
    try (final Jedis jedis = playRedis.getConnection()) {
        jedis.set("key", "Hello World!")
        jedis.expire("key", 60);
    }
```



## License
This project is released under terms of the [MIT license](https://raw.githubusercontent.com/0xbaadf00d/play-redis-module/master/LICENSE).
