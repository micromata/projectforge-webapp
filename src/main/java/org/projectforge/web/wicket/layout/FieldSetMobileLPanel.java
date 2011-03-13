/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

import org.projectforge.web.mobile.CollapsiblePanel;

/**
 * Represents a field set panel. A form or page can contain multiple field sets. This view for mobile devices is implemented with
 * collapsible panels.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class FieldSetMobileLPanel extends FieldSetLPanel
{
  private static final long serialVersionUID = -4126362330312626485L;

  private CollapsiblePanel childPanel;

  /**
   * @see AbstractFormRenderer#createFieldSetPanel(String, String)
   */
  FieldSetMobileLPanel(final String id, final String heading)
  {
    super(id);
    childPanel = new CollapsiblePanel("collapsiblePanel", heading);
    add(childPanel);
  }

  @Override
  public FieldSetLPanel init()
  {
    return this;
  }

  @Override
  public FieldSetLPanel add(final GroupLPanel groupPanel)
  {
    childPanel.add(groupPanel);
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
}
