/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.mail;

import org.projectforge.core.ConfigXml;
import org.projectforge.core.ConfigXmlSecretField;
import org.projectforge.xml.stream.XmlField;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class MailAccountConfig
{
  private String hostname = null;

  private String username = null;

  private String protocol = null;

  @XmlField(defaultIntValue = -1)
  private int port = -1;

  @ConfigXmlSecretField
  private String password = null;

  private boolean readonly;

  /**
   * For example: mail.projectforge.org
   * @return
   */
  public String getHostname()
  {
    return hostname;
  }

  public MailAccountConfig setHostname(final String hostname)
  {
    this.hostname = hostname;
    return this;
  }

  /**
   * imap, imaps (with ssl) or pop3.
   */
  public String getProtocol()
  {
    return protocol;
  }

  public MailAccountConfig setProtocol(final String protocol)
  {
    this.protocol = protocol;
    return this;
  }

  /**
   * If not given then the default port of the mail protocol is used.
   * @return
   */
  public int getPort()
  {
    return port;
  }

  public MailAccountConfig setPort(final int port)
  {
    this.port = port;
    return this;
  }

  public String getUsername()
  {
    return username;
  }

  public MailAccountConfig setUsername(final String username)
  {
    this.username = username;
    return this;
  }

  public String getPassword()
  {
    return password;
  }

  public MailAccountConfig setPassword(final String password)
  {
    this.password = password;
    return this;
  }

  /**
   * If read-only then no modifications were done on the mail server.
   */
  public boolean isReadonly()
  {
    return readonly;
  }

  public void setReadonly(final boolean readonly)
  {
    this.readonly = readonly;
  }

  /**
   * @see ConfigXml#toString(Object)
   */
  @Override
  public String toString()
  {
    return ConfigXml.toString(this);
  }
}
