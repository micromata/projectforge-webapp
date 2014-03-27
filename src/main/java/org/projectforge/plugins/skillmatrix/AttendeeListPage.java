/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.skillmatrix;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.excel.PropertyMapping;
import org.projectforge.export.DOListExcelExporter;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.user.UserFormatter;
import org.projectforge.web.user.UserPropertyColumn;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.IListPageColumnsCreator;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;

/**
 * The controller of the list page. Most functionality such as search etc. is done by the super class.
 * @author Werner Feder (werner.feder@t-online.de)
 */
@ListPage(editPage = AttendeeEditPage.class)
public class AttendeeListPage extends AbstractListPage<AttendeeListForm, AttendeeDao, AttendeeDO> implements
IListPageColumnsCreator<AttendeeDO>
{

  private static final long serialVersionUID = 685671613717879800L;

  public static final String I18N_KEY_PREFIX = "plugins.skillmatrix.skilltraining.attendee";

  @SpringBean(name = "attendeeDao")
  private AttendeeDao attendeeDao;

  @SpringBean(name = "userFormatter")
  private UserFormatter userFormatter;

  public AttendeeListPage(final PageParameters parameters)
  {
    super(parameters, I18N_KEY_PREFIX);
  }

  /**
   * @see org.projectforge.web.wicket.IListPageColumnsCreator#createColumns(org.apache.wicket.markup.html.WebPage, boolean)
   */
  @SuppressWarnings("serial")
  @Override
  public List<IColumn<AttendeeDO, String>> createColumns(final WebPage returnToPage, final boolean sortable)
  {
    final List<IColumn<AttendeeDO, String>> columns = new ArrayList<IColumn<AttendeeDO, String>>();
    final CellItemListener<AttendeeDO> cellItemListener = new CellItemListener<AttendeeDO>() {
      public void populateItem(final Item<ICellPopulator<AttendeeDO>> item, final String componentId, final IModel<AttendeeDO> rowModel)
      {
        final AttendeeDO attendeeDO = rowModel.getObject();
        appendCssClasses(item, attendeeDO.getId(), attendeeDO.isDeleted());
      }
    };

    columns.add(new CellItemListenerPropertyColumn<AttendeeDO>(getString("plugins.skillmatrix.skillrating.skill"),
        getSortable("training.skill.title", sortable), "training.skill.title", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<AttendeeDO>(getString("plugins.skillmatrix.skilltraining.training"),
        getSortable("training.title", sortable), "training.title", cellItemListener));

    columns.add(new CellItemListenerPropertyColumn<AttendeeDO>(getString("plugins.skillmatrix.skilltraining.startDate"),
        getSortable("training.startDate", sortable), "training.startDate",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<AttendeeDO>(getString("plugins.skillmatrix.skilltraining.endDate"),
        getSortable("training.endDate", sortable), "training.endDate",
        cellItemListener));

    columns.add(new UserPropertyColumn<AttendeeDO>(AttendeeDO.class, getSortable("attendeeId", sortable), "attendee",
        cellItemListener).withUserFormatter(userFormatter));
    columns.add(new CellItemListenerPropertyColumn<AttendeeDO>(AttendeeDO.class, getSortable("rating", sortable), "rating",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<AttendeeDO>(AttendeeDO.class, getSortable("certificate", sortable), "certificate",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<AttendeeDO>(AttendeeDO.class, getSortable("description", sortable), "description",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<AttendeeDO>(AttendeeDO.class, getSortable("created", sortable), "created",
        cellItemListener) {
      @SuppressWarnings({ "unchecked", "rawtypes"})
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final AttendeeDO attendeeDO = (AttendeeDO) rowModel.getObject();
        item.add(new ListSelectActionPanel(componentId, rowModel, AttendeeEditPage.class, attendeeDO.getId(), returnToPage,
            DateTimeFormatter.instance().getFormattedDateTime(attendeeDO.getCreated())));
        addRowClick(item);
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });

    columns.add(new CellItemListenerPropertyColumn<AttendeeDO>(AttendeeDO.class, getSortable("lastUpdate", sortable), "lastUpdate",
        cellItemListener));

    return columns;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListPage#init()
   */
  @Override
  protected void init()
  {
    dataTable = createDataTable(createColumns(this, true), "lastUpdate", SortOrder.DESCENDING);
    form.add(dataTable);
    addExcelExport(getString("plugins.skillmatrix.skilltraining.attendee.menu"), getString("plugins.skillmatrix.skilltraining.attendee.menu"));
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListPage#createExcelExporter(java.lang.String)
   */
  @Override
  protected DOListExcelExporter createExcelExporter(final String filenameIdentifier)
  {
    return new DOListExcelExporter(filenameIdentifier) {
      // /**
      // * @see org.projectforge.excel.ExcelExporter#onBeforeSettingColumns(java.util.List)
      // */
      // @Override
      // protected List<ExportColumn> onBeforeSettingColumns(final ContentProvider sheetProvider, final List<ExportColumn> columns)
      // {
      // final List<ExportColumn> sortedColumns = reorderColumns(columns, "kreditor", "konto", "kontoBezeichnung", "betreff", "datum",
      // "faelligkeit", "bezahlDatum", "zahlBetrag");
      // I18nExportColumn col = new I18nExportColumn("kontoBezeichnung", "fibu.konto.bezeichnung", MyXlsContentProvider.LENGTH_STD);
      // sortedColumns.add(2, col);
      // col = new I18nExportColumn("netSum", "fibu.common.netto");
      // putCurrencyFormat(sheetProvider, col);
      // sortedColumns.add(7, col);
      // col = new I18nExportColumn("grossSum", "fibu.common.brutto");
      // putCurrencyFormat(sheetProvider, col);
      // sortedColumns.add(8, col);
      // return sortedColumns;
      // }

      /**
       * @see org.projectforge.excel.ExcelExporter#addMapping(org.projectforge.excel.PropertyMapping, java.lang.Object,
       *      java.lang.reflect.Field)
       */
      @Override
      public void addMapping(final PropertyMapping mapping, final Object entry, final Field field)
      {
        if ("training".equals(field.getName()) == true) {
          final SkillDO skill = ((AttendeeDO) entry).getTraining().getSkill();
          mapping.add(field.getName(), skill != null ? skill.getTitle() : "");
        } else {
          super.addMapping(mapping, entry, field);
        }
      }
    };
  }
  /**
   * @see org.projectforge.web.wicket.AbstractListPage#getBaseDao()
   */
  @Override
  protected AttendeeDao getBaseDao()
  {
    return attendeeDao;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListPage#select(java.lang.String, java.lang.Object)
   */
  @Override
  public void select(final String property, final Object selectedValue)
  {
    if ("attendeeId".equals(property) == true) {
      form.getSearchFilter().setAttendeeId((Integer) selectedValue);
      refresh();
    } else if ("trainingId".equals(property) == true) {
      form.getSearchFilter().setTrainingId((Integer) selectedValue);
      refresh();
    } else {
      super.select(property, selectedValue);
    }
  }

  /**
   * 
   * @see org.projectforge.web.fibu.ISelectCallerPage#unselect(java.lang.String)
   */
  @Override
  public void unselect(final String property)
  {
    if ("attendeeId".equals(property) == true) {
      form.getSearchFilter().setAttendeeId(null);
      refresh();
    } else if ("trainingId".equals(property) == true) {
      form.getSearchFilter().setTrainingId(null);
      refresh();
    } else {
      super.unselect(property);
    }
  }
  /**
   * @see org.projectforge.web.wicket.AbstractListPage#newListForm(org.projectforge.web.wicket.AbstractListPage)
   */
  @Override
  protected AttendeeListForm newListForm(final AbstractListPage< ? , ? , ? > parentPage)
  {
    return new AttendeeListForm(this);
  }

}
