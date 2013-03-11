/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.core;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.ResourceModel;
import org.projectforge.web.Menu;
import org.projectforge.web.MenuEntry;

/**
 * @author Dennis Hilpmann (d.hilpmann.extern@micromata.de)
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class MenuConfig extends Panel
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MenuConfig.class);

  private static final long serialVersionUID = 7330216552642637127L;

  /**
   * @param id
   */
  public MenuConfig(final String id, final Menu menu)
  {
    super(id);
    final RepeatingView mainMenuRepeater = new RepeatingView("mainMenuItem");
    add(mainMenuRepeater);
    if (menu == null) {
      mainMenuRepeater.setVisible(false);
      log.error("Oups, menu is null. Configuration of favorite menu not possible.");
      return;
    }
    int counter = 0;
    for (final MenuEntry mainMenuEntry : menu.getMenuEntries()) {
      if (mainMenuEntry.hasSubMenuEntries() == false) {
        continue;
      }
      final WebMarkupContainer mainMenuContainer = new WebMarkupContainer(mainMenuRepeater.newChildId());
      mainMenuRepeater.add(mainMenuContainer);
      if (counter++ % 5 == 0) {
        mainMenuContainer.add(AttributeModifier.append("class", "mm_clear"));
      }
      mainMenuContainer.add(new Label("label", new ResourceModel(mainMenuEntry.getI18nKey())));
      final RepeatingView subMenuRepeater = new RepeatingView("menuItem");
      mainMenuContainer.add(subMenuRepeater);
      for (final MenuEntry subMenuEntry : mainMenuEntry.getSubMenuEntries()) {
        final WebMarkupContainer subMenuContainer = new WebMarkupContainer(subMenuRepeater.newChildId());
        subMenuRepeater.add(subMenuContainer);
        final AbstractLink link = NavAbstractPanel.getMenuEntryLink(subMenuEntry, false);
        if (link != null) {
          subMenuContainer.add(link);
        } else {
          subMenuContainer.setVisible(false);
        }
      }
    }
  }
}
