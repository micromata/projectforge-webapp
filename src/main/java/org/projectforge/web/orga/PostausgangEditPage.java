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

package org.projectforge.web.orga;

import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.calendar.DayHolder;
import org.projectforge.orga.PostType;
import org.projectforge.orga.PostausgangDO;
import org.projectforge.orga.PostausgangDao;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.EditPage;


@EditPage(defaultReturnPage = PostausgangListPage.class)
public class PostausgangEditPage extends AbstractEditPage<PostausgangDO, PostausgangEditForm, PostausgangDao> implements ISelectCallerPage
{
  private static final long serialVersionUID = 4375220914096256551L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PostausgangEditPage.class);

  @SpringBean(name = "postausgangDao")
  private PostausgangDao postausgangDao;

  public PostausgangEditPage(PageParameters parameters)
  {
    super(parameters, "orga.postausgang");
    init();
    if (isNew() == true) {
      getData().setDatum(new DayHolder().getSQLDate());
      getData().setType(PostType.BRIEF);
    }
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#select(java.lang.String, java.lang.Integer)
   */
  public void select(String property, Object selectedValue)
  {
    if ("datum".equals(property) == true) {
      final Date date = (Date) selectedValue;
      final java.sql.Date sqlDate = new java.sql.Date(date.getTime());
      getData().setDatum(sqlDate);
      form.datumPanel.markModelAsChanged();
    }
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#unselect(java.lang.String)
   */
  public void unselect(String property)
  {
    log.error("Property '" + property + "' not supported for selection.");
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#cancelSelection(java.lang.String)
   */
  public void cancelSelection(String property)
  {
    // Do nothing.
  }

  @Override
  protected PostausgangDao getBaseDao()
  {
    return postausgangDao;
  }

  @Override
  protected PostausgangEditForm newEditForm(AbstractEditPage< ? , ? , ? > parentPage, PostausgangDO data)
  {
    return new PostausgangEditForm(this, data);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
