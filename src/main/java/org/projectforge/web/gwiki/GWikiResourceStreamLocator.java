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

package org.projectforge.web.gwiki;

import java.util.Locale;

import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.locator.IResourceStreamLocator;

import de.micromata.genome.gwiki.model.GWikiElementInfo;
import de.micromata.genome.gwiki.model.GWikiWeb;

/**
 * Locates Content from the GWiki.
 * 
 * @author Roger Rene Kommer (r.kommer@micromata.de)
 * 
 */
public class GWikiResourceStreamLocator implements IResourceStreamLocator
{
  protected IResourceStreamLocator parent;

  public GWikiResourceStreamLocator(IResourceStreamLocator parent)
  {
    this.parent = parent;
  }

  public IResourceStream locate(Class< ? > clazz, String path)
  {
    return parent.locate(clazz, path);
  }

  public IResourceStream locate(Class< ? > clazz, String path, String style, Locale locale, String extension)
  {
    String id = path + "." + extension;
    GWikiWeb wiki = GWikiWeb.getWiki();
    GWikiElementInfo el = wiki.findElementInfo(id);
    if (el == null) {
      return parent.locate(clazz, path, style, locale, extension);
    }
    return new GWikiPageResourceStream(id);
  }

  public IResourceStreamLocator getParent()
  {
    return parent;
  }

  public void setParent(IResourceStreamLocator parent)
  {
    this.parent = parent;
  }

}
