/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

if (typeof __vertxload === 'string') {
  throw "Use require() to load Vert.x API modules";
}


var Buffer = require('vertx/buffer');
var jfs = __jvertx.fileSystem();

/**
 * File system operations are handled asynchronously. The result of the operations
 * are given to a <code>ResultHandler</code>. The <code>ResultHandler</code> is 
 * just a {@linkcode Handler} that accepts two parameters: 1) an exception object
 * if an error occurred; and 2) the result of the operation, the type of which is
 * determined by the firing event.
 *
 * @typedef {function} ResultHandler
 * @param {Exception} cause This will be <code>null</code> if the operation succeeded
 * @param {{}} result The result of the operation event this handler cares about
 */

/**
 * @exports vertx/file_system
 */
var fileSystem = {};

var streams     = require('vertx/streams');
var helpers     = require('vertx/helpers.js');
var wrapHandler = helpers.adaptAsyncResultHandler;

function propsHandler(handler) {
  return wrapHandler(handler, function(result) {
    return convertProps(result);
  });
}

function asyncFileHandler(handler) {
  return wrapHandler(handler, function(result) {
    return new fileSystem.AsyncFile(result);
  });
}

function arrayResultHandler(handler) {
  return wrapHandler(handler, function(result) {
    var arry = [];
    for (var i = 0; i < result.length; i++) {
      arry.push(result[i]);
    }
    return arry;
  });
}

function convertProps(j_props) {
  /** 
   * @typedef {{}} FileProps 
   * @property {date} creationTime The date the file was created
   * @property {date} lastAccessTime The date the file was last accessed
   * @property {date} lastModifiedTime The date the file was last modified
   * @property {boolean} isDirectory is the file a directory?
   * @property {boolean} isOther Is the file some other type? I.e. not a directory, regular file or symbolic link
   * @property {boolean} isRegularFile is the file a regular file?
   * @property {boolean} isSymbolicLink is the file a symlink?
   * @property {number} size the size of the file in bytes
   * */
  return {
    creationTime: j_props.creationTime().getTime(),
    lastAccessTime: j_props.lastAccessTime().getTime(),
    lastModifiedTime: j_props.lastModifiedTime().getTime(),
    isDirectory: j_props.isDirectory(),
    isOther: j_props.isOther(),
    isRegularFile: j_props.isRegularFile(),
    isSymbolicLink: j_props.isSymbolicLink(),
    size: j_props.size()
  };
}

/**
 * Copy a file, asynchronously. The copy will fail if <code>from</code> 
 * does not exist, or if <code>to</code> already exists.
 *
 * @param {string} from path of file to copy
 * @param {string} to path of file to copy to
 * @param {boolean} [recursive] copy recursively (default is false)
 * @param {ResultHandler} handler the handler which is called on completion.
 * @returns {module:vertx/file_system}
 */
fileSystem.copy = function(from, to, arg2, arg3) {
  var handler;
  var recursive;
  if (arguments.length === 4) {
    handler = arg3;
    recursive = arg2;
  } else {
    handler = arg2;
    recursive = false;
  }
  jfs.copy(from, to, recursive, wrapHandler(handler));
  return fileSystem;
};

/**
 * Synchronous version of copy
 *
 * @param {string} from the path of the file to copy
 * @param {string} to the path to copy the file to
 * @param {boolean} recursive copy recursively (default is false)
 * @returns {module:vertx/file_system}
 */
fileSystem.copySync = function(from, to, recursive) {
  if (!recursive) recursive = false;
  jfs.copySync(from, to, recursive);
  return fileSystem;
};

/**
 * Move a file, asynchronously. The move will fail if <code>from</code> 
 * does not exist, or if <code>to</code> already exists.
 *
 * @param {string} from the path of file to move
 * @param {string} to the path to move the file to
 * @param {ResultHandler} handler the function to call when complete
 * @returns {module:vertx/file_system}
 */
fileSystem.move = function(from, to, handler) {
  jfs.move(from, to, wrapHandler(handler));
  return fileSystem;
};

/**
 * Synchronous version of move.
 *
 * @param {string} from the path of file to move
 * @param {string} to the path to move the file to
 * @returns {module:vertx/file_system}
 */
fileSystem.moveSync = function(from, to) {
  jfs.moveSync(from, to);
  return fileSystem;
};

