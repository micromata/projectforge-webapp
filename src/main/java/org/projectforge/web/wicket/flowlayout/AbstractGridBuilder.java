/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
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

import java.io.Serializable;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.projectforge.web.BrowserScreenWidthType;
import org.projectforge.web.wicket.MySession;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public abstract class AbstractGridBuilder<T extends AbstractFieldsetPanel< ? >> implements Serializable
{
  private static final long serialVersionUID = -8804674487579491611L;

  protected BrowserScreenWidthType browserScreenWidthType;

  protected RepeatingView parentRepeatingView;

  protected DivPanel parentDivPanel;

  public AbstractGridBuilder(final RepeatingView parent, final MySession session)
  {
    this(session);
    this.parentRepeatingView = parent;
  }

  public AbstractGridBuilder(final DivPanel parent, final MySession session)
  {
    this(session);
    this.parentDivPanel = parent;
  }

  private AbstractGridBuilder(final MySession session)
  {
    this.browserScreenWidthType = session.getBrowserScreenWidthType();
    if (this.browserScreenWidthType == null) {
      this.browserScreenWidthType = BrowserScreenWidthType.NORMAL;
    }
  }

  protected AbstractGridBuilder()
  {
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#getString(java.lang.String)
   */
  public String getString(final String i18nKey)
  {
    if (this.parentRepeatingView != null) {
      return this.parentRepeatingView.getString(i18nKey);
    } else {
      return this.parentDivPanel.getString(i18nKey);
    }
  }

  protected WebMarkupContainer getParent()
  {
    if (parentRepeatingView != null) {
      return parentRepeatingView;
    } else {
      return parentDivPanel;
    }
  }

  protected String newParentChildId()
  {
    if (parentRepeatingView != null) {
      return parentRepeatingView.newChildId();
    } else {
      return parentDivPanel.newChildId();
    }
  }

  public abstract T newFieldset(final String label);

  public abstract T newFieldset(final FieldProperties<?> fieldProperties);

  public abstract T newFieldset(final String label, final boolean multipleChildren);

  public abstract T newFieldset(final String labelText, final String labelDescription);

  public abstract T newFieldset(final String labelText, final String labelDescription, final boolean multipleChildren);
}
