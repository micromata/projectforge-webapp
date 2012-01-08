/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

package org.projectforge.web.humanresources;

import java.sql.Date;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.calendar.DayHolder;
import org.projectforge.common.DateHelper;
import org.projectforge.common.DateHolder;
import org.projectforge.common.NumberHelper;
import org.projectforge.humanresources.HRPlanningDO;
import org.projectforge.humanresources.HRPlanningDao;
import org.projectforge.humanresources.HRPlanningEntryDO;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractBasePage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.EditPage;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.components.ContentMenuEntryPanel;

/**
 * 
 * @author Mario Groß (m.gross@micromata.de)
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@EditPage(defaultReturnPage = HRPlanningListPage.class)
public class HRPlanningEditPage extends AbstractEditPage<HRPlanningDO, HRPlanningEditForm, HRPlanningDao> implements ISelectCallerPage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HRPlanningEditPage.class);

  private static final long serialVersionUID = -8192471994161712577L;

  private static final String SESSION_KEY_RECENT_WEEK = "HRPlanningEditPage.recentWeek";

  private HRPlanningDO predecessor;

  @SpringBean(name = "hrPlanningDao")
  private HRPlanningDao hrPlanningDao;

  public HRPlanningEditPage(PageParameters parameters)
  {
    super(parameters, "hr.planning");
    final Integer userId = parameters.getAsInteger(WebConstants.PARAMETER_USER_ID);
    final Long millis = parameters.getAsLong(WebConstants.PARAMETER_DATE);
    final java.sql.Date week;
    if (millis != null) {
      week = new DayHolder(new Date(millis)).getSQLDate();
    } else {
      week = null;
    }
    HRPlanningDO planning = null;
    if (userId != null && week != null) {
      // Check if there exists already an entry (deleted or not):
      planning = hrPlanningDao.getEntry(userId, week);
    }
    if (planning != null) {
      super.init(planning);
    } else {
      super.init();
    }
    addTopMenuPanel();
    if (userId != null) {
      getBaseDao().setUser(getData(), userId);
    }
    if (week != null) {
      getData().setWeek(week);
    }
    if (getData().getWeek() != null) {
      final DateHolder date = new DateHolder(getData().getWeek(), DateHelper.UTC, Locale.GERMANY);
      if (date.isBeginOfWeek() == false) {
        date.setBeginOfWeek();
        getData().setWeek(date.getSQLDate());
      }
    } else {
      // Get week of last edited entry as default.
      final Object obj = getUserPrefEntry(SESSION_KEY_RECENT_WEEK);
      if (obj instanceof Long) {
        getData().setWeek(new java.sql.Date((Long) obj));
      }
    }
    if (userId != null) {
      // Get the entry from the predecessor week:
      final DayHolder dh = new DayHolder(getData().getWeek());
      dh.add(Calendar.WEEK_OF_YEAR, -1);
      predecessor = hrPlanningDao.getEntry(userId, dh.getSQLDate());
    }
  }

  @Override
  protected HRPlanningDao getBaseDao()
  {
    return hrPlanningDao;
  }

  @Override
  protected HRPlanningEditForm newEditForm(AbstractEditPage< ? , ? , ? > parentPage, HRPlanningDO data)
  {
    return new HRPlanningEditForm(this, data);
  }

  @Override
  public AbstractBasePage afterSaveOrUpdate()
  {
    putUserPrefEntry(SESSION_KEY_RECENT_WEEK, getData().getWeek().getTime(), true); // Store as recent date.
    return null;
  }

  @SuppressWarnings("serial")
  protected void addTopMenuPanel()
  {
    final Link< ? > copyFromPredecessorButton = new Link<Object>("link") {
      public void onClick()
      {
        if (predecessor != null && predecessor.getEntries() != null) {
          final Iterator<HRPlanningEntryDO> it = getData().getEntries().iterator();
          while (it.hasNext() == true) {
            if (it.next().isEmpty() == true) {
              it.remove();
            }
          }
          for (final HRPlanningEntryDO entry : predecessor.getEntries()) {
            getData().addEntry(entry.newClone());
          }
        }
        predecessor = null;
        form.refresh();
      };
    };
    final ContentMenuEntryPanel copyFromPredecessorMenuPanel = new ContentMenuEntryPanel(getNewContentMenuChildId(), copyFromPredecessorButton,
        getString("hr.planning.entry.copyFromPredecessor")) {
      @Override
      public boolean isVisible()
      {
        return (predecessor != null);
      }
    };
    contentMenuEntries.add(copyFromPredecessorMenuPanel);
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#select(java.lang.String, java.lang.Integer)
   */
  public void select(String property, Object selectedValue)
  {
    if (property.startsWith("projektId:") == true) {
      final Integer idx = NumberHelper.parseInteger(property.substring(property.indexOf(':') + 1));
      final HRPlanningEntryDO entry = getData().getEntry(idx);
      hrPlanningDao.setProjekt(entry, (Integer) selectedValue);
    } else if ("weekDate".equals(property) == true) {
      // startDate is automatically the beginning of the chosen week
      final Date date = (Date) selectedValue;
      if (date != null) {
        getData().setFirstDayOfWeek(date);
      }
      form.weekDatePanel.markModelAsChanged();
    } else if ("userId".equals(property) == true) {
      getBaseDao().setUser(getData(), (Integer) selectedValue);
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#unselect(java.lang.String)
   */
  public void unselect(String property)
  {
    if (property.startsWith("projektId:") == true) {
      final Integer idx = NumberHelper.parseInteger(property.substring(property.indexOf(':') + 1));
      final HRPlanningEntryDO entry = getData().getEntry(idx);
      entry.setProjekt(null);
      // form.refresh();
    } else if ("userId".equals(property) == true) {
      getData().setUser(null);
      // form.refresh();
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#cancelSelection(java.lang.String)
   */
  public void cancelSelection(String property)
  {
    // Do nothing.
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