/**
 * Truncate a file, asynchronously. The move will fail if path does not exist.
 *
 * @param {string} path Path of file to truncate
 * @param {number} len Length to truncate file to. Will fail if len < 0. If len > file size then will do nothing.
 * @param {ResultHandler} handler the function to call when complete
 * @returns {module:vertx/file_system}
 */
fileSystem.truncate = function(path, len, handler) {
  jfs.truncate(path, len, wrapHandler(handler));
  return fileSystem;
};

/**
 * Synchronous version of truncate.
 *
 * @param {string} path Path of file to truncate
 * @param {number} len Length to truncate file to. Will fail if len < 0. If len > file size then will do nothing.
 * @returns {module:vertx/file_system}
 */
fileSystem.truncateSync = function(path, len) {
  jfs.truncateSync(path, len);
  return fileSystem;
};

/**
 * Change the permissions on a file, asynchronously. If the file is directory
 * then all contents will also have their permissions changed recursively.
 *
 * @param {string} path path of file to change permissions
 * @param {string} perms a permission string of the form rwxr-x--- as specified
 * in http://download.oracle.com/javase/7/docs/api/java/nio/file/attribute/PosixFilePermissions.html.
 * This is used to set the permissions for any regular files (not directories).
 * @param {string} [dir_perms] similar to <code>perms</code> above, but refers only to directories.
 * @param {ResultHandler} handler The handler to call when the operation has completed
 *
 * @returns {module:vertx/file_system}
 */
fileSystem.chmod = function(path, perms, arg2, arg3) {
  var handler;
  var dirPerms;
  if (arguments.length === 4) {
    handler = arg3;
    dirPerms = arg2;
  } else {
    handler = arg2;
    dirPerms = null;
  }
  jfs.chmod(path, perms, dirPerms, wrapHandler(handler));
  return fileSystem;
};

/**
 * Synchronous version of chmod.
 *
 * @param {string} path path of file to change permissions
 * @param {string} perms a permission string of the form rwxr-x--- as specified
 * in http://download.oracle.com/javase/7/docs/api/java/nio/file/attribute/PosixFilePermissions.html.
 * This is used to set the permissions for any regular files (not directories).
 * @param {string} [dir_perms] similar to <code>perms</code> above, but refers only to directories.
 * @returns {module:vertx/file_system}
 */
fileSystem.chmodSync = function(path, perms, dirPerms) {
  if (!dirPerms) dirPerms = null;
  jfs.chmodSync(path, perms, dirPerms);
  return fileSystem;
};

/**
 * Change the ownership on the file represented by {@code path} to {@code user} and {code group}, asynchronously
 *
 * @param {string} path path of file to change permissions
 * @param {string} user the username
 * @param {string} [group] the groupname
 * @param {ResultHandler} handler The handler to call when the operation has completed
 *
 * @returns {module:vertx/file_system}
 */
fileSystem.chown = function(path, user, arg1, arg2) {
  var handler;
  var group;
  if (arguments.length === 4) {
    handler = arg2;
    group = arg1;
  } else {
    handler = arg1;
    group = null;
  }
  jfs.chown(path, user, group, wrapHandler(handler));
  return fileSystem;
};

/**
 * Synchronous version of chown
 *
 * @param {string} path path of file to change permissions
 * @param {string} user the username
 * @param {string} [group] the groupname
 *
 * @returns {module:vertx/file_system}
 */
fileSystem.chownSync = function(path, user, group) {
  if (!group) group = null;
  jfs.chownSync(path, user, group);
  return fileSystem;
};

/**
 * Get file properties for a file, asynchronously.
 *
 * @param {string} path path to file
 * @param {ResultHandler} handler the function to call when complete
 * @returns {module:vertx/file_system}
 */
fileSystem.props = function(path, handler) {
  jfs.props(path, propsHandler(handler));
  return fileSystem;
};

/**
 * Synchronous version of props.
 * @param {string} path path to file
 * @returns {FileProps}
 */
fileSystem.propsSync = function(path) {
  var j_props = jfs.propsSync(path);
  return convertProps(j_props);
};

/**
 * Obtain properties for the link represented by <code>path</code>, asynchronously.
 * The link will not be followed.
 *
 * @param {string} path path to file
 * @param {ResultHandler} handler the function to call when complete
 * @returns {module:vertx/file_system}
 */
