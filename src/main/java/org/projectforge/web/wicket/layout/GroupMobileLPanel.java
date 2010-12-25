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

/**
 * Represents a mobile group panel. A field set, form or page can contain multiple group panels. A group panel groups fields.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class GroupMobileLPanel extends GroupLPanel
{
  private static final long serialVersionUID = -280050296848404710L;

  /**
   * @see AbstractRenderer#createGroupLPanel(String)
   */
  GroupMobileLPanel(final String id)
  {
    super(id);
  }

  /**
   * @see AbstractRenderer#createGroupLPanel(String, String)
   */
  GroupMobileLPanel(final String id, final String heading)
  {
    super(id, heading);
  }
}
