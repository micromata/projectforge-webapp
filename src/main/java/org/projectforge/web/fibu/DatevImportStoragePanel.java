/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
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
import org.projectforge.fibu.kost.BusinessAssessment;
import org.projectforge.fibu.kost.Kost1DO;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.PlainLabel;
import org.projectforge.web.wicket.flowlayout.DiffTextPanel;
import org.projectforge.web.wicket.flowlayout.IconPanel;
import org.projectforge.web.wicket.flowlayout.IconType;
import org.springframework.util.CollectionUtils;

import de.micromata.hibernate.history.delta.PropertyDelta;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class DatevImportStoragePanel extends Panel
{
  private static final long serialVersionUID = -5732520730823126042L;

  protected WebMarkupContainer errorPropertiesTable;

  protected RepeatingView sheetRepeatingView;

  private Label businessAssessmentLabel, storageHeadingLabel;

  protected Map<String, Set<Object>> errorProperties;

  protected BusinessAssessment businessAssessment;

  protected transient ImportStorage< ? > storage;

  protected DatevImportPage parentPage;

  protected DatevImportFilter filter;

  /**
   * @param id
   */
  public DatevImportStoragePanel(final String id, final DatevImportPage parentPage, final DatevImportFilter filter)
  {
    super(id);
    this.parentPage = parentPage;
    this.filter = filter;
    sheetRepeatingView = new RepeatingView("sheetRepeater");
    add(sheetRepeatingView);
  }

  public void refresh()
  {
    if (businessAssessmentLabel != null) {
      remove(businessAssessmentLabel);
    }
    if (errorPropertiesTable != null) {
      remove(errorPropertiesTable);
    }
    if (storageHeadingLabel != null) {
      remove(storageHeadingLabel);
    }
    add(storageHeadingLabel = new Label("storageHeading", "Import storage: " + (storage != null ? storage.getFilename() : "")))
    .setRenderBodyOnly(true);

    add(errorPropertiesTable = new WebMarkupContainer("errorPropertiesTable"));
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
    if (errorPropertiesTable == null) {
      add(errorPropertiesTable = new WebMarkupContainer("errorPropertiesTable")).setVisible(false);
    }
    if (storageHeadingLabel == null) {
      add(storageHeadingLabel = (Label) new Label("storageHeading", "[invisible]").setVisible(false));
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
    toggleLink.add(new IconPanel("zoomInImage", IconType.ZOOM_IN) {
      @Override
      public boolean isVisible()
      {
        return !sheet.isOpen();
      }
    });
    toggleLink.add(new IconPanel("zoomOutImage", IconType.ZOOM_OUT) {
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
          parentPage.showBusinessAssessment(sheet.getName());
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
      rowContainer.add(AttributeModifier.replace("class", (row++ % 2 == 0) ? "even" : "odd"));
      rowContainer.add(AttributeModifier.replace("onmousedown", "javascript:rowCheckboxClick(this, event);"));
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
        firstCell.add(AttributeModifier.replace("style", style));
      }
      rowContainer.add(firstCell);
      final CheckBox checkBox = new CheckBox("selectItem", new PropertyModel<Boolean>(element, "selected"));
      if (sheet.getStatus() != ImportStatus.RECONCILED) {
        checkBox.setVisible(false);
      }
      firstCell.add(checkBox);
      final IconType iconType;
      if (element.isNew() == true) {
        iconType = IconType.PLUS_SIGN;
      } else if (element.isModified() == true) {
        iconType = IconType.MODIFIED;
      } else {
        iconType = IconType.DOCUMENT;
      }
      firstCell.add(new IconPanel("icon", iconType));

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
        final StringBuffer oldValue = new StringBuffer();
        boolean first = true;
        for (final PropertyDelta delta : element.getPropertyChanges()) {
          StringHelper.append(buf, first, delta.getPropertyName(), "; ");
          first = StringHelper.append(oldValue, first, delta.getPropertyName(), "; ");
          buf.append("=").append(delta.getNewValue());
          oldValue.append("=").append(delta.getOldValue());
        }
        final DiffTextPanel diffTextPanel = new DiffTextPanel("value", Model.of(buf.toString()), Model.of(oldValue
            .toString()));
        addCell(cellRepeater, diffTextPanel, style);
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

  private Component addCell(final RepeatingView cellRepeater, final Component comp, final String style)
  {
    final WebMarkupContainer cell = new WebMarkupContainer(cellRepeater.newChildId());
    cellRepeater.add(cell);
    cell.add(comp);
    if (style != null) {
      comp.add(AttributeModifier.replace("style", style));
    }
    return comp;
  }

  private Component addCell(final RepeatingView cellRepeater, final String value, final String style)
  {
    final Component comp = new Label("value", StringUtils.defaultString(value));
    return addCell(cellRepeater, comp, style);
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
