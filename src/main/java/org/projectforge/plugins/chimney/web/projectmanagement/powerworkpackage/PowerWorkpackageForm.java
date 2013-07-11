/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projectmanagement.powerworkpackage;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.RangeValidator;
import org.joda.time.Period;
import org.projectforge.core.Priority;
import org.projectforge.core.UserException;
import org.projectforge.plugins.chimney.activities.WbsActivityDO;
import org.projectforge.plugins.chimney.activities.WbsActivityDao;
import org.projectforge.plugins.chimney.gantt.GanttScheduledActivity;
import org.projectforge.plugins.chimney.gantt.IScheduler;
import org.projectforge.plugins.chimney.resourceplanning.ResourceAssignmentDO;
import org.projectforge.plugins.chimney.resourceplanning.ResourceAssignmentDao;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.PlanningStatus;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.wbs.WorkpackageDO;
import org.projectforge.plugins.chimney.wbs.WorkpackageDao;
import org.projectforge.plugins.chimney.web.DetachableDOModel;
import org.projectforge.plugins.chimney.web.WicketWbsUtils;
import org.projectforge.plugins.chimney.web.components.ChimneyJodaPeriodField;
import org.projectforge.plugins.chimney.web.components.ChimneyUserSelectPanel;
import org.projectforge.plugins.chimney.web.components.PhaseSelectPanel;
import org.projectforge.plugins.chimney.web.projecttree.ProjectTreePage;
import org.projectforge.plugins.chimney.web.utils.WicketUtil;
import org.projectforge.task.TaskStatus;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.user.UserSelectPanel;

public class PowerWorkpackageForm extends TransactionalSubmitForm<WorkpackageDO>
{
  private static final long serialVersionUID = -5898477856544440400L;

  @SpringBean
  private WbsActivityDao wbsActivityDao;

  @SpringBean
  private WorkpackageDao workpackageDao;

  @SpringBean
  private WicketWbsUtils wbsNodeUtils;

  @SpringBean
  private IScheduler scheduler;

  @SpringBean
  private ResourceAssignmentDao resDao;

  private IModel<WbsActivityDO> activityModel;

  private final boolean isNew;

  private AbstractWbsNodeDO parent;

  private final transient WorkpackageDO workpackage;

  private final IModel<Boolean> stayOnFormModel = new Model<Boolean>(false);

  private Integer parentId;

  /**
   * Instantiate a new edit form
   * @param id wicket id
   * @param workpackage workpackage to edit, must not be null
   */
  public PowerWorkpackageForm(final String id, final WorkpackageDO workpackage)
  {
    this(id, workpackage, null);
  }

  /**
   * Instantiate a new workpackage form
   * @param id wicket id
   * @param workpackage workpackage to use
   * @param parent new parent of the workpackage. If this is not null, the form assumes a new workpackage should be created.
   */
  public PowerWorkpackageForm(final String id, final WorkpackageDO workpackage, final AbstractWbsNodeDO parent)
  {
    super(id);
    this.workpackage = workpackage;
    this.parent = parent;
    if (parent != null) {
      isNew = true;
      parentId = parent.getId();
    } else {
      isNew = false;
    }
  }

