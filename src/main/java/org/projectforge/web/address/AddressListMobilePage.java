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

import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.web.mobile.AbstractSecuredMobilePage;
import org.projectforge.web.mobile.MenuMobilePage;
import org.projectforge.web.mobile.MobileWebConstants;
import org.projectforge.web.mobile.PageItemEntryMenuPanel;
import org.projectforge.web.wicket.components.ImageBookmarkablePageLinkPanel;
import org.springframework.util.CollectionUtils;

public class AddressListMobilePage extends AbstractSecuredMobilePage
{
  protected static final int MAX_ROWS = 50;

  @SpringBean(name = "addressDao")
  private AddressDao addressDao;

  private AddressListMobileForm form;

  private List<AddressDO> list;

  private WebMarkupContainer resultList;

  private RepeatingView addressRepeater;

  public AddressListMobilePage(final PageParameters parameters)
  {
    super(parameters);
    leftnavRepeater.add(new ImageBookmarkablePageLinkPanel(leftnavRepeater.newChildId(), MenuMobilePage.class, getResponse(),
        MobileWebConstants.IMAGE_HOME));
    leftnavContainer.setVisible(true);
    form = new AddressListMobileForm(this);
    add(form);
    form.init();
    add(resultList = new WebMarkupContainer("resultList"));
    resultList.setVisible(false);
  }

  protected void search()
  {
    if (addressRepeater != null) {
      resultList.remove(addressRepeater);
    }
    list = addressDao.getList(form.filter);
    if (CollectionUtils.isEmpty(list) == true) {
      resultList.setVisible(false);
      return;
    }
    resultList.setVisible(true);
    resultList.add(addressRepeater = new RepeatingView("addressRepeater"));

    int counter = 0;
    for (final AddressDO address : list) {
      addressRepeater.add(new PageItemEntryMenuPanel(addressRepeater.newChildId(), AddressListMobilePage.class, null, address
          .getFirstName()
          + " "
          + address.getName(), address.getOrganization()));
      if (++counter >= MAX_ROWS) {
        break;
      }
    }
  }

  @Override
  protected String getTitle()
  {
    return getString("address.title.heading");
  }
}
