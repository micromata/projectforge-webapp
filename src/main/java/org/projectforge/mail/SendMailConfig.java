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

package org.projectforge.mail;

import org.projectforge.AppVersion;
import org.projectforge.core.ConfigurationData;


public class SendMailConfig implements ConfigurationData
{
  private String protocol = "smtp";

  private String host;

  private Integer port = 25;

  private Boolean debug;

  private String user;

  private String password;

  private String charset = "UTF-8";

  private String from = "noreply";

  private String fromReal = AppVersion.APP_ID;

  /**
   * Default: smtp.
   */
  public String getProtocol()
  {
    return protocol;
  }

  /** The host used for sending messages.If not given then no e-mail will be sent. In that case every e-mail will be logged instead. */
  public String getHost()
  {
    return host;
  }

  /** The port of the smtp host (default 25). */
  public Integer getPort()
  {
    return port;
  }

  /**
   * If true, then javax.mail.Session will configured with debug option. Default is false.
   * @return
   */
  public Boolean getDebug()
  {
    return debug;
  }

  /** The username for the host, if authentication is needed. */
  public String getUser()
  {
    return user;
  }

  /** The password for the user, if authentication is needed. */
  public String getPassword()
  {
    return password;
  }

  /** The charset of sent messages. Default is "UTF-8". */
  public String getCharset()
  {
    return charset;
  }

  /**
   * Default is "noreply".
   */
  public String getFrom()
  {
    return from;
  }

  /**
   * Default is the application's id.
   * @see AppVersion#APP_ID
   */
  public String getFromReal()
  {
    return fromReal;
  }
}
