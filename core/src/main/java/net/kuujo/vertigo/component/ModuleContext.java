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
package net.kuujo.vertigo.component;

import net.kuujo.vertigo.component.impl.DefaultModuleContext;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Module component context.
 * 
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 * 
 * @param <T> The component type
 */
@JsonTypeInfo(
  use=JsonTypeInfo.Id.CLASS,
  include=JsonTypeInfo.As.PROPERTY,
  property="class",
  defaultImpl=DefaultModuleContext.class
)
public interface ModuleContext extends ComponentContext<ModuleContext> {

  /**
   * Returns the module name.
   * 
   * @return The module name.
   */
  String module();

}
