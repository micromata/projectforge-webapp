/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.projectforge.continuousdb.UpdateEntry;
import org.projectforge.plugins.chimney.activities.DependencyRelationDO;
import org.projectforge.plugins.chimney.activities.DependencyRelationRight;
import org.projectforge.plugins.chimney.activities.WbsActivityDO;
import org.projectforge.plugins.chimney.activities.WbsActivityRight;
import org.projectforge.plugins.chimney.resourceplanning.ResourceAssignmentDO;
import org.projectforge.plugins.chimney.resourceplanning.ResourceAssignmentRight;
import org.projectforge.plugins.chimney.wbs.MilestoneDO;
import org.projectforge.plugins.chimney.wbs.PhaseDO;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.wbs.SubtaskDO;
import org.projectforge.plugins.chimney.wbs.WbsNodeRight;
import org.projectforge.plugins.chimney.wbs.WorkpackageDO;
import org.projectforge.plugins.chimney.web.gantt.GanttPage;
import org.projectforge.plugins.chimney.web.gantt.GanttTaskXmlPage;
import org.projectforge.plugins.chimney.web.navigation.BreadcrumbConstants;
import org.projectforge.plugins.chimney.web.navigation.Breadcrumbs;
import org.projectforge.plugins.chimney.web.navigation.NavigationBars;
import org.projectforge.plugins.chimney.web.navigation.NavigationConstants;
import org.projectforge.plugins.chimney.web.navigation.NavigationItem;
import org.projectforge.plugins.chimney.web.projectmanagement.PhaseEditPage;
import org.projectforge.plugins.chimney.web.projectmanagement.ProjectListPage;
import org.projectforge.plugins.chimney.web.projectmanagement.powerworkpackage.PowerMilestoneEditPage;
import org.projectforge.plugins.chimney.web.projectmanagement.powerworkpackage.PowerProjectEditPage;
import org.projectforge.plugins.chimney.web.projectmanagement.powerworkpackage.PowerSubtaskEditPage;
import org.projectforge.plugins.chimney.web.projectmanagement.powerworkpackage.PowerWorkpackageEditPage;
import org.projectforge.plugins.chimney.web.projectmanagement.wizard.WizardActivityEditPage;
import org.projectforge.plugins.chimney.web.projectmanagement.wizard.WizardDependencyEditPage;
import org.projectforge.plugins.chimney.web.projectmanagement.wizard.WizardMilestoneEditPage;
import org.projectforge.plugins.chimney.web.projectmanagement.wizard.WizardProjectEditPage;
import org.projectforge.plugins.chimney.web.projectmanagement.wizard.WizardSubtaskEditPage;
import org.projectforge.plugins.chimney.web.projectmanagement.wizard.WizardWorkpackageEditPage;
import org.projectforge.plugins.chimney.web.projecttree.DependencyTreePage;
import org.projectforge.plugins.chimney.web.projecttree.PhasePlanningTreePage;
import org.projectforge.plugins.chimney.web.projecttree.ProjectTreePage;
import org.projectforge.plugins.chimney.web.resourceplanning.ResourceAssignmentEditPage;
import org.projectforge.plugins.chimney.web.resourceplanning.ResourceAssignmentTreePage;
import org.projectforge.plugins.chimney.web.resourceworkload.ProjectWorkloadPage;
import org.projectforge.plugins.chimney.web.resourceworkload.ResourceWorkloadPage;
import org.projectforge.plugins.core.AbstractPlugin;
import org.projectforge.user.UserRight;
import org.projectforge.web.MenuItemDef;
import org.projectforge.web.MenuItemDefId;

/**
 * Chimney plugin initialization. Registers all components such as i18n files, DAOs, access rights, menu entries and pages.
 * 
 * @author Team Sweeps
 */
public class ChimneyPlugin extends AbstractPlugin
{
  public static final String ID = "chimney";

  public static final String RESOURCE_BUNDLE_NAME = ChimneyPlugin.class.getPackage().getName() + ".ChimneyI18nResources";

