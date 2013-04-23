/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.rest;

import java.util.Collection;

import javax.ws.rs.core.MediaType;

import org.projectforge.ProjectForgeVersion;
import org.projectforge.rest.JsonUtils;
import org.projectforge.rest.ServerInfo;
import org.projectforge.rest.UserObject;
import org.projectforge.task.rest.RTask;

import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class RestClientMain
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RestClientMain.class);

  private static final String URL = "http://localhost:8080/ProjectForge/rest";

  public static void main(final String[] args)
  {
    // http://localhost:8080/ProjectForge/rest/authenticate/getToken // username / password
    final Client client = Client.create();
    WebResource webResource = client.resource(URL + "/authenticate/getToken");
    ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).header(RestUserFilter.AUTHENTICATION_USERNAME, "demo")
        .header(RestUserFilter.AUTHENTICATION_PASSWORD, "demo123").get(ClientResponse.class);
    if (response.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
      throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
    }
    String json = response.getEntity(String.class);
    log.info(json);
    final UserObject user = JsonUtils.fromJson(json, UserObject.class);
    if (user == null) {
      throw new RuntimeException("Can't deserialize user : " + json);
    }
    final Integer userId = user.getId();
    final String authenticationToken = user.getAuthenticationToken();

    // http://localhost:8080/ProjectForge/rest/authenticate/initialContact?clientVersion=5.0 // userId / token
    webResource = client.resource(URL + "/authenticate/initialContact");
    response = webResource.queryParam("clientVersion", ProjectForgeVersion.VERSION_STRING).accept(MediaType.APPLICATION_JSON)
        .header(RestUserFilter.AUTHENTICATION_USER_ID, userId.toString()).header(RestUserFilter.AUTHENTICATION_TOKEN, authenticationToken)
        .get(ClientResponse.class);
    if (response.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
      throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
    }
    json = response.getEntity(String.class);
    log.info(json);
    final ServerInfo serverInfo = JsonUtils.fromJson(json, ServerInfo.class);
    if (serverInfo == null) {
      throw new RuntimeException("Can't deserialize serverInfo : " + json);
    }

    // http://localhost:8080/ProjectForge/rest/task/tree // userId / token
    webResource = client.resource(URL + "/task/tree");
    response = webResource.queryParam("search", "projectforge").accept(MediaType.APPLICATION_JSON)
        .header(RestUserFilter.AUTHENTICATION_USER_ID, userId.toString()).header(RestUserFilter.AUTHENTICATION_TOKEN, authenticationToken)
        .get(ClientResponse.class);
    if (response.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
      throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
    }
    json = response.getEntity(String.class);
    log.info(json);
    final Collection<RTask> col = JsonUtils.fromJson(json, new TypeToken<Collection<RTask>>() {
    }.getType());
    for (final RTask task : col) {
      logTask(task, "");
    }
  }

  private static void logTask(final RTask task, final String indent)
  {
    log.info(indent + task.getTitle());
    final Collection<RTask> children = task.getChildren();
    if (children == null || children.size() == 0) {
      return;
    }
    for (final RTask child : children) {
      logTask(child, indent + "  ");
    }
  }
}
