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
