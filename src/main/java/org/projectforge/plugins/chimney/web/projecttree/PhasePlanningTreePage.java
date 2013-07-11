/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projecttree;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.MilestoneDO;
import org.projectforge.plugins.chimney.wbs.PhaseDO;
import org.projectforge.plugins.chimney.wbs.WbsNodeFilter;
import org.projectforge.plugins.chimney.web.components.PhaseAddPanel;
import org.projectforge.plugins.chimney.web.navigation.BreadcrumbConstants;

/**
 * A page that allow to view milestones and phases, add, delete and move them around
 * @author Sweeps <pf@byte-storm.com>
 */
public class PhasePlanningTreePage extends ExtendableProjectTreePage
{

  private static final long serialVersionUID = 7503909807860787752L;

  public static final String PAGE_ID = "phasePlanningTreePage";

  private PhaseAddPanel addPanel;

  public PhasePlanningTreePage(final Integer... projectIds)
  {
    super(projectIds);
  }

  public PhasePlanningTreePage(final PageParameters parameters)
  {
    super(parameters);
  }

  @Override
  protected void onInitialize()
  {
    super.onInitialize();

    if (getProjectIds().length == 0)
      error("No projects selected");
    else if (getProjectIds().length == 1)
      // The PhaseAddPanel only makes sense if exactly one project is viewed
      addPanel.setProjectId(getProjectIds()[0]);
  }

  /**
   * @return A filter that determines which node types are displayed or not displayed. By default, all phases are excluded.
   */
  @Override
  protected WbsNodeFilter getWbsNodeFilter()
  {
    final WbsNodeFilter filter = new WbsNodeFilter(false);
    filter.addChildType(PhaseDO.class);
    filter.addChildType(MilestoneDO.class);

    return filter;
  }

  @Override
  protected IColumn<AbstractWbsNodeDO, String> getCustomColumn()
  {
    return null;
  }

  @Override
  protected void addActionLinks(final RepeatingView rv, final AbstractWbsNodeDO wbsNode)
  {
    final Component newDeleteLink = createDeleteLinkFor(rv.newChildId(), wbsNode);
    rv.add(newDeleteLink);

    final Component newUpLink = createMoveUpLink(rv.newChildId(), wbsNode);
    rv.add(newUpLink);

    final Component newDownLink = createMoveDownLink(rv.newChildId(), wbsNode);
    rv.add(newDownLink);
  }

  @Override
  protected void addAdditionalFields(final RepeatingView container)
  {
    addPanel = new PhaseAddPanel(container.newChildId());
    container.add(addPanel);
  }

  @Override
  protected void insertBreadcrumbItems(final List<String> items)
  {
    items.add(BreadcrumbConstants.PROJECT_PLANNING);
    items.add(BreadcrumbConstants.PHASE_PLANNING);
  }

  @Override
  protected String getTitle()
  {
    return getString("plugins.chimney.phaseplanning.title");
  }

}
