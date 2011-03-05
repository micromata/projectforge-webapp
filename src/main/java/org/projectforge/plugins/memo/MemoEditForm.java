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

package org.projectforge.plugins.memo;

import org.apache.log4j.Logger;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.layout.LayoutContext;

/**
 * This is the edit formular page.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 *
 */
public class MemoEditForm extends AbstractEditForm<MemoDO, MemoEditPage>
{
  private static final long serialVersionUID = -6208809585214296635L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MemoEditForm.class);

  protected MemoFormRenderer renderer;

  public MemoEditForm(MemoEditPage parentPage, MemoDO data)
  {
    super(parentPage, data);
    data.setOwner(PFUserContext.getUser());
    renderer = new MemoFormRenderer(this, new LayoutContext(this), data);
  }

  @Override
  protected void init()
  {
    super.init();
    renderer.add();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
