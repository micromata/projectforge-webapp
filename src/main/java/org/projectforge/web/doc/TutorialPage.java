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
import org.projectforge.access.AccessDao;
import org.projectforge.access.GroupTaskAccessDO;
import org.projectforge.core.BaseDao;
import org.projectforge.core.QueryFilter;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskDao;
import org.projectforge.task.TaskTree;
import org.projectforge.user.GroupDO;
import org.projectforge.user.GroupDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.access.AccessEditPage;
import org.projectforge.web.task.TaskEditPage;
import org.projectforge.web.user.GroupEditPage;
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
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TutorialPage.class);

  private static final String KEY_TYPE = "type";

  private static final String KEY_REF = "ref";

  private static final String TYPE_CREATE_ACCESS = "createAccess";

  private static final String TYPE_CREATE_GROUP = "createGroup";

  private static final String TYPE_CREATE_USER = "createUser";

  private static final String TYPE_CREATE_TASK = "createTask";

  private static final String REF_TASK_JAVA_GURUS = "JavaGurus";

  private static final String REF_TASK_ACME_WEBPORTAL = "ACME-WebPortal";

  private static final String REF_GROUP_JAVA_GURUS = "JavaGurusEmployees";

  private static final String REF_GROUP_ACME_WEBPORTAL = "ACME-WebPortal-Team";

  private static final String ACCESS_TEMPLATE_EMPLOYEE = "employee";

  private String type;

  private String reference;

  @SpringBean(name = "accessDao")
  private AccessDao accessDao;

  @SpringBean(name = "groupDao")
  private GroupDao groupDao;

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
    } else if (TYPE_CREATE_GROUP.equals(type) == true) {
      createGroup();
    } else if (TYPE_CREATE_TASK.equals(type) == true) {
      createTask();
    } else if (TYPE_CREATE_ACCESS.equals(type) == true) {
      createAccess();
    } else {
      log.warn("Unknown tutorial request: type=" + type);
      setResponsePage(new MessagePage("tutorial.unknown").setWarning(true));
    }
  }

  private void createUser()
  {
    final String tutorialReference = getTutorialReference(reference);
    if (doesEntryAlreadyExist(userDao, tutorialReference) == true) {
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
      log.warn("Unknown tutorial request: user=" + reference);
      setResponsePage(new MessagePage("tutorial.unknown").setWarning(true));
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
    if (doesEntryAlreadyExist(taskDao, tutorialReference) == true) {
      return;
    }
    final PageParameters params = new PageParameters();
    final TaskDO task;
    if (REF_TASK_JAVA_GURUS.equals(reference) == true) {
      task = createTask(taskTree.getRootTaskNode().getTask(), "Java Gurus inc.", tutorialReference);
    } else if (REF_TASK_ACME_WEBPORTAL.equals(reference) == true) {
      task = createTask(taskTree.getRootTaskNode().getTask(), "ACME web portal", tutorialReference);
    } else {
      log.warn("Unknown tutorial request: task=" + reference);
      setResponsePage(new MessagePage("tutorial.unknown").setWarning(true));
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

  private void createGroup()
  {
    final String tutorialReference = getTutorialReference(reference);
    if (doesEntryAlreadyExist(groupDao, tutorialReference) == true) {
      return;
    }
    final PageParameters params = new PageParameters();
    final GroupDO group;
    if (REF_GROUP_JAVA_GURUS.equals(reference) == true) {
      group = createGroup("JavaGurus employees", "linda", "dave", "betty");
    } else if (REF_GROUP_ACME_WEBPORTAL.equals(reference) == true) {
      group = createGroup("ACME web portal team", "linda", "dave", "betty");
    } else {
      log.warn("Unknown tutorial request: group=" + reference);
      setResponsePage(new MessagePage("tutorial.unknown").setWarning(true));
      return;
    }
    if (group != null) {
      group.setDescription(tutorialReference);
      params.put(AbstractEditPage.PARAMETER_KEY_DATA_PRESET, group);
      final GroupEditPage groupEditPage = new GroupEditPage(params);
      setResponsePage(groupEditPage);
    }
  }

  private GroupDO createGroup(final String name, final String... usernames)
  {
    final GroupDO group = new GroupDO();
    group.setName(name);
    if (usernames != null) {
      for (final String username : usernames) {
        final PFUserDO user = getRequiredUser(username);
        if (user == null) {
          return null;
        }
        group.addUser(user);
      }
    }
    return group;
  }

  private void createAccess()
  {
    final String tutorialReference = getTutorialReference(reference);
    if (doesEntryAlreadyExist(accessDao, tutorialReference) == true) {
      return;
    }
    final PageParameters params = new PageParameters();
    final GroupTaskAccessDO access;
    TaskDO task = null;
    GroupDO group = null;
    if ("JavaGurusEmployees".equals(reference) == true) {
      access = createAccess(getRequiredTask(REF_TASK_JAVA_GURUS), getRequiredGroup(REF_TASK_JAVA_GURUS), ACCESS_TEMPLATE_EMPLOYEE,
          tutorialReference);
    } else if ("ACME-WebPortal".equals(reference) == true) {
      access = createAccess(getRequiredTask(REF_TASK_ACME_WEBPORTAL), getRequiredGroup(REF_TASK_ACME_WEBPORTAL), ACCESS_TEMPLATE_EMPLOYEE,
          tutorialReference);
    } else {
      log.warn("Unknown tutorial request: task=" + reference);
      setResponsePage(new MessagePage("tutorial.unknown").setWarning(true));
      return;
    }
    if (task == null || group == null) {
      return;
    }
    params.put(AbstractEditPage.PARAMETER_KEY_DATA_PRESET, access);
    final AccessEditPage accessEditPage = new AccessEditPage(params);
    setResponsePage(accessEditPage);
  }

  private GroupTaskAccessDO createAccess(final TaskDO task, final GroupDO group, final String template, final String description)
  {
    final GroupTaskAccessDO access = new GroupTaskAccessDO();
    access.setTask(task);
    access.setGroup(group);
    if (ACCESS_TEMPLATE_EMPLOYEE.equals(template) == true) {
      access.employee();
    }
    access.setDescription(description);
    return access;
  }

  private boolean doesEntryAlreadyExist(final BaseDao< ? > dao, final String tutorialReference)
  {
    if (getEntry(dao, tutorialReference) != null) {
      setResponsePage(new MessagePage("tutorial.objectAlreadyCreated", tutorialReference).setWarning(true));
      return true;
    }
    return false;
  }

  private Object getEntry(final BaseDao< ? > dao, final String tutorialReference)
  {
    final QueryFilter filter = new QueryFilter();
    filter.add(Restrictions.ilike("description", "%" + tutorialReference + "%"));
    final List< ? > list = dao.internalGetList(filter);
    if (CollectionUtils.isNotEmpty(dao.internalGetList(filter)) == true) {
      return list.get(0);
    }
    return null;
  }

  private GroupDO getRequiredGroup(final String reference)
  {
    final GroupDO group = (GroupDO) getEntry(groupDao, getTutorialReference(reference));
    if (group == null) {
      setResponsePage(new MessagePage("tutorial.expectedGroupNotFound", reference).setWarning(true));
    }
    return group;
  }

  private PFUserDO getRequiredUser(final String reference)
  {
    final PFUserDO user = (PFUserDO) getEntry(userDao, getTutorialReference(reference));
    if (user == null) {
      setResponsePage(new MessagePage("tutorial.expectedUserNotFound", reference).setWarning(true));
    }
    return user;
  }

  private TaskDO getRequiredTask(final String reference)
  {
    final TaskDO task = (TaskDO) getEntry(taskDao, getTutorialReference(reference));
    if (task == null) {
      setResponsePage(new MessagePage("tutorial.expectedTaskNotFound", reference).setWarning(true));
    }
    return task;
  }

  @Override
  protected String getTitle()
  {
    return "TutorialRedirectPage";
  }
}
