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

import static com.sequenceiq.ambari.shell.support.TableRenderer.renderMultiValueMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.sequenceiq.ambari.client.AmbariClient;
import com.sequenceiq.ambari.shell.model.AmbariContext;

import groovyx.net.http.HttpResponseException;

@RunWith(MockitoJUnitRunner.class)
public class ClusterCommandsTest {

  @InjectMocks
  private ClusterCommands clusterCommands;

  @Mock
  private AmbariClient client;
  @Mock
  private AmbariContext context;
  @Mock
  private HttpResponseException responseException;

  @Test
  public void testBuildClusterForNonExistingBlueprint() {
    when(client.doesBlueprintExists("id")).thenReturn(false);

    clusterCommands.buildCluster("id");

    verify(client).doesBlueprintExists("id");
  }

  @Test
  public void testBuildCluster() {
    Map<String, List<String>> map = Collections.singletonMap("group1", Arrays.asList("comp1", "comp2"));
    when(client.doesBlueprintExists("id")).thenReturn(true);
    when(client.getBlueprintMap("id")).thenReturn(map);
    when(context.getFocusValue()).thenReturn("id");

    String result = clusterCommands.buildCluster("id");

    verify(client).doesBlueprintExists("id");
    verify(client).getBlueprintMap("id");
    verify(client).getHostGroups("id");
    assertEquals(renderMultiValueMap(map, "HOSTGROUP", "COMPONENT"), result);
  }

  @Test
  public void testAssignForInvalidHostGroup() {
    Map<String, List<String>> map = Collections.singletonMap("group1", Arrays.asList("host", "host2"));
    ReflectionTestUtils.setField(clusterCommands, "hostGroups", map);
    ReflectionTestUtils.setField(clusterCommands, "hostNames", Arrays.asList("host3"));

    String result = clusterCommands.assign("host3", "group0");

    assertEquals("group0 is not a valid host group", result);
  }

  @Test
  public void testAssignForValidHostGroup() {
    Map<String, List<String>> map = new HashMap<String, List<String>>();
    map.put("group1", new ArrayList<String>());
    ReflectionTestUtils.setField(clusterCommands, "hostGroups", map);
    ReflectionTestUtils.setField(clusterCommands, "hostNames", Arrays.asList("host3"));

    String result = clusterCommands.assign("host3", "group1");

    assertEquals("host3 has been added to group1", result);
  }

  @Test
  public void testCreateClusterForException() throws HttpResponseException {
    String blueprint = "blueprint";
    Map<String, List<String>> map = Collections.singletonMap("group1", Arrays.asList("host", "host2"));
    ReflectionTestUtils.setField(clusterCommands, "hostGroups", map);
    when(context.getFocusValue()).thenReturn(blueprint);
    doThrow(responseException).when(client).createCluster(blueprint, blueprint, map);
    doThrow(responseException).when(client).deleteCluster(blueprint);

    String result = clusterCommands.createCluster();

    verify(client).createCluster(blueprint, blueprint, map);
    verify(client).getHostGroups(blueprint);
    verify(client).deleteCluster(blueprint);
    assertTrue(result.contains("Failed"));
  }

  @Test
  public void testCreateCluster() throws HttpResponseException {
    String blueprint = "blueprint";
    Map<String, List<String>> map = Collections.singletonMap("group1", Arrays.asList("host", "host2"));
    ReflectionTestUtils.setField(clusterCommands, "hostGroups", map);
    when(context.getFocusValue()).thenReturn(blueprint);
    when(client.getClusterName()).thenReturn("cluster");

    String result = clusterCommands.createCluster();

    verify(client).createCluster(blueprint, blueprint, map);
    verify(context).connectCluster();
    verify(context).resetFocus();
    assertFalse(result.contains("Failed"));
    assertTrue(result.contains("Successfully"));
  }

  @Test
  public void testDeleteClusterForException() throws HttpResponseException {
    when(context.getCluster()).thenReturn("cluster");
    when(responseException.getMessage()).thenReturn("msg");
    doThrow(responseException).when(client).deleteCluster("cluster");

    String result = clusterCommands.deleteCluster();

    verify(client).deleteCluster("cluster");
    verify(context).getCluster();
    verify(responseException).getMessage();
    assertEquals("Could not delete the cluster: msg", result);
  }

  @Test
  public void testDeleteCluster() throws HttpResponseException {
    when(context.getCluster()).thenReturn("cluster");
    when(responseException.getMessage()).thenReturn("msg");

    String result = clusterCommands.deleteCluster();

    verify(client).deleteCluster("cluster");
    verify(context).getCluster();
    assertEquals("Successfully deleted the cluster", result);
  }
}
