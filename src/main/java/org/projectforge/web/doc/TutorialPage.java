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

package org.projectforge.web.doc;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.hibernate.criterion.Restrictions;
import org.projectforge.core.QueryFilter;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskDao;
import org.projectforge.task.TaskTree;
import org.projectforge.user.GroupDO;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.task.TaskEditPage;
import org.projectforge.web.user.UserEditForm;
import org.projectforge.web.user.UserEditPage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.MessagePage;

/**
 * Standard error page should be shown in production mode.
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class TutorialPage extends AbstractSecuredPage
{
  private static final String KEY_TYPE = "type";

  private static final String KEY_REF = "ref";

  private static final String TYPE_CREATE_USER = "createUser";

  private static final String TYPE_CREATE_TASK = "createTask";

  private String type;

  private String reference;

  @SpringBean(name = "userDao")
  private UserDao userDao;

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  @SpringBean(name = "taskDao")
  private TaskDao taskDao;

  @SpringBean(name = "taskTree")
  private TaskTree taskTree;

  public TutorialPage(final PageParameters params)
  {
    super(params);
    type = params.getString(KEY_TYPE);
    reference = params.getString(KEY_REF);
    if (TYPE_CREATE_USER.equals(type) == true) {
      createUser();
    } else if (TYPE_CREATE_TASK.equals(type) == true) {
      createTask();
    } else {
      setResponsePage(new MessagePage("tutorial.unknown"));
    }
  }

  private void createUser()
  {
    final String tutorialReference = getTutorialReference(reference);
    final QueryFilter filter = new QueryFilter();
    filter.add(Restrictions.ilike("description", "%" + tutorialReference + "%"));
    if (CollectionUtils.isNotEmpty(userDao.internalGetList(filter)) == true) {
      setResponsePage(new MessagePage("tutorial.objectAlreadyCreated"));
      return;
    }
    final PageParameters params = new PageParameters();
    final PFUserDO user;
    if ("linda".equals(reference) == true) {
      user = createUser("linda", "Evans", "Linda", "l.evans@javagurus.com", addTutorialReference("Project manager", tutorialReference));
      params.put(UserEditForm.TUTORIAL_ADD_GROUPS, addGroups(user, ProjectForgeGroup.PROJECT_MANAGER));
    } else if ("dave".equals(reference) == true) {
      user = createUser("dave", "Jones", "Dave", "d.jones@javagurus.com", addTutorialReference("Developer", tutorialReference));
    } else if ("betty".equals(reference) == true) {
      user = createUser("betty", "Brown", "Betty", "b.brown@javagurus.com", addTutorialReference("Developer", tutorialReference));
    } else {
      setResponsePage(new MessagePage("tutorial.unknown"));
      return;
    }
    params.put(AbstractEditPage.PARAMETER_KEY_DATA_PRESET, user);
    final UserEditPage userEditPage = new UserEditPage(params);
    setResponsePage(userEditPage);
  }

  private String getTutorialReference(final String reference)
  {
    return "{tutorial-ref:" + reference + "}";
  }

  private String addTutorialReference(final String text, final String tutorialReference)
  {
    if (StringUtils.isEmpty(text) == true) {
      return tutorialReference;
    } else {
      return text + "\n" + tutorialReference;
    }
  }

  private PFUserDO createUser(final String userName, final String lastName, final String firstName, final String email,
      final String description)
  {
    final PFUserDO user = new PFUserDO();
    user.setUsername(userName);
    user.setEmail(email);
    user.setLastname(lastName);
    user.setFirstname(firstName);
    user.setDescription(description);
    user.setPassword(UserEditForm.TUTORIAL_DEFAULT_PASSWORD);
    return user;
  }

  private List<Integer> addGroups(final PFUserDO user, ProjectForgeGroup... groups)
  {
    final List<Integer> groupsToAssign = new ArrayList<Integer>();
    final GroupDO group = userGroupCache.getGroup(ProjectForgeGroup.PROJECT_MANAGER);
    groupsToAssign.add(group.getId());
    return groupsToAssign;
  }

  private void createTask()
  {
    final String tutorialReference = getTutorialReference(reference);
    final QueryFilter filter = new QueryFilter();
    filter.add(Restrictions.ilike("description", "%" + tutorialReference + "%"));
    if (CollectionUtils.isNotEmpty(taskDao.internalGetList(filter)) == true) {
      setResponsePage(new MessagePage("tutorial.objectAlreadyCreated"));
      return;
    }
    final PageParameters params = new PageParameters();
    final TaskDO task;
    if ("JavaGurus".equals(reference) == true) {
      task = createTask(taskTree.getRootTaskNode().getTask(), "Java Gurus inc.", tutorialReference);
    } else {
      setResponsePage(new MessagePage("tutorial.unknown"));
      return;
    }
    params.put(AbstractEditPage.PARAMETER_KEY_DATA_PRESET, task);
    final TaskEditPage taskEditPage = new TaskEditPage(params);
    setResponsePage(taskEditPage);
  }

  private TaskDO createTask(final TaskDO parentTask, final String title, final String description)
  {
    final TaskDO task = new TaskDO();
    task.setParentTask(parentTask);
    task.setTitle(title);
    task.setDescription(description);
    return task;
  }

  @Override
  protected String getTitle()
  {
    return "TutorialRedirectPage";
  }
}
