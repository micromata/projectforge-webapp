/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.DateHelper;
import org.projectforge.fibu.EmployeeSalaryDO;
import org.projectforge.fibu.EmployeeSalaryDao;
import org.projectforge.fibu.datev.EmployeeSalaryExportDao;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.CurrencyPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.DownloadUtils;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;

@ListPage(editPage = EmployeeSalaryEditPage.class)
public class EmployeeSalaryListPage extends AbstractListPage<EmployeeSalaryListForm, EmployeeSalaryDao, EmployeeSalaryDO>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EmployeeSalaryListPage.class);

  private static final long serialVersionUID = -8406452960003792763L;

  @SpringBean(name = "employeeSalaryDao")
  private EmployeeSalaryDao employeeSalaryDao;

  @SpringBean(name = "employeeSalaryExportDao")
  private EmployeeSalaryExportDao employeeSalaryExportDao;

  public EmployeeSalaryListPage(PageParameters parameters)
  {
    super(parameters, "fibu.employee.salary");
  }

  public EmployeeSalaryListPage(final ISelectCallerPage caller, final String selectProperty)
  {
    super(caller, selectProperty, "fibu.employeeSalary");
  }

  @SuppressWarnings("serial")
  public List<IColumn<EmployeeSalaryDO>> createColumns(final WebPage returnToPage, final boolean sortable)
  {
    List<IColumn<EmployeeSalaryDO>> columns = new ArrayList<IColumn<EmployeeSalaryDO>>();

    CellItemListener<EmployeeSalaryDO> cellItemListener = new CellItemListener<EmployeeSalaryDO>() {
      public void populateItem(Item<ICellPopulator<EmployeeSalaryDO>> item, String componentId, IModel<EmployeeSalaryDO> rowModel)
      {
        final EmployeeSalaryDO employeeSalary = rowModel.getObject();
        String cellStyle = "";
        if (employeeSalary.isDeleted() == true) {
          cellStyle = "text-decoration: line-through;";
        }
        item.add(new AttributeModifier("style", true, new Model<String>(cellStyle)));
      }
    };
    columns.add(new CellItemListenerPropertyColumn<EmployeeSalaryDO>(new Model<String>(getString("calendar.month")), getSortable(
        "formattedYearAndMonth", sortable), "formattedYearAndMonth", cellItemListener) {
      @SuppressWarnings("unchecked")
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final EmployeeSalaryDO employeeSalary = (EmployeeSalaryDO) rowModel.getObject();
        if (isSelectMode() == false) {
          item.add(new ListSelectActionPanel(componentId, rowModel, EmployeeSalaryEditPage.class, employeeSalary.getId(), returnToPage,
              employeeSalary.getFormattedYearAndMonth()));
        } else {
          item.add(new ListSelectActionPanel(componentId, rowModel, caller, selectProperty, employeeSalary.getId(), employeeSalary
              .getFormattedYearAndMonth()));
        }
        cellItemListener.populateItem(item, componentId, rowModel);
        addRowClick(item);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<EmployeeSalaryDO>(new Model<String>(getString("name")), getSortable(
        "employee.user.lastname", sortable), "employee.user.lastname", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<EmployeeSalaryDO>(new Model<String>(getString("firstName")), getSortable(
        "employee.user.firstname", sortable), "employee.user.firstname", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<EmployeeSalaryDO>(new Model<String>(getString("fibu.employee.salary.type")),
        getSortable("type", sortable), "type", cellItemListener));
    columns.add(new CurrencyPropertyColumn<EmployeeSalaryDO>(getString("fibu.employee.salary.bruttoMitAgAnteil"), getSortable(
        "bruttoMitAgAnteil", sortable), "bruttoMitAgAnteil", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<EmployeeSalaryDO>(new Model<String>(getString("comment")), getSortable("comment",
        sortable), "comment", cellItemListener));
    return columns;
  }

  void exportExcel()
  {
    refresh();

    log.info("Exporting employee salaries as excel sheet for: "
        + DateHelper.formatMonth(form.getSearchFilter().getYear(), form.getSearchFilter().getMonth()));
    final List<EmployeeSalaryDO> list = getList();
    if (list == null || list.size() == 0) {
      // Nothing to export.
      form.addError("validation.error.nothingToExport");
      return;
    }
    final String filename = "ProjectForge-EmployeeSalaries_"
        + DateHelper.formatMonth(form.getSearchFilter().getYear(), form.getSearchFilter().getMonth())
        + "_"
        + DateHelper.getDateAsFilenameSuffix(new Date())
        + ".xls";
    final byte[] xls = employeeSalaryExportDao.export(list);
    DownloadUtils.setDownloadTarget(xls, filename);
  }

  @Override
  protected void init()
  {
    final List<IColumn<EmployeeSalaryDO>> columns = createColumns(this, true);
    dataTable = createDataTable(columns, "employee.user.lastname", true);
    form.add(dataTable);
  }

  @Override
  protected EmployeeSalaryListForm newListForm(AbstractListPage< ? , ? , ? > parentPage)
  {
    return new EmployeeSalaryListForm(this);
  }

  @Override
  protected EmployeeSalaryDao getBaseDao()
  {
    return employeeSalaryDao;
  }

  @Override
  protected IModel<EmployeeSalaryDO> getModel(EmployeeSalaryDO object)
  {
    return new DetachableDOModel<EmployeeSalaryDO, EmployeeSalaryDao>(object, getBaseDao());
  }

  protected EmployeeSalaryDao getEmployeeSalaryDao()
  {
    return employeeSalaryDao;
  }
}
