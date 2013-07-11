/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projectmanagement.powerworkpackage;

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
import org.joda.time.Period;
import org.projectforge.core.UserException;
import org.projectforge.plugins.chimney.activities.WbsActivityDO;
import org.projectforge.plugins.chimney.activities.WbsActivityDao;
import org.projectforge.plugins.chimney.gantt.GanttScheduledActivity;
import org.projectforge.plugins.chimney.gantt.IScheduler;
import org.projectforge.plugins.chimney.gantt.WbsActivityNavigator;
import org.projectforge.plugins.chimney.utils.CachingEffortEstimator;
import org.projectforge.plugins.chimney.utils.date.StandardWorkdayNormalizer;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.wbs.SubtaskDO;
import org.projectforge.plugins.chimney.wbs.SubtaskDao;
import org.projectforge.plugins.chimney.web.DetachableDOModel;
import org.projectforge.plugins.chimney.web.WicketWbsUtils;
import org.projectforge.plugins.chimney.web.components.ChimneyUserSelectPanel;
import org.projectforge.plugins.chimney.web.components.I18nEnumReadonlyModel;
import org.projectforge.plugins.chimney.web.components.PhaseSelectPanel;
import org.projectforge.plugins.chimney.web.projecttree.ProjectTreePage;
import org.projectforge.task.TaskStatus;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.user.UserSelectPanel;

public class PowerSubtaskForm extends Form<SubtaskDO>
{
  private static final long serialVersionUID = -5898477856544440400L;

  @SpringBean
  private WbsActivityDao wbsActivityDao;

  @SpringBean
  private SubtaskDao subtaskDao;

  @SpringBean
  private WicketWbsUtils wbsNodeUtils;

  @SpringBean
  private IScheduler scheduler;

  @SpringBean
  private WbsActivityNavigator wbsActivityNavigator;

  private final boolean isNew;

  private AbstractWbsNodeDO parent;

  private final transient SubtaskDO subtask;

  private Integer parentId;

  /**
   * Instantiate a new edit form
   * @param id wicket id
   * @param subtask subtask to edit, must not be null
   */
  public PowerSubtaskForm(final String id, final SubtaskDO subtask)
  {
    this(id, subtask, null);
  }

  /**
   * Instantiate a new subtask form
   * @param id wicket id
   * @param subtask subtask to use
   * @param parent new parent of the subtask. If this is not null, the form assumes a new subtask should be created.
   */
  public PowerSubtaskForm(final String id, final SubtaskDO subtask, final AbstractWbsNodeDO parent)
  {
    super(id);
    this.subtask = subtask;
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

    final AbstractWbsNodeDO parent = this.parent;

    setModel(parent);

    final IModel<ProjectDO> projectModel = wbsNodeUtils.getModelFor(WicketWbsUtils.getProject(parent != null ? parent : getModelObject()));
    add(new Label("projectTitle", new PropertyModel<String>(projectModel, "title")));
    add(new Label("projectLead", new PropertyModel<String>(new PropertyModel<PFUserDO>(projectModel, "responsibleUser"), "fullname")));

    addBeginAndEndFields(getModelObject());
    add(new TextField<String>("title").setRequired(true).add(RangeValidator.minimum(3)));
    add(new Label("wbsCode"));
    addResponsibleUserPanel();
    add(new PhaseSelectPanel<SubtaskDO>("phase_panel", getModel(), new Model<AbstractWbsNodeDO>(parent != null ? parent : getModelObject()
        .getParent()), projectModel));
    add(new TextField<String>("shortDescription"));
    add(new TextArea<String>("description"));

    add(new Label("status", new I18nEnumReadonlyModel(new PropertyModel<TaskStatus>(getModelObject(), "status"), this)));
    add(new Label("progress"));
    addEffortLabel();
  }

  private void addEffortLabel()
  {
    String estimatedEffort = "-";
    if (!isNew) {
      try {
        final WbsActivityDO activity = wbsActivityDao.getByWbsNode(subtask);
        final CachingEffortEstimator effortEstimator = new CachingEffortEstimator(wbsActivityNavigator);
        final Period estEffPio = StandardWorkdayNormalizer.toNormalizedPeriod(effortEstimator.estimateEffort(activity));
        estimatedEffort = String.format("%d PT", estEffPio.toStandardDays().getDays());
      } catch (final Exception e) {
      }
    }
    add(new Label("effortEstimation", estimatedEffort));
  }

  private void setModel(final AbstractWbsNodeDO parent)
  {
    final SubtaskDO subtask = this.subtask;
    subtask.setWbsCode(getWbsCode(subtask, parent));

    if (isNew) {
      setTransientModel(subtask);
    } else {
      setPersistentModel(subtask);
    }
  }

  private void setPersistentModel(final SubtaskDO subtask)
  {
    setModel(new CompoundPropertyModel<SubtaskDO>(new DetachableDOModel<SubtaskDO, SubtaskDao>(subtask, subtaskDao)));
  }

  private void setTransientModel(final SubtaskDO subtask)
  {
    setModel(new CompoundPropertyModel<SubtaskDO>(new Model<SubtaskDO>(subtask)));
  }

  private void addBeginAndEndFields(final SubtaskDO subtask)
  {
    String begin = "?";
    String end = "?";

    if (!isNew) {
      try {
        scheduler.schedule(wbsActivityDao.getByWbsNode(WicketWbsUtils.getProject(subtask)));
        final GanttScheduledActivity result = scheduler.getResult(wbsActivityDao.getByWbsNode(subtask));
        begin = result.getBegin().toString("dd.MM.yyyy");
        end = result.getEnd().toString("dd.MM.yyyy");
      } catch (final Exception e) {
      }
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

  private String getWbsCode(final SubtaskDO wp, final AbstractWbsNodeDO maybeParent)
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

    final SubtaskDO subtask = getModelObject();
    subtask.setWbsCode(getWbsCode(subtask, parent));

    // Currently there is no possibility (at least I have not found any) to fully validate the subtask before trying to save it. The
    // case of sister tasks with the same title can only be observed by trying to save can catch the exception on error.

    try {
      if (isNew) {
        wbsNodeUtils.changeParentAndSave(subtask, parent);
      } else {
        subtaskDao.update(subtask);
      }
    } catch (final UserException e) {
      error(getString(e.getI18nKey()));
      return;
    }

    try {
      setResponsePage(new ProjectTreePage(WicketWbsUtils.getProject(subtask).getId()));
    } catch (final UserException e) {
      error(getString(e.getI18nKey()));
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
