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
package com.sequenceiq.ambari.shell.converter;

import java.util.List;
import java.util.Set;

import org.springframework.shell.core.Completion;
import org.springframework.shell.core.Converter;
import org.springframework.shell.core.MethodTarget;

import com.sequenceiq.ambari.client.AmbariClient;
import com.sequenceiq.ambari.shell.completion.Host;

public class HostConverter implements Converter<Host> {

  private AmbariClient client;

  public HostConverter(AmbariClient client) {
    this.client = client;
  }

  @Override
  public boolean supports(Class<?> type, String optionContext) {
    return Host.class.isAssignableFrom(type);
  }

  @Override
  public Host convertFromText(String value, Class<?> targetType, String optionContext) {
    return new Host(value);
  }

  @Override
  public boolean getAllPossibleValues(List<Completion> completions, Class<?> targetType, String existingData, String optionContext, MethodTarget target) {
    Set<String> hosts = client.getHostNames().keySet();
    for (String host : hosts) {
      completions.add(new Completion(host));
    }
    return true;
  }
}