fileSystem.lprops = function(path, handler) {
  jfs.lprops(path, propsHandler(handler));
  return fileSystem;
};

/**
 * Synchronous version of lprops.
 *
 * @param {string} path path to file
 * @returns {FileProps}
 */
fileSystem.lpropsSync = function(path) {
  var j_props = jfs.lpropsSync(path);
  return convertProps(j_props);
};

/**
 * Create a hard link, asynchronously.
 *
 * @param {string} link path of the link to create.
 * @param {string} existing path of where the link points to.
 * @param {ResultHandler} handler the function to call when complete
 * @returns {module:vertx/file_system}
 */
fileSystem.link = function(link, existing, handler) {
  jfs.link(link, existing, wrapHandler(handler));
  return fileSystem;
};

/**
 * Synchronous version of link.
 * @param {string} link path of the link to create.
 * @param {string} existing path of where the link points to.
 * @returns {module:vertx/file_system}
 */
fileSystem.linkSync = function(link, existing) {
  jfs.linkSync(link, existing);
  return fileSystem;
};

/**
 * Create a symbolic link, asynchronously.
 *
 * @param {string} link Path of the link to create.
 * @param {string} existing Path of where the link points to.
 * @param {ResultHandler} handler the function to call when complete
 * @returns {module:vertx/file_system}
 */
fileSystem.symlink = function(link, existing, handler) {
  jfs.symlink(link, existing, wrapHandler(handler));
  return fileSystem;
};

/**
 * Synchronous version of symlink.
 * @param {string} link Path of the link to create.
 * @param {string} existing Path of where the link points to.
 * @returns {module:vertx/file_system}
 */
fileSystem.symlinkSync = function(link, existing) {
  jfs.symlinkSync(link, existing);
  return fileSystem;
};

/**
 * Unlink a hard link.
 *
 * @param {string} link path of the link to unlink.
 * @param {ResultHandler} handler the handler to notify on completion.
 * @returns {module:vertx/file_system}
 */
fileSystem.unlink = function(link, handler) {
  jfs.unlink(link, wrapHandler(handler));
  return fileSystem;
};

/**
 * Synchronous version of unlink.
 *
 * @param {string} link path of the link to unlink.
 * @returns {module:vertx/file_system}
 */
fileSystem.unlinkSync = function(link) {
  jfs.unlinkSync(link);
  return fileSystem;
};

/**
 * Read a symbolic link, asynchronously. I.e. tells you where the symbolic link points.
 *
 * @param {string} link path of the link to read.
 * @param {ResultHandler} handler the function to call when complete, the function will be 
 *        called with a string representing the path of the file that the <code>link</code>
 *        symlink is linked to.
 * @returns {module:vertx/file_system}
 */
fileSystem.readSymlink = function(link, handler) {
  jfs.readSymlink(link, wrapHandler(handler));
  return fileSystem;
};

/**
 * Synchronous version of readSymlink.
 *
 * @param {string} link path of the link to read.
 * @returns {string} the path of the file that the <code>link</code> symlink is linked to.
 */
fileSystem.readSymlinkSync = function(link) {
  return jfs.readSymlinkSync(link);
};

/**
 * Delete a file on the file system, asynchronously.
 * The delete will fail if the file does not exist, or is a directory and is not empty.
 *
 * @param {string} path path of the file to delete.
 * @param {boolean} [recursive] recurse into subdirectories (default: false)
 * @param {ResultHandler} handler The handler to call when complete
 * @returns {module:vertx/file_system}
 */
fileSystem.delete = function(path, arg1, arg2) {
  var handler;
  var recursive;
  if (arguments.length === 3) {
    handler = arg2;
    recursive = arg1;
  } else {
    handler = arg1;
    recursive = false;
  }
  jfs.delete(path, recursive, wrapHandler(handler));
  return fileSystem;
};

/**
 * Synchronous version of delete.
 *
 * @param {string} path path of the file to delete.
 * @param {boolean} [recursive] recurse into subdirectories (default: false)
 * @returns {module:vertx/file_system}
 */
fileSystem.deleteSync = function(path, recursive) {
  if (!recursive) recursive = false;
  jfs.deleteSync(path, recursive);
  return fileSystem;
};

