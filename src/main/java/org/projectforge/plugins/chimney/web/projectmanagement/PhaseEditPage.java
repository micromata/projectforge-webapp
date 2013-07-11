/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projectmanagement;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.core.UserException;
import org.projectforge.plugins.chimney.utils.spring.RunInTransactionService;
import org.projectforge.plugins.chimney.wbs.PhaseDO;
import org.projectforge.plugins.chimney.wbs.PhaseDao;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.wbs.WbsNodeUtils;
import org.projectforge.plugins.chimney.web.AbstractSecuredChimneyPage;
import org.projectforge.plugins.chimney.web.DetachableDOModel;
import org.projectforge.plugins.chimney.web.navigation.BreadcrumbConstants;
import org.projectforge.plugins.chimney.web.navigation.NavigationConstants;
import org.projectforge.plugins.chimney.web.projecttree.PhasePlanningTreePage;
import org.projectforge.web.wicket.flowlayout.ButtonPanel;
import org.projectforge.web.wicket.flowlayout.ButtonType;

public class PhaseEditPage extends AbstractSecuredChimneyPage
{

  private static final long serialVersionUID = -3695965884725026504L;

  public static final String PAGE_ID = "phaseEdit";

  @SpringBean
  private PhaseDao phaseDao;

  private final Integer phaseId;

  protected Form<PhaseDO> form;

  protected TextField<String> wbsCodeTextField;

  @SpringBean(name = "runInTransactionService")
  protected RunInTransactionService run;

  /**
   * @param parameters
   * @param node
   * */
  public PhaseEditPage(final PageParameters parameters, final Integer phaseId)
  {
    super(parameters);
    this.phaseId = phaseId;
  }

  @Override
  protected void onInitialize()
  {
    super.onInitialize();

    addFeedbackPanel();
    addHeading();
    addForm();
  }

  private void addHeading()
  {
    body.add(new Label("heading", getString(getHeadingI18n())));
  }

  private void addForm()
  {

    form = new Form<PhaseDO>("form", getPhaseModel());
    body.add(form);

    addTitleField();
    addDescField();

    addSaveButton();

  }

  private CompoundPropertyModel<PhaseDO> getPhaseModel()
  {
    final PhaseDO node = phaseDao.getOrLoad(phaseId);
    return new CompoundPropertyModel<PhaseDO>(new DetachableDOModel<PhaseDO, PhaseDao>(node, phaseDao));
  }

  private void addDescField()
  {
    form.add(new TextArea<String>("description").setOutputMarkupId(true));
  }

  private void addSaveButton()
  {
    form.add(new ButtonPanel("submit", getString("plugins.chimney.editpage.save"), new Button("button") {

      private static final long serialVersionUID = 3622140195484212172L;

      @Override
      public void onSubmit()
      {
        try {
          run.inTransaction(new Runnable() {
            @Override
            public void run()
            {
              onFormSubmit(form.getModel());
            }
          });
        } catch (final UserException ex) {
          form.error(translateParams(ex));
        }
      }
    }, ButtonType.DEFAULT_SUBMIT));
  }

  private void addTitleField()
  {
    form.add(new TextField<String>("title").setRequired(true).setOutputMarkupId(true));
  }

  /**
   * Perform actions here after the user submitted the form
   * @param model Model of the WbsNodeDO of the submitted object
   */
  protected void onFormSubmit(final IModel<PhaseDO> model)
  {
    phaseDao.update(model.getObject());
    goToPhasePlanningTreePage(model.getObject(), getSuccessMessage(model));
  }

  private String getSuccessMessage(final IModel<PhaseDO> model)
  {
    return getString("plugins.chimney.editphase.phasesaved") + " " + model.getObject().getTitle();
  }

  protected void goToPhasePlanningTreePage(final PhaseDO node, final String infoText)
  {
    Validate.notNull(node);
    final ProjectDO project = WbsNodeUtils.getProject(node);
    final PhasePlanningTreePage newPage = new PhasePlanningTreePage(project.getId());
    if (infoText != null && !infoText.isEmpty())
      newPage.info(infoText);
    setResponsePage(newPage);
  }

  @Override
  protected String getTitle()
  {
    return getString("plugins.chimney.editphase.title");
  }

  @Override
  protected void insertBreadcrumbItems(final List<String> items)
  {
    items.add(BreadcrumbConstants.PROJECT_PLANNING);
    items.add(BreadcrumbConstants.PHASE_PLANNING);
    items.add(BreadcrumbConstants.EDIT_PHASE);
  }

  /**
   * @return An i18n string denoting the string resource displayed as heading
   */
  protected String getHeadingI18n()
  {
    return "plugins.chimney.editphase.heading";
  }

  @Override
  protected String getNavigationBarName()
  {
    return NavigationConstants.MAIN;
  }

}
