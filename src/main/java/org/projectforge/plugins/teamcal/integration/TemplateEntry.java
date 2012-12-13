/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.teamcal.integration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.projectforge.plugins.teamcal.admin.TeamCalCache;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;

/**
 * Persist the settings of one calendar entry in the filter.
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * @author K. Reinhard (k.reinhard@micromata.de)
 * 
 */
public class TemplateEntry implements Serializable, Comparable<TemplateEntry>, Cloneable
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TemplateEntry.class);

  private static final long serialVersionUID = 409057949195992116L;

  private final Set<TemplateCalendarProperties> calendarProperties = new TreeSet<TemplateCalendarProperties>();

  private Set<Integer> visibleCalendarIds;

  private String name;

  Set<TemplateCalendarProperties> getCalendarProperties()
  {
    return calendarProperties;
  }

  /**
   * @return the name
   */
  public String getName()
  {
    return name;
  }

  /**
   * @param name the name to set
   * @return this for chaining.
   */
  public TemplateEntry setName(final String name)
  {
    this.name = name;
    return this;
  }

  public TemplateCalendarProperties addNewCalendarProperties(final TeamCalCalendarFilter filter, final Integer calId)
  {
    Validate.notNull(calId);
    final TemplateCalendarProperties props = new TemplateCalendarProperties();
    props.setCalId(calId);
    props.setColorCode(filter.getUsedColor(calId));
    this.calendarProperties.add(props);
    this.visibleCalendarIds = null;
    return props;
  }

  public void removeCalendarProperties(final Integer calId)
  {
    Validate.notNull(calId);
    final TemplateCalendarProperties props = getCalendarProperties(calId);
    if (props != null) {
      this.calendarProperties.remove(props);
    }
    this.visibleCalendarIds = null;
  }

  public String getColorCode(final Integer calendarId)
  {
    final TemplateCalendarProperties props = getCalendarProperties(calendarId);
    if (props == null) {
      return null;
    }
    return props.getColorCode();
  }

  public TemplateCalendarProperties getCalendarProperties(final Integer calendarId)
  {
    if (calendarId == null) {
      return null;
    }
    for (final TemplateCalendarProperties props : calendarProperties) {
      if (calendarId.equals(props.getCalId()) == true) {
        return props;
      }
    }
    return null;
  }

  public boolean contains(final Integer calendarId)
  {
    return getCalendarProperties(calendarId) != null;
  }

  public boolean isVisible(final Integer calendarId)
  {
    final TemplateCalendarProperties props = getCalendarProperties(calendarId);
    return props != null && props.isVisible();
  }

  /**
   * @return the visibleCalendarIds
   */
  public Set<Integer> getVisibleCalendarIds()
  {
    if (this.visibleCalendarIds == null) {
      this.visibleCalendarIds = new HashSet<Integer>();
      for (final TemplateCalendarProperties props : this.calendarProperties) {
        if (props.isVisible() == true) {
          this.visibleCalendarIds.add(props.getCalId());
        }
      }
    }
    return this.visibleCalendarIds;
  }

  /**
   * @return All contained calendars (visible and not visible ones).
   */
  public List<TeamCalDO> getCalendars()
  {
    final List<TeamCalDO> result = new ArrayList<TeamCalDO>();
    final TeamCalCache cache = TeamCalCache.getInstance();
    for (final TemplateCalendarProperties props : this.calendarProperties) {
      final TeamCalDO cal = cache.getCalendar(props.getCalId());
      if (cal != null) {
        result.add(cal);
      } else {
        log.error("Oups, calendar with id " + props.getCalId() + " not found in TeamCalCache.");
      }
    }
    return result;
  }

  /**
   * @return All contained calendars (visible and not visible ones).
   */
  public Set<Integer> getCalendarIds()
  {
    final Set<Integer> result = new HashSet<Integer>();
    for (final TemplateCalendarProperties props : this.calendarProperties) {
      result.add(props.getCalId());
    }
    return result;
  }

  /**
   * Should be called every time if new entries are added or the visibility of any entry was changed. After recalculation of visible
   * calendars is forced.
   */
  public void setDirty()
  {
    this.visibleCalendarIds = null;
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(final TemplateEntry o)
  {
    if (this == o) {
      return 0;
    }
    if (this.name == null) {
      if (o.name == null) {
        return 0;
      }
      return -1;
    }
    if (o.name == null) {
      return 1;
    }
    return this.name.compareTo(o.name);
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj)
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final TemplateEntry other = (TemplateEntry) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

  @Override
  public TemplateEntry clone()
  {
    //    try {
    final TemplateEntry cloned = new TemplateEntry(); // super.clone();
    cloned.name = this.name;
    for (final TemplateCalendarProperties props : this.calendarProperties) {
      final TemplateCalendarProperties clonedProps = props.clone();
      cloned.calendarProperties.add(clonedProps);
    }
    cloned.setDirty();
    return cloned;
    // } catch (final CloneNotSupportedException ex) {
    // log.error(ex.getMessage(), ex);
    // return null;
    // }
  }

  /**
   * For avoiding reload of Calendar if no changes are detected. (Was für'n Aufwand für so'n kleines Feature...)
   * @param filter
   * @return
   */
  public boolean isModified(final TemplateEntry other)
  {
    if (StringUtils.equals(this.name, other.name) == false) {
      return true;
    }
    if (calendarProperties.size() != other.calendarProperties.size()) {
      return true;
    }
    final Iterator<TemplateCalendarProperties> it1 = this.calendarProperties.iterator();
    final Iterator<TemplateCalendarProperties> it2 = other.calendarProperties.iterator();
    while (it1.hasNext() == true) {
      final TemplateCalendarProperties entry1 = it1.next();
      final TemplateCalendarProperties entry2 = it2.next();
      if (entry1.isModified(entry2) == true) {
        return true;
      }
    }
    return false;
  }
}
