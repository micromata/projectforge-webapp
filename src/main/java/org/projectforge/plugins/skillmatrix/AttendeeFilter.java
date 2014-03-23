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
public class AttendeeFilter extends BaseSearchFilter
{
  private static final long serialVersionUID = 1278054558397436842L;

  private Integer attendeeId, trainingId;

  public AttendeeFilter()
  {
  }

  public AttendeeFilter(final BaseSearchFilter filter)
  {
    super(filter);
  }
  /**
   * @return the attendeeId
   */
  public Integer getAttendeeId()
  {
    return attendeeId;
  }

  /**
   * @param attendeeId the attendeeId to set
   * @return this for chaining.
   */
  public AttendeeFilter setAttendeeId(final Integer attendeeId)
  {
    this.attendeeId = attendeeId;
    return this;
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
  public AttendeeFilter setTrainingId(final Integer trainingId)
  {
    this.trainingId = trainingId;
    return this;
  }


}
