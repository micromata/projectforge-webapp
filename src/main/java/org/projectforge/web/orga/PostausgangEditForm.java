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

package org.projectforge.web.orga;

import org.apache.log4j.Logger;
import org.projectforge.orga.PostausgangDO;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.layout.LayoutContext;


public class PostausgangEditForm extends AbstractEditForm<PostausgangDO, PostausgangEditPage>
{
  private static final long serialVersionUID = -2138017238114715368L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PostausgangEditForm.class);

  protected PostausgangFormRenderer renderer;

  public PostausgangEditForm(PostausgangEditPage parentPage, PostausgangDO data)
  {
    super(parentPage, data);
    renderer = new PostausgangFormRenderer(this, parentPage, new LayoutContext(this), parentPage.getBaseDao(), data);
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
