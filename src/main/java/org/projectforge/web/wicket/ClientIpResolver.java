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

package org.projectforge.web.wicket;

import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.request.cycle.RequestCycle;

public class ClientIpResolver extends WebClientInfo
{
  private static final long serialVersionUID = -1001665111323720357L;

  public static String getClientIp()
  {
    final RequestCycle requestCycle = RequestCycle.get();
    return new ClientIpResolver(requestCycle).getRemoteAddr(requestCycle);
  }

  /**
   * @param requestCycle
   */
  public ClientIpResolver(final RequestCycle requestCycle)
  {
    super(requestCycle);
  }

}
