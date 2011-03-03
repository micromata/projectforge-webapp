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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.projectforge.access.AccessType;
import org.projectforge.access.OperationType;
import org.projectforge.core.BaseDao;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.core.ConfigXml;
import org.projectforge.core.CurrencyFormatter;
import org.projectforge.core.DisplayHistoryEntry;
import org.projectforge.core.QueryFilter;
import org.projectforge.database.Table;
import org.projectforge.fibu.AuftragsPositionDO;
import org.projectforge.fibu.KostFormatter;
import org.projectforge.mail.Mail;
import org.projectforge.mail.SendMail;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskTree;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class ToDoDao extends BaseDao<ToDoDO>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ToDoDao.class);

  private static final String[] ADDITIONAL_SEARCH_FIELDS = new String[] { "reporter.username", "reporter.firstname", "reporter.lastname",
      "assignee.username", "assignee.firstname", "assignee.lastname", "task.title", "task.taskpath"};

  private Table table = new Table(ToDoDO.class);

  private DataSource dataSource;

  private UserDao userDao;
  
  private SendMail sendMail;

  private TaskTree taskTree;

  private ToDoCache toDoCache = new ToDoCache(this);

  public ToDoDao()
  {
    super(ToDoDO.class);
  }

  @Override
  protected String[] getAdditionalSearchFields()
  {
    return ADDITIONAL_SEARCH_FIELDS;
  }

  @Override
  public List<ToDoDO> getList(final BaseSearchFilter filter)
  {
    final ToDoFilter myFilter;
    if (filter instanceof ToDoFilter) {
      myFilter = (ToDoFilter) filter;
    } else {
      myFilter = new ToDoFilter(filter);
    }
    final QueryFilter queryFilter = new QueryFilter(myFilter);
    Collection<ToDoStatus> col = new ArrayList<ToDoStatus>(5);
    if (myFilter.isOpened() == true) {
      col.add(ToDoStatus.OPENED);
    }
    if (myFilter.isClosed() == true) {
      col.add(ToDoStatus.CLOSED);
    }
    if (myFilter.isPostponed() == true) {
      col.add(ToDoStatus.POSTPONED);
    }
    if (myFilter.isReopened() == true) {
      col.add(ToDoStatus.RE_OPENED);
    }
    if (myFilter.isInprogress() == true) {
      col.add(ToDoStatus.IN_PROGRESS);
    }
    if (col.size() > 0) {
      queryFilter.add(Restrictions.in("status", col));
    }
    queryFilter.addOrder(Order.desc("created"));
    return getList(queryFilter);
  }
  
  /**
   * Sends an e-mail to the projekt manager if exists and is not equals to the logged in user.
   * @param todo
   * @param operationType
   * @return
   */
  public boolean sendNotification(final ToDoDO todo, final OperationType operationType, final String requestUrl)
  {
    if (ConfigXml.getInstance().isSendMailConfigured() == false) {
      // Can't send e-mail because no send mail is configured.
      return false;
    }
    final PFUserDO assignee = todo.getAssignee();
    if (assignee == null) {
      return false;
    }
    if (hasAccess(todo, null, operationType, false) == false) {
      return false;
    }
    final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.instance();
    final Map<String, Object> data = new HashMap<String, Object>();
    data.put("todo", todo);
//    data.put("angebotsDatum", dateTimeFormatter.getFormattedDate(todo.getAngebotsDatum()));
//    data.put("bindungsFrist", dateTimeFormatter.getFormattedDate(todo.getBindungsFrist()));
//    data.put("beauftragungsDatum", dateTimeFormatter.getFormattedDate(todo.getBeauftragungsDatum()));
//    data.put("bemerkung", sendMail.formatHtml(todo.getBemerkung()));
//    data.put("statusBeschreibung", sendMail.formatHtml(todo.getStatusBeschreibung()));
//    data.put("requestUrl", requestUrl);
//    final List<Map<String, Object>> positions = new ArrayList<Map<String, Object>>();
//    data.put("positionen", positions);
//    if (CollectionUtils.isNotEmpty(todo.getPositionen()) == true) {
//      for (final AuftragsPositionDO pos : todo.getPositionen()) {
//        final Map<String, Object> map = new HashMap<String, Object>();
//        positions.add(map);
//        map.put("position", pos);
//        map.put("nettoSumme", CurrencyFormatter.format(todo.getNettoSumme()));
//      }
//    }
    final List<DisplayHistoryEntry> history = getDisplayHistoryEntries(todo);
    final List<Object[]> list = new ArrayList<Object[]>();
    int i = 0;
    for (DisplayHistoryEntry entry : history) {
      Object[] oArray = new Object[6];
      oArray[0] = dateTimeFormatter.getFormattedDateTime(entry.getTimestamp());
      oArray[1] = entry.getUser().getFullname();
      oArray[2] = entry.getEntryType();
      oArray[3] = entry.getPropertyName();
      oArray[4] = sendMail.formatHtml(entry.getNewValue());
      oArray[5] = sendMail.formatHtml(entry.getOldValue());
      list.add(oArray);
      if (++i >= 10) {
        break;
      }
    }
    data.put("history", list);
    final Mail msg = new Mail();
    msg.setTo(assignee);
    final String subject;
//    if (operationType == OperationType.INSERT) {
//      subject = "Auftrag #" + todo.getNummer() + " wurde angelegt.";
//    } else if (operationType == OperationType.DELETE) {
//      subject = "Auftrag #" + todo.getNummer() + " wurde gelöscht.";
//    } else {
//      subject = "Auftrag #" + todo.getNummer() + " wurde geändert.";
//    }
//    msg.setProjectForgeSubject(subject);
//    data.put("subject", subject);
    final String content = sendMail.renderJelly(msg, "mail/toDoNotification.html", data, assignee.getLocale());
    msg.setContent(content);
    msg.setContentType(Mail.CONTENTTYPE_HTML);
    return sendMail.send(msg);
  }



  @Override
  protected void afterSaveOrModify(ToDoDO obj)
  {
    toDoCache.setExpired(); // Force reload of the menu item counters for open to-do entrie.
  }

  public void setAssignee(final ToDoDO todo, final Integer userId)
  {
    final PFUserDO user = userDao.getOrLoad(userId);
    todo.setAssignee(user);
  }

  public void setReporter(final ToDoDO todo, final Integer userId)
  {
    final PFUserDO user = userDao.getOrLoad(userId);
    todo.setReporter(user);
  }
  
  public void setSendMail(final SendMail sendMail)
  {
    this.sendMail = sendMail;
  }

  public void setTask(final ToDoDO todo, final Integer taskId)
  {
    final TaskDO task = taskTree.getTaskById(taskId);
    todo.setTask(task);
  }

  /**
   * return Always true, no generic select access needed for book objects.
   * @see org.projectforge.core.BaseDao#hasSelectAccess()
   */
  @Override
  public boolean hasSelectAccess(boolean throwException)
  {
    return true;
  }

  /**
   * @see org.projectforge.core.BaseDao#hasAccess(Object, OperationType)
   */
  @Override
  public boolean hasAccess(ToDoDO obj, ToDoDO oldObj, OperationType operationType, boolean throwException)
  {
    // Reporter and Assignee have full access, otherwise task access is used if a task is given.
    return accessChecker.hasPermission(obj.getTaskId(), AccessType.TASKS, operationType, throwException);
  }

  /**
   * @see org.projectforge.core.BaseDao#hasUpdateAccess(Object, Object)
   */
  @Override
  public boolean hasUpdateAccess(ToDoDO obj, ToDoDO dbObj, boolean throwException)
  {
    Validate.notNull(dbObj);
    Validate.notNull(obj);
    if (accessChecker.hasPermission(obj.getTaskId(), AccessType.TASKS, OperationType.UPDATE, throwException) == false) {
      return false;
    }
    if (ObjectUtils.equals(dbObj.getTaskId(), obj.getTaskId()) == false) {
      // User moves the object to another task:
      if (accessChecker.hasPermission(obj.getTaskId(), AccessType.TASKS, OperationType.INSERT, throwException) == false) {
        // Inserting of object under new task not allowed.
        return false;
      }
      if (accessChecker.hasPermission(dbObj.getTaskId(), AccessType.TASKS, OperationType.DELETE, throwException) == false) {
        // Deleting of object under old task not allowed.
        return false;
      }
    }
    return true;
  }

  /**
   * Get the number of open to-do entries for the given user. Entries are open (in this context) when they're not deleted or closed. <br/>
   * The result is cached (therefore you can call this method very often).
   * @param userId If null then the current logged in user is assumed.
   * @return Number of open to-do entries.
   */
  public int getOpenToDoEntries(Integer userId)
  {
    if (userId == null) {
      userId = PFUserContext.getUserId();
    }
    return toDoCache.getOpenToDoEntries(userId);
  }

  /**
   * Called by ToDoCache to get the number of open entries for the given users.
   * @param userId
   * @return Number of open to-do entries.
   */
  int internalGetOpenEntries(final Integer userId)
  {
    final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    try {
      return jdbc.queryForInt("SELECT COUNT(*) FROM "
          + table.getName()
          + " where assignee_fk="
          + userId
          + " and status != 'CLOSED' and deleted=false");
    } catch (final Exception ex) {
      log.error(ex.getMessage(), ex);
      return 0;
    }
  }

  @Override
  public ToDoDO newInstance()
  {
    return new ToDoDO();
  }

  public void setDataSource(final DataSource dataSource)
  {
    this.dataSource = dataSource;
  }

  public void setUserDao(final UserDao userDao)
  {
    this.userDao = userDao;
  }

  public void setTaskTree(final TaskTree taskTree)
  {
    this.taskTree = taskTree;
  }
}
