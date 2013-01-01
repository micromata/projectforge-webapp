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

package org.projectforge.web.wicket.flowlayout;

import java.io.Serializable;

import org.projectforge.web.mobile.AbstractMobileEditForm;
import org.projectforge.web.wicket.AbstractEditForm;

/**
 * The layout context contains information important for the render process. Has a page to be rendered as a read-only page or as a mobile
 * page? This context is given to several render methods.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class LayoutContext implements Serializable
{
  private static final long serialVersionUID = -679521117659235197L;

  private boolean mobile;

  private boolean readonly;

  private boolean newObject;

  public LayoutContext(final boolean mobile)
  {
    this.mobile = mobile;
  }

  public LayoutContext(final boolean mobile, final boolean readonly, final boolean newObject)
  {
    this.mobile = mobile;
    this.readonly = readonly;
    this.newObject = newObject;
  }

  /**
   * Mobile is false because AbstractEditForm is not a mobile form.
   * @param form
   */
  public LayoutContext(final AbstractEditForm< ? , ? > form)
  {
    this.newObject = form.isNew();
  }

  /**
   * Mobile is false because AbstractEditForm is not a mobile form.
   * @param form
   */
  public LayoutContext(final AbstractMobileEditForm< ? , ? > form)
  {
    this.newObject = form.isNew();
    this.mobile = true;
  }

  public boolean isMobile()
  {
    return mobile;
  }

  public boolean isMobileReadonly()
  {
    return mobile == true && readonly == true;
  }

  /**
   * @param mobile
   * @return this for chaining.
   */
  public LayoutContext setMobile(final boolean mobile)
  {
    this.mobile = mobile;
    return this;
  }

  public boolean isReadonly()
  {
    return readonly;
  }

  /**
   * @param readonly
   * @return this for chaining.
   */
  public LayoutContext setReadonly(final boolean readonly)
  {
    this.readonly = readonly;
    return this;
  }

  /**
   * @return true, if this object is rendered as a new object (normally to insert or to create).
   */
  public boolean isNew()
  {
    return newObject;
  }

  /**
   * @param newObject
   * @return this for chaining.
   */
  public LayoutContext setNew(final boolean newObject)
  {
    this.newObject = newObject;
    return this;
  }
}
