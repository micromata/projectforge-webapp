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

package org.projectforge.web.fibu;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.common.RecentQueue;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.fibu.KundeDO;
import org.projectforge.fibu.KundeDao;
import org.projectforge.fibu.KundeFormatter;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserXmlPreferencesCache;
import org.projectforge.web.wicket.AbstractSelectPanel;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteTextField;
import org.projectforge.web.wicket.flowlayout.ComponentWrapperPanel;

/**
 * This panel shows the actual customer.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class NewCustomerSelectPanel extends AbstractSelectPanel<KundeDO> implements ComponentWrapperPanel
{
  private static final long serialVersionUID = -7114401036341110814L;

  private static final String USER_PREF_KEY_RECENT_CUSTOMERS = "CustomerSelectPanel:recentCustomers";

  private boolean defaultFormProcessing = false;

  @SpringBean(name = "kundeFormatter")
  private KundeFormatter kundeFormatter;

  @SpringBean(name = "kundeDao")
  private KundeDao kundeDao;

  @SpringBean(name = "userXmlPreferencesCache")
  protected UserXmlPreferencesCache userXmlPreferencesCache;

  private RecentQueue<String> recentCustomers;

  private final PFAutoCompleteTextField<KundeDO> customerTextField;

  // Only used for detecting changes:
  private KundeDO currentCustomer;

  /**
   * @param id
   * @param model
   * @param caller
   * @param selectProperty
   */
  public NewCustomerSelectPanel(final String id, final IModel<KundeDO> model, final ISelectCallerPage caller, final String selectProperty)
  {
    this(id, model, null, caller, selectProperty);
  }

  /**
   * @param id
   * @param model
   * @param caller
   * @param selectProperty
   */
  @SuppressWarnings("serial")
  public NewCustomerSelectPanel(final String id, final IModel<KundeDO> model, final String label, final ISelectCallerPage caller,
      final String selectProperty)
  {
    super(id, model, caller, selectProperty);
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
      @Override
      public <C> IConverter<C> getConverter(final Class<C> type)
      {
        return new IConverter() {
          @Override
          public Object convertToObject(final String value, final Locale locale)
          {
            if (StringUtils.isEmpty(value) == true) {
              getModel().setObject(null);
              return null;
            }
            final int ind = value.indexOf(": ");
            final String customername = ind >= 0 ? value.substring(0, ind) : value;
            // final PFUserDO user = kundeDao.getById(customername);
            // if (user == null) {
            // error(getString("user.panel.error.usernameNotFound"));
            // }
            // getModel().setObject(user);
            // return user;
            return null;
          }

          @Override
          public String convertToString(final Object value, final Locale locale)
          {
            if (value == null) {
              return "";
            }
            final PFUserDO user = (PFUserDO) value;
            return user.getUsername();
          }

        };
      }
    };
    currentCustomer = getModelObject();
    customerTextField.enableTooltips().withLabelValue(true).withMatchContains(true).withMinChars(2).withAutoSubmit(false).withWidth(400);
  }

  /**
   * Should be called before init() method. If true, then the validation will be done after submitting.
   * @param defaultFormProcessing
   */
  public void setDefaultFormProcessing(final boolean defaultFormProcessing)
  {
    this.defaultFormProcessing = defaultFormProcessing;
  }

  @Override
  @SuppressWarnings("serial")
  public NewCustomerSelectPanel init()
  {
    super.init();

    add(customerTextField);
    final SubmitLink selectMeButton = new SubmitLink("selectMe") {
      @Override
      public void onSubmit()
      {
        caller.select(selectProperty, PFUserContext.getUserId());
        markTextFieldModelAsChanged();
      }

      @Override
      public boolean isVisible()
      {
        // Is visible if no user is given or the given user is not the current logged in user.
        final KundeDO user = getModelObject();
        return user == null || user.getId().equals(PFUserContext.getUser().getId()) == false;
      }
    };
    add(selectMeButton);
    selectMeButton.setDefaultFormProcessing(defaultFormProcessing);
    //selectMeButton.add(new TooltipImage("selectMeHelp", WebConstants.IMAGE_USER_SELECT_ME, getString("tooltip.selectMe")));
    return this;
  }

  private void markTextFieldModelAsChanged()
  {
    customerTextField.modelChanged();
    final KundeDO user = getModelObject();
    if (user != null) {
      getRecentCustomers().append(formatCustomer(user));
    }
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
      this.recentCustomers = (RecentQueue<String>) userXmlPreferencesCache.getEntry(USER_PREF_KEY_RECENT_CUSTOMERS);
    }
    if (this.recentCustomers == null) {
      this.recentCustomers = new RecentQueue<String>();
      userXmlPreferencesCache.putEntry(USER_PREF_KEY_RECENT_CUSTOMERS, this.recentCustomers, true);
    }
    return this.recentCustomers;
  }

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
}
