/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projectmanagement.powerworkpackage;

import org.apache.commons.lang.Validate;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.RangeValidator;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.projectforge.core.UserException;
import org.projectforge.plugins.chimney.activities.WbsActivityDO;
import org.projectforge.plugins.chimney.activities.WbsActivityDao;
import org.projectforge.plugins.chimney.gantt.GanttScheduledActivity;
import org.projectforge.plugins.chimney.gantt.IScheduler;
import org.projectforge.plugins.chimney.utils.date.InconsistentFixedDatesException;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.wbs.ProjectDao;
import org.projectforge.plugins.chimney.web.DetachableDOModel;
import org.projectforge.plugins.chimney.web.JodaDateModel;
import org.projectforge.plugins.chimney.web.WicketWbsUtils;
import org.projectforge.plugins.chimney.web.components.ChimneyUserSelectPanel;
import org.projectforge.plugins.chimney.web.components.I18nEnumReadonlyModel;
import org.projectforge.plugins.chimney.web.projecttree.ProjectTreePage;
import org.projectforge.task.TaskStatus;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.components.JodaDatePanel;

public class PowerProjectForm extends Form<ProjectDO>
{
  private static final long serialVersionUID = -5898477856544440400L;

  @SpringBean
  private WbsActivityDao wbsActivityDao;

  @SpringBean
  private ProjectDao projectDao;

  @SpringBean
  private IScheduler scheduler;

  private final boolean isNew;

  private final transient ProjectDO project;

  private IModel<WbsActivityDO> activityModel;

  /**
   * Instantiate a new project form
   * @param id wicket id
   * @param project the project to use. Must not be null. If the project has an id, the form will be in edit mode. Otherwise it assumes a
   *          new project should be created.
   */
  public PowerProjectForm(final String id, final ProjectDO project)
  {
    super(id);
    Validate.notNull(project);
    this.project = project;
    isNew = project.getId() == null;
  }

  @Override
  public void onInitialize()
  {
    super.onInitialize();

    setModel(project);
    setActivityModel();

    add(new TextField<String>("title").setRequired(true).add(RangeValidator.minimum(3)));
    add(new TextField<String>("wbsCode").setRequired(true));
    addResponsibleUserPanel();
    add(new Label("status", new I18nEnumReadonlyModel(new PropertyModel<TaskStatus>(getModelObject(), "status"), this)));
    add(new JodaDatePanel("fixedBeginDate", getBeginDateModel()));
    addEndField();
    add(new TextArea<String>("shortDescription"));
  }

  private IModel<WbsActivityDO> getActivityModel()
  {
    return activityModel;
  }

  private void setActivityModel()
  {
    if (isNew) {
      setTransientActivityModel(new WbsActivityDO());
    } else {
      setPersistentActivityModel(wbsActivityDao.getByOrCreateFor(getModelObject()));
    }
  }

  private void setPersistentActivityModel(final WbsActivityDO activity)
  {
    activityModel = new CompoundPropertyModel<WbsActivityDO>(new DetachableDOModel<WbsActivityDO, WbsActivityDao>(activity, wbsActivityDao));
  }

  private void setTransientActivityModel(final WbsActivityDO activity)
  {
    activityModel = new CompoundPropertyModel<WbsActivityDO>(new Model<WbsActivityDO>(activity));
  }

  private void setModel(final ProjectDO project)
  {
    if (isNew) {
      setTransientModel(project);
    } else {
      setPersistentModel(project);
    }
  }

  private void setPersistentModel(final ProjectDO project)
  {
    setModel(new CompoundPropertyModel<ProjectDO>(new DetachableDOModel<ProjectDO, ProjectDao>(project, projectDao)));
  }

  private void setTransientModel(final ProjectDO project)
  {
    setModel(new CompoundPropertyModel<ProjectDO>(new Model<ProjectDO>(project)));
  }

  private void addEndField()
  {
    String end = "?";

    if (!isNew) {
      try {
        scheduler.schedule(getActivityModelObject());
        final GanttScheduledActivity result = scheduler.getResult(getActivityModelObject());
        end = result.getEnd().toString("dd.MM.yyyy");
      } catch (final Exception e) {
      }
    }

    add(new Label("end", end));
  }

  private void addResponsibleUserPanel()
  {
    final UserSelectPanel userSelectPanel = new ChimneyUserSelectPanel("responsibleUser", new PropertyModel<PFUserDO>(getModel(),
        "responsibleUser"));
    add(userSelectPanel);
    userSelectPanel.init();
  }

  protected IModel<DateMidnight> getBeginDateModel()
  {
    return new JodaDateModel<WbsActivityDO>(getActivityModel()) {
      private static final long serialVersionUID = 1L;

      @Override
      protected DateTime getDateTime()
      {
        return getModelObject().getFixedBeginDate();
      }

      @Override
      protected void setDateTime(final DateTime dateTime)
      {
        try {
          getModelObject().setFixedBeginDate(dateTime);
        } catch (final InconsistentFixedDatesException ex) {
          error(getString("plugins.chimney.errors.beginenddateinconsistent"));
        }
      }
    };
  }

  @Override
  protected void onSubmit()
  {
    if (hasError()) {
      return;
    }

    final ProjectDO project = getModelObject();
    final WbsActivityDO activity = getActivityModelObject();

    // Currently there is no possibility (at least I have not found any) to fully validate the project before trying to save it. The
    // case of sister tasks with the same title can only be observed by trying to save can catch the exception on error.

    try {
      projectDao.saveOrUpdate(project);
      if (isNew) {
        activity.setWbsNode(project);
      }
      wbsActivityDao.saveOrUpdate(activity);
    } catch (final UserException e) {
      error(getString(e.getI18nKey()));
      return;
    }

    try {
      setResponsePage(new ProjectTreePage(WicketWbsUtils.getProject(project).getId()));
    } catch (final UserException e) {
      error(getString(e.getI18nKey()));
    }
  }

  private WbsActivityDO getActivityModelObject()
  {
    return getActivityModel().getObject();
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