  private static final Class< ? >[] PERSISTENT_ENTITIES = new Class< ? >[] { ProjectDO.class, WorkpackageDO.class, SubtaskDO.class,
    MilestoneDO.class, WbsActivityDO.class, DependencyRelationDO.class, ResourceAssignmentDO.class, PhaseDO.class,};

  private static final Class< ? extends Page> DEFAULT_PAGE = ProjectListPage.class;

  @Override
  public Class< ? >[] getPersistentEntities()
  {
    return PERSISTENT_ENTITIES;
  }

  @Override
  protected void initialize()
  {
    ChimneyPluginUpdates.dao = getDatabaseUpdateDao();

    // Register pages with nicely looking URL (not using AbstractPlugin.registerWeb, as it has no working support for simple single pages)
    mountAllChimneyPages();

    // Register menu item
    registerMenuItem(new MenuItemDef(getMenuItemDef(MenuItemDefId.PROJECT_MANAGEMENT), ID, 10, "plugins.chimney.menu.projectplanning", DEFAULT_PAGE));
    registerMenuItem(new MenuItemDef(getMenuItemDef(MenuItemDefId.PROJECT_MANAGEMENT), ID+".resourceload", 11, "plugins.chimney.menu.resourceworkload", ResourceWorkloadPage.class));

    // Register wizard navigation bar items
    // Note: Navigation has been removed from display. All navigation stuff may be removed from all pages in the future.
    registerNavigationBarItems();

    // Register Breadcrumb links
    registerBreadcrumbItems();

    // Add resources
    addResourceBundle(RESOURCE_BUNDLE_NAME);

    // Define the access management:
    registerAllRights();

  }

  private void mountAllChimneyPages()
  {
    addMountPage(ProjectListPage.PAGE_ID, ProjectListPage.class);
    addMountPage(WizardProjectEditPage.PAGE_ID, WizardProjectEditPage.class);
    addMountPage(WizardWorkpackageEditPage.PAGE_ID, WizardWorkpackageEditPage.class);
    addMountPage(WizardSubtaskEditPage.PAGE_ID, WizardSubtaskEditPage.class);
    addMountPage(WizardMilestoneEditPage.PAGE_ID, WizardMilestoneEditPage.class);
    addMountPage(ProjectTreePage.PAGE_ID, ProjectTreePage.class);
    addMountPage(GanttPage.PAGE_ID, GanttPage.class);
    addMountPage(GanttTaskXmlPage.PAGE_ID, GanttTaskXmlPage.class);
    addMountPage(WizardActivityEditPage.PAGE_ID, WizardActivityEditPage.class);
    addMountPage(WizardDependencyEditPage.PAGE_ID, WizardDependencyEditPage.class);
    addMountPage(DependencyTreePage.PAGE_ID, DependencyTreePage.class);
    addMountPage(ResourceAssignmentEditPage.PAGE_ID, ResourceAssignmentEditPage.class);
    addMountPage(ResourceAssignmentTreePage.PAGE_ID, ResourceAssignmentTreePage.class);
    addMountPage(ResourceWorkloadPage.PAGE_ID, ResourceWorkloadPage.class);
    addMountPage(ProjectWorkloadPage.PAGE_ID, ProjectWorkloadPage.class);
    addMountPage(PowerProjectEditPage.PAGE_ID, PowerProjectEditPage.class);
    addMountPage(PowerMilestoneEditPage.PAGE_ID, PowerMilestoneEditPage.class);
    addMountPage(PowerWorkpackageEditPage.PAGE_ID, PowerWorkpackageEditPage.class);
    addMountPage(PowerSubtaskEditPage.PAGE_ID, PowerSubtaskEditPage.class);
    addMountPage(PhasePlanningTreePage.PAGE_ID, PhasePlanningTreePage.class);
    addMountPage(PhaseEditPage.PAGE_ID, PhaseEditPage.class);
  }

