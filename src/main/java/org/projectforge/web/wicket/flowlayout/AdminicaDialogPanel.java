/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.web.wicket.flowlayout;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

/**
 * Represents a entry of a group panel. This can be a label, text field or other form components.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class AdminicaDialogPanel extends Panel
{
  private static final long serialVersionUID = 6130552547273354134L;

  private final WebMarkupContainer dialog;

  private final RepeatingView content;

  /**
   * @param id
   */
  public AdminicaDialogPanel(final String id, final String markupId, final String title)
  {
    super(id);
    dialog = new WebMarkupContainer("dialog");
    dialog.setMarkupId(markupId).setOutputMarkupId(true);
    dialog.add(AttributeModifier.append("title", title));
    super.add(dialog);
    content = new RepeatingView("content");
    dialog.add(content);
    content.add(new TextPanel(content.newChildId(), "Hurzel"));
  }

  /**
   * Adds components to the content's RepeatingView.
   * @see org.apache.wicket.MarkupContainer#add(org.apache.wicket.Component[])
   */
  @Override
  public AdminicaDialogPanel add(final Component... components)
  {
    content.add(components);
    return this;
  }

  /**
   * @return New child id of the content's RepeatingView.
   * @see RepeatingView#newChildId()
   */
  public String newChildId()
  {
    return content.newChildId();
  }
}
