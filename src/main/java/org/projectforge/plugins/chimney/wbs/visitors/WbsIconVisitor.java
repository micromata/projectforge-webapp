/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.wbs.visitors;

import org.apache.wicket.request.resource.ResourceReference;
import org.projectforge.plugins.chimney.resources.ImageResources;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.MilestoneDO;
import org.projectforge.plugins.chimney.wbs.PhaseDO;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.wbs.SubtaskDO;
import org.projectforge.plugins.chimney.wbs.WorkpackageDO;

public class WbsIconVisitor implements IWbsNodeVisitor
{
  private final boolean bigIcons;

  private ResourceReference selectedImageResource;

  public WbsIconVisitor(final boolean bigIcons)
  {
    this.bigIcons = bigIcons;
  }

  @Override
  public void visit(final MilestoneDO node)
  {
    if (bigIcons)
      selectedImageResource = ImageResources.MILESTONE_BIG_IMAGE;
    else selectedImageResource = ImageResources.MILESTONE_SMALL_IMAGE;
  }

  @Override
  public void visit(final ProjectDO node)
  {
    if (bigIcons)
      selectedImageResource = ImageResources.PROJECT_BIG_IMAGE;
    else selectedImageResource = ImageResources.PROJECT_SMALL_IMAGE;
  }

  @Override
  public void visit(final SubtaskDO node)
  {
    if (bigIcons)
      selectedImageResource = ImageResources.SUBTASK_BIG_IMAGE;
    else selectedImageResource = ImageResources.SUBTASK_SMALL_IMAGE;
  }

  @Override
  public void visit(final WorkpackageDO node)
  {
    if (bigIcons)
      selectedImageResource = ImageResources.WORKPACKAGE_BIG_IMAGE;
    else selectedImageResource = ImageResources.WORKPACKAGE_SMALL_IMAGE;
  }

  @Override
  public void visit(final AbstractWbsNodeDO node)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(final PhaseDO node)
  {
    if (bigIcons)
      selectedImageResource = ImageResources.WORKPACKAGE_BIG_IMAGE;
    else selectedImageResource = ImageResources.PHASE;
  }

  public ResourceReference getSelectedImageResource()
  {
    return selectedImageResource;
  }

}
