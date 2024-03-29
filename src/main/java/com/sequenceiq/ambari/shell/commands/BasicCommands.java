/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sequenceiq.ambari.shell.commands;

import static com.sequenceiq.ambari.shell.support.TableRenderer.renderMapValueMap;
import static com.sequenceiq.ambari.shell.support.TableRenderer.renderSingleMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import com.sequenceiq.ambari.client.AmbariClient;
import com.sequenceiq.ambari.shell.model.AmbariContext;

/**
 * Basic commands used in the shell. Delegating the commands
 * to the Ambari Server via a Groovy based client.
 *
 * @see com.sequenceiq.ambari.client.AmbariClient
 */
@Component
public class BasicCommands implements CommandMarker {

  private AmbariClient client;
  private AmbariContext context;

  @Autowired
  public BasicCommands(AmbariClient client, AmbariContext context) {
    this.client = client;
    this.context = context;
  }

  /**
   * Checks whether the tasks command is available or not.
   *
   * @return true if its available false otherwise
   */
  @CliAvailabilityIndicator("tasks")
  public boolean isTasksCommandAvailable() {
    return context.isConnectedToCluster();
  }

  /**
   * Prints the tasks of the Ambari Server.
   *
   * @param id id of the request
   * @return task list
   */
  @CliCommand(value = "tasks", help = "Lists the Ambari tasks")
  public String tasks(
    @CliOption(key = "id", mandatory = false, help = "Id of the request; default is: 1", unspecifiedDefaultValue = "1") String id) {
    return renderSingleMap(client.getTaskMap(id), "TASK", "STATUS");
  }

  /**
   * Checks whether the service list command is available or not.
   *
   * @return true if available false otherwise
   */
  @CliAvailabilityIndicator("services list")
  public boolean isServiceListCommandAvailable() {
    return context.isConnectedToCluster();
  }

  /**
   * Prints the available service list of the Ambari Server.
   *
   * @return service list
   */
  @CliCommand(value = "services list", help = "Lists the available services")
  public String servicesList() {
    return renderSingleMap(client.getServicesMap(), "SERVICE", "STATE");
  }

  /**
   * Checks whether the service components command is available or not.
   *
   * @return true if available false otherwise
   */
  @CliAvailabilityIndicator("services components")
  public boolean isServiceComponentsCommandAvailable() {
    return context.isConnectedToCluster();
  }

  /**
   * Prints the service components of the Ambari Server.
   *
   * @return service component list
   */
  @CliCommand(value = "services components", help = "Lists all services with their components")
  public String serviceComponents() {
    return renderMapValueMap(client.getServiceComponentsMap(), "SERVICE", "COMPONENT", "STATE");
  }

  /**
   * Checks whether the debug on command is available or not.
   *
   * @return true if available false otherwise
   */
  @CliAvailabilityIndicator("debug on")
  public boolean isDebugOnCommandAvailable() {
    return !client.isDebugEnabled();
  }

  /**
   * Turns the debug on. From now on users will see the URLs of the API calls.
   *
   * @return status message
   */
  @CliCommand(value = "debug on", help = "Shows the URL of the API calls")
  public String debugOn() {
    client.setDebugEnabled(true);
    return "debug enabled";
  }

  /**
   * Checks whether the debug off command is available or not.
   *
   * @return true if available false otherwise
   */
  @CliAvailabilityIndicator("debug off")
  public boolean isDebugOffCommandAvailable() {
    return client.isDebugEnabled();
  }

  /**
   * Turns the debug off. URLs are not visible anymore.
   *
   * @return status message
   */
  @CliCommand(value = "debug off", help = "Stops showing the URL of the API calls")
  public String debugOff() {
    client.setDebugEnabled(false);
    return "debug disabled";
  }

  /**
   * Checks whether the hint command is available or not.
   *
   * @return true if available false otherwise
   */
  @CliAvailabilityIndicator("hint")
  public boolean isHintCommandAvailable() {
    return true;
  }

  /**
   * Provides some hints what you can do in the current context.
   *
   * @return hint message
   */
  @CliCommand(value = "hint", help = "Shows some hints")
  public String hint() {
    return context.getHint();
  }

  @CliAvailabilityIndicator("services stop")
  public boolean isServiceStopCommandAvailable() {
    return context.isConnectedToCluster();
  }

  @CliCommand(value = "services stop", help = "Stops all the running services")
  public String stopServices() {
    String message;
    try {
      client.stopAllServices();
      message = "Stopping all services..";
    } catch (Exception e) {
      message = "Cannot stop services";
    }
    return String.format("%s\n\n%s", message, servicesList());
  }

  @CliAvailabilityIndicator("services start")
  public boolean isServiceStartCommandAvailable() {
    return context.isConnectedToCluster();
  }

  @CliCommand(value = "services start", help = "Starts all the services")
  public String startServices() {
    String message;
    try {
      client.startAllServices();
      message = "Starting all services..";
    } catch (Exception e) {
      message = "Cannot start services";
    }
    return String.format("%s\n\n%s", message, servicesList());
  }
}
