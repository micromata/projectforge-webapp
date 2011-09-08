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
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
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
import org.projectforge.core.CurrencyFormatter;
import org.projectforge.fibu.KontoDO;
import org.projectforge.fibu.KostFormatter;
import org.projectforge.fibu.datev.DatevImportDao;
import org.projectforge.fibu.kost.BuchungssatzDO;
import org.projectforge.fibu.kost.Bwa;
import org.projectforge.fibu.kost.Kost1DO;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.ImageDef;
import org.projectforge.web.wicket.PresizedImage;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.PlainLabel;
import org.projectforge.web.wicket.components.RadioButtonLabelPanel;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.springframework.util.CollectionUtils;

import de.micromata.hibernate.history.delta.PropertyDelta;

public class DatevImportForm extends AbstractForm<DatevImportFilter, DatevImportPage>
{
  private static final long serialVersionUID = -4812284533159635654L;

  protected DatevImportFilter filter = new DatevImportFilter();

  protected FileUploadField fileUploadField;

  protected WebMarkupContainer storageContainer;

  protected WebMarkupContainer errorPropertiesTable;

  protected RepeatingView sheetRepeatingView;

  private Label bwaLabel, storageHeadingLabel;

  protected Map<String, Set<Object>> errorProperties;

  protected Bwa bwa;

  protected transient ImportStorage< ? > storage;

  public DatevImportForm(final DatevImportPage parentPage)
  {
    super(parentPage);
    // set this form to multipart mode (always needed for uploads!)
    setMultiPart(true);
    // Add one file input field
    add(fileUploadField = new FileUploadField("fileInput"));
    setMaxSize(Bytes.megabytes(10));
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
    add(storageContainer);
    sheetRepeatingView = new RepeatingView("sheetRepeater");
    storageContainer.add(sheetRepeatingView);
  }

  @Override
  public void onBeforeRender()
  {
    refresh();
    super.onBeforeRender();
  }

