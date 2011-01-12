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

import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.lang.Bytes;
import org.projectforge.common.ImportStorage;
import org.projectforge.common.ImportedSheet;
import org.projectforge.common.StringHelper;
import org.projectforge.fibu.kost.Bwa;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.components.RadioButtonLabelPanel;
import org.projectforge.web.wicket.components.SingleButtonPanel;

public class DatevImportForm extends AbstractForm<DatevImportFilter, DatevImportPage>
{
  private static final long serialVersionUID = -4812284533159635654L;

  protected DatevImportFilter filter = new DatevImportFilter();

  protected FileUploadField fileUploadField;

  protected WebMarkupContainer storageContainer;

  protected WebMarkupContainer errorPropertiesTable;

  protected RepeatingView sheetRepeatingView;

  private Label bwaLabel, storageHeadingLabel;

  private Map<String, Set<Object>> errorProperties;

  private Bwa bwa;

  protected ImportStorage< ? > storage;

  public DatevImportForm(final DatevImportPage parentPage)
  {
    super(parentPage);
    // set this form to multipart mode (always needed for uploads!)
    setMultiPart(true);
    // Add one file input field
    add(fileUploadField = new FileUploadField("fileInput"));
    setMaxSize(Bytes.megabytes(1));
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();

    final RadioGroup<String> filterType = new RadioGroup<String>("filterType", new PropertyModel<String>(filter, "listType"));
    add(filterType);
    final RepeatingView radioButtonRepeater = new RepeatingView("repeater");
    radioButtonRepeater.add(new RadioButtonLabelPanel<String>(radioButtonRepeater.newChildId(), new Model<String>("all"),
        getString("filter.all")).setSubmitOnChange());
    radioButtonRepeater.add(new RadioButtonLabelPanel<String>(radioButtonRepeater.newChildId(), new Model<String>("modified"),
        getString("modified")).setSubmitOnChange());
    radioButtonRepeater.add(new RadioButtonLabelPanel<String>(radioButtonRepeater.newChildId(), new Model<String>("faulty"),
        getString("filter.faulty")).setSubmitOnChange());
    filterType.add(radioButtonRepeater);

    final Button importAccountListButton = new Button("button", new Model<String>(getString("finance.datev.importAccountList"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.importAccountList();
      }
    };
    add(new SingleButtonPanel("importAccountList", importAccountListButton));
    final Button importAccountingRecordsButton = new Button("button", new Model<String>(getString("finance.datev.importAccountingRecords"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.importAccountRecords();
      }
    };
    add(new SingleButtonPanel("importAccountingRecords", importAccountingRecordsButton));
    final Button clearStorageButton = new Button("button", new Model<String>(getString("finance.datev.clearStorage"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.clear();
      }
    };
    clearStorageButton.add(WebConstants.BUTTON_CLASS_RESET);
    add(new SingleButtonPanel("clearStorage", clearStorageButton) {
      @Override
      public boolean isVisible()
      {
        return storageContainer.isVisible();
      }
    });

    storageContainer = new WebMarkupContainer("storage");
    refresh();
    sheetRepeatingView = new RepeatingView("sheetRepeater");
    storageContainer.add(sheetRepeatingView);
  }

  protected void refresh()
  {
    if (storage == null) {
      storageContainer.setVisible(false);
      return;
    }
    storageContainer.setVisible(true);
    if (bwaLabel != null) {
      storageContainer.remove(bwaLabel);
    }
    if (errorPropertiesTable != null) {
      storageContainer.remove(errorPropertiesTable);
    }
    if (storageHeadingLabel != null) {
      storageContainer.remove(storageHeadingLabel);
    }
    storageContainer.add(new Label("storageHeading", "Import storage: " + storage != null ? storage.getFilename() : "")).setRenderBodyOnly(
        true);

    if (MapUtils.isNotEmpty(errorProperties) == true) {
      storageContainer.add(errorPropertiesTable = new WebMarkupContainer("errorPropertiesTable"));
      final RepeatingView errorPropertiesView = new RepeatingView("errorProperties");
      storageContainer.add(errorPropertiesView);
      for (final Map.Entry<String, Set<Object>> entry : errorProperties.entrySet()) {
        final WebMarkupContainer entryContainer = new WebMarkupContainer(errorPropertiesView.newChildId());
        errorPropertiesView.add(entryContainer);
        final StringBuffer buf = new StringBuffer();
        boolean first = true;
        for (final Object value : entry.getValue()) {
          first = StringHelper.append(buf, first, String.valueOf(value), ", ");
        }
        errorPropertiesView.add(new Label("propertyItems", buf.toString()));
      }
    } else if (bwa != null) {
      storageContainer.add(bwaLabel = new Label("bwa", bwa.toString()));
    }
    if (bwaLabel == null) {
      storageContainer.add(bwaLabel = new Label("bwa", "[invisible]")).setVisible(false);
    }
    if (errorPropertiesTable == null) {
      storageContainer.add(errorPropertiesTable = new WebMarkupContainer("errorPropertiesTable")).setVisible(false);
    }
    if (storageHeadingLabel == null) {
      storageContainer.add(storageHeadingLabel = new Label("storageHeading", "[invisible]")).setVisible(false);
    }
    sheetRepeatingView.removeAll();
    if (storage.getSheets() != null) {
      for (final ImportedSheet< ? > sheet : storage.getSheets()) {
        addSheet(sheet);
      }
    }
  }

  protected void addSheet(final ImportedSheet< ? > sheet)
  {
    final WebMarkupContainer cont = new WebMarkupContainer(sheetRepeatingView.newChildId());
    sheetRepeatingView.add(cont);
    // sheetName">[Sheet_: ${sheet.name}&nbsp;<c:choose>
    // <c:when test="${sheet.reconciled == true}">(<fmt:message key="common.import.status.${sheet.status.key}" />
    // <c:if test="${sheet.numberOfCommittedElements >= 0}">: #${sheet.numberOfCommittedElements}</c:if>)</c:when>
    // <c:otherwise>(<fmt:message key="common.import.status.notReconciled" />)</c:otherwise>
    // </c:choose>
  }
}
