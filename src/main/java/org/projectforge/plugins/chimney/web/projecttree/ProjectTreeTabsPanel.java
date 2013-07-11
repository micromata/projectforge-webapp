/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projecttree;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.plugins.chimney.web.components.TabsPanel;
import org.projectforge.plugins.chimney.web.gantt.GanttPage;
import org.projectforge.plugins.chimney.web.resourceworkload.ProjectWorkloadPage;

/**
 * A panel of tabs that allows switching between different project views. The list of projects that is displayed is kept between those
 * views.
 * @author Sweeps <pf@byte-storm.com>
 */
public class ProjectTreeTabsPanel extends TabsPanel
{
  private static final long serialVersionUID = 3159909996795431993L;

  private final Integer[] projectIds;

  public ProjectTreeTabsPanel(final String id, final Integer[] projectIds, final Page callerPage)
  {
    super(id, callerPage);
    this.projectIds = projectIds;
  }

  @Override
  protected void addTabs()
  {
    addTab(newProjectTreePageLink(), callerPage.getString("plugins.chimney.projecttree.tabs.projecttree"));
    addTab(newPhasePlanningPageLink(), callerPage.getString("plugins.chimney.projecttree.tabs.phaseplanning"));
    //addTab(newDependencyTreePageLink(), callerPage.getString("plugins.chimney.projecttree.tabs.dependencies"));
    //addTab(newResourceAssignmentTreePageLink(), callerPage.getString("plugins.chimney.projecttree.tabs.resourceplanning"));
    addTab(newGanttPageLink(), callerPage.getString("plugins.chimney.projecttree.tabs.gantt"));
    addTab(newProjectWorkloadPageLink(), callerPage.getString("plugins.chimney.projecttree.tabs.projectworkload"));


  }

  /*
  private IPageLink newResourceAssignmentTreePageLink()
  {
    return new IPageLink() {
      private static final long serialVersionUID = 1L;

      @Override
      public Page getPage()
      {
        if (projectIds == null)
          return new ResourceAssignmentTreePage(new PageParameters());
        return new ResourceAssignmentTreePage(projectIds);
      }

      @Override
      public Class< ? extends Page> getPageIdentity()
      {
        return ResourceAssignmentTreePage.class;
      }
    };
  }*/

  /*
  private IPageLink newDependencyTreePageLink()
  {
    return new IPageLink() {
      private static final long serialVersionUID = 1L;

      @Override
      public Page getPage()
      {
        if (projectIds == null)
          return new DependencyTreePage(new PageParameters());
        return new DependencyTreePage(projectIds);
      }

      @Override
      public Class< ? extends Page> getPageIdentity()
      {
        return DependencyTreePage.class;
      }
    };
  }*/

  private IPageLink newProjectTreePageLink()
  {
    return new IPageLink() {
      private static final long serialVersionUID = 1L;

      @Override
      public Page getPage()
      {
        if (projectIds == null)
          return new ProjectTreePage(new PageParameters());
        return new ProjectTreePage(projectIds);
      }

      @Override
      public Class< ? extends Page> getPageIdentity()
      {
        return ProjectTreePage.class;
      }
    };
  }

  private IPageLink newPhasePlanningPageLink()
  {
    return new IPageLink() {
      private static final long serialVersionUID = 1L;

      @Override
      public Page getPage()
      {
        if (projectIds == null)
          return new PhasePlanningTreePage(new PageParameters());
        return new PhasePlanningTreePage(projectIds);
      }

      @Override
      public Class< ? extends Page> getPageIdentity()
      {
        return PhasePlanningTreePage.class;
      }
    };
  }

  private IPageLink newGanttPageLink()
  {
    return new IPageLink() {
      private static final long serialVersionUID = 1L;

      @Override
      public Page getPage()
      {
        if (projectIds == null)
          return new GanttPage();
        return new GanttPage(projectIds);
      }

      @Override
      public Class< ? extends Page> getPageIdentity()
      {
        return GanttPage.class;
      }
    };
  }

  private IPageLink newProjectWorkloadPageLink()
  {
    return new IPageLink() {
      private static final long serialVersionUID = 1L;

      @Override
      public Page getPage()
      {
        if (projectIds == null)
          return new ProjectWorkloadPage();
        return new ProjectWorkloadPage(projectIds);
      }

      @Override
      public Class< ? extends Page> getPageIdentity()
      {
        return ProjectWorkloadPage.class;
      }
    };
  }

}
