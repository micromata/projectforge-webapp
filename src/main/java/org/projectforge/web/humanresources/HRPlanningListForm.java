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
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.ProjektDao;
import org.projectforge.humanresources.HRPlanningEntryDO;
import org.projectforge.humanresources.HRPlanningFilter;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.calendar.QuickSelectPanel;
import org.projectforge.web.fibu.ProjektSelectPanel;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.AbstractListForm;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.CheckBoxLabelPanel;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.TooltipImage;

/**
 * 
 * @author Mario Groß (m.gross@micromata.de)
 * 
 */
public class HRPlanningListForm extends AbstractListForm<HRPlanningListFilter, HRPlanningListPage>
{
  private static final long serialVersionUID = 3167681159669386691L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HRPlanningListForm.class);

  @SpringBean(name = "projektDao")
  private ProjektDao projektDao;

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  protected DatePanel startDate;

  protected DatePanel stopDate;

  @SuppressWarnings( { "serial"})
  @Override
  protected void init()
  {
    super.init();
    final boolean hasFullAccess = parentPage.hasFullAccess();
    final HRPlanningFilter filter = getSearchFilter();
    if (hasFullAccess == false) {
      filter.setUserId(getUser().getId());
    }
    startDate = new DatePanel("startDate", new PropertyModel<Date>(getSearchFilter(), "startTime"), DatePanelSettings.get().withCallerPage(
        parentPage).withSelectPeriodMode(true));
    filterContainer.add(startDate);

    stopDate = new DatePanel("stopDate", new PropertyModel<Date>(getSearchFilter(), "stopTime"), DatePanelSettings.get().withCallerPage(
        parentPage).withSelectPeriodMode(true));

    filterContainer.add(stopDate);
    final WebMarkupContainer projektRow = new WebMarkupContainer("projektRow");
    filterContainer.add(projektRow);
    if (hasFullAccess == true) {
      if (projektDao.hasSelectAccess(false) == true) {
        final ProjektSelectPanel projektSelectPanel = new ProjektSelectPanel("projekt", new Model<ProjektDO>() {
          @Override
          public ProjektDO getObject()
          {
            return projektDao.getById(filter.getProjektId());
          }
        }, parentPage, "projektId");
        projektRow.add(projektSelectPanel);
        projektSelectPanel.init();
      } else {
        projektRow.add(createInvisibleDummyComponent("projekt"));
      }
      final UserSelectPanel userSelectPanel = new UserSelectPanel("user", new Model<PFUserDO>() {
        @Override
        public PFUserDO getObject()
        {
          return userGroupCache.getUser(filter.getUserId());
        }
      }, parentPage, "userId");
      filterContainer.add(userSelectPanel);
      userSelectPanel.setDefaultFormProcessing(false); // No validation.
      userSelectPanel.init();
    } else {
      projektRow.add(createInvisibleDummyComponent("projekt"));
      filterContainer.add(new Label("user", getUser().getFullname()));
    }
    filterContainer.add(new CheckBoxLabelPanel("groupEntriesCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "groupEntries"),
        "hr.planning.filter.groupEntries").setSubmitOnChange());
    filterContainer.add(new CheckBoxLabelPanel("onlyMyProjectsCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "onlyMyProjects"),
        "hr.planning.filter.onlyMyProjects").setSubmitOnChange().setVisible(hasFullAccess));
    filterContainer.add(new CheckBoxLabelPanel("longFormatCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "longFormat"),
        "longFormat").setSubmitOnChange());
    filterContainer.add(new CheckBoxLabelPanel("deletedCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "deleted"), "onlyDeleted")
        .setSubmitOnChange().setVisible(hasFullAccess));
    {
      final SubmitLink clearPeriodButton = new SubmitLink("clearPeriod") {
        public void onSubmit()
        {
          getSearchFilter().setStartTime(null);
          getSearchFilter().setStopTime(null);
          clearInput();
          parentPage.refresh();
        };
      };
      filterContainer.add(clearPeriodButton);
      clearPeriodButton.add(new TooltipImage("clearPeriodImage", getResponse(), WebConstants.IMAGE_DATE_UNSELECT,
          getString("calendar.tooltip.unselectPeriod")));
    }
    final QuickSelectPanel quickSelectPanel = new QuickSelectPanel("quickSelect", parentPage, "quickSelect", startDate);
    filterContainer.add(quickSelectPanel);
    quickSelectPanel.init();

    filterContainer.add(new Label("calendarWeeks", new Model<String>() {
      @Override
      public String getObject()
      {
        return WicketUtils.getCalendarWeeks(HRPlanningListForm.this, filter.getStartTime(), filter.getStopTime());
      }
    }).setRenderBodyOnly(true));
    filterContainer.add(new Label("datesAsUTC", new Model<String>() {
      @Override
      public String getObject()
      {
        return WicketUtils.getUTCDates(filter.getStartTime(), filter.getStopTime());
      }
    }));

    final Label totalHoursLabel = new Label("totalHours", new Model<String>() {
      @Override
      public String getObject()
      {
        BigDecimal duration = new BigDecimal(0);
        if (parentPage.getList() != null) {
          for (HRPlanningEntryDO sheet : parentPage.getList()) {
            BigDecimal temp = sheet.getTotalHours();
            duration = duration.add(temp);
          }
        }
        return duration.toString();
      }
    });
    totalHoursLabel.setRenderBodyOnly(true);
    filterContainer.add(totalHoursLabel);
  }

  public HRPlanningListForm(HRPlanningListPage parentPage)
  {
    super(parentPage);
  }

  @Override
  protected HRPlanningListFilter newSearchFilterInstance()
  {
    return new HRPlanningListFilter();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
