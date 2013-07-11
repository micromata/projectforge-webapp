/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projectmanagement;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.plugins.chimney.resources.ImageResources;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.wbs.ProjectDao;
import org.projectforge.plugins.chimney.web.AbstractSecuredChimneyPage;
import org.projectforge.plugins.chimney.web.components.I18nEnumReadonlyModel;
import org.projectforge.plugins.chimney.web.components.ImageLinkPanel;
import org.projectforge.plugins.chimney.web.components.TextLinkPanel;
import org.projectforge.plugins.chimney.web.navigation.BreadcrumbConstants;
import org.projectforge.plugins.chimney.web.navigation.NavigationConstants;
import org.projectforge.plugins.chimney.web.projectmanagement.powerworkpackage.PowerProjectEditPage;
import org.projectforge.plugins.chimney.web.projecttree.ProjectTreePage;
import org.projectforge.task.TaskStatus;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.wicket.WicketUtils;

/**
 * Displays a list of projects with the most relevant info for each project.
 * @author Sweeps <pf@byte-storm.com>
 */
public class ProjectListPage extends AbstractSecuredChimneyPage
{
  private static final long serialVersionUID = -1119854557054917947L;
  public static final String PAGE_ID = "projectList";

  private final int projectsPerPage = 20;

  @SpringBean(name = "projectDao")
  private ProjectDao projectDao;

  private DataView<ProjectDO> dataView;
  private SortableProjectDataProvider dataProvider;

  public ProjectListPage(final PageParameters parameters)
  {
    super(parameters);

    addTitle();
    addFeedbackPanel();
    populateTable();
    addColumnTitlesAndSorting();
    addPagination();
    addNewLink();
  }