  private void registerBreadcrumbItems()
  {
    Breadcrumbs.addItem(BreadcrumbConstants.PROJECT_PLANNING, new NavigationItem(ProjectListPage.class,
        "plugins.chimney.navitem.projectplanning"));
    Breadcrumbs.addItem(BreadcrumbConstants.PROJECTS, new NavigationItem(ProjectListPage.class, "plugins.chimney.navitem.projects"));
    Breadcrumbs.addItem(BreadcrumbConstants.WIZARD, new NavigationItem(WizardProjectEditPage.class, "plugins.chimney.navitem.wizard"));
    Breadcrumbs.addItem(BreadcrumbConstants.CREATE_PROJECT, new NavigationItem(WizardProjectEditPage.class,
        "plugins.chimney.navitem.createproject"));
    Breadcrumbs.addItem(BreadcrumbConstants.EDIT_PROJECT, new NavigationItem(WizardProjectEditPage.class, "plugins.chimney.navitem.editproject"));
    Breadcrumbs.addItem(BreadcrumbConstants.CREATE_SUBTASK, new NavigationItem(WizardSubtaskEditPage.class,
        "plugins.chimney.navitem.createsubtask"));
    Breadcrumbs.addItem(BreadcrumbConstants.EDIT_SUBTASK, new NavigationItem(WizardSubtaskEditPage.class, "plugins.chimney.navitem.editsubtask"));
    Breadcrumbs.addItem(BreadcrumbConstants.CREATE_WORKPACKAGE, new NavigationItem(WizardWorkpackageEditPage.class,
        "plugins.chimney.navitem.createworkpackage"));
    Breadcrumbs.addItem(BreadcrumbConstants.EDIT_WORKPACKAGE, new NavigationItem(WizardWorkpackageEditPage.class,
        "plugins.chimney.navitem.editworkpackage"));
    Breadcrumbs.addItem(BreadcrumbConstants.CREATE_MILESTONE, new NavigationItem(WizardMilestoneEditPage.class,
        "plugins.chimney.navitem.createmilestone"));
    Breadcrumbs.addItem(BreadcrumbConstants.EDIT_MILESTONE, new NavigationItem(WizardMilestoneEditPage.class,
        "plugins.chimney.navitem.editmilestone"));
    Breadcrumbs.addItem(BreadcrumbConstants.PROJECT_TREE, new NavigationItem(ProjectTreePage.class, "plugins.chimney.navitem.projecttree"));
    Breadcrumbs.addItem(BreadcrumbConstants.CREATE_ACTIVITY, new NavigationItem(WizardActivityEditPage.class,
        "plugins.chimney.navitem.createactivity"));
    Breadcrumbs.addItem(BreadcrumbConstants.EDIT_ACTIVITY, new NavigationItem(WizardActivityEditPage.class,
        "plugins.chimney.navitem.editactivity"));
    Breadcrumbs.addItem(BreadcrumbConstants.CREATE_DEPENDENCY, new NavigationItem(WizardActivityEditPage.class,
        "plugins.chimney.navitem.createdependency"));
    Breadcrumbs.addItem(BreadcrumbConstants.DEPENDENCY_TREE, new NavigationItem(WizardDependencyEditPage.class,
        "plugins.chimney.navitem.dependencytree"));
    Breadcrumbs.addItem(BreadcrumbConstants.CREATE_RESOURCEASSIGNMENT, new NavigationItem(ResourceAssignmentEditPage.class,
        "plugins.chimney.navitem.createresourceassignment"));
    Breadcrumbs.addItem(BreadcrumbConstants.PHASE_PLANNING, new NavigationItem(ResourceAssignmentEditPage.class,
        "plugins.chimney.navitem.phaseplanning"));
    Breadcrumbs.addItem(BreadcrumbConstants.EDIT_PHASE, new NavigationItem(ResourceAssignmentEditPage.class,
        "plugins.chimney.navitem.editphase"));

    Breadcrumbs.addItem(BreadcrumbConstants.GANTT_VIEW, new NavigationItem(GanttPage.class,
        "plugins.chimney.navitem.ganttview"));
    Breadcrumbs.addItem(BreadcrumbConstants.PROJECT_WORKLOAD_VIEW, new NavigationItem(ProjectWorkloadPage.class,
        "plugins.chimney.navitem.projectworkloadview"));
    Breadcrumbs.addItem(BreadcrumbConstants.RESOURCE_WORKLOAD_VIEW, new NavigationItem(ResourceWorkloadPage.class,
        "plugins.chimney.navitem.resourceworkloadview"));
  }

