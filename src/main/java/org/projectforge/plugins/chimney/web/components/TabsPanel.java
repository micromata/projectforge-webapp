/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

/**
 * A panel that displays links to pages in tabs style manner.
 * Page links can be added dynamically by either overriding the
 * {@link #addTabs()} method or by adding them via the method
 * {@link #addTab(Class, String, ICallPage)} from external.
 * @author Sweeps <pf@byte-storm.com>
 */
public class TabsPanel extends Panel
{
  private static final long serialVersionUID = -4943957703300758118L;

  public static String TAB_SELECTED_CLASSES = "ui-state-active ui-tabs-selected";

  protected final Page callerPage;
  private RepeatingView tabs;

  public TabsPanel(final String id, final Page callerPage)
  {
    super(id);
    this.callerPage = callerPage;

    init();
    addTabs();
  }

  /**
   * Override this method to add your tabs. Tabs are added by calling the method {@link #addTab(Page, String, ICallPage)}.
   */
  protected void addTabs()
  {
  }

  /**
   * Call this method to a add a new tab
   * @param pageClass The class of the target page
   * @param linkText
   * @param pageLink
   */
  public final void addTab(final IPageLink pageLink, final String linkText) {
    // add CSS classes if tab is active
    final WebMarkupContainer li = new WebMarkupContainer(tabs.newChildId());
    if (pageLink.getPageIdentity().equals(callerPage.getClass())) {
      li.add(AttributeModifier.append("class", TAB_SELECTED_CLASSES));
    }

    // add link
    final Link<Void> link = new Link<Void>("link") {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick()
      {
        setResponsePage(pageLink.getPage());
      }

    };
    link.setEnabled(pageLink.getPageIdentity() != callerPage.getClass());
    li.add(link);

    // add link text
    link.add(new Label("linktext", linkText));

    tabs.add(li);
  }

  private void init()
  {
    tabs = new RepeatingView("tabRepeater");
    add(tabs);
  }

}
