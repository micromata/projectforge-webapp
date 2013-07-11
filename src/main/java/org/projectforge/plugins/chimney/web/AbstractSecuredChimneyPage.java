/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.AbstractItem;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.plugins.chimney.resources.FileResources;
import org.projectforge.plugins.chimney.resources.ImageResources;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.wbs.WbsNodeUtils;
import org.projectforge.plugins.chimney.web.components.ChimneyFeedbackPanel;
import org.projectforge.plugins.chimney.web.navigation.BreadcrumbConstants;
import org.projectforge.plugins.chimney.web.navigation.Breadcrumbs;
import org.projectforge.plugins.chimney.web.navigation.NavigationBars;
import org.projectforge.plugins.chimney.web.navigation.NavigationConstants;
import org.projectforge.plugins.chimney.web.navigation.NavigationItem;
import org.projectforge.plugins.chimney.web.projecttree.ProjectTreePage;
import org.projectforge.web.wicket.AbstractSecuredPage;

/**
 * Class for creating the navigation bars of the chimney plugin.
 * Pages can register with this class to get an link in a navigation bar.
 * An arbitrary number of navigation bars can be registered, identified by name.
 * Parameters can optionally be passed to all link targets.
 */
public abstract class AbstractSecuredChimneyPage extends AbstractSecuredPage
{
  private static final long serialVersionUID = -4615364099918195003L;
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractSecuredChimneyPage.class);
  private final PageParameters linkParameters;


  public AbstractSecuredChimneyPage(final PageParameters parameters)
  {
    this(parameters, null, false);
  }

  /**
   * @param parameters
   * @param deferNavCreation If true, creation of navigation bar and breadcrumb are deferred. In this case, your constructor MUST call the createNavigation() method manually. This allows to change navigation bar/breadcrumb entries based on input parameters.
   */
  public AbstractSecuredChimneyPage(final PageParameters parameters, final boolean deferNavCreation)
  {
    this(parameters, null, deferNavCreation);
  }

  /**
   * @param parameters
   * @param linkParameters Parameters that are added to all links in the navigation bar, useful for passing context information between different pages (e.g. project id)
   * @param deferNavCreation If true, creation of navigation bar and breadcrumb are deferred. In this case, your constructor MUST call the createNavigation() method manually. This allows to change navigation bar/breadcrumb entries based on input parameters.
   */
  public AbstractSecuredChimneyPage(final PageParameters parameters, final PageParameters linkParameters, final boolean deferNavCreation)
  {
    super(parameters);
    this.linkParameters = linkParameters;

    if (!deferNavCreation)
      createNavigation();

    body.add(new Image("chimney_navlogo", ImageResources.CHIMNEY_LOGO));
  }

  /**
   * @return Name of the navigation bar for this page. You should return one of the constants in NavigationConstants.
   * @see NavigationConstants
   */
  protected abstract String getNavigationBarName();

  /**
   * Add breadcrumb items for this page by adding NavigationItems. Items are displayed in the order they are added.
   * The default implementation calls {@link #insertBreadcrumbItems(List)}
   * and uses the returned strings to fetch entries from {@link Breadcrumbs}.
   * @param items A non-null list that can be used to add your items
   */
  protected void insertNavigationItems(final List<NavigationItem> items) {
    final List<String> breadcrumbNames = new ArrayList<String>();
    insertBreadcrumbItems(breadcrumbNames);

    for (final Iterator<String> it = breadcrumbNames.iterator(); it.hasNext();) {
      final String name = it.next();
      if (name == null) continue;
      final NavigationItem breadcrumbItem = Breadcrumbs.getItem(name);
      if (breadcrumbItem == null)
        throw new IllegalArgumentException("Could not find a registered breadcrumb named: "+name);
      items.add(breadcrumbItem);
    }
  }

  /**
   * Add breadcrumb items for this page by adding string names for items registered in {@link Breadcrumbs}.
   * Items are displayed in the order they are added. {@link BreadcrumbConstants} contains some possible names.
   * You can change how breadcrumbs are built by overriding {@link #insertNavigationItems(List)}, which calls this method.
   * @param items A non-null list that can be used to add your items
   */
  protected abstract void insertBreadcrumbItems(List<String> items);


  protected void createNavigation() {
    // Add a navigation entry for each registered navigation item of the selected navigation item set
    // createNavEntryMarkup(linkParameters); (no longer used)
    // Create Breadcrumb from string array
    createBreadcrumb();
  }

  private void createBreadcrumb()
  {
    // ask insertBreadcrumbItems() to populate our list with items
    final ArrayList<NavigationItem> navItems = new ArrayList<NavigationItem>();
    insertNavigationItems(navItems);

    final RepeatingView rv = new RepeatingView("breadcrumb");
    boolean first = true;
    for (final Iterator<NavigationItem> it = navItems.iterator(); it.hasNext();) {
      final NavigationItem navItem = it.next();
      // create a dummy container for holding the wicket components needed to display a NavigationItem
      final AbstractItem breadcrumbItem = new AbstractItem(rv.newChildId());
      // put an arrow in front of items, starting at the second one
      if (first) {
        first = false;
        breadcrumbItem.add(new Label("arrow", ""));
      } else {
        breadcrumbItem.add(new Label("arrow", " &rarr; ").setEscapeModelStrings(false));
      }
      // get the link component from the navigation item
      if (it.hasNext())
        breadcrumbItem.add(navItem.getLinkInstance("link", "linktext"));
      else
        breadcrumbItem.add(navItem.getNullLinkInstance("link", "linktext"));
      rv.add(breadcrumbItem);
    }
    body.add(rv);
  }

  private void createNavEntryMarkup(final PageParameters linkParameters)
  {
    final List<NavigationItem> navBarItems = getNavigationBarItemList();
    populateNavigationBarWith(navBarItems, linkParameters);
  }

  private void populateNavigationBarWith(final List<NavigationItem> navBarItems, final PageParameters linkParameters)
  {
    @SuppressWarnings("serial")
    final ListView<NavigationItem> listview = new ListView<NavigationItem>("nav_item", navBarItems) {
      @Override
      protected void populateItem(final ListItem<NavigationItem> item) {
        final BookmarkablePageLink<Void> link = item.getModelObject().getLinkInstance("nav_link", "linktext");
        // pass parameters, if any
        if (linkParameters != null)
          link.getPageParameters().overwriteWith(linkParameters);
        item.add(link);
      }
    };
    body.add(listview);
  }

  private List<NavigationItem> getNavigationBarItemList()
  {
    // get the navigation bar using the name returned by getNavigationBarName()
    final String navBarName = getNavigationBarName();
    List<NavigationItem> navBarItems = null;
    if (navBarName != null)
      navBarItems = NavigationBars.getNavigationBar(navBarName);
    if (navBarItems == null) {
      // navbar not found, log a warning and create an empty nav item list
      log.warn("Navigation bar does not exist: "+navBarName);
      return new ArrayList<NavigationItem>();
    }
    return navBarItems;
  }

  @Override
  public void renderHead(final IHeaderResponse response) {
    super.renderHead(response);
    response.render(CssHeaderItem.forReference(FileResources.CHIMNEY_CSS));
  }

  protected void gotoProjectTreePage(final AbstractWbsNodeDO node, final String infoText) {
    Validate.notNull(node);
    final ProjectDO project = WbsNodeUtils.getProject(node);
    final ProjectTreePage newPage = new ProjectTreePage(project.getId());
    if (infoText != null && !infoText.isEmpty())
      newPage.info(infoText);
    setResponsePage(newPage);
  }

  protected void addFeedbackPanel()
  {
    final FeedbackPanel feedbackPanel = new ChimneyFeedbackPanel("feedback");
    feedbackPanel.setOutputMarkupId(true);
    //feedbackPanel.add(AttributeModifier.append("class", "alert alert_green feedback"));
    body.add(feedbackPanel);
  }
}
