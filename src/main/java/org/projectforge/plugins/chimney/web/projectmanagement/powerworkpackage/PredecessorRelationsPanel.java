/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projectmanagement.powerworkpackage;

import java.util.Set;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.projectforge.plugins.chimney.activities.DependencyRelationDO;
import org.projectforge.plugins.chimney.activities.WbsActivityDO;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;

public class PredecessorRelationsPanel extends Panel
{
  private static final long serialVersionUID = 2278303860660289282L;
  private final TransactionalSubmitForm<?> self;
  private final WbsActivityDO wbsActivityDO;
  private final AbstractWbsNodeDO node;

  public PredecessorRelationsPanel(final String id, final TransactionalSubmitForm<?> self, final WbsActivityDO wbsActivityDO, final AbstractWbsNodeDO node)
  {
    super(id);
    this.self = self;
    this.wbsActivityDO = wbsActivityDO;
    this.node = node;
  }

  @Override
  protected void onInitialize()
  {
    super.onInitialize();

    final WbsActivityDO activity = wbsActivityDO;
    final AbstractWbsNodeDO node = this.node;


    final WebMarkupContainer predecessorRelationsDiv = new WebMarkupContainer("predecessor_relations");
    predecessorRelationsDiv.setOutputMarkupId(true);
    add(predecessorRelationsDiv);

    final Set<DependencyRelationDO> predRels = activity.getPredecessorRelations();
    final RepeatingView repView = new RepeatingView("predecessor_relation_form");
    repView.setOutputMarkupId(true);
    predecessorRelationsDiv.add(repView);

    for (final DependencyRelationDO predRel : predRels) {
      final PredecessorRelationForm predForm = new PredecessorRelationForm(repView.newChildId(), predRel, node, self);
      repView.add(predForm);
    }

    final AjaxLink< ? > ajaxLink = new AjaxLink<Object>("add_predecessor_link") {
      private static final long serialVersionUID = 6876154721339199145L;

      @Override
      public void onClick(final AjaxRequestTarget target)
      {
        final DependencyRelationDO predRel = new DependencyRelationDO();
        predRel.setAndPropagateSuccessor(activity);
        final PredecessorRelationForm predForm = new PredecessorRelationForm(repView.newChildId(), predRel, node, self);
        repView.add(predForm);

        target.prependJavaScript(String.format("var item=document.createElement('%s');item.id='%s';" + "Wicket.$('%s').appendChild(item);",
            "tr", predForm.getMarkupId(), predecessorRelationsDiv.getMarkupId()));

        target.add(predForm);
      }
    };
    add(ajaxLink);
  }

}
