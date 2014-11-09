/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kuujo.vertigo.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Configuration mapping test.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ConfigsTest {

  @Test
  public void testConfigToJsonObject() {
    Config config = ConfigFactory.parseResourcesAnySyntax("test-config").withFallback(ConfigFactory.empty().withValue("foobarbaz", ConfigValueFactory.fromAnyRef("bazbarfoo"))).resolve();
    Assert.assertEquals("bar", config.getString("foo"));
    Assert.assertEquals(1, config.getInt("bar.baz"));
    Assert.assertEquals("Hello world", config.getString("baz.foo"));
    Assert.assertEquals("bazbarfoo", config.getString("baz.bar"));
    Assert.assertEquals("foo", config.getStringList("baz.baz").get(0));
    Assert.assertEquals("bar", config.getStringList("baz.baz").get(1));
    Assert.assertEquals("baz", config.getStringList("baz.baz").get(2));
    JsonObject jsonConfig = Configs.configObjectToJson(config);
    Assert.assertNotNull(jsonConfig);
    Assert.assertEquals("bar", jsonConfig.getString("foo"));
    Assert.assertEquals(1, (int) jsonConfig.getJsonObject("bar").getInteger("baz"));
    Assert.assertEquals("Hello world", jsonConfig.getJsonObject("baz").getString("foo"));
    Assert.assertEquals("bazbarfoo", jsonConfig.getJsonObject("baz").getString("bar"));
    Assert.assertEquals("foo", jsonConfig.getJsonObject("baz").getJsonArray("baz").getString(0));
    Assert.assertEquals("bar", jsonConfig.getJsonObject("baz").getJsonArray("baz").getString(1));
    Assert.assertEquals("baz", jsonConfig.getJsonObject("baz").getJsonArray("baz").getString(2));
  }

}
