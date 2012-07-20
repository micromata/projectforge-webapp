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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * @author Billy Duong (duong.billy@yahoo.de)
 * 
 */
@XStreamAlias("SkillRatingFilter")
public class SkillRatingFilter extends BaseSearchFilter
{
  private static final long serialVersionUID = -2566471514679020942L;

  @XStreamAsAttribute
  private SkillRating skillRating;

  public SkillRatingFilter()
  {
  }

  public SkillRatingFilter(final BaseSearchFilter baseSearchFilter)
  {
    super(baseSearchFilter);
  }

  public SkillRating getSkillRating()
  {
    return skillRating;
  }

  public void setSkillRating(final SkillRating skillRating)
  {
    this.skillRating = skillRating;
  }

}
