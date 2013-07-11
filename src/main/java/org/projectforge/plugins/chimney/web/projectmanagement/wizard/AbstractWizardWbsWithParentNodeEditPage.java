/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projectmanagement.wizard;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.web.SessionAttribute;
import org.projectforge.plugins.chimney.web.WicketWbsUtils;
import org.projectforge.plugins.chimney.web.components.PhaseSelectPanel;
import org.projectforge.plugins.chimney.web.components.WbsNodeSelectPanel;

/**
 * Adds a form field for selecting a parent node. Not all WbsNodes have a parent, so AbstractWbsNodeEditPage would not be the right place.
 * @author Sweeps <pf@byte-storm.com>
 */
public abstract class AbstractWizardWbsWithParentNodeEditPage<T extends AbstractWbsNodeDO> extends AbstractWizardWbsNodeEditPage<T>
{
  private static final long serialVersionUID = -6749675841168214378L;

  @SpringBean(name = "wicketWbsUtils")
  protected WicketWbsUtils wbsUtils;

  protected final IModel<AbstractWbsNodeDO> parentModel;

  protected int wbsAutoIncrementValue = -1;

  /**
   * Default constructor used by navigation
   * @param parameters HTTP Request parameters
   * @param newInstance a new instance of the data object class
   */
  public AbstractWizardWbsWithParentNodeEditPage(final PageParameters parameters, final T newInstance)
  {
    this(parameters, null, newInstance);
  }

  /**
   * Constructor for editing an existing wbs node
   * @param parameters HTTP Request parameters
   * @param wbsNodeId Id of the wbs node to be edited
   */
  public AbstractWizardWbsWithParentNodeEditPage(final PageParameters parameters, final int wbsNodeId, final T prototype)
  {
    super(parameters);
    final IModel<T> model = wbsUtils.getModelFor(wbsUtils.getById(wbsNodeId, prototype));
    parentModel = wbsUtils.getModelFor(model.getObject().getParent());
    init(model, false);
  }

  /**
   * Constructor for creating a new wbs data object using the given parent node. If parent is null, it is attempted to set the parent node
   * to the last edited project
   * @param parameters HTTP Request parameters
   * @param parent An optional proposed parent wbs node
   * @param newInstance a new instance of the data object class
   */
  public AbstractWizardWbsWithParentNodeEditPage(final PageParameters parameters, final AbstractWbsNodeDO parent, final T newInstance)
  {
    super(parameters);
    final IModel<T> model = new Model<T>(newInstance);
    if (parent == null) {
      // no parent given; try to get default parent from the session
      final AbstractWbsNodeDO projectNode = getProjectFromSession();
      if (projectNode != null)
        parentModel = wbsUtils.getModelFor(projectNode);
      else
        parentModel = new Model<AbstractWbsNodeDO>(); // no default parent
    } else {
      // set parent from parameter
      parentModel = wbsUtils.getModelFor(parent);
    }

    if (parentModel.getObject() != null)
      model.getObject().setWbsCode(getGeneratedWbsCode());

    init(model, true);
  }

  /**
   * Convenience method that gets the next value of the auto increment field of the parent node. This number is used to autogenerate a wbs
   * code. Once the value is calculated, it is cached, so that subsequent calls to this method will return the same value.
   * @return The auto increment value used to generate a wbs code
   */
  protected int getWbsAutoIncrement()
  {
    if (wbsAutoIncrementValue >= 0)
      return wbsAutoIncrementValue;

    if (parentModel != null && parentModel.getObject() != null)
      wbsAutoIncrementValue = parentModel.getObject().getAutoIncrementChildren() + 1;

    return wbsAutoIncrementValue;
  }

  /**
   * Generates a wbs code concatening the parents wbs code with a period and the output of {@link #getWbsAutoIncrement()}.
   * @return A generated wbs code in the format &lt;parent code&gt;.&lt;autoincrement value&gt;
   */
  protected String getGeneratedWbsCode()
  {
    if (parentModel != null && parentModel.getObject() != null)
      return parentModel.getObject().getWbsCode() + "." + getWbsAutoIncrement();
    return null;
  }

  /**
   * @return The project of the wbs node that has most recently been edited
   */
  protected AbstractWbsNodeDO getProjectFromSession()
  {
    final Serializable projectFromSession = getSession().getAttribute(SessionAttribute.LAST_USED_PROJECT_ID);
    if (projectFromSession instanceof Integer) {
      return wbsUtils.getById(projectFromSession, ProjectDO.prototype);
    }
    return null;
  }

  @Override
  protected void init(final IModel<T> wbsNodeModel, final boolean isNew)
  {
    super.init(wbsNodeModel, isNew);
    form.add(new WbsNodeSelectPanel("form_top", parentModel, wbsNodeModel, getString("plugins.chimney.editwbsnode.parent")) {
      private static final long serialVersionUID = -6621208703669454029L;

      @Override
      protected void onModalWindowClosed(final IModel<AbstractWbsNodeDO> model, final AjaxRequestTarget target)
      {
        if (isNew) {
          final String wbsCode = getGeneratedWbsCode();
          AbstractWizardWbsWithParentNodeEditPage.this.wbsNodeModel.getObject().setWbsCode(wbsCode);
          target.add(wbsCodeTextField);
        }
      }
    });
  }

  @Override
  protected void onFormSubmit(final IModel<T> model)
  {
    updateParentAndSave(model);
  }

  @Override
  protected void onFormSubmitAndReturn(final IModel<T> model)
  {
    updateParentAndSave(model);

    gotoProjectTreePage(model.getObject(), getSubmitSuccessInfo(model));
  }

  private void updateParentAndSave(final IModel<T> model)
  {
    final T dataObject = model.getObject();
    final AbstractWbsNodeDO parentObject = parentModel.getObject();
    // the parent object has been reloaded from database on form submit,
    // so we have to make sure that the auto-increment value is incremented again to be updated correctly.

    // wbsUtils is updated to take care of that, no need to do it here
    // if (isNew && getWbsAutoIncrement() >=0 && parentObject.getAutoIncrementChildren() < getWbsAutoIncrement())
    // parentObject.autoIncrementAndGet();
    wbsUtils.changeParentAndSave(dataObject, parentObject);
  }

  abstract protected String getSubmitSuccessInfo(IModel<T> model);

  @Override
  protected void addAdditionalFields(final String id, final Form<T> form, final IModel<T> wbsNodeModel)
  {
    final IModel<ProjectDO> rootModel = new Model<ProjectDO>();

    if (parentModel != null && parentModel.getObject() != null) {
      final ProjectDO project = WicketWbsUtils.getProject(parentModel.getObject());
      rootModel.setObject(project);
    }

    final RepeatingView repeater = new RepeatingView(id);
    form.add(repeater);

    repeater.add(new PhaseSelectPanel<T>(repeater.newChildId(), wbsNodeModel, parentModel, rootModel));
  }

}
