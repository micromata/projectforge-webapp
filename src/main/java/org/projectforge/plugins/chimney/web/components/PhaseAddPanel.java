/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.components;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.core.I18nEnum;
import org.projectforge.core.UserException;
import org.projectforge.plugins.chimney.utils.spring.RunInTransactionService;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.MilestoneDO;
import org.projectforge.plugins.chimney.wbs.PhaseDO;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.web.WicketWbsUtils;
import org.projectforge.plugins.chimney.web.projecttree.PhasePlanningTreePage;
import org.projectforge.plugins.chimney.web.utils.WicketUtil;
import org.projectforge.task.TaskStatus;
import org.projectforge.web.wicket.flowlayout.ButtonPanel;
import org.projectforge.web.wicket.flowlayout.ButtonType;

/**
 * A panel component with an input field, a type dropdown and a submit button
 * for adding phases and milestones to a project
 * @author Sweeps <pf@byte-storm.com>
 */
public class PhaseAddPanel extends Panel
{

  private static final long serialVersionUID = 2261460422550623942L;

  @SpringBean(name = "wicketWbsUtils")
  private WicketWbsUtils wbsUtils;

  @SpringBean(name = "runInTransactionService")
  protected RunInTransactionService run;

  private Integer projectId = Integer.MIN_VALUE;
  private IModel<String> titleModel;
  private PhasePlanningElementType dropdownSelection;

  public PhaseAddPanel(final String id)
  {
    super(id, null);
  }

  public void setProjectId(final Integer projectId)
  {
    this.projectId = projectId;
  }

  @Override
  public boolean isVisible()
  {
    return getProject() != null;
  }

  @Override
  protected void onInitialize()
  {
    super.onInitialize();

    this.titleModel = new Model<String>();

    final Form<AbstractWbsNodeDO> form = new Form<AbstractWbsNodeDO>("form") {
      private static final long serialVersionUID = 6762245028062603362L;

      @Override
      protected void onSubmit()
      {
        submit();
      }
    };
    add(form);

    final DropDownChoice<TaskStatus> dropDownChoice = WicketUtil.getNewDropDownChoice(this, "elementChoice", "dropdownSelection",
        Model.of(this), PhasePlanningElementType.values());
    dropDownChoice.setNullValid(false);
    dropDownChoice.setRequired(true);
    form.add(dropDownChoice);

    form.add(new TextField<String>("title", titleModel).setRequired(true));
    form.add(new ButtonPanel("submit", getString("plugins.chimney.phaseplanning.add"), new Button("button"), ButtonType.DEFAULT_SUBMIT));

  }

  private void submit()
  {
    run.inTransaction(new Runnable() {

      @Override
      public void run()
      {

        final ProjectDO project = getProject();
        final PhasePlanningTreePage page = new PhasePlanningTreePage(new Integer[] { project.getId()});

        AbstractWbsNodeDO newNode;
        if (dropdownSelection == PhasePlanningElementType.MILESTONE) {
          newNode = new MilestoneDO();
          newNode.setWbsCode(getGeneratedWbsCodeFrom(project));
        } else if (dropdownSelection == PhasePlanningElementType.PHASE) {
          newNode = new PhaseDO();
          newNode.setWbsCode("P");
        } else throw new RuntimeException("User selected non-existing type!?");

        newNode.setTitle(titleModel.getObject());
        project.addChild(newNode);

        try {
          wbsUtils.saveOrUpdate(newNode);
          wbsUtils.saveOrUpdate(project);
        } catch (final UserException ex) {
          page.error(page.translateParams(ex));
        }

        setResponsePage(page);

      }
    });
  }

  private ProjectDO getProject()
  {
    if (projectId < 0)
      return null;
    return wbsUtils.getById(projectId, ProjectDO.prototype);
  }

  /**
   * Generates a wbs code concatening the parents wbs code with a period and the output of {@link #getWbsAutoIncrement()}.
   * @param project
   * @return A generated wbs code in the format &lt;parent code&gt;.&lt;autoincrement value&gt;
   */
  protected String getGeneratedWbsCodeFrom(final ProjectDO project)
  {
    if (project != null) {
      return project.getWbsCode() + "." + project.autoIncrementAndGet();
    }
    return null;
  }

  public PhasePlanningElementType getDropdownSelection()
  {
    return dropdownSelection;
  }

  public void setDropdownSelection(final PhasePlanningElementType dropdownSelection)
  {
    this.dropdownSelection = dropdownSelection;
  }

  public enum PhasePlanningElementType implements I18nEnum
  {
    MILESTONE("milestone"), PHASE("phase");

    private String key;

    /**
     * The key will be used e. g. for i18n.
     * @return
     */
    public String getKey()
    {
      return key;
    }

    public String getI18nKey()
    {
      return "plugins.chimney.enum.phaseplanningelementtype." + key;
    }

    PhasePlanningElementType(final String key)
    {
      this.key = key;
    }
  }

}