  @Override
  public void onInitialize()
  {
    super.onInitialize();

    final PowerWorkpackageForm self = this;
    final AbstractWbsNodeDO parent = this.parent;

    setModel(parent);
    setActivityModel();

    final IModel<ProjectDO> projectModel = wbsNodeUtils.getModelFor(WicketWbsUtils.getProject(parent != null ? parent : getModelObject()));
    add(new Label("projectTitle", new PropertyModel<String>(projectModel, "title")));
    add(new Label("projectLead", new PropertyModel<String>(new PropertyModel<PFUserDO>(projectModel, "responsibleUser"), "fullname")));

    addBeginAndEndFields(getActivityModelObject(), getModelObject());

    if (!isNew) {
      add(new PredecessorRelationsPanel("predecessor_relations_panel", self, getActivityModelObject(), getModelObject()));
    } else {
      add(new WebMarkupContainer("predecessor_relations_panel").setVisible(false));
    }

    addResourcesPanel(self, getModelObject());
    add(new TextField<String>("title").setRequired(true).add(RangeValidator.minimum(3)));
    add(new Label("wbsCode"));
    addResponsibleUserPanel();
    add(new PhaseSelectPanel<WorkpackageDO>("phase_panel", getModel(), new Model<AbstractWbsNodeDO>(parent != null ? parent
        : getModelObject().getParent()), projectModel));
    add(new ChimneyJodaPeriodField("effortEstimation", new PropertyModel<Period>(activityModel, "effortEstimation")));
    add(WicketUtil.getNewDropDownChoice(this, "priority", "priority", getModel(), Priority.values()).setNullValid(true));
    add(WicketUtil.getNewDropDownChoice(this, "status", "status", getModel(), TaskStatus.values()).setNullValid(false).setRequired(true));
    add(new TextField<String>("shortDescription").add(RangeValidator.minimum(3)));
    add(new TextArea<String>("description"));
    add(WicketUtil.getNewDropDownChoice(this, "planningStatus", "planningStatus", getModel(), PlanningStatus.values()).setNullValid(false)
        .setRequired(true));
    add(new TextField<String>("progress").setOutputMarkupId(true).add(new RangeValidator<Integer>(0, 100)));

    if (isNew) {
      add(new Button("stay_on_form_button") {
        private static final long serialVersionUID = -4276087364210461107L;

        @Override
        public void onSubmit()
        {
          stayOnFormModel.setObject(true);
        }
      });
    } else {
      add(new WebMarkupContainer("stay_on_form_button").setVisible(false));
    }
  }

  private void setModel(final AbstractWbsNodeDO parent)
  {
    final WorkpackageDO workpackage = this.workpackage;
    workpackage.setWbsCode(getWbsCode(workpackage, parent));

    if (isNew) {
      setTransientModel(workpackage);
    } else {
      setPersistentModel(workpackage);
    }
  }

  private void setActivityModel()
  {
    if (isNew) {
      setTransientActivityModel(new WbsActivityDO());
    } else {
      setPersistentActivityModel(wbsActivityDao.getByOrCreateFor(getModelObject()));
    }
  }

  private WbsActivityDO getActivityModelObject()
  {
    return activityModel.getObject();
  }

  private void setPersistentActivityModel(final WbsActivityDO activity)
  {
    activityModel = new DetachableDOModel<WbsActivityDO, WbsActivityDao>(activity, wbsActivityDao);
  }

  private void setTransientActivityModel(final WbsActivityDO activity)
  {
    activityModel = new Model<WbsActivityDO>(activity);
  }

  private void setPersistentModel(final WorkpackageDO workpackage)
  {
    setModel(new CompoundPropertyModel<WorkpackageDO>(new DetachableDOModel<WorkpackageDO, WorkpackageDao>(workpackage, workpackageDao)));
  }

  private void setTransientModel(final WorkpackageDO workpackage)
  {
    setModel(new CompoundPropertyModel<WorkpackageDO>(new Model<WorkpackageDO>(workpackage)));
  }

  private void addBeginAndEndFields(final WbsActivityDO activity, final WorkpackageDO workpackage)
  {
    String begin = "?";
    String end = "?";
    try {
      scheduler.schedule(wbsActivityDao.getByWbsNode(WicketWbsUtils.getProject(workpackage)));
      final GanttScheduledActivity result = scheduler.getResult(activity);
      begin = result.getBegin().toString("dd.MM.yyyy");
      end = result.getEnd().toString("dd.MM.yyyy");
    } catch (final Exception e) {
    }
    add(new Label("begin", begin));
    add(new Label("end", end));
  }

  private void addResponsibleUserPanel()
  {
    final UserSelectPanel userSelectPanel = new ChimneyUserSelectPanel("responsibleUser", new PropertyModel<PFUserDO>(getModel(),
        "responsibleUser"));
    add(userSelectPanel);
    userSelectPanel.init();
  }

