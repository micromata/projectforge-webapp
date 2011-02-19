/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.plugins.todo;

import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.book.BookDO;
import org.projectforge.book.BookDao;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractAutoLayoutEditPage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.EditPage;

@EditPage(defaultReturnPage = ToDoListPage.class)
public class ToDoEditPage extends AbstractAutoLayoutEditPage<BookDO, ToDoEditForm, BookDao> implements ISelectCallerPage
{
  private static final long serialVersionUID = 7091721062661400435L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ToDoEditPage.class);

  @SpringBean(name = "bookDao")
  private BookDao bookDao;

  public ToDoEditPage(PageParameters parameters)
  {
    super(parameters, "book");
    init();
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#select(java.lang.String, java.lang.Integer)
   */
  public void select(String property, Object selectedValue)
  {
    if ("lendOutById".equals(property) == true) {
      bookDao.setLendOutBy(getData(), (Integer) selectedValue);
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#unselect(java.lang.String)
   */
  public void unselect(String property)
  {
    if ("lendOutById".equals(property) == true) {
      getData().setLendOutBy(null);
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#cancelSelection(java.lang.String)
   */
  public void cancelSelection(String property)
  {
    // Do nothing.
  }

  @Override
  protected BookDao getBaseDao()
  {
    return bookDao;
  }

  @Override
  protected ToDoEditForm newEditForm(AbstractEditPage< ? , ? , ? > parentPage, BookDO data)
  {
    return new ToDoEditForm(this, data);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
