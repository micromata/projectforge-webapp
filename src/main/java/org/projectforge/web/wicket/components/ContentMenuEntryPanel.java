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

package org.projectforge.web.wicket.components;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * Panel for using as content top menu entry (needed for css decoration).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class ContentMenuEntryPanel extends Panel
{
  private static final long serialVersionUID = -5507326592369611604L;

  public ContentMenuEntryPanel(final String id, final AbstractLink link, final String label)
  {
    super(id);
    setRenderBodyOnly(true);
    add(link);
    final Label labelComp = new Label("label", label);
    labelComp.setRenderBodyOnly(true);
    link.add(labelComp);
  }
}
