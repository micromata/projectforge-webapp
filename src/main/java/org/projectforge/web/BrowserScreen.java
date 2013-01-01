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

package org.projectforge.web;

import org.apache.wicket.protocol.http.ClientProperties;

/**
 * Not yet in use.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 *
 */
public class BrowserScreen
{
  private final int height, width;

  public BrowserScreen(final ClientProperties clientProperties)
  {
    height = clientProperties.getBrowserHeight();
    width = clientProperties.getBrowserWidth();
  }

  /**
   * @return true if the width of browser window is less than 800.
   */
  public boolean isNarrowScreen()
  {
    return height > 0 && height < 800;
  }

  /**
   * @return true if the width of browser window is wider than 1200.
   */
  public boolean isWideScreen()
  {
    return height > 1024;
  }

  /**
   * @return height of the browser window.
   */
  public int getHeight()
  {
    return height;
  }

  /**
   * @return width of the browser window.
   */
  public int getWidth()
  {
    return width;
  }
}
