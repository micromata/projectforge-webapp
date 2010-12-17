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

package org.projectforge.web.fibu;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.DateHelper;
import org.projectforge.export.ContentProvider;
import org.projectforge.export.ExportColumn;
import org.projectforge.export.ExportSheet;
import org.projectforge.export.ExportWorkbook;
import org.projectforge.export.I18nExportColumn;
import org.projectforge.export.PropertyMapping;
import org.projectforge.export.XlsContentProvider;
import org.projectforge.fibu.KostFormatter;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.fibu.kost.Kost2Dao;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.HtmlHelper;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.DownloadUtils;
import org.projectforge.web.wicket.IListPageColumnsCreator;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;
import org.projectforge.web.wicket.WicketLocalizerAndUrlBuilder;

@ListPage(editPage = Kost2EditPage.class)
public class Kost2ListPage extends AbstractListPage<Kost2ListForm, Kost2Dao, Kost2DO> implements IListPageColumnsCreator<Kost2DO>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Kost2ListPage.class);

  private static final long serialVersionUID = -8406452960003792763L;

  @SpringBean(name = "kost2Dao")
  private Kost2Dao kost2Dao;

  @SpringBean(name = "htmlHelper")
  private HtmlHelper htmlHelper;

  public Kost2ListPage(final PageParameters parameters)
  {
    super(parameters, "fibu.kost2");
  }

  public Kost2ListPage(final ISelectCallerPage caller, final String selectProperty)
  {
    super(caller, selectProperty, "fibu.kost2");
  }

  public Kost2ListPage(final PageParameters parameters, final ISelectCallerPage caller, final String selectProperty)
  {
    super(parameters, caller, selectProperty, "fibu.kost2");
  }

  @SuppressWarnings("serial")
  @Override
  public List<IColumn<Kost2DO>> createColumns(final WebPage returnToPage)
  {
    final List<IColumn<Kost2DO>> columns = new ArrayList<IColumn<Kost2DO>>();
    CellItemListener<Kost2DO> cellItemListener = new CellItemListener<Kost2DO>() {
      public void populateItem(Item<ICellPopulator<Kost2DO>> item, String componentId, IModel<Kost2DO> rowModel)
      {
        final Kost2DO kost2 = rowModel.getObject();
        final StringBuffer cssStyle = getCssStyle(kost2.getId(), kost2.isDeleted());
        if (cssStyle.length() > 0) {
          item.add(new AttributeModifier("style", true, new Model<String>(cssStyle.toString())));
        }
      }
    };
    columns.add(new CellItemListenerPropertyColumn<Kost2DO>(new Model<String>(getString("fibu.kost2")), "formattedNumber",
        "formattedNumber", cellItemListener) {
      @SuppressWarnings("unchecked")
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final Kost2DO kost2 = (Kost2DO) rowModel.getObject();
        if (isSelectMode() == false) {
          item.add(new ListSelectActionPanel(componentId, rowModel, Kost2EditPage.class, kost2.getId(), returnToPage, String.valueOf(kost2
              .getFormattedNumber())));
          cellItemListener.populateItem(item, componentId, rowModel);
        } else {
          item.add(new ListSelectActionPanel(componentId, rowModel, caller, selectProperty, kost2.getId(), String.valueOf(kost2
              .getFormattedNumber())));
        }
        cellItemListener.populateItem(item, componentId, rowModel);
        addRowClick(item);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<Kost2DO>(new Model<String>(getString("fibu.kost2.art")), "kost2Art.name",
        "kost2Art.name", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<Kost2DO>(new Model<String>(getString("fibu.fakturiert")), "kost2Art.fakturiert",
        "kost2Art.fakturiert", cellItemListener) {
      @Override
      public void populateItem(Item<ICellPopulator<Kost2DO>> item, String componentId, IModel<Kost2DO> rowModel)
      {
        Kost2DO kost2 = (Kost2DO) rowModel.getObject();
        StringBuffer buf = new StringBuffer();
        if (kost2.getKost2Art() != null && kost2.getKost2Art().isFakturiert() == true) {
          htmlHelper.appendImageTag(new WicketLocalizerAndUrlBuilder(getResponse()), buf, "/images/accept.png", null);
        }
        final Label label = new Label(componentId, buf.toString());
        label.setEscapeModelStrings(false);
        item.add(label);
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<Kost2DO>(new Model<String>(getString("fibu.kost2.workFraction")), "workFraction",
        "workFraction", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<Kost2DO>(new Model<String>(getString("fibu.kunde")), "projekt.kunde.name",
        "projekt.kunde.name", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<Kost2DO>(new Model<String>(getString("fibu.projekt")), "projekt.name", "projekt.name",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<Kost2DO>(new Model<String>(getString("status")), "kostentraegerStatus",
        "kostentraegerStatus", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<Kost2DO>(new Model<String>(getString("description")), "description", "description",
        cellItemListener));
    columns
        .add(new CellItemListenerPropertyColumn<Kost2DO>(new Model<String>(getString("comment")), "comment", "comment", cellItemListener));
    return columns;
  }

  @Override
  protected void init()
  {
    dataTable = createDataTable(createColumns(this), "formattedNumber", true);
    form.add(dataTable);
  }

  private enum Col
  {
    STATUS, KOST, ART, FAKTURIERT, PROJEKT, DESCRIPTION, COMMENT;
  }

  void exportExcel()
  {
    log.info("Exporting kost2 list.");
    refresh();
    final List<Kost2DO> kost2List = getList();
    if (kost2List == null || kost2List.size() == 0) {
      // Nothing to export.
      form.addError("validation.error.nothingToExport");
      return;
    }
    final String filename = "ProjectForge-Kost2Export_" + DateHelper.getDateAsFilenameSuffix(new Date()) + ".xls";
    final ExportWorkbook xls = new ExportWorkbook();
    final ContentProvider contentProvider = new XlsContentProvider(xls);
    xls.setContentProvider(contentProvider);
    final ExportSheet sheet = xls.addSheet(PFUserContext.getLocalizedString("fibu.kost2.kost2s"));
    final ExportColumn[] cols = new ExportColumn[] { //
    new I18nExportColumn(Col.KOST, "fibu.kost2", XlsContentProvider.LENGTH_KOSTENTRAEGER),
        new I18nExportColumn(Col.ART, "fibu.kost2.art", XlsContentProvider.LENGTH_STD),
        new I18nExportColumn(Col.FAKTURIERT, "fibu.fakturiert", 5),
        new I18nExportColumn(Col.PROJEKT, "fibu.projekt", XlsContentProvider.LENGTH_STD),
        new I18nExportColumn(Col.STATUS, "status", XlsContentProvider.LENGTH_STD),
        new I18nExportColumn(Col.DESCRIPTION, "description", XlsContentProvider.LENGTH_STD),
        new I18nExportColumn(Col.COMMENT, "comment", XlsContentProvider.LENGTH_STD)};
    sheet.setColumns(cols);
    final PropertyMapping mapping = new PropertyMapping();
    for (final Kost2DO kost : kost2List) {
      mapping.add(Col.KOST, kost.getFormattedNumber());
      mapping.add(Col.ART, kost.getKost2Art().getName());
      mapping.add(Col.FAKTURIERT, kost.getKost2Art().isFakturiert() ? "X" : "");
      mapping.add(Col.PROJEKT, KostFormatter.formatProjekt(kost.getProjekt()));
      mapping.add(Col.STATUS, kost.getKostentraegerStatus());
      mapping.add(Col.DESCRIPTION, kost.getDescription());
      mapping.add(Col.COMMENT, kost.getComment());
      sheet.addRow(mapping.getMapping(), 0);
    }
    sheet.setZoom(3, 4); // 75%
    DownloadUtils.setDownloadTarget(xls.getAsByteArray(), filename);
  }

  @Override
  protected Kost2ListForm newListForm(AbstractListPage< ? , ? , ? > parentPage)
  {
    return new Kost2ListForm(this);
  }

  @Override
  protected Kost2Dao getBaseDao()
  {
    return kost2Dao;
  }

  @Override
  protected IModel<Kost2DO> getModel(Kost2DO object)
  {
    return new DetachableDOModel<Kost2DO, Kost2Dao>(object, getBaseDao());
  }

  protected Kost2Dao getKost2Dao()
  {
    return kost2Dao;
  }
}
