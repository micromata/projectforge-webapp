/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2010, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.components;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.lang.Objects;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.PhaseDO;
import org.projectforge.plugins.chimney.wbs.ProjectDO;

/**
 * A panel that either displaya or allows to change the phase of a wbs node.
 * A phase can only be changed, if the wbs node is a direct child of a project node.
 * @author Sweeps <pf@byte-storm.com>
 */
public class PhaseSelectPanel<T extends AbstractWbsNodeDO> extends Panel
{

  private static final long serialVersionUID = -3493227006816427348L;

  private final IModel<ProjectDO> projectModel;

  public PhaseSelectPanel(final String id, final IModel<T> wbsNodeModel, final IModel<AbstractWbsNodeDO> parentNodeModel,
      final IModel<ProjectDO> projectModel)
  {
    super(id, wbsNodeModel);
    this.projectModel = projectModel;

    Validate.notNull(wbsNodeModel);

    final DropDownChoice<PhaseDO> phaseChoice = new DropDownChoice<PhaseDO>("phaseChoice", new PropertyModel<PhaseDO>(wbsNodeModel, "phase"), getChoicesModel(), getChoiceRenderer());
    phaseChoice.setNullValid(true);
    add(phaseChoice);

    final Label label = new Label("phase", new PropertyModel<String>(new PropertyModel<PhaseDO>(parentNodeModel, "phase"), "title"));
    add(label);

    if (parentAndProjectAreEqual(parentNodeModel, projectModel))
      label.setVisible(false);
    else
      phaseChoice.setVisible(false);
  }

  private boolean parentAndProjectAreEqual(final IModel<AbstractWbsNodeDO> parentNodeModel, final IModel<ProjectDO> projectModel)
  {
    if (parentNodeModel == null || projectModel == null || parentNodeModel.getObject() == null)
      return false;
    return Objects.equal(parentNodeModel.getObject(), projectModel.getObject());
  }

  private IModel<List<PhaseDO>> getChoicesModel()
  {
    return new LoadableDetachableModel<List<PhaseDO>>() {

      private static final long serialVersionUID = -7131396658363341551L;

      @Override
      protected List<PhaseDO> load()
      {

        final List<PhaseDO> list = new ArrayList<PhaseDO>();

        if (projectModel == null || projectModel.getObject() == null)
          return list;

        for (int i = 0; i < projectModel.getObject().childrenCount(); i++) {
          final AbstractWbsNodeDO node = projectModel.getObject().getChild(i);

          if (node instanceof PhaseDO && node.childrenCount() == 0)
            list.add((PhaseDO) node);
        }

        return list;
      }
    };
  }

  private IChoiceRenderer<PhaseDO> getChoiceRenderer()
  {
    return new IChoiceRenderer<PhaseDO>() {

      private static final long serialVersionUID = 6813936885003587561L;

      @Override
      public Object getDisplayValue(final PhaseDO phase)
      {
        return phase.getTitle();
      }

      @Override
      public String getIdValue(final PhaseDO phase, final int arg1)
      {
        return phase.getId().toString();
      }
    };
  }

}
