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

import static net.kuujo.vertigo.util.Config.parseCluster;
import static net.kuujo.vertigo.util.Config.parseContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.kuujo.vertigo.cluster.ClusterScope;
import net.kuujo.vertigo.cluster.ClusterType;
import net.kuujo.vertigo.cluster.ClusterTypeInfo;
import net.kuujo.vertigo.cluster.LocalType;
import net.kuujo.vertigo.cluster.LocalTypeInfo;
import net.kuujo.vertigo.cluster.XyncType;
import net.kuujo.vertigo.cluster.XyncTypeInfo;
import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.component.impl.DefaultComponentFactory;

import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

/**
 * Factory utility.<p>
 *
 * This helper class should be used with the {@link net.kuujo.vertigo.util.Factory}
 * annotation to construct types from annotated factory methods. Specifically,
 * when constructing types that are specific to different cluster modes, the factory
 * utility will automatically fall back to the appropriate default local- or cluster-
 * type according to the current Vertigo cluster mode.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public final class Factories {

  /**
   * Creates a component instance for the current Vert.x instance.
   */
  public static Component createComponent(Vertx vertx, Container container) {
    return new DefaultComponentFactory().setVertx(vertx).setContainer(container).createComponent(parseContext(container.config()), parseCluster(container.config(), vertx, container));
  }

  /**
   * Creates an object for the current cluster scope.<p>
   *
   * This method will create a cluster-scope specific object based on an abstract
   * type. The abstract type must define its concrete local and cluster implementations
   * using annotations.
   *
   * @param scope The scope in which to create the object.
   * @param clazz The abstract type from which to create the object. This type should
   *        define concrete implementations for each cluster scope.
   * @param args Factory method arguments.
   * @return The created object.
   */
  @SuppressWarnings("unchecked")
  public static <T> T createObject(ClusterScope scope, Class<T> clazz, Object... args) {
    if (scope.equals(ClusterScope.LOCAL)) {
      if (!clazz.isAnnotationPresent(LocalTypeInfo.class)) {
        throw new IllegalArgumentException("No local type info available.");
      }
      LocalTypeInfo info = clazz.getAnnotation(LocalTypeInfo.class);
      return createObjectFromFactoryMethod((Class<? extends T>) info.defaultImpl(), args);
    } else if (scope.equals(ClusterScope.CLUSTER)) {
      if (!clazz.isAnnotationPresent(ClusterTypeInfo.class)) {
        throw new IllegalArgumentException("No cluster type info available.");
      }
      ClusterTypeInfo info = clazz.getAnnotation(ClusterTypeInfo.class);
      return createObjectFromFactoryMethod((Class<? extends T>) info.defaultImpl(), args);
    } else if (scope.equals(ClusterScope.XYNC)) {
      if (!clazz.isAnnotationPresent(XyncTypeInfo.class)) {
        throw new IllegalArgumentException("No Xync type info available.");
      }
      XyncTypeInfo info = clazz.getAnnotation(XyncTypeInfo.class);
      return createObjectFromFactoryMethod((Class<? extends T>) info.defaultImpl(), args);
    }
    return null;
  }

  /**
   * Creates an object for the current cluster scope.<p>
   *
   * This method will create a cluster-scope specific object based on a concrete type.
   * If the concrete type is not supported by the given cluster scope then the
   * factory will fall back to the default implementation for the current cluster scope.
   *
   * @param scope The current cluster scope.
   * @param clazz The concrete type to attempt to construct. If the type is not valid
   *        for the current cluster scope then a different type may be constructed.
   * @param args Factory method arguments.
   * @return The created object.
   */
  @SuppressWarnings("unchecked")
  public static <T> T resolveObject(ClusterScope scope, Class<? extends T> clazz, Object... args) {
    return createObjectFromFactoryMethod((Class<? extends T>) resolveType(scope, clazz), args);
  }

  /**
   * Creates an object from a factory method on the given class.
   *
   * @param clazz The class to instantiate.
   * @param args Factory method arguments.
   * @return The created object.
   */
  @SuppressWarnings("unchecked")
  private static <T> T createObjectFromFactoryMethod(Class<? extends T> clazz, Object... args) {
    // Search the class for a factory method.
    for (Method method : clazz.getDeclaredMethods()) {
      if (method.isAnnotationPresent(Factory.class)) {
         // The method must be public static.
        if (!Modifier.isPublic(method.getModifiers()) || !Modifier.isStatic(method.getModifiers())) {
          throw new IllegalArgumentException("Factory method " + method.getName() + " in " + clazz.getCanonicalName() + " must be public and static.");
        }
        // The method return type must be a Class<T> instance.
        if (!method.getReturnType().isAssignableFrom(clazz)) {
          throw new IllegalArgumentException("Factory method " + method.getName() + " in " + clazz.getCanonicalName() + " must return a " + clazz.getCanonicalName() + " instance.");
        }

        // Invoke the factory method.
        try {
          return (T) method.invoke(null, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
          continue; // Just skip it. An exception will be thrown later.
        }
      }
    }
    throw new IllegalArgumentException(clazz.getCanonicalName() + " does not contain a valid factory method.");
  }

  /**
   * Validates that the given type's required cluster mode matches the current
   * Vertigo cluster mode. This is done by checking for the
   * <code>@LocalType</code> and <code>@ClusterType</code> annotations. If the
   * class does not have either annotation then it is assumed to be valid for both
   * modes. If the class does have an annotation that doesn't match the current
   * cluster mode then we attempt to resolve the type to a default for the current
   * cluster mode. This allows local-specific implementations to be used
   * during testing in local-mode where the network is otherwise configured
   * for clustered communication and persistence.
   *
   * @param clazz The class to check.
   * @return The resolved type to instantiate.
   */
  private static Class<?> resolveType(ClusterScope scope, Class<?> clazz) {
    if (scope.equals(ClusterScope.LOCAL)) {
      if (isAnnotationPresentInHierarchy(clazz, LocalType.class)
          || (!isAnnotationPresentInHierarchy(clazz, LocalType.class)
              && !isAnnotationPresentInHierarchy(clazz, ClusterType.class)
              && !isAnnotationPresentInHierarchy(clazz, XyncType.class))) {
        return clazz;
      } else {
        // Find the base data type for the local cluster mode and look
        // for a default implementation. If no default implementation is
        // provided for the current cluster mode then an exception is thrown.
        LocalTypeInfo info = getAnnotationInHierarchy(clazz, LocalTypeInfo.class);
        if (info == null) {
          throw new IllegalStateException("Cannot instantiate " + clazz.getName() + " object in local mode.");
        }
        return info.defaultImpl();
      }
    } else if (scope.equals(ClusterScope.CLUSTER)) {
      if (isAnnotationPresentInHierarchy(clazz, ClusterType.class)
          || (!isAnnotationPresentInHierarchy(clazz, LocalType.class)
              && !isAnnotationPresentInHierarchy(clazz, ClusterType.class)
              && !isAnnotationPresentInHierarchy(clazz, XyncType.class))) {
        return clazz;
      } else {
        // Find the base data type for the remote cluster mode and look
        // for a default implementation. If no default implementation is
        // provided for the current cluster mode then an exception is thrown.
        ClusterTypeInfo info = getAnnotationInHierarchy(clazz, ClusterTypeInfo.class);
        if (info == null) {
          throw new IllegalStateException("Cannot instantiate " + clazz.getName() + " object in cluster mode.");
        }
        return info.defaultImpl();
      }
    } else if (scope.equals(ClusterScope.XYNC)) {
      if (isAnnotationPresentInHierarchy(clazz, XyncType.class)
          || (!isAnnotationPresentInHierarchy(clazz, LocalType.class)
              && !isAnnotationPresentInHierarchy(clazz, ClusterType.class)
              && !isAnnotationPresentInHierarchy(clazz, XyncType.class))) {
        return clazz;
      } else {
        // Find the base data type for the Xync cluster mode and look
        // for a default implementation. If no default implementation is
        // provided for the current cluster mode then an exception is thrown.
        XyncTypeInfo info = getAnnotationInHierarchy(clazz, XyncTypeInfo.class);
        if (info == null) {
          throw new IllegalStateException("Cannot instantiate " + clazz.getName() + " object in Xync mode.");
        }
        return info.defaultImpl();
      }
    } else {
      throw new IllegalStateException("Vertigo mode not initialized.");
    }
  }

  /**
   * Returns a boolean indicating whether an annotation is present on a class
   * or any of its ancestors.
   *
   * @param clazz The class to check.
   * @param annotation The annotation for which to search.
   * @return Indicates whether the given annotation is present on any class
   *         or interface in the class/interface hierarchy.
   */
  private static boolean isAnnotationPresentInHierarchy(Class<?> clazz, Class<? extends Annotation> annotation) {
    Class<?> current = clazz;
    while (current != Object.class) {
      if (current.isAnnotationPresent(annotation)) {
        return true;
      }
      for (Class<?> iface : current.getInterfaces()) {
        if (iface.isAnnotationPresent(annotation)) {
          return true;
        }
      }
      current = current.getSuperclass();
    }
    return false;
  }

  /**
   * Finds the highest level instance of an annotation on a class hierarchy.
   *
   * @param clazz The class to check.
   * @param annotation The annotation for which to search.
   * @return The annotation if present, otherwise <code>null</code>
   */
  @SuppressWarnings("unchecked")
  private static <T extends Annotation> T getAnnotationInHierarchy(Class<?> clazz, Class<? extends Annotation> annotation) {
    Class<?> current = clazz;
    while (current != Object.class) {
      if (current.isAnnotationPresent(annotation)) {
        return (T) current.getAnnotation(annotation);
      }
      for (Class<?> iface : current.getInterfaces()) {
        if (iface.isAnnotationPresent(annotation)) {
          return (T) iface.getAnnotation(annotation);
        }
      }
      current = current.getSuperclass();
    }
    return null;
  }

}
