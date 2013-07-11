/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projectmanagement.wizard;

import java.util.List;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.core.Priority;
import org.projectforge.core.UserException;
import org.projectforge.plugins.chimney.utils.spring.RunInTransactionService;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.wbs.WbsNodeUtils;
import org.projectforge.plugins.chimney.web.SessionAttribute;
import org.projectforge.plugins.chimney.web.components.ResponsibleUserPanel;
import org.projectforge.plugins.chimney.web.navigation.NavigationConstants;
import org.projectforge.plugins.chimney.web.utils.WicketUtil;
import org.projectforge.task.TaskStatus;
import org.projectforge.web.wicket.flowlayout.ButtonPanel;
import org.projectforge.web.wicket.flowlayout.ButtonType;

/**
 * Adds a form and form fields for generic fields that apply to all wbs nodes,
 * and adds an abstract form submit event handler.
 * @author Sweeps <pf@byte-storm.com>
 */
public abstract class AbstractWizardWbsNodeEditPage<T extends AbstractWbsNodeDO> extends AbstractChimneyWizardPage
{
  private static final long serialVersionUID = -406870004247796446L;

  protected IModel<T> wbsNodeModel;
  protected boolean isNew;
  protected Form<T> form;
  private boolean initialized = false;

  protected TextField<String> wbsCodeTextField;

  @SpringBean(name="runInTransactionService")
  protected RunInTransactionService run;

  /**
   * Constructor for an abstract form for WbsNodeDOs. Form elements are automatically initialized using the given model.
   * @param parameters
   * @param wbsNodeModel Model of the edited WbsNodeDO
   * @param isNew true if a new node is created, false if an existing node is edited
   */
  public AbstractWizardWbsNodeEditPage(final PageParameters parameters, final IModel<T> wbsNodeModel, final boolean isNew) {
    super(parameters, true); // defer navigation creation to allow edit or new check first
    init(wbsNodeModel, isNew);
  }

  /**
   * Constructor for an abstract form for WbsNodeDOs. Form elements are not automatically initialized.
   * You must explicitly call {@link #init(IModel, boolean)} in your constructor, otherwise the page will fail to render.
   * @param parameters
   */
  public AbstractWizardWbsNodeEditPage(final PageParameters parameters) {
    super(parameters, true); // defer navigation creation to allow edit or new check first
  }

  /**
   * Creates navigation bar and form using the given the given model
   * @param wbsNodeModel
   * @param isNew
   */
  protected void init(final IModel<T> wbsNodeModel, final boolean isNew) {
    if (initialized) return;
    this.wbsNodeModel = wbsNodeModel;
    this.isNew = isNew;
    createNavigation();
    addFeedbackPanel();
    addForm();

    if (!isNew) {
      // save the last edited page id for use by other pages
      setSessionLastProjectId();
    }

    addAdditionalFields("additional_fields", form, wbsNodeModel);
    initialized = true;
  }

  /**
   * Can be used to insert additional form fields after the progress bar and before the description field.
   * Add a component using the wicket id in the id parameter to the form.
   * By default, this method adds an empty WebMarkupContainer without any functionality.
   * Override this method to change this.
   * @param id The wicket id that has to be used by the component
   * @param form The form that the new component must be added to
   * @param wbsNodeModel Model of the wbs node this form is supposed to edit
   */
  protected void addAdditionalFields(final String id, final Form<T> form, final IModel<T> wbsNodeModel)
  {
    form.add(new WebMarkupContainer(id));
  }

  private void setSessionLastProjectId()
  {
    final ProjectDO project = WbsNodeUtils.getProject(wbsNodeModel.getObject());
    if (project != null)
      getSession().setAttribute(SessionAttribute.LAST_USED_PROJECT_ID, project.getId());
  }

