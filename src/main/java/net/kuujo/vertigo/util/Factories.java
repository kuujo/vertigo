package net.kuujo.vertigo.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import net.kuujo.vertigo.annotations.Factory;

/**
 * Factory utility.
 *
 * @author Jordan Halterman
 */
public class Factories {

  @SuppressWarnings("unchecked")
  public static <T> T createObject(Class<? extends T> clazz, Object... args) {
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

        // Set up the factory arguments.
        Class<?>[] types = method.getParameterTypes();
        Object[] params = new Object[types.length];
        final Set<Integer> used = new HashSet<>();
        for (int i = 0; i < types.length; i++) {
          for (int j = 0; j < args.length; j++) {
            if (!used.contains(j) && args[j].getClass().isAssignableFrom(types[i])) {
              params[i] = args[j];
              used.add(j);
            }
          }
        }

        // Invoke the factory method.
        try {
          return (T) method.invoke(null, args);
        }
        catch (IllegalAccessException | InvocationTargetException e) {
          continue; // Just skip it. An exception will be thrown later.
        }
      }
    }
    throw new IllegalArgumentException(clazz.getCanonicalName() + " does not contain a valid factory method.");
  }

}
