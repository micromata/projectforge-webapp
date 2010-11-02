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

package org.projectforge.web.displaytag;

import javax.servlet.jsp.PageContext;

import org.displaytag.decorator.DisplaytagColumnDecorator;
import org.displaytag.exception.DecoratorException;
import org.displaytag.properties.MediaTypeEnum;
import org.projectforge.web.HtmlHelper;


/**
 * In result lists this decorate is used for formatting texts in columns.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class TextColumnDecorator implements DisplaytagColumnDecorator
{
  private HtmlHelper htmlHelper;

  /**
   * Filters and formats the given text.
   * @see org.displaytag.decorator.DisplaytagColumnDecorator#decorate(java.lang.Object, javax.servlet.jsp.PageContext, org.displaytag.properties.MediaTypeEnum)
   * @see HtmlHelper#formatText(String)
   * @see HtmlHelper#filter(String)
   */
  public Object decorate(Object columnValue, PageContext pageContext, MediaTypeEnum media) throws DecoratorException
  {
    if (columnValue == null) {
      // For example if Priority is null;
      return "";
    }
    return htmlHelper.formatText((String) columnValue, true);
  }

  public void setHtmlHelper(HtmlHelper htmlHelper)
  {
    this.htmlHelper = htmlHelper;
  }
}
