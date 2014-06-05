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
package com.sequenceiq.ambari.shell.flash;

import static org.apache.commons.lang.StringUtils.rightPad;

import org.springframework.shell.core.JLineShellComponent;

import com.sequenceiq.ambari.client.AmbariClient;

public class ServiceStopProgress extends AbstractFlash {

  private static final int ROLL_COUNT = 4;
  private volatile int counter = 1;
  private volatile boolean stopped;
  private AmbariClient client;

  public ServiceStopProgress(JLineShellComponent shell, AmbariClient client) {
    super(shell, FlashType.SERVICE_STOP);
    this.client = client;
  }

  @Override
  public String getText() {
    int dotCount = counter++ % ROLL_COUNT;
    if (counter == ROLL_COUNT) {
      counter = 1;
    }
    String message;
    if (!client.servicesStopped()) {
      message = rightPad("STOPPING", 8 + dotCount, ".");
    } else if (!stopped) {
      message = "STOPPED";
      stopped = true;
    } else {
      message = "";
    }
    return message;
  }
}
