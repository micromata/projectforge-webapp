/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.wicket.layout;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;

/**
 * Base class for renderers of data objects. This renderer can be re-used by different pages (mobile pages as well as read-only or edit form
 * pages).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public abstract class AbstractRenderer implements Serializable
{
  private static final long serialVersionUID = -5202334758019046183L;

  protected LayoutContext layoutContext;

  protected MarkupContainer container;

  /**
   * Creates a FieldSetLPanel (for normal or mobile version) depending on the layout context.
   * @param id
   * @param heading
   */
  public FieldSetLPanel createFieldSetLPanel(final String id, final String heading)
  {
    if (layoutContext.isMobile() == true) {
      return new FieldSetMobileLPanel(id, heading);
    } else {
      return new FieldSetLPanel(id, heading);
    }
  }

  public GroupLPanel createGroupLPanel(final String id)
  {
    return createGroupLPanel(id, null);
  }

  public GroupLPanel createGroupLPanel(final String id, final String heading)
  {
    if (layoutContext.isMobile() == true) {
      return new GroupMobileLPanel(id, heading);
    } else {
      return new GroupLPanel(id, heading);
    }
  }

  public AbstractRenderer(final MarkupContainer container, final LayoutContext layoutContext)
  {
    this.layoutContext = layoutContext;
    this.container = container;
  }

  public abstract void add();

  protected AbstractRenderer add(final Component component)
  {
    container.add(component);
    return this;
  }

  protected String getString(final String i18nKey)
  {
    return container.getString(i18nKey);
  }
}
