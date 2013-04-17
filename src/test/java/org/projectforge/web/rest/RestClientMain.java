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

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class RestClientMain
{
  private static final String URL = "http://localhost:8080/ProjectForge/rest";

  public static void main(final String[] args)
  {
    final Client client = Client.create();
    final WebResource webResource = client.resource(URL + "/authenticate/getToken");
    webResource.header(RestUserFilter.AUTHENTICATION_USERNAME, "kai").header(RestUserFilter.AUTHENTICATION_PASSWORD, "test123");
    final ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
    if (response.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
      throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
    }
    final String output = response.getEntity(String.class);
    if ("OK".equals(output) == false) {
      throw new RuntimeException("Initialization of ProjectForge's storage failed: " + output);
    }

  }
}