  private void addForm()
  {
    form = new Form<T>("form", new CompoundPropertyModel<T>(wbsNodeModel)) {
      private static final long serialVersionUID = 2751506990747617101L;

      @Override
      protected void onBeforeRender()
      {
        // make "form_top" and "form_bottom" containers invisible
        // if they have not been set otherwise
        if (get("form_top") == null)
          add(new WebMarkupContainer("form_top").setVisible(false));
        if (get("form_bottom") == null)
          add(new WebMarkupContainer("form_bottom").setVisible(false));
        super.onBeforeRender();
      }
    };

    body.add(new Label("heading", getString(getHeadingI18n())));
    wbsCodeTextField = new TextField<String>("wbsCode");
    wbsCodeTextField.setOutputMarkupId(true);
    wbsCodeTextField.setRequired(true);
    form.add(wbsCodeTextField);
    form.add(new ResponsibleUserPanel<T>("responsibleUser", wbsNodeModel));
    form.add(new TextField<String>("title").setRequired(true).setOutputMarkupId(true));
    form.add(new TextField<String>("shortDescription").setOutputMarkupId(true));
    form.add(new TextField<String>("progress").setOutputMarkupId(true));
    form.add(new TextArea<String>("description").setOutputMarkupId(true));

    //Nutzung der PF Wicketkomponente, die gleich eine Längenbegrenzung im Formular setzt funktioniert nicht wegen Bug in MaxLengthText... Konstruktor
    // (versucht die Property aus der DO zu holen, die ja wegen Durchreichung nicht existiert und schmeißt NullPointerException):
    //form.add(new MaxLengthTextField("wbsCode", new PropertyModel<String>(projectDo, "wbsCode")));
    //form.add(new MaxLengthTextField("title", new PropertyModel<String>(projectDo, "title")));
    //form.add(new MaxLengthTextField("shortDescription", new PropertyModel<String>(projectDo, "shortDescription")));
    //form.add(new MaxLengthTextArea("description", new PropertyModel<String>(projectDo, "description")));

    form.add(new ButtonPanel("submit", getString("plugins.chimney.editpage.save"),
        new Button("button"){
      private static final long serialVersionUID = -4731504357594442987L;

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
        } catch(final UserException ex){
          form.error(translateParams(ex));
        }
      }
    }, ButtonType.DEFAULT_SUBMIT));

    // save and return to project list page, only visible if creating a new node
    form.add(new ButtonPanel("submitAndReturn", getString("plugins.chimney.editpage.saveandreturn"), new Button("button"){
      private static final long serialVersionUID = -8122069034450580512L;

      @Override
      public void onSubmit()
      {
        try{
          run.inTransaction(new Runnable() {
            @Override
            public void run()
            {
              onFormSubmitAndReturn(form.getModel());
            }
          });
        } catch(final UserException ex){
          form.error(translateParams(ex));
        }
      }
    }, ButtonType.DEFAULT_SUBMIT).setVisible(isNew));

    final DropDownChoice<Priority> priorityChoice = WicketUtil.getNewDropDownChoice(this, "priority", "priority", wbsNodeModel, Priority.values());
    priorityChoice.setNullValid(true);
    form.add(priorityChoice);

    final DropDownChoice<TaskStatus> statusChoice = WicketUtil.getNewDropDownChoice(this, "status", "status", wbsNodeModel, TaskStatus.values());
    statusChoice.setNullValid(false).setRequired(true);
    form.add(statusChoice);

    body.add(form);
  }

  @Override
  protected String getTitle()
  {
    return getString("plugins.chimney.createproject.title");
  }

  @Override
  public void renderHead(final IHeaderResponse response) {
    super.renderHead(response);
    response.render(JavaScriptHeaderItem.forScript("  $(document).ready(function() { $(\"#progressSlider\").slider({ max: 100, min: 0, value: "+wbsNodeModel.getObject().getProgress()+", "+
        "slide: function(event, ui) {  $(\"#p_progress\").val ( ui.value ); } }); });", "progresssliderscript"));
  }

  @Override
  protected void insertBreadcrumbItems(final List<String> items)
  {
    // force sub-classes to implement this method
    throw new UnsupportedOperationException();
  }

  @Override
  protected String getNavigationBarName()
  {
    if (isNew)
      return NavigationConstants.WIZARD;
    return NavigationConstants.MAIN;
  }

  /**
   * @return An i18n string denoting the string resource displayed as heading
   */
  protected abstract String getHeadingI18n();


  /**
   * Perform actions here after the user submitted the form
   * @param model Model of the WbsNodeDO of the submitted object
   */
  protected abstract void onFormSubmit(IModel<T> model);

  /**
   * Perform actions here after the user submitted the form with the SubmitAndReturn button
   * @param model Model of the WbsNodeDO of the submitted object
   */
  protected abstract void onFormSubmitAndReturn(IModel<T> model);

}
