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

import org.apache.wicket.Component;
import org.projectforge.web.mobile.CollapsiblePanel;
import org.projectforge.web.mobile.ThemeType;

/**
 * Represents a mobile group panel. A field set, form or page can contain multiple group panels. A group panel groups fields.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class GroupMobileLPanel extends GroupLPanel
{
  private static final long serialVersionUID = -280050296848404710L;

  private CollapsiblePanel childPanel;

  /**
   * @see AbstractRenderer#createGroupPanel(String)
   */
  GroupMobileLPanel(final String id)
  {
    this(id, null);
  }

  @Override
  public GroupMobileLPanel init()
  {
    return this;
  }

  /**
   * @see AbstractRenderer#createGroupPanel(String, String)
   */
  GroupMobileLPanel(final String id, final String heading)
  {
    super(id);
    childPanel = new CollapsiblePanel("collapsiblePanel", heading);
    add(childPanel);
    childPanel.setTheme(ThemeType.C);
    if (heading != null) {
      setHeading(heading);
    }
  }

  @Override
  public GroupLPanel add(final AbstractLPanel layoutPanel)
  {
    childPanel.add(layoutPanel);
    return this;
  }

  @Override
  public GroupLPanel add(final IField field)
  {
    childPanel.add((Component)field);
    return this;
  }

  @Override
  public boolean hasChildren()
  {
    return childPanel.hasChildren();
  }

  @Override
  public String newChildId()
  {
    return childPanel.newChildId();
  }

  /**
   * @see org.projectforge.web.wicket.layout.GroupLPanel#setHeading(java.lang.String)
   */
  @Override
  public GroupMobileLPanel setHeading(final String heading)
  {
    childPanel.setHeadingLabel(heading);
    return this;
  }
  
  /**
   * @return this for chaining.
   * @see CollapsiblePanel#setCollapsed()
   */
  public GroupMobileLPanel setCollapsed() {
    childPanel.setCollapsed();
    return this;
  }
}
