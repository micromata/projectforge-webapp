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
import org.projectforge.orga.PosteingangDO;
import org.projectforge.orga.PosteingangDao;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.EditPage;


@EditPage(defaultReturnPage = PosteingangListPage.class)
public class PosteingangEditPage extends AbstractEditPage<PosteingangDO, PosteingangEditForm, PosteingangDao> implements ISelectCallerPage
{
  private static final long serialVersionUID = 4375220914096256551L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PosteingangEditPage.class);

  @SpringBean(name = "posteingangDao")
  private PosteingangDao posteingangDao;

  public PosteingangEditPage(PageParameters parameters)
  {
    super(parameters, "orga.posteingang");
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
  protected PosteingangDao getBaseDao()
  {
    return posteingangDao;
  }

  @Override
  protected PosteingangEditForm newEditForm(AbstractEditPage< ? , ? , ? > parentPage, PosteingangDO data)
  {
    return new PosteingangEditForm(this, data);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
