package net.kuujo.vertigo.test.integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.vertx.testtools.ScriptClassRunner;
import org.vertx.testtools.TestVerticleInfo;

@TestVerticleInfo(filenameFilter=".+\\.py", funcRegex="def[\\s]+(test[^\\s(]+)")
@RunWith(ScriptClassRunner.class)
public class PythonIntegrationTests {

  @Test
  public void __vertxDummy() {
  }

}
