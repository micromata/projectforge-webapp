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

package org.projectforge.web.wicket;

import java.io.Serializable;

import org.apache.wicket.markup.html.link.Link;
import org.projectforge.web.wicket.components.ContentMenuEntryPanel;

/**
 * Helper for creating re-index menu items in the top right drop down menu.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public abstract class AbstractReindexTopRightMenu implements Serializable
{
  private static final long serialVersionUID = 2959661327690147049L;

  protected abstract void rebuildDatabaseIndex(boolean onlyNewest);

  protected abstract String getString(final String i18nKey);

  @SuppressWarnings("serial")
  protected AbstractReindexTopRightMenu(final AbstractSecuredPage page, final boolean enableFullReindex)
  {
    final ContentMenuEntryPanel  reindex = new ContentMenuEntryPanel(page.getNewContentRightMenuChildId(), new Link<Object>("link") {
      @Override
      public void onClick()
      {
        rebuildDatabaseIndex(true);
      };
    }, getString("menu.reindexNewestDatabaseEntries"));
    WicketUtils.addTooltip(reindex, getString("menu.reindexNewestDatabaseEntries.tooltip"));
    page.addContentRightMenuEntry(reindex);
    if (enableFullReindex == true) {
      final ContentMenuEntryPanel  reindexAll = new ContentMenuEntryPanel(page.getNewContentRightMenuChildId(), new Link<Object>("link") {
        @Override
        public void onClick()
        {
          rebuildDatabaseIndex(false);
        };
      }, getString("menu.reindexAllDatabaseEntries"));
      WicketUtils.addTooltip(reindexAll, getString("menu.reindexAllDatabaseEntries.tooltip"));
      page.addContentRightMenuEntry(reindexAll);
    }
  }
}
