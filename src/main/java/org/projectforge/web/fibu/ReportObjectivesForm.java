/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.fibu;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.lang.Bytes;
import org.projectforge.common.DateHolder;
import org.projectforge.common.LabelValueBean;
import org.projectforge.common.NumberHelper;
import org.projectforge.core.CurrencyFormatter;
import org.projectforge.core.Priority;
import org.projectforge.fibu.kost.Bwa;
import org.projectforge.fibu.kost.BwaTable;
import org.projectforge.fibu.kost.BwaZeile;
import org.projectforge.fibu.kost.reporting.Report;
import org.projectforge.fibu.kost.reporting.ReportStorage;
import org.projectforge.web.HtmlHelper;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.PlainLabel;
import org.projectforge.web.wicket.components.SingleButtonPanel;

public class ReportObjectivesForm extends AbstractForm<ReportObjectivesFilter, ReportObjectivesPage>
{
  private static final long serialVersionUID = -2262096357903710703L;

  private static final String KEY_REPORT_FILTER = "ReportObjectivesForm:filter";

  protected FileUploadField fileUploadField;

  protected ReportObjectivesFilter filter;

  protected Priority priority = Priority.HIGH;

  private WebMarkupContainer uploadContainer, filterSettingsContainer, storageContainer;

  private Button clearButton;

  private DatePanel fromDatePanel, toDatePanel;

  public ReportObjectivesForm(final ReportObjectivesPage parentPage)
  {
    super(parentPage);
    // set this form to multipart mode (always needed for uploads!)
    setMultiPart(true);
    // Add one file input field
    setMaxSize(Bytes.megabytes(10));
  }

