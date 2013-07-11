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
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.core.UserException;
import org.projectforge.plugins.chimney.activities.DependencyRelationDO;
import org.projectforge.plugins.chimney.activities.DependencyRelationDao;
import org.projectforge.plugins.chimney.activities.DependencyRelationType;
import org.projectforge.plugins.chimney.activities.WbsActivityDO;
import org.projectforge.plugins.chimney.activities.WbsActivityDao;
import org.projectforge.plugins.chimney.resources.ImageResources;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.web.DetachableChangeableDOModel;
import org.projectforge.plugins.chimney.web.components.ChimneyJodaPeriodField;
import org.projectforge.plugins.chimney.web.components.WbsNodeModalSelectWindowPanel;
import org.projectforge.plugins.chimney.web.components.WbsNodeSelectPanel;
import org.projectforge.plugins.chimney.web.utils.WicketUtil;

public class PredecessorRelationForm extends Form<DependencyRelationDO>
{
  private static final long serialVersionUID = 2968147680143560714L;

  @SpringBean
  DependencyRelationDao dependencyRelationDao;

  @SpringBean
  WbsActivityDao wbsActivityDao;

  private IModel<Boolean> _destroy;

  private IModel<Integer> newPredecessorId;

  private WbsActivityDO transientSuccessor;

  private final DependencyRelationDO predecessorRelation;

  private final AbstractWbsNodeDO someWbsNodeInTheProjectTree;

  private final TransactionalSubmitForm<?> powerWorkpackageForm;

  @Override
  protected void onSubmit()
  {
    final DependencyRelationDO dep = getModelObject();

    if (_destroy.getObject()) {
      if (dep.getId() != null && !dep.isDeleted()) {
        powerWorkpackageForm.addTransactionalSubmitAction(new Runnable() {
          @Override
          public void run()
          {
            dependencyRelationDao.markAsDeleted(dep);
          }
        });
      }
      return;
    }

    if (dep.getSuccessor() == null) {
      // the dependency is new
      dep.setAndPropagateSuccessor(transientSuccessor);
    }

    if (newPredecessorId != null) {
      final WbsActivityDO act = wbsActivityDao.getById(newPredecessorId.getObject());
      dep.setAndPropagatePredecessor(act);
    }

    if (dep.getPredecessor() != null) {
      try {
        dep.ensureValid();
        powerWorkpackageForm.addTransactionalSubmitAction(new Runnable() {
          @Override
          public void run()
          {
            dependencyRelationDao.saveOrUpdate(dep);
            setModel(new CompoundPropertyModel<DependencyRelationDO>(
                new DetachableChangeableDOModel<DependencyRelationDO, DependencyRelationDao>(dep, dependencyRelationDao)));
            newPredecessorId = null;
          }
        });
      } catch (final UserException e) {
        error(getString(e.getI18nKey()));
      }
    }
  }

  public PredecessorRelationForm(final String id, final DependencyRelationDO predecessorRelation,
      final AbstractWbsNodeDO someNodeInTheProject, final TransactionalSubmitForm<?> powerWorkpackageForm)
  {
    super(id);
    this.predecessorRelation = predecessorRelation;
    this.someWbsNodeInTheProjectTree = someNodeInTheProject;
    this.powerWorkpackageForm = powerWorkpackageForm;
  }

  @Override
  public void onInitialize()
  {
    super.onInitialize();

    IModel<DependencyRelationDO> predRelModel;
    if (predecessorRelation.getId() == null) {
      predRelModel = new Model<DependencyRelationDO>(predecessorRelation);
    } else {
      predRelModel = new DetachableChangeableDOModel<DependencyRelationDO, DependencyRelationDao>(predecessorRelation,
          dependencyRelationDao);
    }

    setModel(new CompoundPropertyModel<DependencyRelationDO>(predRelModel));

    _destroy = new Model<Boolean>(false);
    transientSuccessor = predecessorRelation.getSuccessor();

    Model<AbstractWbsNodeDO> predNodeModel;
    final WbsActivityDO predecessor = predecessorRelation.getPredecessor();
    if (predecessor != null) {
      predNodeModel = new Model<AbstractWbsNodeDO>(predecessor.getWbsNode());
    } else {
      predNodeModel = new Model<AbstractWbsNodeDO>();
    }
    final WbsNodeSelectPanel wbsNodeSelectPanel = new WbsNodeSelectPanel("predNodeSelect", predNodeModel,
        getString("plugins.chimney.editdependency.predecessor")) {

      private static final long serialVersionUID = 7256050161354796736L;

      @Override
      protected void onModalWindowClosed(final IModel<AbstractWbsNodeDO> model, final AjaxRequestTarget target)
      {
        final AbstractWbsNodeDO node = model.getObject();
        if (node != null) {
          final WbsActivityDO activity = wbsActivityDao.getByOrCreateFor(node);
          if (predecessor != null && activity.getId().equals(predecessor.getId())) {
            return;
          }
          newPredecessorId = new Model<Integer>(activity.getId());
        }
      }
    };

    final WbsNodeModalSelectWindowPanel modalWindowPanel = wbsNodeSelectPanel.getModalWindowPanel();
    modalWindowPanel.setProjectSelectorVisible(false);
    modalWindowPanel.setDisplayedProjectByWbsNode(someWbsNodeInTheProjectTree);

    add(wbsNodeSelectPanel);
    add(WicketUtil.getNewDropDownChoice(this, "type", "type", getModel(), DependencyRelationType.values()));
    add(new ChimneyJodaPeriodField("offset"));

    final PredecessorRelationForm formRef = this;

    final AjaxLink<?> destroyLink = new AjaxLink<Object>("destroy_link") {
      private static final long serialVersionUID = -4823126063367092069L;

      @Override
      public void onClick(final AjaxRequestTarget target)
      {
        _destroy.setObject(true);
        formRef.add(new AttributeModifier("style", "opacity:0.25;"));
        target.add(formRef);
      }
    };
    destroyLink.add(new Image("destroy_image", ImageResources.DELETE_SMALL_IMAGE));
    add(destroyLink);
  }

  @Override
  protected void onValidateModelObjects()
  {
    try {
      // ensure that the dependency can be saved
      getModelObject().ensureValid();
    } catch (final UserException ex) {
      error(getString(ex.getI18nKey()));
    }
  }
}
