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

package org.projectforge.web.core;

import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskTree;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractStandardFormPage;

public class SearchPage extends AbstractStandardFormPage implements ISelectCallerPage
{
  private static final long serialVersionUID = -8416731462457080883L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SearchPage.class);

  private final SearchForm form;

  @SpringBean(name = "taskTree")
  private TaskTree taskTree;

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  public SearchPage(final PageParameters parameters)
  {
    super(parameters);
    form = new SearchForm(this);
    body.add(form);
    form.init();
    body.add(new SearchAreaPanel("results", form.filter));
  }

  @Override
  public void cancelSelection(final String property)
  {
  }

  public void select(final String property, final Object selectedValue)
  {
    if ("taskId".equals(property) == true) {
      final TaskDO task = taskTree.getTaskById((Integer) selectedValue);
      form.filter.setTask(task);
    } else if ("userId".equals(property) == true) {
      final PFUserDO user = userGroupCache.getUser((Integer) selectedValue);
      form.filter.setModifiedByUser(user);
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  public void unselect(final String property)
  {
    if ("taskId".equals(property) == true) {
      form.filter.setTask(null);
    } else {
      log.error("Property '" + property + "' not supported for unselection.");
    }
  }

  @Override
  public void renderHead(final IHeaderResponse response)
  {
    super.renderHead(response);
    response.renderJavaScriptReference("scripts/zoom.js");
  }

  @Override
  protected String getTitle()
  {
    return getString("search.title");
  }
}
