package com.zero_x_baadf00d.play.module.redis;

import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;

/**
 * Play Framework module entry point. Reference this class in
 * your {@code application.conf} file to enable Redis module.
 *
 * @author Thibault Meyer
 * @version 16.03.09
 * @see Module
 * @since 16.03.09
 */
public class RedisModuleBinder extends Module {

    @Override
    public Seq<Binding<?>> bindings(final Environment environment, final Configuration configuration) {
        return seq(bind(RedisModule.class).to(RedisModuleImpl.class));
    }
}
