/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.skillmatrix;

import javax.persistence.Column;
import javax.persistence.Entity;
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
import org.projectforge.core.PropertyInfo;
import org.projectforge.core.UserPrefParameter;
import org.projectforge.database.Constants;
import org.projectforge.user.PFUserDO;

/**
 * This data object is the Java representation of a data-base entry of attendee.
 * @author Werner Feder (werner.feder@t-online.de)
 * 
 */
@Entity
@Indexed
@Table(name = "T_PLUGIN_SKILL_ATTENDEE")
public class AttendeeDO extends DefaultBaseDO
{
  private static final long serialVersionUID = -3676402473986512186L;

  @PropertyInfo(i18nKey = "plugins.skillmatrix.skilltraining.attendee.menu")
  @UserPrefParameter(i18nKey = "plugins.skillmatrix.skilltraining.attendee.menu")
  @IndexedEmbedded(depth = 1)
  private PFUserDO attendee;

  @PropertyInfo(i18nKey = "plugins.skillmatrix.skilltraining.training")
  @UserPrefParameter(i18nKey = "plugins.skillmatrix.skilltraining.training")
  @IndexedEmbedded(depth = 1)
  private TrainingDO training;

  @PropertyInfo(i18nKey = "plugins.skillmatrix.skill.description")
  @UserPrefParameter(i18nKey = "description", multiline = true)
  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String description;

  @PropertyInfo(i18nKey = "plugins.skillmatrix.skilltraining.rating")
  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String rating;

  @PropertyInfo(i18nKey = "plugins.skillmatrix.skilltraining.certificate")
  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String certificate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "attendee_fk")
  public PFUserDO getAttendee()
  {
    return attendee;
  }

  @Transient
  public Integer getAttendeeId()
  {
    return attendee != null ? attendee.getId() : null;
  }

  /**
   * @param skill
   * @return this for chaining.
   */
  public AttendeeDO setAttendee(final PFUserDO attendee)
  {
    this.attendee = attendee;
    return this;
  }

  @Transient
  public Integer getTrainingId()
  {
    return training != null ? training.getId() : null;
  }

  /**
   * @param skill
   * @return this for chaining.
   */
  public AttendeeDO setTraining(final TrainingDO training)
  {
    this.training = training;
    return this;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "training_fk")
  public TrainingDO getTraining()
  {
    return training;
  }

  @Column(length = Constants.LENGTH_TEXT)
  public String getDescription()
  {
    return description;
  }

  /**
   * @return this for chaining.
   */
  public AttendeeDO setDescription(final String description)
  {
    this.description = description;
    return this;
  }

  /**
   * @return the rating
   */
  public String getRating()
  {
    return rating;
  }

  /**
   * @param rating the rating to set
   * @return this for chaining.
   */
  public AttendeeDO setRating(final String rating)
  {
    this.rating = rating;
    return this;
  }

  /**
   * @return the certificate
   */
  public String getCertificate()
  {
    return certificate;
  }

  /**
   * @param certificate the certificate to set
   * @return this for chaining.
   */
  public AttendeeDO setCertificate(final String certificate)
  {
    this.certificate = certificate;
    return this;
  }

}
