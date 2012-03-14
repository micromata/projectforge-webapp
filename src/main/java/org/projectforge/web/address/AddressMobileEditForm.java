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

package org.projectforge.web.address;

import org.apache.wicket.model.PropertyModel;
import org.projectforge.address.AddressDO;
import org.projectforge.web.mobile.AbstractMobileEditForm;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.flowlayout.DivType;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

public class AddressMobileEditForm extends AbstractMobileEditForm<AddressDO, AddressMobileEditPage>
{
  private static final long serialVersionUID = -8781593985402346929L;

  //  @SpringBean(name = "personalAddressDao")
  //  private PersonalAddressDao personalAddressDao;

  //  protected PersonalAddressDO personalAddress;

  public AddressMobileEditForm(final AddressMobileEditPage parentPage, final AddressDO data)
  {
    super(parentPage, data);
    // personalAddress = null;
    // if (isNew() == false) {
    // personalAddress = personalAddressDao.getByAddressId(getData().getId());
    // }
    // if (personalAddress == null) {
    // personalAddress = new PersonalAddressDO();
    // }
  }

  @Override
  protected void init()
  {
    super.init();
    gridBuilder.newGrid8().newColumnsPanel().newColumnPanel(DivType.COL_50);
    {
      // Name
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("name"));
      final MaxLengthTextField name = new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, "name"));
      //fs.add(dependentFormComponents[1] = name);
    }
  }
}
