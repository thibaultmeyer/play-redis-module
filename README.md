# Play Redis Module


[![Latest release](https://img.shields.io/badge/latest_release-20.11-orange.svg)](https://github.com/thibaultmeyer/play-redis-module/releases)
[![JitPack](https://jitpack.io/v/thibaultmeyer/play-redis-module.svg)](https://jitpack.io/#thibaultmeyer/play-redis-module)
[![Build](https://api.travis-ci.org/thibaultmeyer/play-redis-module.svg)](https://travis-ci.org/thibaultmeyer/play-redis-module)
[![codecov](https://codecov.io/gh/thibaultmeyer/play-redis-module/branch/develop/graph/badge.svg)](https://codecov.io/gh/thibaultmeyer/play-redis-module)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/thibaultmeyer/play-redis-module/master/LICENSE)

Redis module for Play Framework 2
*****

## Add play-redis-module to your project

#### build.sbt

     resolvers += "jitpack" at "https://jitpack.io"

     libraryDependencies += "com.github.thibaultmeyer" % "play-redis-module" % "release~YY.MM"


#### application.conf

    ## Play Redis Module
    # https://github.com/thibaultmeyer/play-redis-module
    # ~~~~~
    redis {

      # Redis host. Must be an IP address or a valid hostname
      host = "127.0.0.1"

      # Defines the port on which the server is listening. By
      # default, Redis server listen on 6379
      port = 6379

      # Defines the database to use by default. Must be a valid
      # number. Check your Redis configuration to know the hightest
      # value you are able to use
      defaultdb = 0
      
      # Cooldown in seconds before allowing pool re-initialisation
      reinit-pool-cooldown = 5000

      # Pool connections tuning
      conn {
          timeout = 2000
          maxtotal = 64
          maxidle = 16
          minidle = 8
      }
    }



## Usage

#### Example 1

```java
    import play.cache.SyncCacheApi;
    
    public class MyController extends Controller {

        private final SyncCacheApi cacheApi;

        @Inject
        public MyController(final SyncCacheApi cacheApi) {
            this.cacheApi = cacheApi;
        }

        public Result index() {
            final String token = this.cacheApi.getOrElseUpdate("key", () -> "new-token", 60);
            return ok(token);
        }
    }
```

#### Example 2

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


#### Example 3

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


#### Example 4

```java
    final PlayRedis playRedis = Play.application().injector().instanceOf(PlayRedis.class);
    try (final Jedis jedis = playRedis.getConnection()) {
        jedis.set("key", "Hello World!")
        jedis.expire("key", 60);
    }
```



## License
This project is released under terms of the [MIT license](https://raw.githubusercontent.com/thibaultmeyer/play-redis-module/master/LICENSE).
