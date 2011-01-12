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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.lang.Bytes;
import org.projectforge.common.ImportStatus;
import org.projectforge.common.ImportStorage;
import org.projectforge.common.ImportedElement;
import org.projectforge.common.ImportedSheet;
import org.projectforge.common.StringHelper;
import org.projectforge.fibu.datev.DatevImportDao;
import org.projectforge.fibu.kost.Bwa;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.ImageDef;
import org.projectforge.web.wicket.PresizedImage;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.components.PlainLabel;
import org.projectforge.web.wicket.components.RadioButtonLabelPanel;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.springframework.util.CollectionUtils;

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

  @SuppressWarnings("serial")
  protected void addSheet(final ImportedSheet< ? > sheet)
  {
    final WebMarkupContainer cont = new WebMarkupContainer(sheetRepeatingView.newChildId());
    sheetRepeatingView.add(cont);
    StringBuffer buf = new StringBuffer();
    buf.append("Sheet: ").append(sheet.getName()).append(" ");
    if (sheet.isReconciled() == true) {
      buf.append(getString(sheet.getStatus().getI18nKey())).append(" ");
      if (sheet.getNumberOfCommittedElements() >= 0) {
        buf.append(": #").append(sheet.getNumberOfCommittedElements());
      }
    } else {
      buf.append(getString(ImportStatus.NOT_RECONCILED.getI18nKey()));
    }
    cont.add(new SubmitLink("toggle") {
      @Override
      public void onSubmit()
      {
        sheet.setOpen(!sheet.isOpen()); // Toggle open status.
      }
    });
    cont.add(new PresizedImage("zoomInImage", getResponse(), ImageDef.ZOOM_IN) {
      @Override
      public boolean isVisible()
      {
        return !sheet.isOpen();
      }
    });
    cont.add(new PresizedImage("zoomOutImage", getResponse(), ImageDef.ZOOM_OUT) {
      @Override
      public boolean isVisible()
      {
        return sheet.isOpen();
      }
    });
    buf = new StringBuffer();
    buf.append("Total=").append(sheet.getTotalNumberOfElements()).append(" ");
    if (sheet.getNumberOfNewElements() >= 0) {
      buf.append(" | New=<span style=\"color: red;\">").append(sheet.getNumberOfNewElements()).append("</span>");
    }
    if (sheet.getNumberOfModifiedElements() > 0) {
      buf.append(" | Modified=<span style=\"color: red;\">").append(sheet.getNumberOfModifiedElements()).append("</span>");
    }
    if (sheet.getNumberOfUnmodifiedElements() > 0) {
      buf.append(" | Unmodified=").append(sheet.getNumberOfUnmodifiedElements());
    }
    if (sheet.getNumberOfFaultyElements() > 0) {
      buf.append(" | Modified=<span style=\"color: red; font-weight: bold;\">").append(sheet.getNumberOfFaultyElements()).append("</span>");
    }
    cont.add(new PlainLabel("statistics", buf.toString()).setEscapeModelStrings(false));
    if (sheet.isReconciled() == false
        || sheet.getStatus().isIn(ImportStatus.IMPORTED, ImportStatus.NOTHING_TODO, ImportStatus.HAS_ERRORS) == true) {
      // <a onclick="javascript:submitSelectedEvent('reconcile', '${sheet.name}')" href="#"> Verproben_ </a>
    } else if (sheet.isReconciled() == true) {
      // <a onclick="javascript:submitSelectedEvent('commit', '${sheet.name}')" href="#"> Commit_ </a>
      // <a onclick="javascript:submitSelectedEvent('selectAll', '${sheet.name}')" href="#"> Select all_ </a>
      // <a onclick="javascript:submitSelectedEvent('deselectAll', '${sheet.name}')" href="#"> Unselect all_ </a>
    }
    if (sheet.isFaulty() == true) {
      // <a onclick="javascript:submitSelectedEvent('showErrorSummary', '${sheet.name}')" href="#"> show error summary_ </a>
    }
    if (getStorageType() == DatevImportDao.Type.BUCHUNGSSAETZE) {
      // <a onclick="javascript:submitSelectedEvent('showBWA', '${sheet.name}')" href="#"> show BWA_ </a>
    }
    addSheetTable(sheet, cont);
  }

  private void addSheetTable(final ImportedSheet< ? > sheet, final WebMarkupContainer container)
  {
    final WebMarkupContainer table = new WebMarkupContainer("sheetTable");
    container.add(table);
    final List< ? > elements = sheet.getElements();
    if (sheet.isOpen() == false || CollectionUtils.isEmpty(elements) == true) {
      table.setVisible(false);
      return;
    }
    final RepeatingView colHeadRepeater = new RepeatingView("colHeadRepeater");
    container.add(colHeadRepeater);
    if (getStorageType() == DatevImportDao.Type.KONTENPLAN) {
      colHeadRepeater.add(new Label(colHeadRepeater.newChildId(), getString("fibu.konto.nummer")));
      colHeadRepeater.add(new Label(colHeadRepeater.newChildId(), getString("fibu.konto.bezeichnung")));
    } else {
      colHeadRepeater.add(new Label(colHeadRepeater.newChildId(), getString("date")));
      colHeadRepeater.add(new Label(colHeadRepeater.newChildId(), getString("fibu.common.betrag")));
      colHeadRepeater.add(new Label(colHeadRepeater.newChildId(), getString("fibu.buchungssatz.text")));
      colHeadRepeater.add(new Label(colHeadRepeater.newChildId(), getString("fibu.buchungssatz.konto")));
      colHeadRepeater.add(new Label(colHeadRepeater.newChildId(), getString("fibu.buchungssatz.gegenKonto")));
      colHeadRepeater.add(new Label(colHeadRepeater.newChildId(), getString("fibu.kost1")));
      colHeadRepeater.add(new Label(colHeadRepeater.newChildId(), getString("fibu.kost2")));
    }
    colHeadRepeater.add(new Label(colHeadRepeater.newChildId(), getString("modifications")));
    colHeadRepeater.add(new Label(colHeadRepeater.newChildId(), getString("errors")));
    final RepeatingView rowRepeater = new RepeatingView("rowRepeater");
    container.add(rowRepeater);
    int col = 0;
    for (final Object rawElement : sheet.getElements()) {
      final ImportedElement< ? > element = (ImportedElement< ? >) rawElement;
      final String listType = filter.getListType();
      if ("all".equals(listType) == true //
          || ("faulty".equals(listType) == true && element.isFaulty() == true)//
          || ("modified".equals(listType) == true && (element.isNew() == true || element.isModified() == true || element.isFaulty() == true)) //
      ) {
        // Yes, show this element.
      } else {
        // Don't show this element.
        continue;
      }
      final WebMarkupContainer rowContainer = new WebMarkupContainer(rowRepeater.newChildId());
      rowRepeater.add(rowContainer);
      rowContainer.add(new SimpleAttributeModifier("class", (col++ % 2 == 0) ? "even" : "odd"));
      final String style;
      if (element.isFaulty() == true) {
        style = "color: red;";
      } else if (element.getOldValue() != null && element.getValue() == null) {
        style = "text-decoration: line-through;";
      } else {
        style = null;
      }
      if (getStorageType() == DatevImportDao.Type.KONTENPLAN) {
      } else {
      }
    }
  }

  private DatevImportDao.Type getStorageType()
  {
    if (storage == null) {
      return null;
    } else {
      return (DatevImportDao.Type) storage.getId();
    }
  }
}