  @Override
  @SuppressWarnings("serial")
  protected void addFeedbackPanel()
  {
    final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback") {
      @Override
      public boolean isVisible()
      {
        return this.anyMessage();
      }
    };
    feedbackPanel.setOutputMarkupId(true);
    feedbackPanel.add(AttributeModifier.append("class", "alert alert_green feedback"));
    body.add(feedbackPanel);
  }


  private void addNewLink()
  {
    body.add(new ImageLinkPanel("createProjectLink", ImageResources.NEWPROJECT, PowerProjectEditPage.class, getString("plugins.chimney.projectlist.createproject"), getString("plugins.chimney.projectlist.createproject")));
  }

  private void addPagination()
  {
    dataView.setItemsPerPage(projectsPerPage);
    body.add(new PagingNavigator("navigator", dataView));
  }

  private void addColumnTitlesAndSorting()
  {
    // create sorting borders
    final OrderByBorder orderByNameBorder = new OrderByBorder("orderByName", "title", dataProvider);
    body.add(orderByNameBorder);
    final OrderByBorder orderByProgressBorder = new OrderByBorder("orderByProgress", "progress", dataProvider);
    body.add(orderByProgressBorder);
    final OrderByBorder orderByPriorityBorder = new OrderByBorder("orderByPriority", "priority", dataProvider);
    body.add(orderByPriorityBorder);
    final OrderByBorder orderByStatusBorder = new OrderByBorder("orderByStatus", "status", dataProvider);
    body.add(orderByStatusBorder);
    final OrderByBorder orderByResponsibleBorder = new OrderByBorder("orderByResponsible", "responsibleUser", dataProvider);
    body.add(orderByResponsibleBorder);

    // add i18n labels to column heads
    orderByNameBorder.add(new Label("headname", getString("plugins.chimney.projectlist.headname")));
    orderByProgressBorder.add(new Label("headprogress", getString("plugins.chimney.projectlist.headprogress")));
    body.add(new Label("headdesc", getString("plugins.chimney.projectlist.headdesc")));
    orderByPriorityBorder.add(new Label("headpriority", getString("plugins.chimney.projectlist.headpriority")));
    orderByStatusBorder.add(new Label("headstatus", getString("plugins.chimney.projectlist.headstatus")));
    orderByResponsibleBorder.add(new Label("headresponsible", getString("plugins.chimney.projectlist.headresponsible")));
    body.add(new Label("headactions", getString("plugins.chimney.projectlist.headactions")));
  }

  private void populateTable()
  {
    // get a sortable data provider that fetches projects from the DB and sorts them
    dataProvider = new SortableProjectDataProvider("title", SortOrder.ASCENDING, new BaseSearchFilter());
    // create a data view table using the data provider as data source
    dataView = new ProjectDataView("row", dataProvider);
    body.add(dataView);
  }

  private void deleteProject(final ProjectDO selectedProject)
  {
    projectDao.markAsDeleted(selectedProject);
    ProjectListPage.this.info(getLocalizedMessage("plugins.chimney.projectlist.projectmarkedasdeleted", selectedProject.getTitle()));
  }

  private void addTitle()
  {
    body.add(new Label("titlehead", getString("plugins.chimney.projectlist.heading")));
  }

  @Override
  protected String getTitle()
  {
    return getString("plugins.chimney.projectlist.title");
  }

  @Override
  protected String getNavigationBarName()
  {
    return NavigationConstants.MAIN;
  }

  @Override
  protected void insertBreadcrumbItems(final List<String> items)
  {
    items.add(BreadcrumbConstants.PROJECT_PLANNING);
    items.add(BreadcrumbConstants.PROJECTS);
  }

  /**
   * DataView component for displaying project data with odd/even row styling
   * and edit/delete buttons.
   * @author Sweeps <pf@byte-storm.com>
   */
  class ProjectDataView extends DataView<ProjectDO> {

    protected ProjectDataView(final String id, final IDataProvider<ProjectDO> dataProvider)
    {
      super(id, dataProvider);
    }

    private static final long serialVersionUID = 1L;
    //private ProjectDO project;

    @Override
    protected void populateItem(final Item<ProjectDO> item)
    {
      populateRowWithData(item);
      addEditLinkTo(item);
      addDeleteLinkTo(item);
      addRowFormattingTo(item);
    }

    private void addRowFormattingTo(final Item<ProjectDO> item)
    {
      // add formatting for odd/even rows
      item.add(AttributeModifier.replace("class", new AbstractReadOnlyModel<String>()
          {
        private static final long serialVersionUID = 1L;

        @Override
        public String getObject()
        {
          return (item.getIndex() % 2 == 1) ? "even" : "odd";
        }
          }));
    }

    private void addDeleteLinkTo(final Item<ProjectDO> item)
    {
      final ProjectDO project = item.getModelObject();
      // add delete link and icon
      final Link<Void> deleteLink = new Link<Void>("delete_link") {
        private static final long serialVersionUID = 1L;
        @Override
        public void onClick()
        {
          deleteProject(item.getModelObject());
          setResponsePage(ProjectListPage.this);
        }
      };
      deleteLink.add(WicketUtils.javaScriptConfirmDialogOnClick(getLocalizedMessage(
          "plugins.chimney.projectlist.projectconfirmdelete", project.getTitle())));
      deleteLink.add(new Image("delete_icon", ImageResources.DELETE_SMALL_IMAGE));
      item.add(deleteLink);
    }

    private void addEditLinkTo(final Item<ProjectDO> item)
    {
      final ProjectDO project = item.getModelObject();
      // add edit link and icon
      final Link<Void> editLink = new Link<Void>("edit_link") {
        private static final long serialVersionUID = 1L;
        @Override
        public void onClick()
        {
          setResponsePage(new PowerProjectEditPage(new PageParameters(), project.getId()));
        }
      };
      editLink.add(new Image("edit_icon", ImageResources.EDIT_SMALL_IMAGE));
      item.add(editLink);
    }

    private void populateRowWithData(final Item<ProjectDO> item)
    {
      // put ProjectDO's data into the right columns
      final ProjectDO project = item.getModelObject();

      item.add(new TextLinkPanel("name",project.getTitle()) {
        private static final long serialVersionUID = 7943147663723750177L;
        @Override
        public void onClick()
        {
          setResponsePage(new ProjectTreePage( item.getModelObject().getId()));
        }
      }
      );
      final int progress = project.getProgress();
      item.add(new Label("progresspercent", String.format("%d", progress)));
      item.add(new WebMarkupContainer("progressstyle").add(AttributeModifier.prepend("style", "width:"+progress+"%;")));
      final Label descLabel = new Label("desc", new PropertyModel<String>(project, "shortDescription"));
      item.add(descLabel);
      item.add(new MultiLineLabel("fulldesc", new PropertyModel<String>(project, "description")) {
        private static final long serialVersionUID = 1L;
        @Override
        public boolean isVisible()
        { // display hovered full description only if there is one
          final String fieldText = this.getDefaultModelObjectAsString();
          return fieldText != null && !fieldText.isEmpty();
        }
      });
      item.add(new Label("priority", new I18nEnumReadonlyModel(new PropertyModel<TaskStatus>(project, "priority"), ProjectListPage.this)));
      item.add(new Label("status", new I18nEnumReadonlyModel(new PropertyModel<TaskStatus>(project, "status"), ProjectListPage.this)));
      item.add(new Label("responsible", new PropertyModel<String>(new PropertyModel<PFUserDO>(project, "responsibleUser"), "fullname")));
    }

  }
}
