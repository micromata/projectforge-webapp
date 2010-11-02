/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.EnumeratedTypeConverter;
import net.sourceforge.stripes.validation.IntegerTypeConverter;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;

import org.apache.log4j.Logger;
import org.projectforge.common.DateHolder;
import org.projectforge.common.DatePrecision;
import org.projectforge.common.LabelValueBean;
import org.projectforge.common.StringHelper;
import org.projectforge.core.SearchArea;
import org.projectforge.core.SearchDao;
import org.projectforge.core.SearchFilter;
import org.projectforge.core.SearchResultData;
import org.projectforge.web.core.BaseAction;
import org.projectforge.web.core.ExtendedActionBean;
import org.projectforge.web.core.FlowScope;
import org.projectforge.web.stripes.DateTypeConverter;


/**
 * Search all the objects in the database including history etc.
 */
@StrictBinding
@UrlBinding("/secure/Search.action")
@BaseAction(jspUrl = "/WEB-INF/jsp/search.jsp")
public class SearchAction extends ExtendedActionBean
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SearchAction.class);

  private final String FLOW_FILTER_KEY = this.getClass().getName() + ":filter";

  private SearchDao searchDao;

  private Integer lastDays;

  private SearchActionFilter actionFilter;

  private List<List<SearchResultData>> resultLists;

  public List<LabelValueBean<String, Integer>> getMaxRows()
  {
    List<LabelValueBean<String, Integer>> list = new ArrayList<LabelValueBean<String, Integer>>();
    list.add(new LabelValueBean<String, Integer>("5", 5));
    list.add(new LabelValueBean<String, Integer>("10", 10));
    list.add(new LabelValueBean<String, Integer>("25", 25));
    list.add(new LabelValueBean<String, Integer>("50", 50));
    list.add(new LabelValueBean<String, Integer>("100", 100));
    list.add(new LabelValueBean<String, Integer>("250", 250));
    list.add(new LabelValueBean<String, Integer>("500", 500));
    return list;
  }

  @DefaultHandler
  @DontValidate
  public Resolution execute()
  {
    if (getContext().getRequestParameter("reset") != null) {
      getActionFilter().reset();
    }
    if (lastDays != null && lastDays >= 0) {
      DateHolder dh = new DateHolder(new Date(), DatePrecision.MILLISECOND, getContext().getLocale());
      dh.setEndOfDay();
      actionFilter.setStopTime(dh.getDate());
      dh.setBeginOfDay();
      dh.add(Calendar.DAY_OF_YEAR, -lastDays);
      actionFilter.setStartTime(dh.getDate());
      lastDays = -1;
      return getInputPage();
    }
    Resolution resolution;
    if ("expand".equals(getEventKey()) == true) {
      getActionFilter().setArea(SearchArea.valueOf(selectedValue));
      Integer maxRows = getActionFilter().getMaxRows();
      if (maxRows == null) {
        maxRows = 5;
      } else if (maxRows <= 5) {
        maxRows = 10;
      } else if (maxRows <= 10) {
        maxRows = 25;
      } else if (maxRows <= 25) {
        maxRows = 50;
      } else if (maxRows <= 50) {
        maxRows = 100;
      } else if (maxRows <= 100) {
        maxRows = 250;
      } else if (maxRows <= 250) {
        maxRows = 500;
      }
      getActionFilter().setMaxRows(maxRows);
      selectedValue = "";
      eventKey = "";
    } else if (StringHelper.startsWith(getEventKey(), "select.") == true) {
      // User has chosen to select/browse a value (e. g. task, date or user)
      resolution = processEvent(getActionFilter(), getEventKey().substring(7));
      if (resolution != null) {
        return resolution;
      }
    } else if (StringHelper.startsWith(getEventKey(), "selectDefault.") == true) {
      // User has clicked an event button for selecting default value.
      processSelectDefaultEvent(getActionFilter(), getEventKey().substring(14));
      log.info("Search with filter: " + actionFilter);
      return getInputPage();
    } else if (StringHelper.startsWith(getEventKey(), "unselect.") == true) {
      // User has clicked an unselect button (e. g. unselect task, unselect date or unselect user).
      processUnselectEvent(getActionFilter(), getEventKey().substring(9));
      log.info("Search with filter: " + actionFilter);
      return getInputPage();
    }
    FlowScope scope = getFlowScope(false);
    if (scope != null) {
      actionFilter = (SearchActionFilter) scope.get(FLOW_FILTER_KEY);
      if (actionFilter != null) {
        getLogger().debug("filter restored in execute: " + actionFilter);
      }
      scope.closeFlowScope(getContext().getRequest());
      processSelection(getActionFilter());
    }
    log.info("Search with filter: " + actionFilter);
    return getInputPage();
  }

  @DontValidate
  public Resolution reset()
  {
    getActionFilter().reset();
    return getInputPage();
  }

  /**
   * For auto selection of predefined time periods (last x days).
   */
  @Validate(converter = IntegerTypeConverter.class)
  public Integer getLastDays()
  {
    return lastDays;
  }

  public void setLastDays(Integer lastDays)
  {
    this.lastDays = lastDays;
  }

  public List<List<SearchResultData>> getResultLists()
  {
    if (resultLists == null) {
      resultLists = searchDao.getResultLists(actionFilter);
    }
    return resultLists;
  }

  protected Resolution getInputPage()
  {
    return new ForwardResolution(getJspUrl());
  }

  public void setSearchDao(SearchDao searchDao)
  {
    this.searchDao = searchDao;
  }

  @ValidateNestedProperties( { @Validate(field = "modifiedByUserId", converter = IntegerTypeConverter.class),
      @Validate(field = "area", converter = EnumeratedTypeConverter.class),
      @Validate(field = "maxRows", converter = IntegerTypeConverter.class),
      @Validate(field = "taskId", converter = IntegerTypeConverter.class),
      @Validate(field = "startTime", converter = DateTypeConverter.class),
      @Validate(field = "stopTime", converter = DateTypeConverter.class)})
  public SearchFilter getActionFilter()
  {
    if (actionFilter == null) {
      actionFilter = new SearchActionFilter();
    }
    return actionFilter;
  }

  public void setActionFilter(SearchActionFilter filter)
  {
    this.actionFilter = filter;
  }

  /**
   * Value in days.
   */
  public List<LabelValueBean<String, Integer>> getTimePeriodList()
  {
    List<LabelValueBean<String, Integer>> list = new ArrayList<LabelValueBean<String, Integer>>();
    list.add(new LabelValueBean<String, Integer>(getLocalizedString("pleaseChoose"), -1));
    list.add(new LabelValueBean<String, Integer>(getLocalizedMessage("search.today"), 0));
    list.add(new LabelValueBean<String, Integer>(getLocalizedMessage("search.lastDay"), 1));
    addDay(list, 3);
    addDay(list, 7);
    addDay(list, 14);
    addDay(list, 30);
    addDay(list, 60);
    addDay(list, 90);
    return list;
  }

  /**
   * Get search areas.
   * @see SearchArea
   */
  public List<LabelValueBean<String, SearchArea>> getAreas()
  {
    List<LabelValueBean<String, SearchArea>> list = new ArrayList<LabelValueBean<String, SearchArea>>();
    addArea(list, SearchArea.ALL, getLocalizedString("filter.all"));
    addArea(list, SearchArea.ADDRESS, getLocalizedString("address.addresses"));
    addArea(list, SearchArea.TIMESHEET, getLocalizedString("timesheet.timesheets"));
    addArea(list, SearchArea.TASK, getLocalizedString("task.tasks"));
    addArea(list, SearchArea.BOOK, getLocalizedString("book.books"));
    addArea(list, SearchArea.RECHNUNG, getLocalizedString("fibu.rechnung.rechnungen"));
    addArea(list, SearchArea.USER, getLocalizedString("user.users"));
    addArea(list, SearchArea.GROUP, getLocalizedString("group.groups"));
    addArea(list, SearchArea.ACCESS, getLocalizedString("access.list.title"));
    addArea(list, SearchArea.BUCHUNGSSATZ, getLocalizedString("fibu.buchungssaetze"));
    addArea(list, SearchArea.KOST1, getLocalizedString("fibu.kost1"));
    addArea(list, SearchArea.KOST2, getLocalizedString("fibu.kost2"));
    addArea(list, SearchArea.KOST2ART, getLocalizedString("fibu.kost2art.kost2arten"));
    addArea(list, SearchArea.KONTO, getLocalizedString("fibu.konto"));
    addArea(list, SearchArea.KUNDE, getLocalizedString("fibu.kunde.kunden"));
    addArea(list, SearchArea.PROJEKT, getLocalizedString("fibu.projekt.projekte"));
    return list;
  }

  private void addArea(List<LabelValueBean<String, SearchArea>> list, SearchArea area, String key)
  {
    if (searchDao.hasUserAccess(area) == true) {
      list.add(new LabelValueBean<String, SearchArea>(key, area));
    }
  }

  private void addDay(List<LabelValueBean<String, Integer>> list, int days)
  {
    list.add(new LabelValueBean<String, Integer>(getLocalizedMessage("search.lastDays", days), days));
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }

  @Override
  protected void storeToFlowScope()
  {
    storeFlowScopeObject(FLOW_FILTER_KEY, getActionFilter());
  }
}
