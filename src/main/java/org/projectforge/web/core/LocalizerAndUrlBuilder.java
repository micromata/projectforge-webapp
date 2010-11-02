/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.core;

/**
 * For combining different worlds: Stripes and Wicket. Wicket has no PageContext. Some formatters need methods for getting i18n keys and
 * building urls e. g. for images.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public interface LocalizerAndUrlBuilder
{
  public String getString(String i18nKey);
  
  public String getLocalizedMessage(String i18nKey, Object... params);
  
  public String buildUrl(String path);
}
