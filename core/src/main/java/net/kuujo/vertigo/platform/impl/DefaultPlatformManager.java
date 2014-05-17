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
package net.kuujo.vertigo.platform.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import net.kuujo.vertigo.platform.ModuleFields;
import net.kuujo.vertigo.platform.ModuleIdentifier;
import net.kuujo.vertigo.platform.ModuleInfo;
import net.kuujo.vertigo.platform.PlatformManager;
import net.kuujo.vertigo.platform.PlatformManagerException;
import net.kuujo.vertigo.util.Task;
import net.kuujo.vertigo.util.TaskRunner;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.DecodeException;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;

/**
 * Default platform manager implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultPlatformManager implements PlatformManager {
  private static final String MODS_DIR_PROP_NAME = "vertx.mods";
  private static final String LOCAL_MODS_DIR = "mods";
  private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
  private static final String FILE_SEPARATOR = System.getProperty("file.separator");
  private static final String MOD_JSON_FILE = "mod.json";
  private static final int BUFFER_SIZE = 4096;
  private final Vertx vertx;
  private final Container container;
  private final String helper = UUID.randomUUID().toString();
  private final TaskRunner tasks = new TaskRunner();

  public DefaultPlatformManager(Vertx vertx, Container container) {
    this.vertx = vertx;
    this.container = container;
    init();
  }

  /**
   * Starts a locally accessible worker verticle which operates on the file system.
   */
  private void init() {
    tasks.runTask(new Handler<Task>() {
      @Override
      public void handle(final Task task) {
        container.deployWorkerVerticle(PlatformHelper.class.getName(), new JsonObject().putString("address", helper), 1, false, new Handler<AsyncResult<String>>() {
          @Override
          public void handle(AsyncResult<String> result) {
            task.complete();
          }
        });
      }
    });
  }

  @Override
  public PlatformManager getModuleInfo(final Handler<AsyncResult<Collection<ModuleInfo>>> resultHandler) {
    tasks.runTask(new Handler<Task>() {
      @Override
      public void handle(final Task task) {
        vertx.eventBus().send(helper, new JsonObject().putString("action", "info"), new Handler<Message<JsonObject>>() {
          @Override
          public void handle(Message<JsonObject> reply) {
            if (reply.body().getString("status").equals("error")) {
              new DefaultFutureResult<Collection<ModuleInfo>>(new PlatformManagerException(reply.body().getString("message"))).setHandler(resultHandler);
            } else if (reply.body().getString("status").equals("ok")) {
              JsonObject jsonInfos = reply.body().getObject("result");
              List<ModuleInfo> info = new ArrayList<>();
              for (String modID : jsonInfos.getFieldNames()) {
                info.add(new ModuleInfo(new ModuleIdentifier(modID), new ModuleFields(jsonInfos.getObject(modID))));
              }
              new DefaultFutureResult<Collection<ModuleInfo>>(info).setHandler(resultHandler);
            }
            task.complete();
          }
        });
      }
    });
    return this;
  }

  @Override
  public PlatformManager getModuleInfo(final String moduleName, final Handler<AsyncResult<ModuleInfo>> resultHandler) {
    tasks.runTask(new Handler<Task>() {
      @Override
      public void handle(final Task task) {
        vertx.eventBus().send(helper, new JsonObject().putString("action", "info").putString("module", moduleName), new Handler<Message<JsonObject>>() {
          @Override
          public void handle(Message<JsonObject> reply) {
            if (reply.body().getString("status").equals("error")) {
              new DefaultFutureResult<ModuleInfo>(new PlatformManagerException(reply.body().getString("message"))).setHandler(resultHandler);
            } else if (reply.body().getString("status").equals("ok")) {
              new DefaultFutureResult<ModuleInfo>(new ModuleInfo(new ModuleIdentifier(moduleName), new ModuleFields(reply.body().getObject("result")))).setHandler(resultHandler);
            }
            task.complete();
          }
        });
      }
    });
    return this;
  }

  @Override
  public PlatformManager zipModule(final String moduleName, final Handler<AsyncResult<String>> doneHandler) {
    tasks.runTask(new Handler<Task>() {
      @Override
      public void handle(final Task task) {
        vertx.eventBus().send(helper, new JsonObject().putString("action", "zip").putString("module", moduleName), new Handler<Message<JsonObject>>() {
          @Override
          public void handle(Message<JsonObject> reply) {
            if (reply.body().getString("status").equals("error")) {
              new DefaultFutureResult<String>(new PlatformManagerException(reply.body().getString("message"))).setHandler(doneHandler);
            } else if (reply.body().getString("status").equals("ok")) {
              new DefaultFutureResult<String>(reply.body().getString("result")).setHandler(doneHandler);
            }
            task.complete();
          }
        });
      }
    });
    return this;
  }

  @Override
  public PlatformManager installModule(final String zipFile, final Handler<AsyncResult<Void>> doneHandler) {
    tasks.runTask(new Handler<Task>() {
      @Override
      public void handle(final Task task) {
        vertx.eventBus().send(helper, new JsonObject().putString("action", "install").putString("file", zipFile), new Handler<Message<JsonObject>>() {
          @Override
          public void handle(Message<JsonObject> reply) {
            if (reply.body().getString("status").equals("error")) {
              new DefaultFutureResult<Void>(new PlatformManagerException(reply.body().getString("message"))).setHandler(doneHandler);
            } else if (reply.body().getString("status").equals("ok")) {
              new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
            }
            task.complete();
          }
        });
      }
    });
    return this;
  }

  @Override
  public PlatformManager uninstallModule(final String moduleName, final Handler<AsyncResult<Void>> doneHandler) {
    tasks.runTask(new Handler<Task>() {
      @Override
      public void handle(final Task task) {
        vertx.eventBus().send(helper, new JsonObject().putString("action", "uninstall").putString("module", moduleName), new Handler<Message<JsonObject>>() {
          @Override
          public void handle(Message<JsonObject> reply) {
            if (reply.body().getString("status").equals("error")) {
              new DefaultFutureResult<Void>(new PlatformManagerException(reply.body().getString("message"))).setHandler(doneHandler);
            } else if (reply.body().getString("status").equals("ok")) {
              new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
            }
            task.complete();
          }
        });
      }
    });
    return this;
  }

  @Override
  public PlatformManager deployModule(String moduleName, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler) {
    container.deployModule(moduleName, config, instances, doneHandler);
    return this;
  }

  @Override
  public PlatformManager deployVerticle(String main, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler) {
    container.deployVerticle(main, config, instances, doneHandler);
    return this;
  }

  @Override
  public PlatformManager deployWorkerVerticle(String main, JsonObject config, int instances, boolean multiThreaded, Handler<AsyncResult<String>> doneHandler) {
    container.deployWorkerVerticle(main, config, instances, multiThreaded, doneHandler);
    return this;
  }

  @Override
  public PlatformManager undeployModule(String deploymentID, Handler<AsyncResult<Void>> doneHandler) {
    container.undeployModule(deploymentID, doneHandler);
    return this;
  }

  @Override
  public PlatformManager undeployVerticle(String deploymentID, Handler<AsyncResult<Void>> doneHandler) {
    container.undeployVerticle(deploymentID, doneHandler);
    return this;
  }

  /**
   * Helper worker verticle for accessing the file system.
   */
  public static class PlatformHelper extends Verticle {
    private File modRoot;

    private final Handler<Message<JsonObject>> messageHandler = new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        String action = message.body().getString("action");
        if (action != null) {
          switch (action) {
            case "info":
              doModuleInfo(message);
              break;
            case "zip":
              doModuleZip(message);
              break;
            case "install":
              doModuleInstall(message);
              break;
            case "uninstall":
              doModuleUninstall(message);
              break;
            default:
              message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid action " + action));
              break;
          }
        }
      }
    };

    @Override
    public void start(final Future<Void> startResult) {
      String modDir = System.getProperty(MODS_DIR_PROP_NAME);
      if (modDir != null && !modDir.trim().equals("")) {
        modRoot = new File(modDir);
      } else {
        modRoot =  new File(LOCAL_MODS_DIR);
      }
      vertx.eventBus().registerLocalHandler(container.config().getString("address"), messageHandler);
      super.start(startResult);
    }

    /**
     * Looks up the mod.json configuration for a module.
     */
    private void doModuleInfo(final Message<JsonObject> message) {
      String moduleName = message.body().getString("module");
      if (moduleName != null) {
        ModuleIdentifier modID = new ModuleIdentifier(moduleName);
        File modDir = locateModule(modID);
        if (modDir != null) {
          File modJson = new File(modDir, MOD_JSON_FILE);
          try {
            JsonObject config = loadModuleConfig(modID, modJson);
            message.reply(new JsonObject().putString("status", "ok").putObject("result", config));
          } catch (Exception e) {
            message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
          }
        } else {
          message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid module."));
        }
      } else {
        List<File> modDirs = locateModules();
        JsonObject modInfo = new JsonObject();
        for (File modDir : modDirs) {
          ModuleIdentifier modID = new ModuleIdentifier(modDir.getName());
          File modJson = new File(modDir, MOD_JSON_FILE);
          try {
            JsonObject config = loadModuleConfig(modID, modJson);
            modInfo.putObject(modID.toString(), config);
          } catch (Exception e) {
            continue;
          }
        }
        message.reply(new JsonObject().putString("status", "ok").putObject("result", modInfo));
      }
    }

    /**
     * Creates a zip for a module.
     */
    private void doModuleZip(final Message<JsonObject> message) {
      String module = message.body().getString("module");
      try {
        File file = zipModule(new ModuleIdentifier(module));
        message.reply(new JsonObject().putString("status", "ok").putString("result", file.getAbsolutePath()));
      } catch (Exception e) {
        message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
      }
    }

    /**
     * Installs a module to the local repository from a zip.
     */
    private void doModuleInstall(final Message<JsonObject> message) {
      String zipFileName = message.body().getString("file");
      File zipFile = new File(zipFileName);
      if (!zipFile.exists()) {
        message.reply(new JsonObject().putString("status", "error").putString("message", "Zip file does not exist."));
      } else {
        try {
          installModule(zipFile);
          message.reply(new JsonObject().putString("status", "ok"));
        } catch (Exception e) {
          message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
        }
      }
    }

    /**
     * Uninstalls a module from the local repository.
     */
    private void doModuleUninstall(final Message<JsonObject> message) {
      String moduleName = message.body().getString("module");
      try {
        uninstallModule(new ModuleIdentifier(moduleName));
        message.reply(new JsonObject().putString("status", "ok"));
      } catch (Exception e) {
        message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
      }
    }

    /**
     * Locates all modules in the local repository.
     */
    private List<File> locateModules() {
      File[] files = modRoot.listFiles();
      List<File> modFiles = new ArrayList<>();
      for (File file : files) {
        if (file.isDirectory()) {
          // Check to determine whether the directory is a valid module directory.
          boolean isValid = true;
          try {
            new ModuleIdentifier(file.getName());
          } catch (Exception e) {
            isValid = false;
          }

          // If the directory is a valid module name then check for a mod.json file.
          if (isValid) {
            File modJson = new File(file, MOD_JSON_FILE);
            if (modJson.exists()) {
              modFiles.add(file);
            }
          }
        }
      }
      return modFiles;
    }

    /**
     * Locates a module in the modules root directory.
     */
    private File locateModule(ModuleIdentifier modID) {
      File modDir = new File(modRoot, modID.toString());
      if (modDir.exists()) {
        return modDir;
      }
      return null;
    }

    /**
     * Loads configuration information for a module from mod.json.
     */
    private ModuleInfo loadModuleInfo(ModuleIdentifier modID, File modJsonFile) {
      return new ModuleInfo(modID, new ModuleFields(loadModuleConfig(modID, modJsonFile)));
    }

    /**
     * Loads configuration information for a module from mod.json.
     */
    private JsonObject loadModuleConfig(ModuleIdentifier modID, File modJsonFile) {
      try (@SuppressWarnings("resource") Scanner scanner = new Scanner(modJsonFile, "UTF-8").useDelimiter("\\A")) {
        return new JsonObject(scanner.next());
      } catch (FileNotFoundException e) {
        throw new PlatformManagerException("Module " + modID + " does not contains a mod.json file");
      } catch (NoSuchElementException e) {
        throw new PlatformManagerException("Module " + modID + " contains an empty mod.json");
      } catch (DecodeException e) {
        throw new PlatformManagerException("Module " + modID + " mod.json contains invalid json");
      }
    }

    /**
     * Pulls in all dependencies for a module.
     */
    private void pullInDependencies(ModuleIdentifier modID, File modDir) {
      // Load the module configuration file.
      File modJsonFile = new File(modDir, MOD_JSON_FILE);
      ModuleInfo info = loadModuleInfo(modID, modJsonFile);

      // Pull in all dependencies according to the "includes" and "deploys" fields.
      ModuleFields fields = info.fields();
      List<String> mods = new ArrayList<>();

      // Add "includes" modules.
      String sincludes = fields.getIncludes();
      if (sincludes != null) {
        String[] includes = parseIncludes(sincludes);
        if (includes != null) {
          mods.addAll(Arrays.asList(includes));
        }
      }

      // Add "deploys" modules.
      String sdeploys = fields.getDeploys();
      if (sdeploys != null) {
        String[] deploys = parseIncludes(sdeploys);
        if (deploys != null) {
          mods.addAll(Arrays.asList(deploys));
        }
      }

      // Iterate through "includes" and "deploys" and attempt to move them
      // into the module directory if they can be found. Note that this
      // requires that the modules be installed. It does not check remote
      // repositories since it's assumed that remote repositories can be
      // access from any node to which the module goes.
      if (!mods.isEmpty()) {
        File internalModsDir = new File(modDir, "mods");
        if (!internalModsDir.exists()) {
          if (!internalModsDir.mkdir()) {
            throw new PlatformManagerException("Failed to create directory " + internalModsDir);
          }
        }

        for (String moduleName : mods) {
          File internalModDir = new File(internalModsDir, moduleName);
          if (!internalModDir.exists()) {
            ModuleIdentifier childModID = new ModuleIdentifier(moduleName);
            File includeModDir = locateModule(childModID);
            if (includeModDir != null) {
              vertx.fileSystem().copySync(includeModDir.getAbsolutePath(), internalModDir.getAbsolutePath(), true);
              pullInDependencies(childModID, internalModDir);
            }
          }
        }
      }
    }

    /**
     * Creates a zip file from a module.
     */
    private File zipModule(ModuleIdentifier modID) {
      File modDir = new File(modRoot, modID.toString());
      if (!modDir.exists()) {
        throw new PlatformManagerException("Cannot find module");
      }

      // Create a temporary directory in which to store the module and its dependencies.
      File modRoot = new File(TEMP_DIR, "vertx-zip-mods");

      // Create a zip file. If the zip file already exists in the temporary
      // Vertigo zips directory then just return the existing zip file.
      File zipFile = new File(modRoot, modID.toString() + ".zip");
      if (zipFile.exists()) {
        return zipFile;
      }

      // Create a temporary directory to which we'll copy the module and its dependencies.
      File modDest = new File(modRoot, modID.toString() + "-" + UUID.randomUUID().toString());
      File modHome = new File(modDest, modID.toString());

      // Create the temporary destination directory.
      vertx.fileSystem().mkdirSync(modHome.getAbsolutePath(), true);

      // Copy the module into the temporary directory.
      vertx.fileSystem().copySync(modDir.getAbsolutePath(), modHome.getAbsolutePath(), true);

      // Pull any module dependencies ("includes" and "deploys") into the temporary directory.
      pullInDependencies(modID, modHome);

      // Zip up the temporary directory into the zip file.
      zipDirectory(zipFile.getPath(), modDest.getAbsolutePath());

      // Delete the temporary directory.
      vertx.fileSystem().deleteSync(modDest.getAbsolutePath(), true);
      return zipFile;
    }

    /**
     * Zips up a directory.
     */
    private void zipDirectory(String zipFile, String dirToZip) {
      File directory = new File(dirToZip);
      try (ZipOutputStream stream = new ZipOutputStream(new FileOutputStream(zipFile))) {
        addDirectoryToZip(directory, directory, stream);
      } catch (Exception e) {
        throw new PlatformManagerException("Failed to zip module", e);
      }
    }

    /**
     * Recursively adds directories to a zip file.
     */
    private void addDirectoryToZip(File topDirectory, File directory, ZipOutputStream out) throws IOException {
      Path top = Paths.get(topDirectory.getAbsolutePath());

      File[] files = directory.listFiles();
      byte[] buffer = new byte[BUFFER_SIZE];

      for (int i = 0; i < files.length; i++) {
        Path entry = Paths.get(files[i].getAbsolutePath());
        Path relative = top.relativize(entry);
        String entryName = relative.toString();
        if (files[i].isDirectory()) {
          entryName += FILE_SEPARATOR;
        }

        out.putNextEntry(new ZipEntry(entryName.replace("\\", "/")));

        if (!files[i].isDirectory()) {
          try (FileInputStream in = new FileInputStream(files[i])) {
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
              out.write(buffer, 0, bytesRead);
            }
          }
          out.closeEntry();
        }

        if (files[i].isDirectory()) {
          addDirectoryToZip(topDirectory, files[i], out);
        }
      }
    }

    /**
     * Parses an includes string.
     */
    private String[] parseIncludes(String sincludes) {
      sincludes = sincludes.trim();
      if ("".equals(sincludes)) {
        return null;
      }
      String[] arr = sincludes.split(",");
      if (arr != null) {
        for (int i = 0; i < arr.length; i++) {
          arr[i] = arr[i].trim();
        }
      }
      return arr;
    }

    /**
     * Installs a module.
     */
    private void installModule(File zipFile) {
      modRoot.mkdirs();
      unzipModuleData(modRoot, zipFile, false);
    }

    /**
     * Unzips a module.
     */
    private void unzipModuleData(File directory, File zipFile, boolean deleteZip) {
      try (InputStream in = new BufferedInputStream(new FileInputStream(zipFile)); ZipInputStream zin = new ZipInputStream(new BufferedInputStream(in))) {
        ZipEntry entry;
        while ((entry = zin.getNextEntry()) != null) {
          String entryName = entry.getName();
          if (!entryName.isEmpty()) {
            if (entry.isDirectory()) {
              if (!new File(directory, entryName).mkdir()) {
                throw new PlatformManagerException("Failed to create directory");
              }
            } else {
              int count;
              byte[] buffer = new byte[BUFFER_SIZE];
              BufferedOutputStream out = null;
              try {
                OutputStream os = new FileOutputStream(new File(directory, entryName));
                out = new BufferedOutputStream(os, BUFFER_SIZE);
                while ((count = zin.read(buffer, 0, BUFFER_SIZE)) != -1) {
                  out.write(buffer, 0, count);
                }
                out.flush();
              } finally {
                if (out != null) {
                  out.close();
                }
              }
            }
          }
        }
      } catch (Exception e) {e.printStackTrace();
        throw new PlatformManagerException("Failed to unzip module", e);
      } finally {
        if (deleteZip) {
          zipFile.delete();
        }
      }
    }

    /**
     * Uninstalls a module.
     */
    private void uninstallModule(ModuleIdentifier modID) {
      File modDir = new File(modRoot, modID.toString());
      if (modDir.exists()) {
        modDir.delete();
      }
    }
  }

}
