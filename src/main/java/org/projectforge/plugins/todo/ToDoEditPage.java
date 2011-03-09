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

import java.sql.Date;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.user.UserPrefEditPage;
import org.projectforge.web.wicket.AbstractAutoLayoutEditPage;
import org.projectforge.web.wicket.AbstractBasePage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.EditPage;
import org.projectforge.web.wicket.WicketUtils;

@EditPage(defaultReturnPage = ToDoListPage.class)
public class ToDoEditPage extends AbstractAutoLayoutEditPage<ToDoDO, ToDoEditForm, ToDoDao> implements ISelectCallerPage
{
  private static final long serialVersionUID = -5058143025817192156L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ToDoEditPage.class);

  @SpringBean(name = "toDoDao")
  private ToDoDao toDoDao;

  public ToDoEditPage(PageParameters parameters)
  {
    super(parameters, "plugins.todo");
    init();
    if (isNew() == true) {
      final ToDoDO pref = getToDoPrefData(false);
      if (pref != null) {
        getData().copyValuesFrom(pref, "id");
      } else {
        getData().setAssignee(PFUserContext.getUser());
        getData().setReporter(PFUserContext.getUser());
      }
    }
  }

  @Override
  protected void onAfterRender()
  {
    super.onAfterRender();
    if (ObjectUtils.equals(PFUserContext.getUserId(), getData().getAssigneeId()) == true) {
      // OK, user has now seen this to-do: delete recent flag:
      if (isNew() == false && getData().isRecent() == true) {
        getData().setRecent(false);
        toDoDao.update(getData());
      }
    }
  }

  @Override
  public AbstractBasePage afterSaveOrUpdate()
  {
    // Save to-do as recent to-do
     final ToDoDO pref = getToDoPrefData(true);
     pref.copyValuesFrom(getData(), "id");
    // Does the user want to store this to-do as template?
    if (form.renderer.sendNotification == true) {
      final String url = WicketUtils.getAbsoluteEditPageUrl(getRequest(), ToDoEditPage.class, getData().getId());
      toDoDao.sendNotification(form.getData(), url);
    }
    if (BooleanUtils.isTrue(form.renderer.saveAsTemplate) == true) {
      final UserPrefEditPage userPrefEditPage = new UserPrefEditPage(ToDoPlugin.USER_PREF_AREA, getData());
      userPrefEditPage.setReturnToPage(this.returnToPage);
      return userPrefEditPage;
    }
    return null;
  }

  /**
   * @param force If true then a pre entry is created if not exist.
   */
  protected ToDoDO getToDoPrefData(final boolean force)
  {
    ToDoDO pref = (ToDoDO) getUserPrefEntry(ToDoDO.class.getName());
    if (pref == null && force == true) {
      pref = new ToDoDO();
      putUserPrefEntry(ToDoDO.class.getName(), pref, true);
    }
    return pref;
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#select(java.lang.String, java.lang.Integer)
   */
  public void select(String property, Object selectedValue)
  {
    if ("reporterId".equals(property) == true) {
      toDoDao.setReporter(getData(), (Integer) selectedValue);
    } else if ("assigneeId".equals(property) == true) {
      toDoDao.setAssignee(getData(), (Integer) selectedValue);
    } else if ("taskId".equals(property) == true) {
      toDoDao.setTask(getData(), (Integer) selectedValue);
    } else if ("groupId".equals(property) == true) {
      toDoDao.setGroup(getData(), (Integer) selectedValue);
    } else if ("dueDate".equals(property) == true) {
      final Date date = (Date) selectedValue;
      getData().setDueDate(date);
      form.renderer.dueDatePanel.markModelAsChanged();
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#unselect(java.lang.String)
   */
  public void unselect(String property)
  {
    if ("reporterId".equals(property) == true) {
      getData().setReporter(null);
    } else if ("reporterId".equals(property) == true) {
      getData().setAssignee(null);
    } else if ("taskId".equals(property) == true) {
      getData().setTask(null);
    } else if ("groupId".equals(property) == true) {
      getData().setGroup(null);
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
  protected ToDoDao getBaseDao()
  {
    return toDoDao;
  }

  @Override
  protected ToDoEditForm newEditForm(AbstractEditPage< ? , ? , ? > parentPage, ToDoDO data)
  {
    return new ToDoEditForm(this, data);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
