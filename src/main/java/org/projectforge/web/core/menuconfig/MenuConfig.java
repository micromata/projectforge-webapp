/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.core.menuconfig;

import org.apache.wicket.markup.html.panel.Panel;
import org.projectforge.web.Menu;

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
    add(new MenuConfigContent("content", menu));
  }
}
