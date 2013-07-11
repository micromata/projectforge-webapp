/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projectmanagement.powerworkpackage;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.joda.time.Period;
import org.projectforge.plugins.chimney.resourceplanning.ResourceAssignmentDO;
import org.projectforge.plugins.chimney.resourceplanning.ResourceAssignmentDao;
import org.projectforge.plugins.chimney.resources.ImageResources;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.web.DetachableChangeableMaybeTransientDOModel;
import org.projectforge.plugins.chimney.web.components.ChimneyJodaPeriodField;
import org.projectforge.plugins.chimney.web.components.ChimneyUserSelectPanel;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.user.UserSelectPanel;

public class ResourceAssignmentForm extends Form<ResourceAssignmentDO>
{
  @Override
  protected void onSubmit()
  {
    final ResourceAssignmentDO resAs = getModelObject();

    if (_destroy.getObject()) {
      if (resAs.getId() != null && !resAs.isDeleted()) {
        powerWorkpackageForm.addTransactionalSubmitAction(new Runnable() {
          @Override
          public void run()
          {
            resDao.markAsDeleted(resAs);
          }
        });
      }
      return;
    }

    if (resAs.getWbsNode() == null) {
      resAs.setWbsNode(node.getObject());
    }

    if (resAs.getUser() != null) {
      powerWorkpackageForm.addTransactionalSubmitAction(new Runnable() {
        @Override
        public void run()
        {
          resDao.saveOrUpdate(resAs);
          setModel(new CompoundPropertyModel<ResourceAssignmentDO>(
              new DetachableChangeableMaybeTransientDOModel<ResourceAssignmentDO, ResourceAssignmentDao>(resAs, resDao)));
        }
      });
    }
  }

  /**
   * 
   */
  private static final long serialVersionUID = 5439242322332977792L;

  @SpringBean
  ResourceAssignmentDao resDao;

  IModel<AbstractWbsNodeDO> node;

  IModel<Boolean> _destroy;

  private final ResourceAssignmentDO resAs;

  private final PowerWorkpackageForm powerWorkpackageForm;

  public ResourceAssignmentForm(final String id, final ResourceAssignmentDO resAs, final PowerWorkpackageForm powerWorkpackageForm)
  {
    super(id);
    this.resAs = resAs;
    this.powerWorkpackageForm = powerWorkpackageForm;
  }

  @Override
  public void onInitialize()
  {
    super.onInitialize();

    _destroy = new Model<Boolean>(false);
    node = new Model<AbstractWbsNodeDO>(resAs.getWbsNode());
    IModel<ResourceAssignmentDO> resModel;

    if (resAs.getId() == null) {
      resModel = new Model<ResourceAssignmentDO>(resAs);
    } else {
      resModel = new DetachableChangeableMaybeTransientDOModel<ResourceAssignmentDO, ResourceAssignmentDao>(resAs, resDao);
    }
    setModel(new CompoundPropertyModel<ResourceAssignmentDO>(resModel));

    final UserSelectPanel userSelectPanel = new ChimneyUserSelectPanel("user", new PropertyModel<PFUserDO>(getModel(), "user"));
    add(userSelectPanel);
    userSelectPanel.init();

    add(new ChimneyJodaPeriodField("plannedEffort", new PropertyModel<Period>(getModel(), "plannedEffort")));

    final ResourceAssignmentForm selfRef = this;
    final AjaxLink< ? > destroyLink = new AjaxLink<Object>("destroy_link") {
      private static final long serialVersionUID = -8125270640163334166L;

      @Override
      public void onClick(final AjaxRequestTarget target)
      {
        _destroy.setObject(true);
        selfRef.add(new AttributeModifier("style", "opacity:0.25;"));
        selfRef.setEnabled(false);
        target.add(selfRef);
      }
    };
    destroyLink.add(new Image("destroy_image", ImageResources.DELETE_SMALL_IMAGE));
    add(destroyLink);
  }

}
