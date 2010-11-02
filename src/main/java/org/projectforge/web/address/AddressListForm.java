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

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.address.AddressFilter;
import org.projectforge.common.StringHelper;
import org.projectforge.web.wicket.AbstractListForm;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteTextField;
import org.projectforge.web.wicket.components.SingleButtonPanel;

public class AddressListForm extends AbstractListForm<AddressListFilter, AddressListPage>
{
  private static final long serialVersionUID = 8124796579658957116L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddressListForm.class);

  @SpringBean(name = "addressDao")
  private AddressDao addressDao;

  @Override
  protected void init()
  {
    super.init();
    filterContainer.add(new CheckBox("uptodate", new PropertyModel<Boolean>(getSearchFilter(), "uptodate")));
    filterContainer.add(new CheckBox("outdated", new PropertyModel<Boolean>(getSearchFilter(), "outdated")));
    filterContainer.add(new CheckBox("leaved", new PropertyModel<Boolean>(getSearchFilter(), "leaved")));

    filterContainer.add(new CheckBox("active", new PropertyModel<Boolean>(getSearchFilter(), "active")));
    filterContainer.add(new CheckBox("nonActive", new PropertyModel<Boolean>(getSearchFilter(), "nonActive")));
    filterContainer.add(new CheckBox("uninteresting", new PropertyModel<Boolean>(getSearchFilter(), "uninteresting")));
    filterContainer.add(new CheckBox("personaIngrata", new PropertyModel<Boolean>(getSearchFilter(), "personaIngrata")));
    filterContainer.add(new CheckBox("departed", new PropertyModel<Boolean>(getSearchFilter(), "departed")));

    // Radio choices:
    final RadioGroup<String> filterType = new RadioGroup<String>("filterType", new PropertyModel<String>(getSearchFilter(), "listType"));
    filterType.add(new Radio<String>("filter", new Model<String>("filter")).setOutputMarkupId(false));
    filterType.add(new Radio<String>("newest", new Model<String>("newest")));
    filterType.add(new Radio<String>("myFavorites", new Model<String>("myFavorites")));
    filterType.add(new Radio<String>("deleted", new Model<String>("deleted")));
    filterContainer.add(filterType);
    
    @SuppressWarnings("serial")
    final Button exportVCards = new Button("button", new Model<String>(getString("exportFavoriteVCards"))) {
      @Override
      public final void onSubmit()
      {
       // getParentPage().onResetSubmit();
      }
    };
    exportVCards.setDefaultFormProcessing(false).add(new SimpleAttributeModifier("title", getString("address.book.vCardExport.tooltip")));
    final SingleButtonPanel exportVCardsPanel = new SingleButtonPanel(getNewActionButtonChildId(), exportVCards);
    prependActionButton(exportVCardsPanel);

    // Export vCards, export, Telefonliste, Apple-Script
}

  public AddressListForm(AddressListPage parentPage)
  {
    super(parentPage);
  }

  @SuppressWarnings("serial")
  @Override
  protected Component createSearchTextField()
  {
    @SuppressWarnings("unchecked")
    final PFAutoCompleteTextField<AddressDO> searchField = new PFAutoCompleteTextField<AddressDO>("searchString", new Model() {
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
        filter.setSearchFields("title", "authors");
        final List<AddressDO> list = addressDao.getList(filter);
        return list;
      }

      @Override
      protected String formatLabel(final AddressDO address)
      {
        return StringHelper.listToString("; ", address.getName(), address.getFirstName());
      }

      @Override
      protected String formatValue(AddressDO address)
      {
        return "id:" + address.getId();
      }

      @Override
      public IConverter getConverter(Class< ? > type)
      {
        return new IConverter() {
          @Override
          public Object convertToObject(String value, Locale locale)
          {
            searchFilter.setSearchString(value);
            return null;
          }

          @Override
          public String convertToString(Object value, Locale locale)
          {
            return searchFilter.getSearchString();
          }
        };
      }
    };
    searchField.withLabelValue(true).withMatchContains(true).withMinChars(2).withFocus(true).withAutoSubmit(true);
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