  protected void refresh()
  {
    if (storage == null) {
      storage = (ImportStorage< ? >) parentPage.getUserPrefEntry(DatevImportPage.KEY_IMPORT_STORAGE);
    }
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
    storageContainer.add(
        storageHeadingLabel = new Label("storageHeading", "Import storage: " + storage != null ? storage.getFilename() : ""))
        .setRenderBodyOnly(true);

    storageContainer.add(errorPropertiesTable = new WebMarkupContainer("errorPropertiesTable"));
    if (MapUtils.isNotEmpty(errorProperties) == true) {
      final RepeatingView errorPropertiesView = new RepeatingView("errorProperties");
      errorPropertiesTable.add(errorPropertiesView);
      for (final Map.Entry<String, Set<Object>> entry : errorProperties.entrySet()) {
        final WebMarkupContainer entryContainer = new WebMarkupContainer(errorPropertiesView.newChildId());
        errorPropertiesView.add(entryContainer);
        entryContainer.add(new Label("propertyKey", entry.getKey()));
        final StringBuffer buf = new StringBuffer();
        boolean first = true;
        for (final Object value : entry.getValue()) {
          first = StringHelper.append(buf, first, String.valueOf(value), ", ");
        }
        entryContainer.add(new Label("propertyItems", buf.toString()));
      }
    } else {
      errorPropertiesTable.setVisible(false);
    }
    if (bwa != null) {
      storageContainer.add(bwaLabel = new Label("bwa", bwa.toString()));
    } else {
      storageContainer.add(bwaLabel = (Label) new Label("bwa", "[invisible]").setVisible(false));
    }
    if (errorPropertiesTable == null) {
      storageContainer.add(errorPropertiesTable = new WebMarkupContainer("errorPropertiesTable")).setVisible(false);
    }
    if (storageHeadingLabel == null) {
      storageContainer.add(storageHeadingLabel = (Label) new Label("storageHeading", "[invisible]").setVisible(false));
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
    cont.add(new Label("sheetName", buf.toString()));
    final SubmitLink toggleLink = new SubmitLink("toggle") {
      @Override
      public void onSubmit()
      {
        sheet.setOpen(!sheet.isOpen()); // Toggle open status.
      }
    };
    cont.add(toggleLink);
    toggleLink.add(new PresizedImage("zoomInImage", getResponse(), ImageDef.ZOOM_IN) {
      @Override
      public boolean isVisible()
      {
        return !sheet.isOpen();
      }
    });
    toggleLink.add(new PresizedImage("zoomOutImage", getResponse(), ImageDef.ZOOM_OUT) {
      @Override
      public boolean isVisible()
      {
        return sheet.isOpen();
      }
    });
    buf = new StringBuffer();
    buf.append("Total=").append(sheet.getTotalNumberOfElements()).append(" ");
    if (sheet.getNumberOfNewElements() > 0) {
      buf.append(" | New=<span style=\"color: red;\">").append(sheet.getNumberOfNewElements()).append("</span>");
    }
    if (sheet.getNumberOfModifiedElements() > 0) {
      buf.append(" | Modified=<span style=\"color: red;\">").append(sheet.getNumberOfModifiedElements()).append("</span>");
    }
    if (sheet.getNumberOfUnmodifiedElements() > 0) {
      buf.append(" | Unmodified=").append(sheet.getNumberOfUnmodifiedElements());
    }
    if (sheet.getNumberOfFaultyElements() > 0) {
      buf.append(" | Errors=<span style=\"color: red; font-weight: bold;\">").append(sheet.getNumberOfFaultyElements()).append("</span>");
    }
    cont.add(new PlainLabel("statistics", buf.toString()).setEscapeModelStrings(false));
    final RepeatingView actionLinkRepeater = new RepeatingView("actionLinkRepeater");
    cont.add(actionLinkRepeater);
    if (sheet.isReconciled() == false
        || sheet.getStatus().isIn(ImportStatus.IMPORTED, ImportStatus.NOTHING_TODO, ImportStatus.HAS_ERRORS) == true) {
      addActionLink(actionLinkRepeater, new SubmitLink("actionLink") {
        @Override
        public void onSubmit()
        {
          parentPage.reconcile(sheet.getName());
        }
      }, "reconcile");
    } else if (sheet.isReconciled() == true) {
      addActionLink(actionLinkRepeater, new SubmitLink("actionLink") {
        @Override
        public void onSubmit()
        {
          parentPage.commit(sheet.getName());
        }
      }, "commit");
      addActionLink(actionLinkRepeater, new SubmitLink("actionLink") {
        @Override
        public void onSubmit()
        {
          parentPage.selectAll(sheet.getName());
        }
      }, "select all");
      addActionLink(actionLinkRepeater, new SubmitLink("actionLink") {
        @Override
        public void onSubmit()
        {
          parentPage.select(sheet.getName(), 100);
        }
      }, "select 100");
      addActionLink(actionLinkRepeater, new SubmitLink("actionLink") {
        @Override
        public void onSubmit()
        {
          parentPage.select(sheet.getName(), 500);
        }
      }, "select 500");
      addActionLink(actionLinkRepeater, new SubmitLink("actionLink") {
        @Override
        public void onSubmit()
        {
          parentPage.deselectAll(sheet.getName());
        }
      }, "deselect all");
    }
    if (sheet.isFaulty() == true) {
      addActionLink(actionLinkRepeater, new SubmitLink("actionLink") {
        @Override
        public void onSubmit()
        {
          parentPage.showErrorSummary(sheet.getName());
        }
      }, "show error summary");
    }
    if (getStorageType() == DatevImportDao.Type.BUCHUNGSSAETZE) {
      addActionLink(actionLinkRepeater, new SubmitLink("actionLink") {
        @Override
        public void onSubmit()
        {
          parentPage.showBWA(sheet.getName());
        }
      }, "show business assessment");
    }
    addSheetTable(sheet, cont);
  }

  private void addActionLink(final RepeatingView actionLinkRepeater, final SubmitLink link, final String label)
  {
    final WebMarkupContainer actionLinkContainer = new WebMarkupContainer(actionLinkRepeater.newChildId());
    actionLinkRepeater.add(actionLinkContainer);
    actionLinkContainer.add(link.add(new PlainLabel("label", label)));
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
    final RepeatingView headColRepeater = new RepeatingView("headColRepeater");
    table.add(headColRepeater);
    if (getStorageType() == DatevImportDao.Type.KONTENPLAN) {
      headColRepeater.add(new Label(headColRepeater.newChildId(), getString("fibu.konto.nummer")));
      headColRepeater.add(new Label(headColRepeater.newChildId(), getString("fibu.konto.bezeichnung")));
    } else {
      headColRepeater.add(new Label(headColRepeater.newChildId(), getString("fibu.buchungssatz.satznr")));
      headColRepeater.add(new Label(headColRepeater.newChildId(), getString("date")));
      headColRepeater.add(new Label(headColRepeater.newChildId(), getString("fibu.common.betrag")));
      headColRepeater.add(new Label(headColRepeater.newChildId(), getString("fibu.buchungssatz.text")));
      headColRepeater.add(new Label(headColRepeater.newChildId(), getString("fibu.buchungssatz.konto")));
      headColRepeater.add(new Label(headColRepeater.newChildId(), getString("fibu.buchungssatz.gegenKonto")));
      headColRepeater.add(new Label(headColRepeater.newChildId(), getString("fibu.kost1")));
      headColRepeater.add(new Label(headColRepeater.newChildId(), getString("fibu.kost2")));
    }
    headColRepeater.add(new Label(headColRepeater.newChildId(), getString("modifications")));
    headColRepeater.add(new Label(headColRepeater.newChildId(), getString("errors")));
    final RepeatingView rowRepeater = new RepeatingView("rowRepeater");
    table.add(rowRepeater);
    int row = 0;
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
      rowContainer.add(new SimpleAttributeModifier("class", (row++ % 2 == 0) ? "even" : "odd"));
      rowContainer.add(new SimpleAttributeModifier("onclick", "javascript:rowCheckboxClick(this);"));
      final String style;
      if (element.isFaulty() == true) {
        style = "color: red;";
      } else if (element.getOldValue() != null && element.getValue() == null) {
        style = "text-decoration: line-through;";
      } else {
        style = null;
      }
      final WebMarkupContainer firstCell = new WebMarkupContainer("firstCell");
      if (style != null) {
        firstCell.add(new SimpleAttributeModifier("style", style));
      }
      rowContainer.add(firstCell);
      final CheckBox checkBox = new CheckBox("selectItem", new PropertyModel<Boolean>(element, "selected"));
      if (sheet.getStatus() != ImportStatus.RECONCILED) {
        checkBox.setVisible(false);
      }
      firstCell.add(checkBox);
      final ImageDef imageDef;
      if (element.isNew() == true) {
        imageDef = ImageDef.ADD;
      } else if (element.isModified() == true) {
        imageDef = ImageDef.EDIT;
      } else {
        imageDef = ImageDef.PAGE;
      }
      firstCell.add(new PresizedImage("icon", getResponse(), imageDef));

      final RepeatingView cellRepeater = new RepeatingView("cellRepeater");
      rowContainer.add(cellRepeater);
      if (getStorageType() == DatevImportDao.Type.KONTENPLAN) {
        final KontoDO konto = (KontoDO) element.getValue();
        addCell(cellRepeater, konto.getNummer(), style + " white-space: nowrap; text-align: right;");
        addCell(cellRepeater, konto.getBezeichnung(), style);
      } else {
        final BuchungssatzDO satz = (BuchungssatzDO) element.getValue();
        addCell(cellRepeater, satz.getSatznr(), style + " white-space: nowrap; text-align: right;");
        addCell(cellRepeater, DateTimeFormatter.instance().getFormattedDate(satz.getDatum()), style + " white-space: nowrap;");
        addCell(cellRepeater, CurrencyFormatter.format(satz.getBetrag()), style + " white-space: nowrap; text-align: right;");
        addCell(cellRepeater, satz.getText(), style);
        addCell(cellRepeater, satz.getKonto() != null ? satz.getKonto().getNummer() : null, style);
        addCell(cellRepeater, satz.getGegenKonto() != null ? satz.getGegenKonto().getNummer() : null, style);
        final Kost1DO kost1 = satz.getKost1();
        Component comp = addCell(cellRepeater, kost1 != null ? kost1.getShortDisplayName() : null, style);
        if (kost1 != null) {
          WicketUtils.addTooltip(comp, KostFormatter.formatToolTip(kost1));
        }
        final Kost2DO kost2 = satz.getKost2();
        comp = addCell(cellRepeater, kost2 != null ? kost2.getShortDisplayName() : null, style);
        if (kost2 != null) {
          WicketUtils.addTooltip(comp, KostFormatter.formatToolTip(kost2));
        }
      }
      if (element.getOldValue() != null && element.getPropertyChanges() != null) {
        final StringBuffer buf = new StringBuffer();
        boolean first = true;
        for (final PropertyDelta delta : element.getPropertyChanges()) {
          first = StringHelper.append(buf, first, delta.getPropertyName(), "; ");
          buf.append("=").append(delta.getNewValue()).append(" [").append(delta.getOldValue()).append("]");
        }
        addCell(cellRepeater, buf.toString(), style);
      } else {
        addCell(cellRepeater, "", null);
      }
      if (element.isFaulty() == true) {
        final StringBuffer buf = new StringBuffer();
        if (element.getErrorProperties() != null) {
          boolean first = true;
          for (final Map.Entry<String, Object> entry : element.getErrorProperties().entrySet()) {
            first = StringHelper.append(buf, first, entry.getKey(), ", ");
            buf.append("=[").append(entry.getValue()).append("]");
          }
        }
        addCell(cellRepeater, buf.toString(), " color: red; font-weight: bold;");
      } else {
        addCell(cellRepeater, "", null);
      }
    }
  }

  private Component addCell(final RepeatingView cellRepeater, final String value, final String style)
  {
    final Component comp;
    cellRepeater.add(comp = new Label(cellRepeater.newChildId(), StringUtils.defaultString(value)));
    if (style != null) {
      comp.add(new SimpleAttributeModifier("style", style));
    }
    return comp;
  }

  private Component addCell(final RepeatingView cellRepeater, final Integer value, final String style)
  {
    if (value == null) {
      return addCell(cellRepeater, "", style);
    } else {
      return addCell(cellRepeater, String.valueOf(value), style);
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
