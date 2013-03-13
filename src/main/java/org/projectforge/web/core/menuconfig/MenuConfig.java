/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.core.menuconfig;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.StringValue;
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

  private final WebMarkupContainer configureLink;

  private final AbstractDefaultAjaxBehavior configureBehavior;

  /**
   * @param id
   */
  public MenuConfig(final String id, final Menu menu)
  {
    super(id);
    configureLink = new WebMarkupContainer("configureLink");
    add(configureLink);
    configureBehavior = new AbstractDefaultAjaxBehavior() {
      @Override
      protected void respond(AjaxRequestTarget target)
      {
        final StringValue configuration = RequestCycle.get().getRequest().getPostParameters().getParameterValue("configuration");
      }
    };
    add(configureBehavior);
    add(new MenuConfigContent("content", menu));
  }

  @Override
  protected void onBeforeRender()
  {
    super.onBeforeRender();
    configureLink.add(AttributeModifier.replace("data-callback", "" + configureBehavior.getCallbackUrl()));
  }
}
