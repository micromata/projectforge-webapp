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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.projectforge.web.calendar.CalendarFilter;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class TeamCalCalendarFilter extends CalendarFilter
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamCalCalendarFilter.class);

  private static final long serialVersionUID = -8318037558891653348L;

  static final String DEFAULT_COLOR = "#FAAF26";

  private static final Set<Integer> EMPTY_INT_SET = new HashSet<Integer>();

  private final List<TemplateEntry> templateEntries;

  private int activeTemplateEntryIndex = 0;

  private transient TemplateEntry activeTemplateEntry = null;

  public TeamCalCalendarFilter()
  {
    super();
    templateEntries = new ArrayList<TemplateEntry>();
  }

  /**
   * Try to find a previous used color for the given calendar in any entry of this filter. If found multiple ones, the newest one is used.
   * @param calId Id of the calendar to search for.
   * @return Previous used color for the given calendar or DEFAULT_COLOR if not found.
   */
  public String getUsedColor(final Integer calId)
  {
    String color = DEFAULT_COLOR;
    long lastEntry = 0;
    if (calId == null) {
      return color;
    }
    // intelligent color choose
    for (final TemplateEntry entry : templateEntries) {
      for (final TemplateCalendarProperties props : entry.getCalendarProperties()) {
        if (calId.equals(props.getCalId()) == true) {
          if (props.getMillisOfLastChange() > lastEntry) {
            lastEntry = props.getMillisOfLastChange();
            color = props.getColorCode();
          }
        }
      }
    }
    return color;
  }

  /**
   * @return the templateEntries
   */
  public List<TemplateEntry> getTemplateEntries()
  {
    return templateEntries;
  }

  public void add(final TemplateEntry entry)
  {
    synchronized (templateEntries) {
      templateEntries.add(entry);
      Collections.sort(templateEntries);
    }
  }

  /**
   * @return the activeTemplateEntryIndex
   */
  public int getActiveTemplateEntryIndex()
  {
    return activeTemplateEntryIndex;
  }

  /**
   * @param activeTemplateEntryIndex the activeTemplateEntryIndex to set
   * @return this for chaining.
   */
  public TeamCalCalendarFilter setActiveTemplateEntryIndex(final int activeTemplateEntryIndex)
  {
    this.activeTemplateEntryIndex = activeTemplateEntryIndex;
    // Force to get active template entry by new index:
    this.activeTemplateEntry = null;
    return this;
  }

  /**
   * @return the activeTemplateEntry
   */
  public TemplateEntry getActiveTemplateEntry()
  {
    if (this.activeTemplateEntry == null) {
      if (this.activeTemplateEntryIndex >= 0 && this.activeTemplateEntryIndex < this.templateEntries.size()) {
        this.activeTemplateEntry = this.templateEntries.get(this.activeTemplateEntryIndex);
        this.activeTemplateEntry.setDirty();
      }
    }
    return this.activeTemplateEntry;
  }

  /**
   * @param activeTemplateEntry the activeTemplateEntry to set
   * @return this for chaining.
   */
  public TeamCalCalendarFilter setActiveTemplateEntry(final TemplateEntry activeTemplateEntry)
  {
    int i = 0;
    for (final TemplateEntry entry : this.templateEntries) {
      if (entry.equals(activeTemplateEntry) == true) {
        this.activeTemplateEntryIndex = i;
        this.activeTemplateEntry = entry;
        this.activeTemplateEntry.setDirty();
        break;
      }
      i++;
    }
    return this;
  }

  public Set<Integer> getActiveVisibleCalendarIds()
  {
    if (getActiveTemplateEntry() != null) {
      return this.activeTemplateEntry.getVisibleCalendarIds();
    } else {
      if (EMPTY_INT_SET.isEmpty() == false) {
        log.error("************** Oups, dear developers, don't add entries to the empty HashSet returned by this method!!!!");
        EMPTY_INT_SET.clear();
      }
      return EMPTY_INT_SET;
    }
  }
}
