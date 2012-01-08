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

package org.projectforge.web.mobile;

import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * A jquery button with an icon (e. g. at the top right corner).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class JQueryButtonPanel extends Panel
{
  private static final long serialVersionUID = 6460153798143225741L;

  private Class< ? extends WebPage> pageClass;

  private PageParameters params;

  private JQueryButtonType type;

  private boolean initialized;

  private String label;

  private boolean relExternal;
  
  private boolean relDialog;

  public JQueryButtonPanel(final String id, final JQueryButtonType type, final Class< ? extends WebPage> pageClass, final String label)
  {
    this(id, type, pageClass, null, label);
  }

  public JQueryButtonPanel(final String id, final JQueryButtonType type, final Class< ? extends WebPage> pageClass,
      final PageParameters params, final String label)
  {
    super(id);
    this.pageClass = pageClass;
    this.params = params;
    this.type = type;
    this.label = label;
  }

  public JQueryButtonPanel setRelExternal()
  {
    this.relExternal = true;
    return this;
  }

  public JQueryButtonPanel setRelDialog()
  {
    this.relDialog = true;
    return this;
  }

  @Override
  protected void onBeforeRender()
  {
    if (initialized == false) {
      initialized = true;
      final BookmarkablePageLink<String> link;
      if (params == null) {
        link = new BookmarkablePageLink<String>("button", pageClass);
      } else {
        link = new BookmarkablePageLink<String>("button", pageClass, params);
      }
      if (type == JQueryButtonType.PLUS) {
        link.add(new SimpleAttributeModifier("data-icon", "plus"));
      } else {
        link.add(new SimpleAttributeModifier("data-icon", "check"));
      }
      link.add(new SimpleAttributeModifier("class", "ui-btn-right"));
      add(link);
      link.add(new Label("label", label));
      if (this.relExternal == true) {
        link.add(new SimpleAttributeModifier("rel", "external"));
      }
      if (this.relDialog == true) {
        link.add(new SimpleAttributeModifier("data-rel", "dialog"));
      }
    }
    super.onBeforeRender();
  }
}
