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

package org.projectforge.web.wicket.components;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.projectforge.web.wicket.WicketUtils;

/**
 * Panel for using as content top menu entry (needed for css decoration).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class ContentMenuEntryPanel extends Panel
{
  private static final long serialVersionUID = -5507326592369611604L;

  private final AbstractLink link;

  public ContentMenuEntryPanel(final String id, final AbstractLink link, final String label)
  {
    super(id);
    this.link = link;
    setRenderBodyOnly(true);
    add(link);
    final Label labelComp = new Label("label", label);
    labelComp.setRenderBodyOnly(true);
    link.add(labelComp);
  }

  /**
   * Adds html attribute "accesskey".
   * @param ch
   * @return this for chaining.
   */
  public ContentMenuEntryPanel setAccessKey(final char ch)
  {
    link.add(new SimpleAttributeModifier("accesskey", String.valueOf(ch)));
    return this;
  }

  /**
   * @param tooltip
   * @return this for chaining.
   * @see WicketUtils#addTooltip(org.apache.wicket.Component, String)
   */
  public ContentMenuEntryPanel setTooltip(final String tooltip)
  {
    WicketUtils.addTooltip(link, tooltip);
    return this;
  }

  /**
   * @param title
   * @param text
   * @return this for chaining.
   * @see WicketUtils#addTooltip(org.apache.wicket.Component, String, String)
   */
  public ContentMenuEntryPanel setTooltip(final String title, final String text)
  {
    WicketUtils.addTooltip(link, title, text);
    return this;
  }
}
