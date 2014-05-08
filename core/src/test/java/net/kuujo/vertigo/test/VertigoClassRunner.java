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
package net.kuujo.vertigo.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;
import org.vertx.testtools.JavaClassRunner;
import org.vertx.testtools.TestVerticleInfo;

/**
 * Special class runner for Vertigo tests that ensures the platform is
 * not reconstructed across test cases.
 *
 * @author Jordan Halterman
 */
public class VertigoClassRunner extends BlockJUnit4ClassRunner {

  public static final String TESTRUNNER_HANDLER_ADDRESS = "vertx.testframework.handler";

  private static final Logger log = LoggerFactory.getLogger(JavaClassRunner.class);

  private static PlatformManager mgr;
  protected static final long TIMEOUT;
  private static final long DEFAULT_TIMEOUT = 300;
  static {
    String timeout = System.getProperty("vertx.test.timeout");
    TIMEOUT = timeout == null ? DEFAULT_TIMEOUT : Long.valueOf(timeout);
    System.setProperty("vertx.clusterManagerFactory", "org.vertx.java.spi.cluster.impl.hazelcast.HazelcastClusterManagerFactory");
  }

  protected String main;
  private TestVerticleInfo annotation;

  public VertigoClassRunner(Class<?> klass) throws InitializationError {
    super(klass);
    if (mgr == null) {
      mgr = PlatformLocator.factory.createPlatformManager(0, "localhost");
    }
    setTestProperties();
  }

  private void setTestProperties() {
    // We set the properties here, rather than letting the build script do it
    // This means tests can run directly in an IDE with the correct properties set
    // without having to create custom test configurations
    String modsDir = null;
    File propsFile = new File("vertx.properties");
    if (propsFile.exists()) {
      loadProps(propsFile);
    } else {
      propsFile = new File("gradle.properties");
      if (propsFile.exists()) {
        loadProps(propsFile);
        modsDir = "build/mods";
      } else {
        File pom = new File("pom.xml");
        if (pom.exists()) {
          try (Scanner scanner = new Scanner(pom).useDelimiter("\\A")) {
            String data = scanner.next();
            String modOwner = extractTag(data, "groupId");
            String modName = extractTag(data, "artifactId");
            String version = extractTag(data, "version");
            setModuleNameProp(modOwner, modName, version);
          } catch (FileNotFoundException e) {
            //Ignore
          }
          modsDir = "target/mods";
        }
      }
    }
    if (System.getProperty("vertx.mods") == null && modsDir != null) {
      System.setProperty("vertx.mods", modsDir);
    }
    System.setProperty("vertx.idedirs", "true");
  }

  private void setModuleNameProp(String modOwner, String modName, String version) {
    String moduleName= modOwner + "~" + modName + "~" + version;
    System.setProperty("vertx.modulename", moduleName);
  }

  private String extractTag(String data, String tag) {
    // This will extract the _first_ instance of the tag it finds in the data
    // Need to make sure groupId, artifactId, version are at the _top_ of the file before any
    // other instances of those tags!!
    // Yes, we could use an XML parser but this is a lot simpler.
    int pos = data.indexOf("<" + tag + ">");
    int endPos = data.indexOf("</" + tag + ">");
    String value = data.substring(pos + tag.length() + 2, endPos);
    return value;
  }

  private void loadProps(File propsFile) {
    // We set the properties here, rather than letting the build script do it
    // This means tests can run directly in an IDE with the correct properties set
    // without having to create custom test configurations
    Properties props = new Properties();
    try (InputStream is = new FileInputStream(propsFile.getName())) {
      props.load(is);
      for (String propName: props.stringPropertyNames()) {
        String propVal = props.getProperty(propName);
        System.setProperty("vertx." + propName, propVal);
      }
      String modOwner = props.getProperty("modowner");
      if (modOwner != null) {
        setModuleNameProp(modOwner, props.getProperty("modname"), props.getProperty("version"));
      }
    } catch (IOException e) {
      log.error("Failed to load props file", e);
    }
  }

  protected TestVerticleInfo getAnnotation() {
    if (annotation == null) {
      Class<?> testClass = getTestClass().getJavaClass();
      Annotation[] anns = testClass.getAnnotations();
      for (Annotation aann: anns) {
        if (aann instanceof TestVerticleInfo) {
          TestVerticleInfo tann = (TestVerticleInfo)aann;
          annotation = tann;
        }
      }
    }
    return annotation;
  }


  protected List<FrameworkMethod> computeTestMethods() {
    Class<?> testClass = getTestClass().getJavaClass();
    this.main = testClass.getName();
    List<FrameworkMethod> testMethods =  getTestMethods();
    return testMethods;
  }

  protected List<FrameworkMethod> getTestMethods() {
    return super.computeTestMethods();
  }

  protected URL getClassPath(String methodName) {
    return null;
  }

  protected String getMain(String methodName) {
    return main;
  }

  public String getActualMethodName(String methodName) {
    return methodName;
  }

