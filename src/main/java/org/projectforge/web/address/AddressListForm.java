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

package org.projectforge.web.address;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.address.AddressFilter;
import org.projectforge.common.StringHelper;
import org.projectforge.web.wicket.AbstractListForm;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteTextField;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivType;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.GridBuilder;
import org.projectforge.web.wicket.flowlayout.InputPanel;
import org.projectforge.web.wicket.flowlayout.RadioGroupPanel;

public class AddressListForm extends AbstractListForm<AddressListFilter, AddressListPage>
{
  private static final long serialVersionUID = 8124796579658957116L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddressListForm.class);

  @SpringBean(name = "addressDao")
  private AddressDao addressDao;

  /**
   * Used by AddressCampaignValueListForm.
   */
  @SuppressWarnings("serial")
  public static void addFilter(final AbstractListPage< ? , ? , ? > parentPage, final AbstractListForm< ? , ? > form,
      final GridBuilder gridBuilder, final AddressFilter searchFilter)
  {
    {
      gridBuilder.newColumnPanel(DivType.COL_60);
      final FieldsetPanel fs = gridBuilder.newFieldset(parentPage.getString("label.options")).setNoLabelFor();
      final DivPanel radioGroupPanel = fs.addNewRadioBoxDiv();
      final RadioGroupPanel<String> radioGroup = new RadioGroupPanel<String>(radioGroupPanel.newChildId(), "listtype", new PropertyModel<String>(
          searchFilter, "listType")) {
        /**
         * @see org.projectforge.web.wicket.flowlayout.RadioGroupPanel#wantOnSelectionChangedNotifications()
         */
        @Override
        protected boolean wantOnSelectionChangedNotifications()
        {
          return true;
        }

        /**
         * @see org.projectforge.web.wicket.flowlayout.RadioGroupPanel#onSelectionChanged(java.lang.Object)
         */
        @Override
        protected void onSelectionChanged(final Object newSelection)
        {
          parentPage.refresh();
        }
      };
      radioGroupPanel.add(radioGroup);
      radioGroup.add(new Model<String>("filter"), parentPage.getString("filter"));
      radioGroup.add(new Model<String>("newest"), parentPage.getString("filter.newest"));
      radioGroup.add(new Model<String>("myFavorites"), parentPage.getString("address.filter.myFavorites"));
      radioGroup
      .add(new Model<String>("deleted"), parentPage.getString(I18N_ONLY_DELETED), parentPage.getString(I18N_ONLY_DELETED_TOOLTIP));
    }
    {
      // DropDownChoice page size
      gridBuilder.newColumnPanel(DivType.COL_40);
      form.addPageSizeFieldset();
    }
    {
      gridBuilder.addColumnPanel(new DivPanel(gridBuilder.newColumnPanelId()) {
        @Override
        public boolean isVisible()
        {
          return searchFilter.isFilter();
        }
      }, DivType.COL_60);
      final FieldsetPanel fieldset = gridBuilder.newFieldset(parentPage.getString("address.contactStatus")).setNoLabelFor();
      final DivPanel checkBoxPanel = fieldset.addNewCheckBoxDiv();
      checkBoxPanel.add(form.createAutoRefreshCheckBoxPanel(checkBoxPanel.newChildId(), new PropertyModel<Boolean>(searchFilter, "active"),
          parentPage.getString("address.contactStatus.active")));
      checkBoxPanel.add(form.createAutoRefreshCheckBoxPanel(checkBoxPanel.newChildId(), new PropertyModel<Boolean>(searchFilter,
          "nonActive"), parentPage.getString("address.contactStatus.nonActive")));
      checkBoxPanel.add(form.createAutoRefreshCheckBoxPanel(checkBoxPanel.newChildId(), new PropertyModel<Boolean>(searchFilter,
          "uninteresting"), parentPage.getString("address.contactStatus.uninteresting")));
      checkBoxPanel.add(form.createAutoRefreshCheckBoxPanel(checkBoxPanel.newChildId(), new PropertyModel<Boolean>(searchFilter,
          "personaIngrata"), parentPage.getString("address.contactStatus.personaIngrata")));
      checkBoxPanel.add(form.createAutoRefreshCheckBoxPanel(checkBoxPanel.newChildId(),
          new PropertyModel<Boolean>(searchFilter, "departed"), parentPage.getString("address.contactStatus.departed")));
    }
    {
      gridBuilder.addColumnPanel(new DivPanel(gridBuilder.newColumnPanelId()) {
        @Override
        public boolean isVisible()
        {
          return searchFilter.isFilter();
        }
      }, DivType.COL_40);
      final FieldsetPanel fieldset = gridBuilder.newFieldset(parentPage.getString("address.addressStatus")).setNoLabelFor();
      final DivPanel checkBoxPanel = fieldset.addNewCheckBoxDiv();
      checkBoxPanel.add(form.createAutoRefreshCheckBoxPanel(checkBoxPanel.newChildId(),
          new PropertyModel<Boolean>(searchFilter, "uptodate"), parentPage.getString("address.addressStatus.uptodate")));
      checkBoxPanel.add(form.createAutoRefreshCheckBoxPanel(checkBoxPanel.newChildId(),
          new PropertyModel<Boolean>(searchFilter, "outdated"), parentPage.getString("address.addressStatus.outdated")));
      checkBoxPanel.add(form.createAutoRefreshCheckBoxPanel(checkBoxPanel.newChildId(), new PropertyModel<Boolean>(searchFilter, "leaved"),
          parentPage.getString("address.addressStatus.leaved")));
    }

  }

  @Override
  protected void init()
  {
    super.init();
    gridBuilder.newColumnsPanel();
    addFilter(parentPage, this, gridBuilder, getSearchFilter());
  }

  public AddressListForm(final AddressListPage parentPage)
  {
    super(parentPage);
  }

  @SuppressWarnings("serial")
  @Override
  protected TextField<?> createSearchTextField()
  {
    @SuppressWarnings({ "unchecked", "rawtypes"})
    final PFAutoCompleteTextField<AddressDO> searchField = new PFAutoCompleteTextField<AddressDO>(InputPanel.WICKET_ID, new Model() {
      @Override
      public Serializable getObject()
      {
        // Pseudo object for storing search string (title field is used for this foreign purpose).
        return new AddressDO().setComment(searchFilter.getSearchString());
      }

      @Override
      public void setObject(final Serializable object)
      {
        if (object != null) {
          if (object instanceof String) {
            searchFilter.setSearchString((String) object);
          }
        } else {
          searchFilter.setSearchString("");
        }
      }
    }) {
      @Override
      protected List<AddressDO> getChoices(final String input)
      {
        final AddressFilter filter = new AddressFilter();
        filter.setSearchString(input);
        filter.setSearchFields("name", "firstName", "organization");
        final List<AddressDO> list = addressDao.getList(filter);
        return list;
      }

      @Override
      protected List<String> getRecentUserInputs()
      {
        return parentPage.getRecentSearchTermsQueue().getRecents();
      }

      @Override
      protected String formatLabel(final AddressDO address)
      {
        return StringHelper.listToString("; ", address.getName(), address.getFirstName(), address.getOrganization());
      }

      @Override
      protected String formatValue(final AddressDO address)
      {
        return "id:" + address.getId();
      }

      /**
       * @see org.apache.wicket.Component#getConverter(java.lang.Class)
       */
      @Override
      public <C> IConverter<C> getConverter(final Class<C> type)
      {
        return new IConverter<C>() {
          @Override
          public C convertToObject(final String value, final Locale locale)
          {
            searchFilter.setSearchString(value);
            return null;
          }

          @Override
          public String convertToString(final Object value, final Locale locale)
          {
            return searchFilter.getSearchString();
          }
        };
      }
    };
    searchField.withLabelValue(true).withMatchContains(true).withMinChars(2).withFocus(true).withAutoSubmit(true);
    createSearchFieldTooltip(searchField);
    return searchField;
  }

  @Override
  protected AddressListFilter newSearchFilterInstance()
  {
    return new AddressListFilter();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
