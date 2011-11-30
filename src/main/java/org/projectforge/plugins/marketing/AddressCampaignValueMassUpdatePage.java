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

package org.projectforge.plugins.marketing;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.address.AddressDO;
import org.projectforge.address.PersonalAddressDO;
import org.projectforge.common.MyBeanComparator;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.AbstractSecuredPage;

public class AddressCampaignValueMassUpdatePage extends AbstractSecuredPage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddressCampaignValueMassUpdatePage.class);

  @SpringBean(name = "addressCampaignValueDao")
  private AddressCampaignValueDao addressCampaignValueDao;

  private final List<AddressDO> addresses;

  private final AddressCampaignValueMassUpdateForm form;

  private final AbstractSecuredPage callerPage;

  public AddressCampaignValueMassUpdatePage(final AbstractSecuredPage callerPage, final List<AddressDO> addresses, final AddressCampaignDO addressCampaign, final Map<Integer, PersonalAddressDO> personalAddressMap, final Map<Integer, AddressCampaignValueDO> addressCampaignValueMap)
  {
    super(new PageParameters());
    this.callerPage = callerPage;
    this.addresses = addresses;
    form = new AddressCampaignValueMassUpdateForm(this, addressCampaign);
    body.add(form);
    form.init();
    final List<IColumn<AddressDO>> columns = AddressCampaignValueListPage.createColumns(this, false, true, null, personalAddressMap, addressCampaignValueMap);
    @SuppressWarnings("serial")
    final SortableDataProvider<AddressDO> sortableDataProvider = new SortableDataProvider<AddressDO>() {
      public Iterator<AddressDO> iterator(final int first, final int count)
      {
        final SortParam sp = getSort();
        if (addresses == null) {
          return null;
        }
        final Comparator<AddressDO> comp = new MyBeanComparator<AddressDO>(sp.getProperty(), sp.isAscending());
        Collections.sort(addresses, comp);
        return addresses.subList(first, first + count).iterator();
      }

      public int size()
      {
        return addresses != null ? addresses.size() : 0;
      }

      public IModel<AddressDO> model(final AddressDO object)
      {
        return new Model<AddressDO>() {
          @Override
          public AddressDO getObject()
          {
            return object;
          }
        };
      }
    };
    sortableDataProvider.setSort("name", false);

    final DefaultDataTable<AddressDO> dataTable = new DefaultDataTable<AddressDO>("table", columns, sortableDataProvider, 1000);
    body.add(dataTable);
    body.add(new Label("showUpdateQuestionDialog", "function showUpdateQuestionDialog() {\n" + //
        "  return window.confirm('"
        + getString("question.massUpdateQuestion")
        + "');\n"
        + "}\n") //
    .setEscapeModelStrings(false));
  }

  @Override
  protected String getTitle()
  {
    return getString("addressCampaignValue.massupdate.title");
  }

  protected void onCancelSubmit()
  {
    setResponsePage(callerPage);
  }

  protected void onUpdateAllSubmit()
  {
    final AddressCampaignValueDO data = form.data;
    addressCampaignValueDao.massUpdate(addresses, data.getAddressCampaign(), data.getValue(), data.getComment());
    if (callerPage instanceof AbstractListPage< ? , ? , ? >) {
      ((AbstractListPage< ? , ? , ? >) callerPage).setMassUpdateMode(false);
    }
    setResponsePage(callerPage);
  }
}
