package net.kuujo.vertigo.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.Vertigo.Mode;
import net.kuujo.vertigo.annotations.ClusterType;
import net.kuujo.vertigo.annotations.ClusterTypeInfo;
import net.kuujo.vertigo.annotations.Factory;
import net.kuujo.vertigo.annotations.LocalType;
import net.kuujo.vertigo.annotations.LocalTypeInfo;

/**
 * Factory utility.<p>
 *
 * This helper class should be used with the {@link net.kuujo.vertigo.annotations.Factory}
 * annotation to construct types from annotated factory methods. Specifically,
 * when constructing types that are specific to different cluster modes, the factory
 * utility will automatically fall back to the appropriate default local- or cluster-
 * type according to the current Vertigo cluster mode.
 *
 * @author Jordan Halterman
 */
public final class Factories {

  @SuppressWarnings("unchecked")
  public static <T> T createObject(Class<? extends T> clazz, Object... args) {
    Class<?> type = resolveType(clazz);

    // Search the class for a factory method.
    for (Method method : type.getDeclaredMethods()) {
      if (method.isAnnotationPresent(Factory.class)) {
         // The method must be public static.
        if (!Modifier.isPublic(method.getModifiers()) || !Modifier.isStatic(method.getModifiers())) {
          throw new IllegalArgumentException("Factory method " + method.getName() + " in " + type.getCanonicalName() + " must be public and static.");
        }
        // The method return type must be a Class<T> instance.
        if (!method.getReturnType().isAssignableFrom(type)) {
          throw new IllegalArgumentException("Factory method " + method.getName() + " in " + type.getCanonicalName() + " must return a " + type.getCanonicalName() + " instance.");
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
  private static Class<?> resolveType(Class<?> clazz) {
    Mode mode = Vertigo.currentMode();
    if (mode.equals(Mode.LOCAL)) {
      if (isAnnotationPresentInHierarchy(clazz, LocalType.class)
          || (!isAnnotationPresentInHierarchy(clazz, LocalType.class)
              && !isAnnotationPresentInHierarchy(clazz, ClusterType.class))) {
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
    } else if (mode.equals(Mode.CLUSTER)) {
      if (isAnnotationPresentInHierarchy(clazz, ClusterType.class)
          || (!isAnnotationPresentInHierarchy(clazz, LocalType.class)
              && !isAnnotationPresentInHierarchy(clazz, ClusterType.class))) {
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
   * @return
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
