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

package org.projectforge.web.address;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.Validate;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.address.FormOfAddress;
import org.projectforge.web.core.BaseAction;
import org.projectforge.web.core.BaseActionBean;


/**
 * Only for displaying address in printable format or for copy and paste purposes.
 */
@UrlBinding("/secure/address/AddressView.action")
@BaseAction(jspUrl = "/WEB-INF/jsp/address/addressView.jsp")
public class AddressViewAction extends BaseActionBean
{
  private AddressDO address;

  private AddressDao addressDao;
  
  private String form;

  protected Integer id;

  public void setAddressDao(AddressDao addressDao)
  {
    this.addressDao = addressDao;
  }

  public AddressDO getAddress()
  {
    return address;
  }

  public void setId(Integer id)
  {
    this.id = id;
  }
  
  public String getForm()
  {
    return form;
  }

  @DefaultHandler
  @DontValidate
  public Resolution execute()
  {
    Validate.notNull(id);
    address = addressDao.getById(id);
    if (address.getForm() != FormOfAddress.UNKNOWN) {
      form = getLocalizedString(address.getForm().getI18nKey());
    } else {
      form = null;
    }
    return new ForwardResolution(getJspUrl());
  }
}