  private void registerNavigationBarItems()
  {
    // NavigationBars.addItem(NavigationConstants.MAIN, new NavigationItem(HelloChimneyPage.class, "plugins.chimney.navitem.hellochimney"));
    NavigationBars.addItem(NavigationConstants.MAIN, new NavigationItem(ProjectListPage.class, "plugins.chimney.navitem.projects"));
    // NavigationBars.addItem(NavigationConstants.MAIN, new NavigationItem(ProjectTreePage.class, "plugins.chimney.navitem.projecttree"));
    // NavigationBars.addItem(NavigationConstants.MAIN, new NavigationItem(DependencyTreePage.class,
    // "plugins.chimney.navitem.dependencytree"));
    // NavigationBars.addItem(NavigationConstants.MAIN, new NavigationItem(ResourceAssignmentTreePage.class,
    // "plugins.chimney.navitem.resourceassignmenttree"));
    // NavigationBars.addItem(NavigationConstants.MAIN, new NavigationItem(GanttPage.class, "plugins.chimney.navitem.ganttview"));
    NavigationBars.addItem(NavigationConstants.MAIN, new NavigationItem(ResourceWorkloadPage.class,
        "plugins.chimney.navitem.viewresourceload"));
    // NavigationBars.addItem(NavigationConstants.MAIN, new NavigationItem(ProjectWorkloadPage.class,
    // "plugins.chimney.navitem.projectworkload"));
    // NavigationBars.addItem(NavigationConstants.MAIN, new NavigationItem(ProjectEditPage.class, "plugins.chimney.navitem.wizard"));
    // NavigationBars.addItem(NavigationConstants.MAIN, new NavigationItem(TabbedProjectPage.class, "plugins.chimney.navitem.projects"));

    NavigationBars.addItem(NavigationConstants.WIZARD, new NavigationItem(WizardProjectEditPage.class, "plugins.chimney.navitem.createproject"));
    NavigationBars.addItem(NavigationConstants.WIZARD, new NavigationItem(WizardSubtaskEditPage.class, "plugins.chimney.navitem.createsubtask"));
    NavigationBars.addItem(NavigationConstants.WIZARD, new NavigationItem(WizardWorkpackageEditPage.class,
        "plugins.chimney.navitem.createworkpackage"));
    NavigationBars.addItem(NavigationConstants.WIZARD, new NavigationItem(WizardMilestoneEditPage.class,
        "plugins.chimney.navitem.createmilestone"));
    NavigationBars.addItem(NavigationConstants.WIZARD, new NavigationItem(WizardActivityEditPage.class, "plugins.chimney.navitem.editactivity"));
    NavigationBars.addItem(NavigationConstants.WIZARD, new NavigationItem(WizardDependencyEditPage.class,
        "plugins.chimney.navitem.createdependency"));
    NavigationBars.addItem(NavigationConstants.WIZARD, new NavigationItem(ResourceAssignmentEditPage.class,
        "plugins.chimney.navitem.createresourceassignment"));
    NavigationBars.addItem(NavigationConstants.WIZARD, new NavigationItem(DEFAULT_PAGE, "plugins.chimney.navitem.exitwizard"));
  }

  @Override
  public UpdateEntry getInitializationUpdateEntry()
  {
    return ChimneyPluginUpdates.getInitializationUpdateEntry();
  }

  public void registerAllRights()
  {
    final List<UserRight> allRights = getAllRights();
    for (final UserRight right : allRights)
      registerRight(right);
  }

  public static List<UserRight> getAllRights()
  {
    final ArrayList<UserRight> rightsList = new ArrayList<UserRight>();
    rightsList.add(new WbsNodeRight());
    rightsList.add(new WbsActivityRight());
    rightsList.add(new DependencyRelationRight());
    rightsList.add(new ResourceAssignmentRight());
    return rightsList;
  }

}
