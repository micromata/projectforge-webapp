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

package org.projectforge.web.mobile;

import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.projectforge.core.AbstractBaseDO;

public abstract class AbstractMobileEditForm<O extends AbstractBaseDO< ? >, P extends AbstractMobileEditPage< ? , ? , ? >> extends
    AbstractMobileForm<O, P>
{
  private static final long serialVersionUID = 1836099012618517190L;

  protected O data;

  public AbstractMobileEditForm(P parentPage, O data)
  {
    super(parentPage);
    this.data = data;
  }

  public O getData()
  {
    return this.data;
  }

  public boolean isNew()
  {
    return this.data == null || this.data.getId() == null;
  }

  protected void init()
  {
    add(new FeedbackPanel("feedback").setOutputMarkupId(true));
  }
}
