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

import java.util.ArrayList;
import java.util.Collection;

import org.projectforge.core.I18nEnum;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public enum SkillRating implements I18nEnum
{
  UNKNOWN("unknown", 1), ZERO("zero", 2), LOW("low", 3), MIDDLE("middle", 4), HIGH("high", 5), EXPERT("expert", 6);

  private String key;

  private int ordering;

  /**
   * @return The full i18n key including the i18n prefix "plugins.skillmatrix.skillrating.rating.".
   */
  public String getI18nKey()
  {
    return "plugins.skillmatrix.skillrating.rating." + key;
  }

  /**
   * The key will be used e. g. for i18n.
   * @return
   */
  public String getKey()
  {
    return key;
  }

  private SkillRating(final String key, final int ordering)
  {
    this.key = key;
    this.ordering = ordering;
  }

  /**
   * 
   * @param rating
   * @return Returns a array of ratings, that are equal or higher than the param rating.
   */
  public static Object[] getRequiredExperienceValues(final SkillRating rating)
  {
    final Collection<SkillRating> values = new ArrayList<SkillRating>(5);
    // The missing breaks are intentionally: this way the key of the case itself and all the cases higher are added.
    switch (rating) {
      case UNKNOWN:
        values.add(UNKNOWN);
      case ZERO:
        values.add(ZERO);
      case LOW:
        values.add(LOW);
      case MIDDLE:
        values.add(MIDDLE);
      case HIGH:
        values.add(HIGH);
      case EXPERT:
        values.add(EXPERT);
    }
    return values.toArray();
  }

}
