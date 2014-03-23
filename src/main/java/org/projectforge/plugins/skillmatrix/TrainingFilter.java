/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.skillmatrix;

import org.projectforge.core.BaseSearchFilter;

/**
 * @author Werner Feder (werner.feder@t-online.de)
 * 
 */
public class TrainingFilter extends BaseSearchFilter
{

  private static final long serialVersionUID = 698546539419443303L;

  private Integer trainingId, skillId;

  public TrainingFilter()
  {
  }

  public TrainingFilter(final BaseSearchFilter filter)
  {
    super(filter);
  }

  /**
   * @return the trainingId
   */
  public Integer getTrainingId()
  {
    return trainingId;
  }

  /**
   * @param trainingId the trainingId to set
   * @return this for chaining.
   */
  public TrainingFilter setTrainingId(final Integer trainingId)
  {
    this.trainingId = trainingId;
    return this;
  }

  /**
   * @return the skillId
   */
  public Integer getSkillId()
  {
    return skillId;
  }

  /**
   * @param skillId the skillId to set
   * @return this for chaining.
   */
  public TrainingFilter setSkillId(final Integer skillId)
  {
    this.skillId = skillId;
    return this;
  }
}
