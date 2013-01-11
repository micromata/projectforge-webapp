/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Store;
import org.projectforge.core.AbstractHistorizableBaseDO;
import org.projectforge.core.DefaultBaseDO;
import org.projectforge.core.Priority;
import org.projectforge.core.UserPrefParameter;
import org.projectforge.database.Constants;
import org.projectforge.task.TaskDO;
import org.projectforge.user.GroupDO;
import org.projectforge.user.PFUserDO;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@Entity
@Indexed
@Table(name = "T_PLUGIN_TODO")
public class ToDoDO extends DefaultBaseDO
{
  private static final long serialVersionUID = 4864250842083720210L;

  @UserPrefParameter(i18nKey = "plugins.todo.subject")
  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String subject;

  @UserPrefParameter(i18nKey = "plugins.todo.reporter")
  @IndexedEmbedded
  private PFUserDO reporter;

  @UserPrefParameter(i18nKey = "plugins.todo.assignee")
  @IndexedEmbedded
  private PFUserDO assignee;

  @UserPrefParameter(i18nKey = "task")
  @IndexedEmbedded(depth = 1)
  private TaskDO task;

  @UserPrefParameter(i18nKey = "group")
  @IndexedEmbedded(depth = 1)
  private GroupDO group;

  @UserPrefParameter(i18nKey = "description", multiline = true)
  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String description;

  @UserPrefParameter(i18nKey = "comment", multiline = true)
  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String comment;

  @UserPrefParameter(i18nKey = "plugins.todo.type")
  @Field(index = Index.UN_TOKENIZED, store = Store.NO)
  private ToDoType type;

  @UserPrefParameter(i18nKey = "plugins.todo.status")
  @Field(index = Index.UN_TOKENIZED, store = Store.NO)
  private ToDoStatus status;

  private boolean recent;

  @UserPrefParameter(i18nKey = "priority")
  @Field(index = Index.UN_TOKENIZED, store = Store.NO)
  private Priority priority;

  @Field(index = Index.UN_TOKENIZED)
  @DateBridge(resolution = Resolution.DAY)
  private Date dueDate;

  @Field(index = Index.UN_TOKENIZED)
  @DateBridge(resolution = Resolution.DAY)
  private Date resubmission;

  static {
    AbstractHistorizableBaseDO.putNonHistorizableProperty(ToDoDO.class, "recent");
  }

  @Column(length = Constants.LENGTH_TITLE)
  public String getSubject()
  {
    return subject;
  }

  /**
   * @param title
   * @return this for chaining.
   */
  public ToDoDO setSubject(final String subject)
  {
    this.subject = subject;
    return this;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reporter_fk")
  public PFUserDO getReporter()
  {
    return reporter;
  }

  @Transient
  public Integer getReporterId()
  {
    return reporter != null ? reporter.getId() : null;
  }

  /**
   * @param reporter
   * @return this for chaining.
   */
  public ToDoDO setReporter(final PFUserDO reporter)
  {
    this.reporter = reporter;
    return this;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assignee_fk")
  public PFUserDO getAssignee()
  {
    return assignee;
  }

  @Transient
  public Integer getAssigneeId()
  {
    return assignee != null ? assignee.getId() : null;
  }

  /**
   * @param assignee
   * @return this for chaining.
   */
  public void setAssignee(final PFUserDO assignee)
  {
    this.assignee = assignee;
  }

  /**
   * Optional task.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "task_id", nullable = true)
  public TaskDO getTask()
  {
    return task;
  }

  /**
   * @param task
   * @return this for chaining.
   */
  public ToDoDO setTask(final TaskDO task)
  {
    this.task = task;
    return this;
  }

  @Transient
  public Integer getTaskId()
  {
    return this.task != null ? task.getId() : null;
  }

  /**
   * Optional group.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id", nullable = true)
  public GroupDO getGroup()
  {
    return group;
  }

  public void setGroup(final GroupDO group)
  {
    this.group = group;
  }

  @Transient
  public Integer getGroupId()
  {
    return this.group != null ? group.getId() : null;
  }

  @Column(length = Constants.LENGTH_TEXT)
  public String getDescription()
  {
    return description;
  }

  /**
   * @return this for chaining.
   */
  public ToDoDO setDescription(final String description)
  {
    this.description = description;
    return this;
  }

  @Column(length = Constants.LENGTH_TEXT)
  public String getComment()
  {
    return comment;
  }

  /**
   * @return this for chaining.
   */
  public ToDoDO setComment(final String comment)
  {
    this.comment = comment;
    return this;
  }

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  public ToDoType getType()
  {
    return type;
  }

  /**
   * @return this for chaining.
   */
  public ToDoDO setType(final ToDoType type)
  {
    this.type = type;
    return this;
  }

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  public ToDoStatus getStatus()
  {
    return status;
  }

  /**
   * @return this for chaining.
   */
  public ToDoDO setStatus(final ToDoStatus status)
  {
    this.status = status;
    return this;
  }

  /**
   * After any modification of a to-do by other users than the assignee this flag is set to true. The assignee see in his menu a red number
   * showing the total number of recent to-do's. After displaying the to-do by the assignee the recent flag will be set to false.
   * @return true if any modification isn't seen by the assignee.
   */
  public boolean isRecent()
  {
    return recent;
  }

  /**
   * @param recent
   * @return this for chaining.
   */
  public ToDoDO setRecent(final boolean recent)
  {
    this.recent = recent;
    return this;
  }

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  public Priority getPriority()
  {
    return priority;
  }

  /**
   * @return this for chaining.
   */
  public ToDoDO setPriority(final Priority priority)
  {
    this.priority = priority;
    return this;
  }

  @Column(name = "due_date")
  public Date getDueDate()
  {
    return dueDate;
  }

  /**
   * @return this for chaining.
   */
  public ToDoDO setDueDate(final Date dueDate)
  {
    this.dueDate = dueDate;
    return this;
  }

  @Column
  public Date getResubmission()
  {
    return resubmission;
  }

  /**
   * @return this for chaining.
   */
  public ToDoDO setResubmission(final Date resubmission)
  {
    this.resubmission = resubmission;
    return this;
  }
}