/**
 * Create a directory, asynchronously.  The create will fail if the directory
 * already exists, or if it contains parent directories which do not already
 * exist.
 *
 * @param {string} path path of the directory to create.
 * @param {boolean} [createParents] create parent directories if necesssary (default is false)
 * @param {string} [permString] the permissions of the directory being created
 * @param {ResultHandler} handler the function to call when complete
 * @returns {module:vertx/file_system}
 */
fileSystem.mkDir = function(path, arg1, arg2, arg3) {
  var createParents;
  var perms;
  var handler;
  switch (arguments.length) {
    case 2:
      createParents = false;
      perms = null;
      handler = arg1;
      break;
    case 3:
      if (typeof(arg1) === 'boolean') {
        createParents = arg1;
        perms=null;
      } else {
        createParents = false;
        perms = arg1;
      }
      handler = arg2;
      break;
    case 4:
      createParents = arg1;
      perms = arg2;
      handler = arg3;
      break;
    default:
      throw 'Invalid number of arguments';
  }
  jfs.mkdir(path, perms, createParents, wrapHandler(handler));
  return fileSystem;
};

/**
 * Synchronous version of mkdir.
 *
 * @param {string} path path of the directory to create.
 * @param {boolean} [createParents] create parent directories if necesssary (default is false)
 * @param {string} [permString] the permissions of the directory being created
 * @returns {module:vertx/file_system}
 */
fileSystem.mkDirSync = function(path, arg1, arg2) {
  var createParents;
  var perms;
  switch (arguments.length) {
    case 1:
      createParents = false;
      perms = null;
      break;
    case 2:
      createParents = arg1;
      perms = null;
      break;
    case 3:
      createParents = arg1;
      perms = arg2;
      break;
    default:
      throw 'Invalid number of arguments';
  }
  jfs.mkdirSync(path, perms, createParents);
  return fileSystem;
};

/**
 * Read a directory, i.e. list it's contents, asynchronously.
 * The read will fail if the directory does not exist.
 *
 * @param {string} path path of the directory to read.
 * @param {string} [filter] a regular expression. If supplied, only paths that match
 *        <code>filter</code> will be passed to the <code>handler</code>.
 * @param {ResultHandler} handler the handler to call when complete. The handler will be
 *        passed an array of strings each representing a matched path name.
 * @returns {module:vertx/file_system}
 */
fileSystem.readDir = function(path, arg1, arg2) {
  var filter;
  var handler;
  if (arguments.length === 3) {
    handler = arg2;
    filter = arg1;
  } else {
    handler = arg1;
    filter = null;
  }
  jfs.readDir(path, filter, arrayResultHandler(handler));
  return fileSystem;
};

/**
 * Synchronous version of readDir.
 *
 * @param {string} path path of the directory to read.
 * @param {string} [filter] a regular expression. If supplied, only paths that match
 *        <code>filter</code> will be passed to the <code>handler</code>.
 * @returns {Array} an array of strings, each representing a matched path name.
 */
fileSystem.readDirSync = function(path, filter) {
  if (!filter) filter = null;
  var result = jfs.readDirSync(path, filter);
  var arry = [];
  for (var i=0; i<result.length; i++) {
    arry.push(result[i]);
  }
  return arry;
};

/**
 * Read the contents of the entire file.
 *
 * @param {string} path path of the file to read.
 * @param {ResultHandler} handler the function to call when complete. The handler function
 *        will receive a Buffer containing the contents of the file.
 * @returns {module:vertx/file_system}
 */
fileSystem.readFile = function(path, handler) {
  jfs.readFile(path, wrapHandler(handler));
  return fileSystem;
};

/**
 * Synchronous version of readFile.
 *
 * @param {string} path path of the file to read.
 * @returns {Buffer} a Buffer containing the contents of the file.
 */
fileSystem.readFileSync = function(path) {
  return jfs.readFileSync(path);
};

/**
 * Write data to a file
 *
 * @param {string} path path of the file to write.
 * @param {string|Buffer} data the data to write
 * @param {ResultHandler} handler the function to call when complete
 * @returns {module:vertx/file_system}
 */
fileSystem.writeFile = function(path, data, handler) {
  if (typeof data === 'string') {
    data = new org.vertx.java.core.buffer.Buffer(data);
  } else {
    data = data._to_java_buffer();
  }
  jfs.writeFile(path, data, wrapHandler(handler));
  return fileSystem;
};

