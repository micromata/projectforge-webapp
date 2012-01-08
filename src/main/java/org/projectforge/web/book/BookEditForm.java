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

package org.projectforge.web.book;

import org.apache.log4j.Logger;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.book.BookDO;
import org.projectforge.book.BookDao;
import org.projectforge.book.BookStatus;
import org.projectforge.book.BookType;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.layout.LayoutContext;

public class BookEditForm extends AbstractEditForm<BookDO, BookEditPage>
{
  private static final long serialVersionUID = 3881031215413525517L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BookEditForm.class);

  @SpringBean(name = "bookDao")
  private BookDao bookDao;

  protected BookFormRenderer renderer;

  public BookEditForm(BookEditPage parentPage, BookDO data)
  {
    super(parentPage, data);
    if (isNew() == true) {
      data.setStatus(BookStatus.PRESENT);
      data.setType(BookType.BOOK);
    }
    if (getData().getTaskId() == null) {
      bookDao.setTask(getData(), bookDao.getDefaultTaskId());
    }
    renderer = new BookFormRenderer(parentPage, this, new LayoutContext(this), parentPage.getBaseDao(), data);
  }

  @Override
  protected void init()
  {
    super.init();
    renderer.add();
  }

  @Override
  protected void validation()
  {
    super.validation();
    renderer.validation();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
