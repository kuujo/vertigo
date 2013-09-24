/*
* Copyright 2013 the original author or authors.
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
package net.kuujo.vevent.feeder;

/**
 * A feeder server.
 *
 * @author Jordan Halterman
 */
public interface FeederServer {

  /**
   * Instructs the server to start listening at an address.
   *
   * @param address
   *   The eventbus address at which to listen.
   * @return
   *   The called server instance.
   */
  public FeederServer listen(String address);

  /**
   * Returns the address on which the server is listening.
   *
   * @return
   *   The address on which the server is currently listening.
   */
  public String address();

  /**
   * Closes the server.
   */
  public void close();

}