/**
 * Synchronous version of writeFile.
 *
 * @param {string} path path of the file to write.
 * @param {string|Buffer} data the data to write
 * @returns {module:vertx/file_system}
 */
fileSystem.writeFileSync = function(path, data) {
  if (typeof data === 'string') {
    data = new org.vertx.java.core.buffer.Buffer(data);
  }
  jfs.writeFileSync(path, data);
  return fileSystem;
};

/** 
 * Flags used when opening files working with the vert.x 
 * {@linkcode module:vertx/file_system|FileSystem}
 * @typedef {number} Flag 
 * */

/** @property {Flag} OPEN_READ Open file for reading flag. */
fileSystem.OPEN_READ = 1;
/** @property {Flag} OPEN_WRITE Open file for writing flag. */
fileSystem.OPEN_WRITE = 2;
/** @property {Flag} CREATE_NEW Create new file flag. */
fileSystem.CREATE_NEW = 4;

function mapOpenArgs(args) {
  var flags = fileSystem.OPEN_READ  | 
              fileSystem.OPEN_WRITE | 
              fileSystem.CREATE_NEW;
  var map = {
    perms:     null,
    flush:     false, 
    read:      true,
    write:     true,
    createNew: true,
  };
  switch (args.length) {
    case 0:
      break;
    case 1:
      flags = args[0];
      break;
    case 2:
      flags = args[0];
      map.flush = args[1];
      break;
    case 3:
      flags = args[0];
      map.flush = args[1];
      map.perms = args[2];
      break;
    default:
      throw 'Invalid number of arguments';
  }

  map.read = (flags & fileSystem.OPEN_READ) == fileSystem.OPEN_READ;
  map.write = (flags & fileSystem.OPEN_WRITE) == fileSystem.OPEN_WRITE;
  map.createNew = (flags & fileSystem.CREATE_NEW) == fileSystem.CREATE_NEW;

  return map;
}

/**
 * Synchronous version of open.
 *
 * @param {string} path the path of the file to open
 * @param {Flag} [openFlags] an integer representing whether to open the file for
 *        reading, writing, creation, or some combination of these with bitwise or
 *        (e.g. <code>fileSystem.OPEN_READ | fileSystem.OPEN_WRITE</code>).  If not
 *        specified the file will be opened for reading, wrting, and creation.
 * @param {boolean} [flush] flush file writes immediately (default is false)
 * @param {string} [permissions] the permissions to create the file with if the
 *        file is created when opened.
 * @returns {module:vertx/file_system.AsyncFile}
 */
fileSystem.openSync = function(path) {
  var rest = Array.prototype.slice.call(arguments, 1);
  var opts = mapOpenArgs(rest);
  return new fileSystem.AsyncFile(
      jfs.openSync(path, opts.perms, opts.read, opts.write, 
                   opts.createNew, opts.flush));
};

/**
 * Open a file on the file system, asynchronously.
 *
 * @param {string} path the path of the file to open
 * @param {Flag} [openFlags] an integer representing whether to open the file for
 *        reading, writing, creation, or some combination of these with bitwise or
 *        (e.g. <code>fileSystem.OPEN_READ | fileSystem.OPEN_WRITE</code>).  If not
 *        specified the file will be opened for reading, wrting, and creation.
 * @param {boolean} [flush] flush file writes immediately (default is false)
 * @param {string} [permissions] the permissions to create the file with if the
 *        file is created when opened.
 * @param {ResultHandler} handler the function to be called when the file is opened. The 
 *        handler will receieve an AsyncFile as a parameter when called.
 * @returns {module:vertx/file_system}
 */
fileSystem.open = function(path) {
  var rest    = Array.prototype.slice.call(arguments, 1);
  var handler = asyncFileHandler(rest.pop());
  var opts    = mapOpenArgs(rest);
  jfs.open(path, opts.perms, opts.read, opts.write, 
           opts.createNew, opts.flush, handler);
  return fileSystem;
};

 /**
 * <p>
 * An <code>AsyncFile</code> represents a file on the file-system which can be
 * read from, or written to asynchronously.  Methods also exist to get a
 * <code>ReadStream</code> or a <code>WriteStream</code> on the file. This
 * allows the data to be pumped to and from other streams. Instances of this
 * class are not thread-safe 
 * </p>
 * <p>
 * These should not be created directly. Rather they are created internally
 * by vert.x and provided to callback handlers and as return values from
 * synchronous functions.
 * </p>
 * @constructor
 * @param {org.vertx.java.core.file.AsyncFile} asyncFile the underlying java representation of this AsyncFile
 * @augments module:vertx/streams~ReadStream
 * @augments module:vertx/streams~WriteStream
 */
