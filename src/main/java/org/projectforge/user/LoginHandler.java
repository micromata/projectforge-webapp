/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

package org.projectforge.user;


/**
 * Different implementations of login handling are supported.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public interface LoginHandler
{
  /**
   * A login handler will be initialized by ProjectForge during start-up.
   */
  public void initialize();

  public LoginResult checkLogin(final String username, final String password);

  public boolean isAdminUser(final PFUserDO user);
}
