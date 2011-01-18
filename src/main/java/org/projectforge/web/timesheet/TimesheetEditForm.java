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

package org.projectforge.web.timesheet;

import org.apache.log4j.Logger;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.DateHolder;
import org.projectforge.common.DatePrecision;
import org.projectforge.task.TaskTree;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.user.UserPrefDao;
import org.projectforge.web.task.TaskFormatter;
import org.projectforge.web.user.UserFormatter;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.layout.LayoutContext;

public class TimesheetEditForm extends AbstractEditForm<TimesheetDO, TimesheetEditPage>
{
  private static final long serialVersionUID = 3150725003240437752L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TimesheetEditForm.class);

  @SpringBean(name = "taskFormatter")
  private TaskFormatter taskFormatter;

  @SpringBean(name = "taskTree")
  private TaskTree taskTree;

  @SpringBean(name = "userFormatter")
  private UserFormatter userFormatter;

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  @SpringBean(name = "userPrefDao")
  private UserPrefDao userPrefDao;

  protected Boolean saveAsTemplate;


  protected TimesheetFormRenderer renderer;

  public TimesheetEditForm(final TimesheetEditPage parentPage, final TimesheetDO data)
  {
    super(parentPage, data);
    renderer = new TimesheetFormRenderer(parentPage, this, new LayoutContext(this), parentPage.getBaseDao(), data);
    renderer.taskTree = taskTree;
    renderer.userGroupCache = userGroupCache;
    renderer.userPrefDao = userPrefDao;
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    renderer.add();
  }
//    addRecentSheetsTable();
//    WebMarkupContainer toggleRecentSheets = new WebMarkupContainer("toggleRecentSheets");
//    toggleRecentSheets.setRenderBodyOnly(true);
//    if (isNew() == false) {
//      toggleRecentSheets.setVisible(false);
//    }
//    add(toggleRecentSheets);
//    add(WicketUtils.getJIRASupportTooltipImage(getResponse(), this));
//    add(new JiraIssuesPanel("jiraIssues", data.getDescription()));
//    add(new CheckBox("saveAsTemplate", new PropertyModel<Boolean>(this, "saveAsTemplate")));
//  }
//
//  @SuppressWarnings("serial")
//  protected void addKost2Row()
//  {
//    kost2List = taskTree.getKost2List(data.getTaskId());
//    kost2Row = new WebMarkupContainer("kost2Row") {
//      @Override
//      public boolean isVisible()
//      {
//        return CollectionUtils.isNotEmpty(kost2List);
//      }
//    };
//    add(kost2Row);
//    final LabelValueChoiceRenderer<Integer> kost2ChoiceRenderer = getKost2LabelValueChoiceRenderer();
//    kost2Choice = createKost2ChoiceRenderer(parentPage.getBaseDao(), taskTree, kost2ChoiceRenderer, getData(), kost2List);
//    kost2Choice.setRequired(true);
//    kost2Row.add(kost2Choice);
//  }
//
//  @SuppressWarnings("serial")
//  protected static DropDownChoice<Integer> createKost2ChoiceRenderer(final TimesheetDao timesheetDao, final TaskTree taskTree,
//      final LabelValueChoiceRenderer<Integer> kost2ChoiceRenderer, final TimesheetDO data, final List<Kost2DO> kost2List)
//  {
//    final DropDownChoice<Integer> choice = new DropDownChoice<Integer>("kost2Id", new Model<Integer>() {
//      public Integer getObject()
//      {
//        return data.getKost2Id();
//      }
//
//      public void setObject(final Integer kost2Id)
//      {
//        if (kost2Id != null) {
//          timesheetDao.setKost2(data, kost2Id);
//        } else {
//          data.setKost2(null);
//        }
//      }
//    }, kost2ChoiceRenderer.getValues(), kost2ChoiceRenderer);
//    choice.setNullValid(true);
//    choice.add(new AbstractValidator<Integer>() {
//      @Override
//      protected void onValidate(IValidatable<Integer> validatable)
//      {
//        final Integer value = validatable.getValue();
//        if (value != null && value >= 0) {
//          return;
//        }
//        if (CollectionUtils.isNotEmpty(kost2List) == true) {
//          // Kost2 available but not selected.
//          error(validatable);
//        }
//      }
//
//      @Override
//      protected String resourceKey()
//      {
//        return "timesheet.error.kost2Required";
//      }
//    });
//    return choice;
//  }
//
//  protected void addConsumptionBar()
//  {
//    final Integer taskId = getData().getTaskId();
//    TaskNode node = taskId != null ? taskTree.getTaskNodeById(taskId) : null;
//    if (node != null) {
//      final TaskNode personDaysNode = taskTree.getPersonDaysNode(node);
//      if (personDaysNode != null) {
//        node = personDaysNode;
//      }
//    }
//    ConsumptionBarPanel consumptionBarPanel = TaskListPage.getConsumptionBarPanel(this, "consumptionBar", taskTree, false, node);
//    consumptionBarPanel.setRenderBodyOnly(true);
//    add(consumptionBarPanel);
//    consumptionBar = consumptionBarPanel;
//  }
//
//  @SuppressWarnings("serial")
//  @Override
//  protected void addButtonPanel()
//  {
//    final Fragment buttonFragment = new Fragment("buttonPanel", "buttonFragment", this);
//    buttonFragment.setRenderBodyOnly(true);
//    buttonCell.add(buttonFragment);
//    cloneButtonPanel = new SingleButtonPanel("clone", new Button("button", new Model<String>(getString("clone"))) {
//      @Override
//      public final void onSubmit()
//      {
//        parentPage.cloneTimesheet();
//      }
//    });
//    if (isNew() == true || getData().isDeleted() == true) {
//      // Show clone button only for existing time sheets.
//      cloneButtonPanel.setVisible(false);
//    }
//    buttonFragment.add(cloneButtonPanel);
//  }
//
  @Override
  public void onBeforeRender()
  {
    renderer.onBeforeRender();
    super.onBeforeRender();
  }
