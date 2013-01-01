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

package org.projectforge.plugins.lessonslearned;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Store;
import org.projectforge.core.DefaultBaseDO;
import org.projectforge.core.Priority;
import org.projectforge.core.UserPrefParameter;
import org.projectforge.database.Constants;
import org.projectforge.task.TaskDO;
import org.projectforge.user.PFUserDO;

/**
 * A skill usable for a skill matrix. Skills are buil
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@Entity
@Indexed
@Table(name = "T_PLUGIN_SKILL")
public class LessonsLearnedDO extends DefaultBaseDO
{
  //Aufnahme in Masterliste LessonsLearned oder BestPractice?     Aufnahme in Abschlussbericht
  // consecutive number
  private int proNumber;

  private String title;

  private TaskDO task;

  private boolean recurrence;

  private boolean processReviewRequired;

  @UserPrefParameter(i18nKey = "description", multiline = true)
  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String description;

  private String reason;

  @UserPrefParameter(i18nKey = "comment", multiline = true)
  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String comment;

  private String arrangements;

  private Priority priority;

  private PFUserDO responsibleUser;

  @Field(index = Index.UN_TOKENIZED)
  @DateBridge(resolution = Resolution.DAY)
  private Date dueDate;

  @Column(length = Constants.LENGTH_TEXT)
  public String getDescription()
  {
    return description;
  }

  /**
   * @return this for chaining.
   */
  public LessonsLearnedDO setDescription(final String description)
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
  public LessonsLearnedDO setComment(final String comment)
  {
    this.comment = comment;
    return this;
  }
}
