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

package org.projectforge.plugins.skillmatrix;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.projectforge.core.DefaultBaseDO;
import org.projectforge.core.UserPrefParameter;
import org.projectforge.database.Constants;
import org.projectforge.user.PFUserDO;

/**
 * A skill usable for a skill matrix. Skills are buil
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@Entity
@Indexed
@Table(name = "T_PLUGIN_SKILL_RATING")
public class SkillRatingDO extends DefaultBaseDO
{
  private static final long serialVersionUID = 3049488664076249000L;

  @IndexedEmbedded
  private PFUserDO user;

  @IndexedEmbedded
  private SkillDO skill;

  @Field(index = Index.UN_TOKENIZED, store = Store.NO)
  private Integer sinceYear;

  @Enumerated(EnumType.STRING)
  @Field(index = Index.TOKENIZED, store = Store.NO)
  private SkillRating skillRating;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String certificates;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String trainingCourses;

  @UserPrefParameter(i18nKey = "description", multiline = true)
  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String description;

  @UserPrefParameter(i18nKey = "comment", multiline = true)
  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String comment;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_fk")
  public PFUserDO getUser()
  {
    return user;
  }

  @Transient
  public Integer getUserId()
  {
    return user != null ? user.getId() : null;
  }

  /**
   * @param user
   * @return this for chaining.
   */
  public SkillRatingDO setUser(final PFUserDO user)
  {
    this.user = user;
    return this;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "skill_fk")
  public SkillDO getSkill()
  {
    return skill;
  }

  @Transient
  public Integer getSkillId()
  {
    return skill != null ? skill.getId() : null;
  }

  /**
   * @param parent
   * @return this for chaining.
   */
  public SkillRatingDO setSkill(final SkillDO skill)
  {
    this.skill = skill;
    return this;
  }

  @Column(length = Constants.LENGTH_TEXT)
  public String getDescription()
  {
    return description;
  }

  /**
   * @return this for chaining.
   */
  public SkillRatingDO setDescription(final String description)
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
  public SkillRatingDO setComment(final String comment)
  {
    this.comment = comment;
    return this;
  }

  public Integer getSinceYear()
  {
    return sinceYear;
  }

  /**
   * @return this for chaining.
   */
  public SkillRatingDO setSinceYear(final Integer sinceYear)
  {
    this.sinceYear = sinceYear;
    return this;
  }

  @Column(length = Constants.LENGTH_SUBJECT)
  public String getCertificates()
  {
    return certificates;
  }

  /**
   * @return this for chaining.
   */
  public SkillRatingDO setCertificates(final String certificates)
  {
    this.certificates = certificates;
    return this;
  }

  @Column(length = Constants.LENGTH_SUBJECT)
  public String getTrainingCourses()
  {
    return trainingCourses;
  }

  /**
   * @return this for chaining.
   */
  public SkillRatingDO setTrainingCourses(final String trainingCourses)
  {
    this.trainingCourses = trainingCourses;
    return this;
  }

  public SkillRating getSkillRating()
  {
    return skillRating;
  }

  /**
   * @return this for chaining.
   */
  public SkillRatingDO setSkillRating(final SkillRating skillRating)
  {
    this.skillRating = skillRating;
    return this;
  }
}
