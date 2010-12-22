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

package org.projectforge.web.humanresources;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.calendar.TimePeriod;
import org.projectforge.common.DateHolder;
import org.projectforge.common.NumberHelper;
import org.projectforge.core.NumberFormatter;
import org.projectforge.core.ReindexSettings;
import org.projectforge.database.DatabaseDao;
import org.projectforge.fibu.KundeDO;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.humanresources.HRDao;
import org.projectforge.humanresources.HRFilter;
import org.projectforge.humanresources.HRPlanningDO;
import org.projectforge.humanresources.HRPlanningEntryDO;
import org.projectforge.humanresources.HRViewData;
import org.projectforge.humanresources.HRViewUserData;
import org.projectforge.humanresources.HRViewUserEntryData;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.HtmlHelper;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.timesheet.TimesheetListPage;
import org.projectforge.web.user.UserFormatter;
import org.projectforge.web.user.UserPropertyColumn;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.AttributeAppendModifier;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;
import org.projectforge.web.wicket.WebConstants;

/**
 * 
 * @author Mario Groß (m.gross@micromata.de)
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@ListPage(editPage = HRPlanningEditPage.class)
public class HRListPage extends AbstractListPage<HRListForm, HRDao, HRViewUserData> implements ISelectCallerPage
{
  private static final long serialVersionUID = -718881597957595460L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HRListPage.class);

  @SpringBean(name = "hrDao")
  private HRDao hrDao;

  @SpringBean(name = "databaseDao")
  private DatabaseDao databaseDao;

  @SpringBean(name = "userFormatter")
  private UserFormatter userFormatter;

  private HRViewData hrViewData;

  private Fragment bottomFragment;

  private Long weekMillis;

  public HRListPage(PageParameters parameters)
  {
    super(parameters, "hr.planning");
    this.colspan = 4;
  }

  @Override
  protected void init()
  {
    recreateDataTable();
  }

  @SuppressWarnings("serial")
  private void recreateDataTable()
  {
    final Date date = form.getSearchFilter().getStartTime();
    weekMillis = date != null ? date.getTime() : null;
    if (dataTable != null) {
      form.remove(dataTable);
    }
    final List<IColumn<HRViewUserData>> columns = new ArrayList<IColumn<HRViewUserData>>();
    final CellItemListener<HRViewUserData> cellItemListener = new CellItemListener<HRViewUserData>() {
      public void populateItem(Item<ICellPopulator<HRViewUserData>> item, String componentId, IModel<HRViewUserData> rowModel)
      {
        final HRViewUserData entry = rowModel.getObject();
        final StringBuffer cssStyle = getCssStyle(entry.getPlanningId(), entry.isDeleted());
        if (cssStyle.length() > 0) {
          item.add(new AttributeModifier("style", true, new Model<String>(cssStyle.toString())));
        }
      }
    };
    columns.add(new UserPropertyColumn<HRViewUserData>(getString("timesheet.user"), "user.fullname", "user", cellItemListener) {
      @Override
      public void populateItem(final Item<ICellPopulator<HRViewUserData>> item, final String componentId,
          final IModel<HRViewUserData> rowModel)
      {
        final Integer planningId = rowModel.getObject().getPlanningId();
        final String[] params;
        if (planningId == null) {
          // Preset fields for adding new entry:
          final Integer userId = rowModel.getObject().getUserId();
          params = new String[] { WebConstants.PARAMETER_USER_ID, userId != null ? String.valueOf(userId) : null,
              WebConstants.PARAMETER_DATE, weekMillis != null ? String.valueOf(weekMillis) : null};
        } else {
          params = null;
        }
        item.add(new ListSelectActionPanel(componentId, rowModel, HRPlanningEditPage.class, planningId, HRListPage.this,
            getLabelString(rowModel), params));
        cellItemListener.populateItem(item, componentId, rowModel);
        addRowClick(item);
      }
    }.withUserFormatter(userFormatter));
    columns.add(new CellItemListenerPropertyColumn<HRViewUserData>(getString("sum"), "plannedDaysSum", "plannedDaysSum", cellItemListener) {
      @Override
      public void populateItem(Item<ICellPopulator<HRViewUserData>> item, String componentId, IModel<HRViewUserData> rowModel)
      {
        final HRViewUserData userData = rowModel.getObject();
        final HRFilter filter = form.getSearchFilter();
        addFragment(item, componentId, userData.getPlannedDaysSum(), userData.getActualDaysSum(), new Link<Object>("actualDaysLink") {
          @Override
          public void onClick()
          {
            // Redirect to time sheet list page and show the corresponding time sheets.
            final PageParameters parameters = new PageParameters();
            parameters.put(TimesheetListPage.PARAMETER_KEY_STORE_FILTER, false);
            parameters.put(TimesheetListPage.PARAMETER_KEY_START_TIME, filter.getStartTime().getTime());
            parameters.put(TimesheetListPage.PARAMETER_KEY_STOP_TIME, filter.getStopTime().getTime());
            parameters.put(TimesheetListPage.PARAMETER_KEY_USER_ID, userData.getUserId());
            final TimesheetListPage timesheetListPage = new TimesheetListPage(parameters);
            setResponsePage(timesheetListPage);
          }
        });
        item.add(new AttributeAppendModifier("style", new Model<String>("text-align: right;")));
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<HRViewUserData>(getString("rest"), "plannedDaysRestSum", "plannedDaysRestSum",
        cellItemListener) {
      @Override
      public void populateItem(Item<ICellPopulator<HRViewUserData>> item, String componentId, IModel<HRViewUserData> rowModel)
      {
        final HRViewUserData userData = rowModel.getObject();
        addLabel(item, componentId, userData.getPlannedDaysRestSum(), userData.getActualDaysRestSum());
        item.add(new AttributeAppendModifier("style", new Model<String>("text-align: right;")));
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    for (final ProjektDO project : getHRViewData().getProjects()) {
      columns.add(new CellItemListenerPropertyColumn<HRViewUserData>(project.getProjektIdentifierDisplayName(), null, null,
          cellItemListener) {
        @Override
        public void populateItem(Item<ICellPopulator<HRViewUserData>> item, String componentId, IModel<HRViewUserData> rowModel)
        {
          cellItemListener.populateItem(item, componentId, rowModel);
          final HRViewUserData userData = rowModel.getObject();
          final HRViewUserEntryData entry = userData.getEntry(project);
          if (entry == null) {
            item.add(createInvisibleDummyComponent(componentId));
            return;
          }
          final HRFilter filter = form.getSearchFilter();
          addFragment(item, componentId, entry.getPlannedDays(), entry.getActualDays(), new Link<Object>("actualDaysLink") {
            @Override
            public void onClick()
            {
              // Redirect to time sheet list page and show the corresponding time sheets.
              final PageParameters parameters = new PageParameters();
              parameters.put(TimesheetListPage.PARAMETER_KEY_STORE_FILTER, false);
              parameters.put(TimesheetListPage.PARAMETER_KEY_TASK_ID, project.getTaskId());
              parameters.put(TimesheetListPage.PARAMETER_KEY_START_TIME, filter.getStartTime().getTime());
              parameters.put(TimesheetListPage.PARAMETER_KEY_STOP_TIME, filter.getStopTime().getTime());
              parameters.put(TimesheetListPage.PARAMETER_KEY_USER_ID, userData.getUserId());
              final TimesheetListPage timesheetListPage = new TimesheetListPage(parameters);
              setResponsePage(timesheetListPage);
            }
          });
          item.add(new AttributeAppendModifier("style", new Model<String>("text-align: right;")));
        }
      });
    }
    for (final KundeDO customer : getHRViewData().getCustomers()) {
      columns
          .add(new CellItemListenerPropertyColumn<HRViewUserData>(customer.getKundeIdentifierDisplayName(), null, null, cellItemListener) {
            @Override
            public void populateItem(Item<ICellPopulator<HRViewUserData>> item, String componentId, IModel<HRViewUserData> rowModel)
            {
              cellItemListener.populateItem(item, componentId, rowModel);
              final HRViewUserEntryData entry = rowModel.getObject().getEntry(customer);
              if (entry == null) {
                item.add(createInvisibleDummyComponent(componentId));
                return;
              }
              addLabel(item, componentId, entry.getPlannedDays(), entry.getActualDays());
              item.add(new AttributeAppendModifier("style", new Model<String>("text-align: right;")));
            }
          });
    }
    dataTable = createDataTable(columns, "user.fullname", true);
    form.add(dataTable);
  }

  private void addFragment(final Item<ICellPopulator<HRViewUserData>> item, final String componentId, final BigDecimal plannedDays,
      final BigDecimal actualDays, final Link< ? > link)
  {
    final Fragment fragment = new Fragment(componentId, "cellFragment", HRListPage.this);
    item.add(fragment);
    final HRFilter filter = form.getSearchFilter();
    final BigDecimal planned = filter.isShowPlanning() == true ? plannedDays : null;
    final BigDecimal actual = filter.isShowBookedTimesheets() == true ? actualDays : null;
    final Label plannedDaysLabel = new Label("plannedDays", NumberFormatter.format(planned, 2));
    fragment.add(plannedDaysLabel.setRenderBodyOnly(true));
    if (NumberHelper.isNotZero(plannedDays) == false) {
      plannedDaysLabel.setVisible(false);
    }
    fragment.add(link);
    final Label actualDaysLabel = new Label("actualDays", "(" + NumberFormatter.format(actual, 2) + ")");
    link.add(actualDaysLabel.setRenderBodyOnly(true));
    if (NumberHelper.isNotZero(actualDays) == false) {
      link.setVisible(false);
    }
  }

  private void addLabel(final Item<ICellPopulator<HRViewUserData>> item, final String componentId, final BigDecimal plannedDays,
      final BigDecimal actualDays)
  {
    final HRFilter filter = form.getSearchFilter();
    final BigDecimal planned = filter.isShowPlanning() == true ? plannedDays : null;
    final BigDecimal actual = filter.isShowBookedTimesheets() == true ? actualDays : null;
    final StringBuffer buf = new StringBuffer();
    if (NumberHelper.isNotZero(plannedDays) == true) {
      buf.append(NumberFormatter.format(planned, 2));
    }
    if (NumberHelper.isNotZero(actualDays) == true) {
      buf.append(" (").append(NumberFormatter.format(actual, 2)).append(")");
    }
    item.add(new Label(componentId, buf.toString()));
  }

  /**
   * Get the current date (start date) and preset this date for the edit page.
   * @see org.projectforge.web.wicket.AbstractListPage#onNewEntryClick(org.apache.wicket.PageParameters)
   */
  @Override
  protected AbstractEditPage< ? , ? , ? > redirectToEditPage(PageParameters params)
  {
    if (params == null) {
      params = new PageParameters();
    }
    if (weekMillis != null) {
      params.add(WebConstants.PARAMETER_DATE, String.valueOf(weekMillis));
    }
    final AbstractEditPage< ? , ? , ? > editPage = super.redirectToEditPage(params);
    return editPage;
  }

  @Override
  protected void addBottomPanel()
  {
    recreateBottomPanel();
  }

  private void recreateBottomPanel()
  {
    if (bottomFragment != null) {
      form.remove(bottomFragment);
    }
    bottomFragment = new Fragment("bottomPanel", "bottomFragment", this);
    bottomFragment.setRenderBodyOnly(true);
    form.add(bottomFragment);
    final RepeatingView userRepeater = new RepeatingView("userRepeater");
    bottomFragment.add(userRepeater);
    final List<PFUserDO> unplannedUsers = hrDao.getUnplannedResources(getHRViewData());
    for (final PFUserDO user : unplannedUsers) {
      final WebMarkupContainer container = new WebMarkupContainer(userRepeater.newChildId());
      userRepeater.add(container);
      @SuppressWarnings("serial")
      final Link<Object> link = new Link<Object>("resourceLink") {
        @Override
        public void onClick()
        {
          final DateHolder date = new DateHolder(form.getSearchFilter().getStartTime());
          final Long millis = date.getSQLDate().getTime();
          final PageParameters pageParams = new PageParameters();
          pageParams.put(WebConstants.PARAMETER_USER_ID, String.valueOf(user.getId()));
          pageParams.put(WebConstants.PARAMETER_DATE, millis.toString());
          final HRPlanningEditPage page = new HRPlanningEditPage(pageParams);
          page.setReturnToPage(HRListPage.this);
          setResponsePage(page);
        }
      };
      container.add(link);
      link.add(new Label("user", HtmlHelper.escapeXml(userFormatter.formatUser(user)) + "<br/>").setEscapeModelStrings(false));
      if (form.getSearchFilter().isOnlyMyProjects() == true) {
        bottomFragment.setVisible(false);
      }
    }
  }

  @Override
  protected HRListForm newListForm(AbstractListPage< ? , ? , ? > parentPage)
  {
    return new HRListForm(this);
  }

  private HRViewData getHRViewData()
  {
    if (hrViewData == null) {
      hrViewData = hrDao.getResources(form.getSearchFilter());
    }
    return hrViewData;
  }

  @Override
  public List<HRViewUserData> getList()
  {
    if (hrViewData != null && list != null) {
      return list;
    }
    list = getHRViewData().getUserDatas();
    recreateDataTable();
    recreateBottomPanel();
    return list;
  }

  @Override
  protected HRDao getBaseDao()
  {
    return hrDao;
  }

  @SuppressWarnings("serial")
  @Override
  protected IModel<HRViewUserData> getModel(final HRViewUserData object)
  {
    return new Model<HRViewUserData>() {
      @Override
      public HRViewUserData getObject()
      {
        return object;
      }
    };
  }

  public void cancelSelection(String property)
  {
    // Do nothing.
  }

  public void select(String property, Object selectedValue)
  {
    if (property.startsWith("quickSelect.") == true) {
      final Date date = (Date) selectedValue;
      final DateHolder dateHolder = new DateHolder(date);
      form.getSearchFilter().setStartTime(dateHolder.getDate());
      if (property.endsWith(".month") == true) {
        dateHolder.setEndOfMonth();
      } else if (property.endsWith(".week") == true) {
        dateHolder.setEndOfWeek();
      } else {
        log.error("Property '" + property + "' not supported for selection.");
      }
      form.getSearchFilter().setStopTime(dateHolder.getDate());
      refresh();
    } else if ("startDate".equals(property) == true) {
      if (selectedValue instanceof Date) {
        // Date selected.
        final Date date = (Date) selectedValue;
        if (date != null) {
          form.getSearchFilter().setStartTime(date);
          refresh();
        }
      } else if (selectedValue instanceof TimePeriod) {
        // Period selected.
        final TimePeriod timePeriod = (TimePeriod) selectedValue;
        form.getSearchFilter().setTimePeriod(timePeriod);
        refresh();
      } else {
        log.error("Unknown date object: " + selectedValue);
      }
    } else if ("stopDate".equals(property) == true) {
      if (selectedValue instanceof Date) {
        // Date selected.
        // stopDate automatisch Ende der gewählten Woche
        final Date date = (Date) selectedValue;
        if (date != null) {
          form.getSearchFilter().setStopTime(date);
          refresh();
        }
      } else if (selectedValue instanceof TimePeriod) {
        // Period selected.
        final TimePeriod timePeriod = (TimePeriod) selectedValue;
        form.getSearchFilter().setTimePeriod(timePeriod);
        refresh();
      } else {
        log.error("Unknown date object: " + selectedValue);
      }
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  public void unselect(String property)
  {
    log.error("Property '" + property + "' not supported for selection.");
  }

  @Override
  public void refresh()
  {
    form.getSearchFilter().setStartTime(new DateHolder(form.getSearchFilter().getStartTime()).setBeginOfWeek().getDate());
    form.getSearchFilter().setStopTime(new DateHolder(form.getSearchFilter().getStopTime()).setEndOfWeek().getDate());
    form.startDate.markModelAsChanged();
    form.stopDate.markModelAsChanged();
    super.refresh();
    this.hrViewData = null;
  }

  @Override
  protected boolean providesOwnRebuildDatabaseIndex()
  {
    return true;
  }

  @Override
  protected void ownRebuildDatabaseIndex(final boolean onlyNewest)
  {
    final ReindexSettings settings = DatabaseDao.createReindexSettings(onlyNewest);
    databaseDao.rebuildDatabaseSearchIndices(HRPlanningDO.class, settings);
    databaseDao.rebuildDatabaseSearchIndices(HRPlanningEntryDO.class, settings);
  }
}
