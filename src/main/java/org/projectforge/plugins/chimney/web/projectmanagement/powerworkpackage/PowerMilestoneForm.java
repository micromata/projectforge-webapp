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
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.RangeValidator;
import org.projectforge.core.UserException;
import org.projectforge.plugins.chimney.activities.WbsActivityDO;
import org.projectforge.plugins.chimney.activities.WbsActivityDao;
import org.projectforge.plugins.chimney.gantt.GanttScheduledActivity;
import org.projectforge.plugins.chimney.gantt.IScheduler;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.MilestoneDO;
import org.projectforge.plugins.chimney.wbs.MilestoneDao;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.web.DetachableDOModel;
import org.projectforge.plugins.chimney.web.WicketWbsUtils;
import org.projectforge.plugins.chimney.web.components.ChimneyUserSelectPanel;
import org.projectforge.plugins.chimney.web.components.PhaseSelectPanel;
import org.projectforge.plugins.chimney.web.projecttree.ProjectTreePage;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.user.UserSelectPanel;

public class PowerMilestoneForm extends TransactionalSubmitForm<MilestoneDO>
{
  private static final long serialVersionUID = -5898477856544440400L;

  @SpringBean
  private WbsActivityDao wbsActivityDao;

  @SpringBean
  private MilestoneDao milestoneDao;

  @SpringBean
  private WicketWbsUtils wbsNodeUtils;

  @SpringBean
  private IScheduler scheduler;

  private IModel<WbsActivityDO> activityModel;

  private final boolean isNew;

  private AbstractWbsNodeDO parent;

  private final transient MilestoneDO milestone;

  private Integer parentId;

  /**
   * Instantiate a new edit form
   * @param id wicket id
   * @param milestone milestone to edit, must not be null
   */
  public PowerMilestoneForm(final String id, final MilestoneDO milestone)
  {
    this(id, milestone, null);
  }

  /**
   * Instantiate a new milestone form
   * @param id wicket id
   * @param milestone milestone to use
   * @param parent new parent of the milestone. If this is not null, the form assumes a new milestone should be created.
   */
  public PowerMilestoneForm(final String id, final MilestoneDO milestone, final AbstractWbsNodeDO parent)
  {
    super(id);
    this.milestone = milestone;
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

    final PowerMilestoneForm self = this;
    final AbstractWbsNodeDO parent = this.parent;

    setModel(parent);
    setActivityModel();

    final IModel<ProjectDO> projectModel = wbsNodeUtils.getModelFor(WicketWbsUtils.getProject(parent != null ? parent : getModelObject()));
    add(new Label("projectTitle", new PropertyModel<String>(projectModel, "title")));
    add(new Label("projectLead", new PropertyModel<String>(new PropertyModel<PFUserDO>(projectModel, "responsibleUser"), "fullname")));

    addDateField(getActivityModelObject(), getModelObject());

    if (!isNew) {
      add(new PredecessorRelationsPanel("predecessor_relations_panel", self, getActivityModelObject(), getModelObject()));
    } else {
      add(new WebMarkupContainer("predecessor_relations_panel").setVisible(false));
    }

    add(new TextField<String>("title").setRequired(true).add(RangeValidator.minimum(3)));
    add(new Label("wbsCode"));
    addResponsibleUserPanel();
    add(new PhaseSelectPanel<MilestoneDO>("phase_panel", getModel(), new Model<AbstractWbsNodeDO>(parent != null ? parent : getModelObject().getParent()), projectModel));

    add(new TextField<String>("shortDescription").add(RangeValidator.minimum(3)));
    add(new TextArea<String>("description"));
  }

  private void setModel(final AbstractWbsNodeDO parent)
  {
    final MilestoneDO milestone = this.milestone;
    milestone.setWbsCode(getWbsCode(milestone, parent));

    if (isNew) {
      setTransientModel(milestone);
    } else {
      setPersistentModel(milestone);
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

  private void setPersistentModel(final MilestoneDO milestone)
  {
    setModel(new CompoundPropertyModel<MilestoneDO>(new DetachableDOModel<MilestoneDO, MilestoneDao>(milestone, milestoneDao)));
  }

  private void setTransientModel(final MilestoneDO milestone)
  {
    setModel(new CompoundPropertyModel<MilestoneDO>(new Model<MilestoneDO>(milestone)));
  }

  private void addDateField(final WbsActivityDO activity, final MilestoneDO milestone)
  {
    String date = "?";
    try {
      scheduler.schedule(wbsActivityDao.getByWbsNode(WicketWbsUtils.getProject(milestone)));
      final GanttScheduledActivity result = scheduler.getResult(activity);
      date = result.getEnd().toString("dd.MM.yyyy");
    } catch (final Exception e) {
    }
    add(new Label("date", date));
  }

  private void addResponsibleUserPanel()
  {
    final UserSelectPanel userSelectPanel = new ChimneyUserSelectPanel("responsibleUser", new PropertyModel<PFUserDO>(getModel(),
        "responsibleUser"));
    add(userSelectPanel);
    userSelectPanel.init();
  }

  private String getWbsCode(final MilestoneDO wp, final AbstractWbsNodeDO maybeParent)
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

    final MilestoneDO milestone = getModelObject();
    milestone.setWbsCode(getWbsCode(milestone, parent));

    // Currently there is no possibility (at least I have not found any) to fully validate the milestone before trying to save it. The
    // case of sister tasks with the same title can only be observed by trying to save can catch the exception on error.

    try {
      if (isNew) {
        wbsNodeUtils.changeParentAndSave(milestone, parent);
      } else {
        milestoneDao.update(milestone);
      }
    } catch (final UserException e) {
      error(getString(e.getI18nKey()));
      return;
    }

    try {
      flushTransactionalSubmitActions();
      setResponsePage(new ProjectTreePage(WicketWbsUtils.getProject(milestone).getId()));
    } catch (final UserException e) {
      error(getString(e.getI18nKey()));
    }
  }

  @Override
  public void renderHead(final IHeaderResponse response)
  {
    super.renderHead(response);
    response.render(JavaScriptHeaderItem.forScript("  $(document).ready(function() { $(\"#progressSlider\").slider({ max: 100, min: 0, value: "
        + getModelObject().getProgress()
        + ", "
        + "slide: function(event, ui) {  $(\"#p_progress\").val ( ui.value ); } }); });", "progresssliderscript"));
  }

}
