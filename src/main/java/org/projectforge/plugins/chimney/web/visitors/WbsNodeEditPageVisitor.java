/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////
/**
 * This Visitor retrieves the correct EditPage for a given WbsNodeDO
 * 
 */
package org.projectforge.plugins.chimney.web.visitors;

import java.io.Serializable;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.MilestoneDO;
import org.projectforge.plugins.chimney.wbs.PhaseDO;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.wbs.SubtaskDO;
import org.projectforge.plugins.chimney.wbs.WorkpackageDO;
import org.projectforge.plugins.chimney.wbs.visitors.IWbsNodeVisitor;
import org.projectforge.plugins.chimney.web.projectmanagement.PhaseEditPage;
import org.projectforge.plugins.chimney.web.projectmanagement.powerworkpackage.PowerMilestoneEditPage;
import org.projectforge.plugins.chimney.web.projectmanagement.powerworkpackage.PowerProjectEditPage;
import org.projectforge.plugins.chimney.web.projectmanagement.powerworkpackage.PowerSubtaskEditPage;
import org.projectforge.plugins.chimney.web.projectmanagement.powerworkpackage.PowerWorkpackageEditPage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractSecuredPage;

public class WbsNodeEditPageVisitor implements IWbsNodeVisitor, Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = -7015561794512987441L;

  private transient AbstractSecuredPage editPage = null;

  /**
   * Retrieves the correct EditPage for a given WbsNodeDO by using this visitor
   * @param wbsNode WbsNode to retrieve the corresponding EditPage
   * @return EditPage for the given WbsNodeDO
   */
  public static AbstractSecuredPage createEditPageFor(final AbstractWbsNodeDO wbsNode)
  {
    final WbsNodeEditPageVisitor visitor = new WbsNodeEditPageVisitor();
    wbsNode.accept(visitor);
    return visitor.editPage;
  }

  @Override
  public void visit(final MilestoneDO node)
  {
    editPage = new PowerMilestoneEditPage(new PageParameters(), node.getId());
  }

  @Override
  public void visit(final ProjectDO node)
  {
    final PageParameters params = new PageParameters();
    params.add(AbstractEditPage.PARAMETER_KEY_ID, String.valueOf( node.getId()));
    editPage = new PowerProjectEditPage(params);
  }

  @Override
  public void visit(final SubtaskDO node)
  {
    editPage = new PowerSubtaskEditPage(new PageParameters(), node.getId());
  }

  @Override
  public void visit(final WorkpackageDO node)
  {
    editPage = new PowerWorkpackageEditPage(new PageParameters(), node.getId());
  }

  @Override
  public void visit(final PhaseDO node)
  {
    editPage = new PhaseEditPage(new PageParameters(), node.getId());
  }

  @Override
  public void visit(final AbstractWbsNodeDO node)
  {
    editPage = null;
    throw new UnsupportedOperationException("No generic EditPage available!");
  }

}