//
  @Override
  protected void validation()
  {
    super.validation();
    renderer.validation();
  }
//
//
//  private LabelValueChoiceRenderer<Integer> getKost2LabelValueChoiceRenderer()
//  {
//    return getKost2LabelValueChoiceRenderer(parentPage.getBaseDao(), kost2List, getData(), kost2Choice);
//  }
//
//  protected static LabelValueChoiceRenderer<Integer> getKost2LabelValueChoiceRenderer(final TimesheetDao timesheetDao,
//      final List<Kost2DO> kost2List, final TimesheetDO data, final DropDownChoice<Integer> kost2Choice)
//  {
//    final LabelValueChoiceRenderer<Integer> kost2ChoiceRenderer = new LabelValueChoiceRenderer<Integer>();
//    if (kost2List != null && kost2List.size() == 1) {
//      // Es ist genau ein Eintrag. Deshalb selektieren wir diesen auch:
//      Integer kost2Id = kost2List.get(0).getId();
//      timesheetDao.setKost2(data, kost2Id);
//      if (kost2Choice != null) {
//        kost2Choice.modelChanged();
//      }
//    }
//    if (CollectionUtils.isEmpty(kost2List) == true) {
//      data.setKost2(null); // No kost2 list given, therefore set also kost2 to null.
//    } else {
//      for (Kost2DO kost2 : kost2List) {
//        kost2ChoiceRenderer.addValue(kost2.getId(), KostFormatter.formatForSelection(kost2));
//      }
//    }
//    return kost2ChoiceRenderer;
//  }
//
//  @SuppressWarnings( { "serial"})
//  private void addRecentSheetsTable()
//  {
//    if (isNew() == false) {
//      WebMarkupContainer invisible = new WebMarkupContainer("recentSheets");
//      invisible.setVisible(false);
//      add(invisible);
//      return;
//    }
//    final List<IColumn<TimesheetDO>> columns = new ArrayList<IColumn<TimesheetDO>>();
//    final CellItemListener<TimesheetDO> cellItemListener = new CellItemListener<TimesheetDO>() {
//      public void populateItem(Item<ICellPopulator<TimesheetDO>> item, String componentId, IModel<TimesheetDO> rowModel)
//      {
//        final TimesheetDO timesheet = rowModel.getObject();
//        final int rowIndex = ((Item< ? >) item.findParent(Item.class)).getIndex();
//        String cssStyle = null;
//        if (timesheet.isDeleted() == true) {
//          cssStyle = "text-decoration: line-through;";
//        } else if (rowIndex < TimesheetEditPage.SIZE_OF_FIRST_RECENT_BLOCK) {
//          cssStyle = "font-weight: bold; color:red;";
//        }
//
//        if (cssStyle != null) {
//          item.add(new AttributeAppendModifier("style", new Model<String>(cssStyle)));
//        }
//      }
//    };
//    columns.add(new CellItemListenerPropertyColumn<TimesheetDO>(getString("fibu.kost2"), null, "kost2.shortDisplayName", cellItemListener) {
//      @Override
//      public void populateItem(Item<ICellPopulator<TimesheetDO>> item, String componentId, IModel<TimesheetDO> rowModel)
//      {
//        final TimesheetDO timesheet = rowModel.getObject();
//        final Fragment fragment = new Fragment(componentId, "selectRecentSheet", parentPage);
//        item.add(fragment);
//        final SubmitLink link = new SubmitLink("selectRecent") {
//          public void onSubmit()
//          {
//            getData().setLocation(timesheet.getLocation());
//            getData().setDescription(timesheet.getDescription());
//            parentPage.getBaseDao().setTask(getData(), timesheet.getTaskId());
//            parentPage.getBaseDao().setUser(getData(), timesheet.getUserId());
//            parentPage.getBaseDao().setKost2(getData(), timesheet.getKost2Id());
//            kost2Choice.modelChanged();
//            locationTextField.modelChanged();
//            descriptionArea.modelChanged();
//            updateStopDate();
//            refresh();
//          };
//        };
//        fragment.add(link);
//        link.setDefaultFormProcessing(false);
//        fragment.add(new Label("label", new Model<String>() {
//          @Override
//          public String getObject()
//          {
//            final StringBuffer buf = new StringBuffer();
//            if (timesheet.getKost2() != null) {
//              buf.append(timesheet.getKost2().getShortDisplayName());
//            }
//            if (timesheet.getUserId() != null && timesheet.getUserId().equals(PFUserContext.getUserId()) == false) {
//              if (timesheet.getKost2() != null) {
//                buf.append(", ");
//              }
//              buf.append(userFormatter.getFormattedUser(timesheet.getUserId()));
//            }
//            return buf.toString();
//          }
//        }));
//        cellItemListener.populateItem(item, componentId, rowModel);
//      }
//    });
//    columns.add(new CellItemListenerPropertyColumn<TimesheetDO>(new Model<String>(getString("fibu.kunde")), null,
//        "kost2.projekt.kunde.name", cellItemListener));
//    columns.add(new CellItemListenerPropertyColumn<TimesheetDO>(new Model<String>(getString("fibu.projekt")), null, "kost2.projekt.name",
//        cellItemListener));
//    columns
//        .add(new CellItemListenerPropertyColumn<TimesheetDO>(new Model<String>(getString("task")), null, "task.title", cellItemListener) {
//          @Override
//          public void populateItem(final Item<ICellPopulator<TimesheetDO>> item, final String componentId,
//              final IModel<TimesheetDO> rowModel)
//          {
//            final TaskDO task = rowModel.getObject().getTask();
//            StringBuffer buf = new StringBuffer();
//            taskFormatter.appendFormattedTask(buf, new WicketLocalizerAndUrlBuilder(getResponse()), task, false, true, false);
//            final Label formattedTaskLabel = new Label(componentId, buf.toString());
//            formattedTaskLabel.setEscapeModelStrings(false);
//            item.add(formattedTaskLabel);
//            cellItemListener.populateItem(item, componentId, rowModel);
//          }
//        });
//    columns.add(new CellItemListenerPropertyColumn<TimesheetDO>(getString("timesheet.location"), null, "location", cellItemListener) {
//
//    });
//    columns.add(new CellItemListenerPropertyColumn<TimesheetDO>(getString("timesheet.description"), null, "shortDescription",
//        cellItemListener));
//    @SuppressWarnings("unchecked")
//    final IColumn<TimesheetDO>[] colArray = columns.toArray(new IColumn[columns.size()]);
//    final IDataProvider<TimesheetDO> dataProvider = new ListDataProvider<TimesheetDO>(parentPage.getRecentTimesheets());
//    final DataTable<TimesheetDO> dataTable = new DataTable<TimesheetDO>("recentSheets", colArray, dataProvider, 100) {
//      @Override
//      protected Item<TimesheetDO> newRowItem(String id, int index, IModel<TimesheetDO> model)
//      {
//        return new OddEvenItem<TimesheetDO>(id, index, model);
//      }
//    };
//    final HeadersToolbar headersToolbar = new HeadersToolbar(dataTable, null);
//    dataTable.addTopToolbar(headersToolbar);
//    add(dataTable);
//    if (isNew() == false) {
//      dataTable.setVisible(false);
//    }
//    dataTable.add(new DataTableBehavior());
//  }
//
//  class DataTableBehavior extends AbstractBehavior implements IHeaderContributor
//  {
//    private static final long serialVersionUID = -3295144120585281383L;
//
//    public void renderHead(IHeaderResponse response)
//    {
//      final String initJS = "// Mache alle Zeilen von recentSheets klickbar\n"
//          + "  $(\".datatable td\").click( function() {\n"
//          + "    $(this).parent().find(\"a:first\").click();\n"
//          + "  });\n";
//      response.renderOnDomReadyJavascript(initJS);
//    }
//  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