fileSystem.AsyncFile = function(jaf) {
  var that = this;

  streams.WriteStream.call(this, jaf);
  streams.ReadStream.call(this, jaf);

  /**
   * Close the file asynchronously
   * @param {ResultHandler} handler the handler to be called when the operation completes
   */
  this.close = function(handler) {
    if (handler) {
      jaf.close(wrapHandler(handler));
    } else {
      jaf.close();
    }
  };

  /**
   * Write to the file asynchronously
   * @param {Buffer|string} buffer the data to write
   * @param {number} position the byte position from which to start writing
   * @param {ResultHandler} handler the handler to call when the write has completed
   * @returns {module:vertx/file_system}
   */
  this.write = function(buffer, position, handler) {
    if (buffer && typeof buffer === 'string') {
      buffer = new Buffer(buffer);
    }
    if (position === null || position === undefined) {
      // WriteStream interface
      jaf.write(buffer._to_java_buffer());
    } else {
      // AsyncFile interface
      jaf.write(buffer._to_java_buffer(), position, wrapHandler(handler));
    }
    return that;
  };

  /**
   * Read from the file asynchronously
   * @param {Buffer} buffer The buffer to fill with the contents of the file
   * @param {number} offset The offset point in the Buffer from which to start writing
   * @param {number} position The position in the file from which to begin reading
   * @param {number} length the number of bytes to read
   * @param {ResultHandler} handler the handler to call when close() has been completed and will
   *        be provided the filled Buffer as a parameter
   */
  this.read = function(buffer, offset, position, length, handler) {
    jaf.read(buffer._to_java_buffer(), offset, position, length, wrapHandler(handler));
    return that;
  };

  /**
   * Flush any writes made to this file to persistent storage.
   * @param {ResultHandler} handler The handler to be called when the flush has completed
   */
  this.flush = function(handler) {
    if (handler) {
      jaf.flush(wrapHandler(handler));
    } else {
      jaf.flush();
    }
  };
};

/**
 * Create a new empty file, asynchronously.
 *
 * @param {string} path path of the file to create.
 * @param {ResultHandler} handler the function to call when complete
 * @returns {module:vertx/file_system}
 */
fileSystem.createFile = function(path, handler) {
  jfs.createFile(path, wrapHandler(handler));
  return fileSystem;
};

/**
 * Synchronous version of createFile.
 *
 * @param {string} path path of the file to create.
 * @returns {module:vertx/file_system}
 */
fileSystem.createFileSync = function(path) {
  jfs.createFileSync(path);
  return fileSystem;
};

/**
 * Check if a file exists, asynchronously.
 *
 * @param {string} path Path of the file to check.
 * @param {ResultHandler} handler the function to call when complete
 * @returns {module:vertx/file_system}
 */
fileSystem.exists = function(path, handler) {
  jfs.exists(path, wrapHandler(handler));
  return fileSystem;
};

/**
 * Synchronous version of exists.
 *
 * @param {string} path Path of the file to check.
 * @returns {boolean} <code>true</code> if the file exists
 */
fileSystem.existsSync = function(path) {
  return jfs.existsSync(path);
};

/** 
 * @typedef {{}} FileSystemProps 
 * @property {number} totalSpace The total space on the file system, in bytes
 * @property {number} unallocatedSpace The total unallocated space on the file syste, in bytes
 * @property {number} usableSpace The total usable space on the file system, in bytes
 * */

/**
 * Asynchronously get properties for the file system being used by the 
 * <code>path</code> specified.
 *
 * @param {string} path the path in the file system.
 * @param {ResultHandler} handler the function to call with the FileSystemProps when complete
 * @returns {module:vertx/file_system}
 */
fileSystem.fsProps = function(path, handler) {
  jfs.fsProps(path, wrapHandler(handler));
  return fileSystem;
};

/**
 * Synchronous version of fsProps.
 * @param {string} path Path in the file system.
 * @returns {FileSystemProps}
 */
fileSystem.fsPropsSync = function(path) {
  return jfs.fsPropsSync(path);
};

module.exports = fileSystem;

