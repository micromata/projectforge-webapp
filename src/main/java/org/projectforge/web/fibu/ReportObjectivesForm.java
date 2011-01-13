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

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Bytes;
import org.projectforge.common.DateHolder;
import org.projectforge.fibu.kost.reporting.Report;
import org.projectforge.fibu.kost.reporting.ReportStorage;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.components.PlainLabel;
import org.projectforge.web.wicket.components.SingleButtonPanel;

public class ReportObjectivesForm extends AbstractForm<ReportObjectivesFilter, ReportObjectivesPage>
{
  private static final long serialVersionUID = -2262096357903710703L;

  private static final String KEY_REPORT_FILTER = "ReportObjectivesForm:filter";

  protected FileUploadField fileUploadField;

  protected ReportObjectivesFilter filter;

  private WebMarkupContainer uploadContainer, filterSettingsContainer, storageContainer;

  private Button clearButton;

  public ReportObjectivesForm(final ReportObjectivesPage parentPage)
  {
    super(parentPage);
    // set this form to multipart mode (always needed for uploads!)
    setMultiPart(true);
    // Add one file input field
    setMaxSize(Bytes.megabytes(10));
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
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
    add(filterSettingsContainer = new WebMarkupContainer("filterSettingsContainer"));
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
    clearButton = new Button("button", new Model<String>(getString("clear"))) {
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
      final RepeatingView actionLinkRepeater = new RepeatingView("actionLinkRepeater");
      storageContainer.add(actionLinkRepeater);
      for (final Report ancestorReport : currentReport.getPath()) {
        final WebMarkupContainer actionLinkContainer = new WebMarkupContainer(actionLinkRepeater.newChildId());
        actionLinkRepeater.add(actionLinkContainer);
        actionLinkContainer.add(new SubmitLink("actionLink") {
          @Override
          public void onSubmit()
          {
            reportStorage.setCurrentReport(ancestorReport.getId());
          }
        }.add(new PlainLabel("label", ancestorReport.getId())));
      }
    } else {
      storageContainer.add(new Label("path", "[invisible]").setVisible(false));
    }
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