  private void addResourcesPanel(final PowerWorkpackageForm self, final WorkpackageDO workpackage)
  {
    final WebMarkupContainer resourcesPanel = new WebMarkupContainer("resources_panel");
    add(resourcesPanel);

    if (isNew) {
      resourcesPanel.setVisible(false);
      return;
    }

    final WebMarkupContainer resourcesDiv = new WebMarkupContainer("resources");
    resourcesDiv.setOutputMarkupId(true);
    resourcesPanel.add(resourcesDiv);

    final RepeatingView resView = new RepeatingView("resources_form");
    resView.setOutputMarkupId(true);
    resourcesDiv.add(resView);

    final List<ResourceAssignmentDO> res = resDao.getListByWbsNode(workpackage);
    for (final ResourceAssignmentDO resAs : res) {
      resView.add(new ResourceAssignmentForm(resView.newChildId(), resAs, self));
    }

    final AjaxLink< ? > newResourceLink = new AjaxLink<Object>("add_resource_link") {
      private static final long serialVersionUID = 3970417627178610876L;

      @Override
      public void onClick(final AjaxRequestTarget target)
      {
        final ResourceAssignmentDO resAs = new ResourceAssignmentDO(workpackage);
        final ResourceAssignmentForm resForm = new ResourceAssignmentForm(resView.newChildId(), resAs, self);
        resForm.setOutputMarkupId(true);
        resView.add(resForm);

        target.prependJavaScript(String.format("var item=document.createElement('%s');item.id='%s';" + "Wicket.$('%s').appendChild(item);",
            "tr", resForm.getMarkupId(), resourcesDiv.getMarkupId()));

        target.add(resForm);
      }
    };
    resourcesPanel.add(newResourceLink);
  }

  private String getWbsCode(final WorkpackageDO wp, final AbstractWbsNodeDO maybeParent)
  {
    return maybeParent != null ? maybeParent.getWbsCode() + "." + (maybeParent.getAutoIncrementChildren() + 1) : wp.getWbsCode();
  }

  @Override
  protected void onSubmit()
  {
    if (hasError()) {
      return;
    }

    if (isNew) {
      parent = wbsNodeUtils.getById(parentId);
    }

    final WorkpackageDO workpackage = getModelObject();
    workpackage.setWbsCode(getWbsCode(workpackage, parent));
    final WbsActivityDO activity = activityModel.getObject();

    // Currently there is no possibility (at least I have not found any) to fully validate the workpackage before trying to save it. The
    // case of sister tasks with the same title can only be observed by trying to save can catch the exception on error.

    try {
      if (isNew) {
        wbsNodeUtils.changeParentAndSave(workpackage, parent);
        activity.setWbsNode(workpackage);
      } else {
        workpackageDao.update(workpackage);
      }
      wbsActivityDao.saveOrUpdate(activity);
    } catch (final UserException e) {
      error(getString(e.getI18nKey()));
      stayOnFormModel.setObject(false);
      return;
    }

    try {
      flushTransactionalSubmitActions();
      if (stayOnFormModel.getObject()) {
        setResponsePage(new PowerWorkpackageEditPage(new PageParameters(), workpackage.getId()));
      } else {
        setResponsePage(new ProjectTreePage(WicketWbsUtils.getProject(workpackage).getId()));
      }
    } catch (final UserException e) {
      error(getString(e.getI18nKey()));
      stayOnFormModel.setObject(false);
    }
  }

  @Override
  public void renderHead(final IHeaderResponse response)
  {
    super.renderHead(response);
    response.render(JavaScriptHeaderItem.forScript(
        "  $(document).ready(function() { $(\"#progressSlider\").slider({ max: 100, min: 0, value: "
            + getModelObject().getProgress()
            + ", "
            + "slide: function(event, ui) {  $(\"#p_progress\").val ( ui.value ); } }); });", "progresssliderscript"));
  }
}
