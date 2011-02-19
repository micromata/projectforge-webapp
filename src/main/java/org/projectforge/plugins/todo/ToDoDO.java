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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
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
import org.projectforge.core.DefaultBaseDO;
import org.projectforge.database.Constants;
import org.projectforge.task.TaskDO;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.user.PFUserDO;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@Entity
@Indexed
@Table(name = "T_TODO")
public class ToDoDO extends DefaultBaseDO
{
  private static final long serialVersionUID = 4864250842083720210L;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String title;

  @IndexedEmbedded
  private PFUserDO reporter;

  @IndexedEmbedded
  private PFUserDO assignee;

  @IndexedEmbedded(depth = 1)
  private TaskDO task;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String comment;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String description;

  @Field(index = Index.UN_TOKENIZED, store = Store.NO)
  private ToDoType type;

  @Field(index = Index.UN_TOKENIZED)
  @DateBridge(resolution = Resolution.DAY)
  private Date resubmission;

  @Column(length = Constants.LENGHT_TITLE)
  public String getTitle()
  {
    return title;
  }

  /**
   * @param title
   * @return this for chaining.
   */
  public ToDoDO setTitle(String title)
  {
    this.title = title;
    return this;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reporter_fk")
  public PFUserDO getReporter()
  {
    return reporter;
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

  public ToDoDO setTask(final TaskDO task)
  {
    this.task = task;
    return this;
  }

  @Transient
  public Integer getTaskId()
  {
    if (this.task == null)
      return null;
    return task.getId();
  }
}
