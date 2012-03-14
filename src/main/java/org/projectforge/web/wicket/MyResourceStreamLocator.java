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

package org.projectforge.web.wicket;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.locator.ResourceStreamLocator;

/**
 * The web sources are not located in java sub directory org/projectforge/web, so this prefix will be replaced by "pf".
 * @author Kai Reinhard (k.reinhard@me.com)
 */
public class MyResourceStreamLocator extends ResourceStreamLocator
{
  public static final String PACKAGE_PREFIX = "org/projectforge/web/";

  public static final String WEB_PREFIX = "wa/";

  @Override
  public IResourceStream locate(final Class<?> clazz, final String path)
  {
    final IResourceStream located = super.locate(clazz, locateWebResource(path));
    if (located != null) {
      return located;
    }
    return super.locate(clazz, path);
  }

  String locateWebResource(final String path)
  {
    if (path == null || path.startsWith(PACKAGE_PREFIX) == false) {
      return path; // Do nothing.
    }
    return WEB_PREFIX + StringUtils.removeStart(path, PACKAGE_PREFIX);
  }
}
