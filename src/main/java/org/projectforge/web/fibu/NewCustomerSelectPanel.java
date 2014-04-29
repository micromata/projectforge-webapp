/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.web.fibu;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.common.NumberHelper;
import org.projectforge.common.RecentQueue;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.fibu.KundeDO;
import org.projectforge.fibu.KundeDao;
import org.projectforge.fibu.KundeFormatter;
import org.projectforge.web.user.UserPreferencesHelper;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.AbstractSelectPanel;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteTextField;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.flowlayout.ComponentWrapperPanel;

/**
 * This panel shows the actual customer.
 * @author Werner Feder (werner.feder@t-online.de)
 * 
 */
public class NewCustomerSelectPanel extends AbstractSelectPanel<KundeDO> implements ComponentWrapperPanel
{
  private static final long serialVersionUID = -7114401036341110814L;

  private static final String USER_PREF_KEY_RECENT_CUSTOMERS = "CustomerSelectPanel:recentCustomers";

  @SuppressWarnings("unused")
  private boolean defaultFormProcessing = false;

  @SpringBean(name = "kundeFormatter")
  private KundeFormatter kundeFormatter;

  @SpringBean(name = "kundeDao")
  private KundeDao kundeDao;

  private RecentQueue<String> recentCustomers;

  private final PFAutoCompleteTextField<KundeDO> customerTextField;

  // Only used for detecting changes:
  private KundeDO currentCustomer;

  private final PropertyModel<String> kundeText;

  private TextField<String> kundeTextField;

  /**
   * @param id
   * @param model
   * @param caller
   * @param selectProperty
   */
  @SuppressWarnings("serial")
  public NewCustomerSelectPanel(final String id, final IModel<KundeDO> model, final PropertyModel<String> kundeText, final ISelectCallerPage caller,
      final String selectProperty)
  {
    super(id, model, caller, selectProperty);
    this.kundeText = kundeText;
    customerTextField = new PFAutoCompleteTextField<KundeDO>("customerField", getModel()) {
      @Override
      protected List<KundeDO> getChoices(final String input)
      {
        final BaseSearchFilter filter = new BaseSearchFilter();
        filter.setSearchFields("id", "name", "identifier", "division");
        filter.setSearchString(input);
        final List<KundeDO> list = kundeDao.getList(filter);
        return list;
      }

      @Override
      protected List<String> getRecentUserInputs()
      {
        return getRecentCustomers().getRecents();
      }

      @Override
      protected String formatLabel(final KundeDO customer)
      {
        if (customer == null) {
          return "";
        }
        return kundeFormatter.format(customer, false);
      }

      @Override
      protected String formatValue(final KundeDO customer)
      {
        if (customer == null) {
          return "";
        }
        return kundeFormatter.format(customer, false);
      }

      @Override
      protected void convertInput()
      {
        final KundeDO customer = getConverter(getType()).convertToObject(getInput(), getLocale());
        setConvertedInput(customer);
        if (customer != null && (currentCustomer == null || customer.getId() != currentCustomer.getId())) {
          getRecentCustomers().append(kundeFormatter.format(customer, false));
        }
        currentCustomer = customer;
      }

      /**
       * @see org.apache.wicket.Component#getConverter(java.lang.Class)
       */

      @SuppressWarnings({ "unchecked", "rawtypes"})
      @Override
      public <C> IConverter<C>  getConverter(final Class<C> type)
      {
        return new IConverter() {
          @Override
          public Object convertToObject(final String value, final Locale locale)
          {
            if (StringUtils.isEmpty(value) == true) {
              getModel().setObject(null);
              return null;
            }
            final int ind = value.indexOf(" - ");
            final String id = ind >= 0 ? value.substring(0, ind) : value;
            final KundeDO kunde = kundeDao.getById(Integer.decode(id));
            if (kunde == null) {
              error(getString("panel.error.customernameNotFound"));
            }
            getModel().setObject(kunde);
            return kunde;
          }

          @Override
          public String convertToString(final Object value, final Locale locale)
          {
            if (value == null) {
              return "";
            }
            final KundeDO kunde = (KundeDO) value;
            return formatLabel(kunde);
          }

        };
      }
    };
    currentCustomer = getModelObject();
    customerTextField.enableTooltips().withLabelValue(true).withMatchContains(true).withMinChars(2).withAutoSubmit(false); //.withWidth(400);
  }


  /**
   * Should be called before init() method. If true, then the validation will be done after submitting.
   * @param defaultFormProcessing
   */
  public void setDefaultFormProcessing(final boolean defaultFormProcessing)
  {
    this.defaultFormProcessing = defaultFormProcessing;
  }

  @SuppressWarnings("serial")
  @Override
  public NewCustomerSelectPanel init()
  {
    super.init();
    if (kundeText != null) {
      kundeTextField = new MaxLengthTextField("kundeText", kundeText) {
        @Override
        public boolean isVisible()
        {
          return (NewCustomerSelectPanel.this.getModelObject() == null || NumberHelper.greaterZero(NewCustomerSelectPanel.this.getModelObject()
              .getId()) == false);
        }
      };
      add(kundeTextField);
    } else {
      add(AbstractForm.createInvisibleDummyComponent("kundeText"));
    }
    add(customerTextField);
    return this;
  }

  public NewCustomerSelectPanel withAutoSubmit(final boolean autoSubmit)
  {
    customerTextField.withAutoSubmit(autoSubmit);
    return this;
  }

  @Override
  public Component getWrappedComponent()
  {
    return customerTextField;
  }

  @Override
  protected void convertInput()
  {
    setConvertedInput(getModelObject());
  }

  @SuppressWarnings("unchecked")
  private RecentQueue<String> getRecentCustomers()
  {
    if (this.recentCustomers == null) {
      this.recentCustomers = (RecentQueue<String>) UserPreferencesHelper.getEntry(USER_PREF_KEY_RECENT_CUSTOMERS);
    }
    if (this.recentCustomers == null) {
      this.recentCustomers = new RecentQueue<String>();
      UserPreferencesHelper.putEntry(USER_PREF_KEY_RECENT_CUSTOMERS, this.recentCustomers, true);
    }
    return this.recentCustomers;
  }

  @SuppressWarnings("unused")
  private String formatCustomer(final KundeDO customer)
  {
    if (customer == null) {
      return "";
    }
    return kundeFormatter.format(customer, false);
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.ComponentWrapperPanel#getComponentOutputId()
   */
  @Override
  public String getComponentOutputId()
  {
    customerTextField.setOutputMarkupId(true);
    return customerTextField.getMarkupId();
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.ComponentWrapperPanel#getFormComponent()
   */
  @Override
  public FormComponent< ? > getFormComponent()
  {
    return customerTextField;
  }

  /**
   * @return The user's raw input of kunde text if given, otherwise null.
   */
  public String getKundeTextInput()
  {
    if (kundeTextField != null) {
      return kundeTextField.getRawInput();
    }
    return null;
  }

  /**
   * @return the kundeTextField
   */
  public TextField<String> getKundeTextField()
  {
    return kundeTextField;
  }

}
