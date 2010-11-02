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

package org.projectforge.web.calendar;

import java.util.Date;

import javax.servlet.jsp.PageContext;

import org.displaytag.decorator.DisplaytagColumnDecorator;
import org.displaytag.exception.DecoratorException;
import org.displaytag.properties.MediaTypeEnum;

/**
 * In result lists this decorate is used for formatting tasks in columns.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class DateColumnDecorator implements DisplaytagColumnDecorator
{
  private DateTimeFormatter dateTimeFormatter;

  public Object decorate(Object columnValue, PageContext pageContext, MediaTypeEnum media) throws DecoratorException
  {
    if (columnValue == null) {
      // For example if Priority is null;
      return "";
    }
    if (columnValue instanceof Date) {
      return dateTimeFormatter.getFormattedDate((Date)columnValue);
    }
    return "";
  }
  
  public void setDateTimeFormatter(DateTimeFormatter dateFormatter)
  {
    this.dateTimeFormatter = dateFormatter;
  }
}