  @Override
  protected void runChild(FrameworkMethod method, RunNotifier notifier) {
    Class<?> testClass = getTestClass().getJavaClass();
    String methodName = method.getName();
    String testDesc = method.getName();
    Description desc = Description.createTestDescription(testClass, testDesc);
    if (method.getAnnotation(Ignore.class) != null) {
        notifier.fireTestIgnored(desc);
        return;
    }
    notifier.fireTestStarted(desc);
    final AtomicReference<Throwable> failure = new AtomicReference<>();
    try {
      JsonObject conf = new JsonObject().putString("methodName", getActualMethodName(methodName));
      final CountDownLatch testLatch = new CountDownLatch(1);
      Handler<Message<JsonObject>> handler = new Handler<Message<JsonObject>>() {
        @Override
        public void handle(Message<JsonObject> msg) {
          JsonObject jmsg = msg.body();
          String type = jmsg.getString("type");
          try {
            switch (type) {
            case "done":
              break;
            case "failure":
              byte[] bytes = jmsg.getBinary("failure");
              // Deserialize
              ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
              Throwable t = (Throwable)ois.readObject();
              // We display this since otherwise Gradle doesn't display it to stdout/stderr
              t.printStackTrace();
              failure.set(t);
              break;
            }
          } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            failure.set(e);
          }
          finally {
            testLatch.countDown();
          }
        }
      };

      EventBus eb = mgr.vertx().eventBus();
      eb.registerHandler(TESTRUNNER_HANDLER_ADDRESS, handler);
      final CountDownLatch deployLatch = new CountDownLatch(1);
      final AtomicReference<String> deploymentIDRef = new AtomicReference<>();
      String includes;
      TestVerticleInfo annotation = getAnnotation();
      if (annotation != null) {
        includes = getAnnotation().includes().trim();
        if (includes.isEmpty()) {
          includes = null;
        }
      } else {
        includes = null;
      }
      System.out.println("Starting test: " + testDesc);
      String main = getMain(methodName);
      URL cp = getClassPath(methodName);
      List<URL> urls = new ArrayList<>();
      if (cp != null) {
        urls.add(cp);
      }
      ClassLoader pcl = Thread.currentThread().getContextClassLoader();
      /*
      We need to add entries from the platform classloader to the module classloader that's created for the verticle that
      we deploy.
      This is especially important if there are tests which deploy Groovy compiled Verticles that are not in the module
      under test.
      In this case the Groovy compiled verticle classes will be on the platform classloader. When the test verticle
      tries to deploy the Groovy compiled verticle it will be found on the platform classloader not the module classloader
      and that classloader will then try to load the org.vertx.groovy.platform.Verticle class which it won't find since
      this is only available in the Groovy lang module.
      To solve this we must add the non jar classpath entries of the platform classloader to the module classloader of
      the test verticle so that they are loaded from there and it then also tries to load org.vertx.groovy.platform.Verticle
      from there which it will now find since the Groovy lang module is a parent (included) by the test verticle module
      classloader
       */
      if (pcl != null && pcl instanceof URLClassLoader) {
        URLClassLoader upcl = (URLClassLoader)pcl;
        for (URL url: upcl.getURLs()) {
          String surl = url.toString();
          if (!surl.endsWith(".jar") && !surl.endsWith(".zip")) {
            urls.add(url);
          }
        }
      }
      final AtomicReference<Throwable> deployThrowable = new AtomicReference<>();
      mgr.deployVerticle(main, conf, urls.toArray(new URL[urls.size()]), 1, includes, new AsyncResultHandler<String>() {
        public void handle(AsyncResult<String> ar) {
          if (ar.succeeded()) {
            deploymentIDRef.set(ar.result());
          } else {
            deployThrowable.set(ar.cause());
          }
          deployLatch.countDown();
        }
      });
      waitForLatch(deployLatch);
      if (deployThrowable.get() != null) {
        notifier.fireTestFailure(new Failure(desc, deployThrowable.get()));
        notifier.fireTestFinished(desc);
        return;
      }
      waitForLatch(testLatch);
      eb.unregisterHandler(TESTRUNNER_HANDLER_ADDRESS, handler);
      final CountDownLatch undeployLatch = new CountDownLatch(1);
      final AtomicReference<Throwable> undeployThrowable = new AtomicReference<>();
      mgr.undeploy(deploymentIDRef.get(), new AsyncResultHandler<Void>() {
        public void handle(AsyncResult<Void> ar) {
          if (ar.failed()) {
            undeployThrowable.set(ar.cause());
          }
          undeployLatch.countDown();
        }
      });
      waitForLatch(undeployLatch);
      if (undeployThrowable.get() != null) {
        notifier.fireTestFailure(new Failure(desc, undeployThrowable.get()));
        notifier.fireTestFinished(desc);
        return;
      }
      if (failure.get() != null) {
        notifier.fireTestFailure(new Failure(desc, failure.get()));
      }
      notifier.fireTestFinished(desc);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void waitForLatch(CountDownLatch latch) {
    while (true) {
      try {
        if (!latch.await(TIMEOUT, TimeUnit.SECONDS)) {
          throw new AssertionError("Timed out waiting for test to complete");
        }
        break;
      } catch (InterruptedException e) {
        // Ignore
      }
    }
  }

}
