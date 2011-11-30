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

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.web.wicket.AbstractListForm;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;

/**
 * The list formular for the list view (this example has no filter settings). See ToDoListPage for seeing how to use filter settings.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class AddressCampaignValueListForm extends AbstractListForm<AddressCampaignFilter, AddressCampaignValueListPage>
{
  private static final long serialVersionUID = 6190615904711764514L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddressCampaignValueListForm.class);

  @SpringBean(name = "addressCampaignDao")
  private AddressCampaignDao addressCampaignDao;

  private Integer addressCampaignId;

  public AddressCampaignValueListForm(final AddressCampaignValueListPage parentPage)
  {
    super(parentPage);
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    this.addressCampaignId = searchFilter.getAddressCampaignId();
    final List<AddressCampaignDO> addressCampaignList = addressCampaignDao.getList(new AddressCampaignFilter());
    {
      final LabelValueChoiceRenderer<Integer> addressCampaignRenderer = new LabelValueChoiceRenderer<Integer>();
      for (final AddressCampaignDO addressCampaign : addressCampaignList) {
        addressCampaignRenderer.addValue(addressCampaign.getId(), addressCampaign.getTitle());
      }
      final DropDownChoice<Integer> addressCampaignChoice = new DropDownChoice<Integer>("addressCampaign", new PropertyModel<Integer>(this,
      "addressCampaignId"), addressCampaignRenderer.getValues(), addressCampaignRenderer) {
        @Override
        protected void onSelectionChanged(final Integer newSelection)
        {
          for (final AddressCampaignDO addressCampaign : addressCampaignList) {
            if (addressCampaign.getId().equals(addressCampaignId) == true) {
              searchFilter.setAddressCampaign(addressCampaign);
              break;
            }
          }
        }
        @Override
        protected boolean wantOnSelectionChangedNotifications()
        {
          return true;
        }
      };
      filterContainer.add(addressCampaignChoice);
    }
  }

  @Override
  protected boolean isFilterVisible()
  {
    return parentPage.isMassUpdateMode() == false;
  }

  @Override
  protected AddressCampaignFilter newSearchFilterInstance()
  {
    return new AddressCampaignFilter();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
