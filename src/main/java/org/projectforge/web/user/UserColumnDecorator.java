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

package org.projectforge.web.user;

import javax.servlet.jsp.PageContext;

import org.displaytag.decorator.DisplaytagColumnDecorator;
import org.displaytag.exception.DecoratorException;
import org.displaytag.properties.MediaTypeEnum;
import org.projectforge.user.PFUserDO;


/**
 * In result lists this decorate is used for formatting tasks in columns.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
@Deprecated
public class UserColumnDecorator implements DisplaytagColumnDecorator
{
  private UserFormatter userFormatter;
  
  public Object decorate(Object columnValue, PageContext pageContext, MediaTypeEnum media) throws DecoratorException
  {
    if (columnValue == null) {
      // User is null:
      return "";
    }
    StringBuffer buf = new StringBuffer();
    PFUserDO user = null;
    if (columnValue instanceof PFUserDO) {
      user = (PFUserDO) columnValue;
      userFormatter.appendFormattedUser(buf, user);
    } else if (columnValue instanceof Integer) {
      userFormatter.appendFormattedUser(buf, (Integer)columnValue);
    }
    return buf.toString();
  }
  
  public void setUserFormatter(UserFormatter userFormatter)
  {
    this.userFormatter = userFormatter;
  }
}
