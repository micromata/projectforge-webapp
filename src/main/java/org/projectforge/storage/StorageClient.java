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

package org.projectforge.storage;

import javax.ws.rs.core.MediaType;

import org.projectforge.core.ConfigXml;
import org.projectforge.core.ConfigurationListener;
import org.projectforge.shared.storage.StorageConstants;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class StorageClient implements ConfigurationListener
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(StorageClient.class);

  private static StorageClient instance = new StorageClient();

  private StorageConfig config;

  private boolean initialized;

  public static StorageClient getInstance()
  {
    instance.checkInitialized();
    return instance;
  }

  private StorageClient()
  {
    ConfigXml.getInstance().register(this);
  }

  private void checkInitialized()
  {
    synchronized (this) {
      if (initialized == true) {
        return;
      }
      this.config = ConfigXml.getInstance().getStorageConfig();
      if (this.config == null) {
        log.info("No storageConfig given in config.xml. Storage not available.");
        return;
      }
      final Client client = Client.create();
      WebResource webResource = client.resource(getUrl("/initialization"))//
          .queryParam(StorageConstants.PARAM_AUTHENTICATION_TOKEN, this.config.getAuthenticationToken())//
          .queryParam(StorageConstants.PARAM_BASE_DIR, ConfigXml.getInstance().getApplicationHomeDir());
      ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
      if (response.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
        throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
      }
      String output = response.getEntity(String.class);
      if ("OK".equals(output) == false) {
        throw new RuntimeException("Initialization of ProjectForge's storage failed: " + output);
      }
      webResource = client.resource(getUrl("/securityCheck"));
      response = webResource.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
      if (response.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
        final String message = "Security alert: storage is available without any authentication!!!!!!!!!!!!!!!!";
        log.fatal(message);
        throw new RuntimeException(message);
      }
      webResource = client.resource(getUrl("/securityCheck"));
      addAuthenticationHeader(webResource);
      response = webResource.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
      if (response.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
        throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
      }
      output = response.getEntity(String.class);
      if (output.equals("authenticated") == false) {
        final String message = "Authentication didn't work. Storage isn't available.";
        log.fatal(message);
        throw new RuntimeException(message);
      }
      initialized = true;
      log.info("Initialization of ProjectForge's storage successfully done.");
    }
  }

  public String getUrl(final String service)
  {
    String url = this.config.getUrl();
    if (url == null) {
      url = System.getProperty(StorageConstants.SYSTEM_PROPERTY_URL);
    }
    return url + "/" + service;
  }

  private Builder addAuthenticationHeader(final WebResource webResource)
  {
    return webResource.header(StorageConstants.PARAM_AUTHENTICATION_TOKEN, config.getAuthenticationToken());
  }

  /**
   * @see org.projectforge.core.ConfigurationListener#afterRead()
   */
  @Override
  public void afterRead()
  {
    initialized = false;
  }
}
