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

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.address.AddressFilter;
import org.projectforge.web.mobile.AbstractMobileForm;

public class AddressListMobileForm extends AbstractMobileForm<AddressListMobileForm, AddressListMobilePage>
{
  private static final long serialVersionUID = -4341937420376832550L;

  AddressFilter filter;

  public AddressListMobileForm(final AddressListMobilePage parentPage)
  {
    super(parentPage);
  }

  @SuppressWarnings("serial")
  protected void init()
  {
    filter = new AddressFilter();
    add(new TextField<String>("searchField", new PropertyModel<String>(filter, "searchString")).add(new SimpleAttributeModifier(
        "placeholder", getString("search"))));
    final Button searchButton = new Button("searchButton", new Model<String>(getString("search"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.search();
      }
    };
    add(searchButton);
  }

}