  @Override
  protected void validation()
  {
    final Date fromDate = fromDatePanel.getConvertedInput();
    final Date toDate = toDatePanel.getConvertedInput();
    if (toDate != null && fromDate != null && fromDate.after(toDate) == true) {
      addError("fibu.buchungssatz.error.invalidTimeperiod");
    }
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    filter = getFilter();
    add(uploadContainer = new WebMarkupContainer("uploadContainer"));
    uploadContainer.add(fileUploadField = new FileUploadField("fileInput"));
    final Button importReportObjectivesButton = new Button("button", new Model<String>(getString("import"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.importReportObjectivs();
      }
    };
    uploadContainer.add(new SingleButtonPanel("importReportObjectives", importReportObjectivesButton));
    add(filterSettingsContainer = new WebMarkupContainer("filterSettings") {
      @Override
      public boolean isVisible()
      {
        return storageContainer != null && storageContainer.isVisible();
      }
    });

    final Label timePeriodLabel = new Label("timePeriodLabel", getString("timePeriod"));
    filterSettingsContainer.add(timePeriodLabel);
    filterSettingsContainer.add(fromDatePanel = new DatePanel("fromDate", new PropertyModel<Date>(filter, "fromDate"), DatePanelSettings
        .get().withRequired(true)));
    WicketUtils.setLabel(fromDatePanel, timePeriodLabel);
    filterSettingsContainer.add(toDatePanel = new DatePanel("toDate", new PropertyModel<Date>(filter, "toDate"), DatePanelSettings.get()));

    final Button createReportButton = new Button("button", new Model<String>(getString("fibu.kost.reporting.createReport"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.createReport();
      }
    };
    createReportButton.add(WebConstants.BUTTON_CLASS_DEFAULT);
    setDefaultButton(createReportButton);
    filterSettingsContainer.add(new SingleButtonPanel("createReport", createReportButton));
    clearButton = new Button("button", new Model<String>(getString("fibu.kost.reporting.clearStorage"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.clear();
      }
    };
    filterSettingsContainer.add(new SingleButtonPanel("clear", clearButton));
    clearButton.add(WebConstants.BUTTON_CLASS_RESET);
  }

  @Override
  public void onBeforeRender()
  {
    refresh();
    super.onBeforeRender();
  }

  @SuppressWarnings("serial")
  protected void refresh()
  {
    final ReportStorage reportStorage = parentPage.getReportStorage();
    if (storageContainer != null) {
      remove(storageContainer);
    }
    add(storageContainer = new WebMarkupContainer("storageContainer"));
    if (reportStorage != null) {
      uploadContainer.setVisible(false);
      clearButton.setVisible(true);
    } else {
      uploadContainer.setVisible(true);
      clearButton.setVisible(false);
      storageContainer.setVisible(false);
      return;
    }
    final Report currentReport = reportStorage.getCurrentReport();
    final Report rootReport = reportStorage.getRoot();
    storageContainer.add(new Label("title", currentReport.getId() + " - " + currentReport.getTitle() + ": " + currentReport.getZeitraum()));
    if (currentReport != rootReport) {
      final WebMarkupContainer pathContainer = new WebMarkupContainer("path");
      storageContainer.add(pathContainer);
      final RepeatingView actionLinkRepeater = new RepeatingView("actionLinkRepeater");
      pathContainer.add(actionLinkRepeater);
      for (final Report ancestorReport : currentReport.getPath()) {
        final WebMarkupContainer actionLinkContainer = new WebMarkupContainer(actionLinkRepeater.newChildId());
        actionLinkRepeater.add(actionLinkContainer);
        actionLinkContainer.add(createReportLink("actionLink", reportStorage, ancestorReport.getId()));
      }
      pathContainer.add(new PlainLabel("reportId", currentReport.getId()));
    } else {
      storageContainer.add(new Label("path", "[invisible]").setVisible(false));
    }
    storageContainer.add(new PlainLabel("reportObjectiveId", currentReport.getReportObjective().getId()));
    final List<Report> childs = currentReport.getChilds();
    final RepeatingView childHeadColRepeater = new RepeatingView("childHeadColRepeater");
    storageContainer.add(childHeadColRepeater);
    storageContainer.add(new SubmitLink("showAccountingRecordsLink") {
      @Override
      public void onSubmit()
      {
        setResponsePage(new AccountingRecordListPage(AccountingRecordListPage.getPageParameters(currentReport.getId())));
      }
    });
    if (CollectionUtils.isNotEmpty(childs) == true) {
      for (final Report childReport : childs) {
        final WebMarkupContainer item = new WebMarkupContainer(childHeadColRepeater.newChildId());
        childHeadColRepeater.add(item);
        if (childReport.hasChilds() == true) {
          item.add(createReportLink("actionLink", reportStorage, childReport.getId()));
          item.add(new Label("childId", "[invisible]").setVisible(false));
        } else {
          item.add(new Label("actionLink", "[invisible]").setVisible(false));
          item.add(new PlainLabel("childId", childReport.getId()));
        }
        item.add(new SubmitLink("showAccountingRecordsLink") {
          @Override
          public void onSubmit()
          {
            setResponsePage(new AccountingRecordListPage(AccountingRecordListPage.getPageParameters(childReport.getId())));
          }
        });
      }
    }
    final RepeatingView rowRepeater = new RepeatingView("rowRepeater");
    storageContainer.add(rowRepeater);
    int row = 0;
    final BwaTable bwaTable = currentReport.getChildBwaTable(true);
    final Bwa firstBwa = bwaTable.getBwaList().get(0).getValue();
    for (final BwaZeile firstBwaZeile : firstBwa.getZeilen()) { // First BWA for getting meta data of BWA.
      if (priority.ordinal() > firstBwaZeile.getPriority().ordinal()) {
        // Don't show all business assessment rows (priority is here a kind of verbose level).
        continue;
      }
      final WebMarkupContainer rowContainer = new WebMarkupContainer(rowRepeater.newChildId());
      rowRepeater.add(rowContainer);
      rowContainer.add(new SimpleAttributeModifier("class", (row++ % 2 == 0) ? "even" : "odd"));
      rowContainer.add(new Label("zeileNo", String.valueOf(firstBwaZeile.getZeile())));
      StringBuffer buf = new StringBuffer();
      for (int i = 0; i < firstBwaZeile.getIndent(); i++) {
        buf.append("&nbsp;&nbsp;");
      }
      buf.append(HtmlHelper.escapeXml(firstBwaZeile.getBezeichnung()));
      rowContainer.add(new Label("description", buf.toString()).setEscapeModelStrings(false));
      final RepeatingView cellRepeater = new RepeatingView("cellRepeater");
      rowContainer.add(cellRepeater);
      int col = 0;
      for (final LabelValueBean<String, Bwa> lv : bwaTable.getBwaList()) {
        // So display the row for every BWA:
        final String reportId = lv.getLabel();
        final Bwa bwa = lv.getValue();
        final BwaZeile bwaZeile = bwa.getZeile(firstBwaZeile.getBwaZeileId());
        final WebMarkupContainer item = new WebMarkupContainer(cellRepeater.newChildId());
        cellRepeater.add(item);
        buf = new StringBuffer();
        buf.append("text-align: right; white-space: nowrap;");
        if (col++ == 0) {
          buf.append(" font-weight: bold;");
        }
        if (bwaZeile.getBwaWert().compareTo(BigDecimal.ZERO) < 0) {
          buf.append(" color: red;");
        }
        item.add(new SimpleAttributeModifier("style", buf.toString()));
        item.add(new PlainLabel("bwaWert", NumberHelper.isNotZero(bwaZeile.getBwaWert()) == true ? CurrencyFormatter.format(bwaZeile
            .getBwaWert()) : ""));
        item.add(new SubmitLink("showAccountingRecordsLink") {
          @Override
          public void onSubmit()
          {
            setResponsePage(new AccountingRecordListPage(AccountingRecordListPage.getPageParameters(reportId, bwaZeile.getZeile())));
          }
        });
      }
    }
  }

  @SuppressWarnings("serial")
  private Component createReportLink(final String id, final ReportStorage reportStorage, final String reportId)
  {
    return new SubmitLink(id) {
      @Override
      public void onSubmit()
      {
        parentPage.getReportStorage().setCurrentReport(reportId);
      }
    }.add(new PlainLabel("label", reportId));
  }

  protected ReportObjectivesFilter getFilter()
  {
    if (filter != null) {
      return filter;
    }
    filter = (ReportObjectivesFilter) parentPage.getUserPrefEntry(KEY_REPORT_FILTER);
    if (filter != null) {
      return filter;
    }
    filter = new ReportObjectivesFilter();
    final DateHolder day = new DateHolder();
    day.setBeginOfYear();
    filter.setFromDate(day.getDate());
    day.setEndOfYear();
    filter.setToDate(day.getDate());
    parentPage.putUserPrefEntry(KEY_REPORT_FILTER, filter, true);
    return filter;
  }
}
