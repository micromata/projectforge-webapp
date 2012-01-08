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

package org.projectforge.web.fibu;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.common.NumberHelper;
import org.projectforge.common.RecentQueue;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.fibu.KundeDO;
import org.projectforge.fibu.KundeDao;
import org.projectforge.fibu.KundeFavorite;
import org.projectforge.fibu.KundeFormatter;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.AbstractSelectPanel;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteTextField;
import org.projectforge.web.wicket.components.FavoritesChoicePanel;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.TooltipImage;


/**
 * This panel show the actual kunde and buttons for select/unselect kunde.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class KontoSelectPanel extends AbstractSelectPanel<KundeDO>
{
  private static final long serialVersionUID = 5452693296383142460L;

  @SpringBean(name = "kundeFormatter")
  private KundeFormatter kundeFormatter;

  private final PropertyModel<String> kundeText;

  private RecentQueue<String> recentCustomers;

  private PFAutoCompleteTextField<KundeDO> customerTextField;

  // Only used for detecting changes:
  private KundeDO currentCustomer;

  @SpringBean(name = "customerDao")
  private KundeDao customerDao;

  /**
   * @param id
   * @param model
   * @param customerText If no Kunde is given then a free text field representing a Kunde can be used.
   * @param caller
   * @param selectProperty
   */
  public KontoSelectPanel(final String id, final IModel<KundeDO> model, final PropertyModel<String> customerText,
      final ISelectCallerPage caller, final String selectProperty)
  {
    super(id, model, caller, selectProperty);
    this.customerText = customerText;
  }

  @Override
  @SuppressWarnings("serial")
  public KontoSelectPanel init()
  {
    super.init();
    customerTextField = new PFAutoCompleteTextField<KundeDO>("customerField", getModel()) {
      @Override
      protected List<KundeDO> getChoices(final String input)
      {
        final BaseSearchFilter filter = new BaseSearchFilter();
        filter.setSearchFields("name", "identifier");
        filter.setSearchString(input);
        final List<KundeDO> list = customerDao.getList(filter);
        return list;
      }

      @Override
      protected List<String> getRecentUserInputs()
      {
        return getRecentUsers().getRecents();
      }

      @Override
      protected String formatLabel(final KundeDO customer)
      {
        if (customer == null) {
          return "";
        }
        return formatKunde(customer);
      }

      @Override
      protected String formatValue(final KundeDO customer)
      {
        if (customer == null) {
          return "";
        }
        return customer.getName();
      }
      @Override
      protected void convertInput()
      {
        final KundeDO customer = (KundeDO) getConverter(getType()).convertToObject(getInput(), getLocale());
        setConvertedInput(customer);
        if (customer != null && (currentCustomer == null || customer.getId() != currentCustomer.getId())) {
          getRecentKundes().append(formatKunde(customer));
        }
        currentCustomer = customer;
      }

      @Override
      public IConverter getConverter(final Class< ? > type)
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
            final KundeDO customer = customerDao.getKundeGroupCache().getKunde(customername);
            if (customer == null) {
              error(getString("customer.panel.error.customernameNotFound"));
            }
            getModel().setObject(customer);
            return customer;
          }

          @Override
          public String convertToString(final Object value, final Locale locale)
          {
            if (value == null) {
              return "";
            }
            final KundeDO customer = (KundeDO) value;
            return customer.getKundename();
          }
        };
      }
    };
    currentKunde = getModelObject();
    customerTextField.enableTooltips().withLabelValue(true).withMatchContains(true).withMinChars(2).withAutoSubmit(false).withWidth(400);
    customerTextField.setLabel(new Model<String>() {
      @Override
      public String getObject()
      {
        if (label != null) {
          return label;
        } else {
          return getString("customer");
        }
      }
    });






    if (customerText != null) {
      customerTextField = new MaxLengthTextField("customerText", customerText) {
        @Override
        public boolean isVisible()
        {
          return (KontoSelectPanel.this.getModelObject() == null || NumberHelper
              .greaterZero(KontoSelectPanel.this.getModelObject().getId()) == false);
        }
      };
      add(customerTextField);
    } else {
      add(AbstractForm.createInvisibleDummyComponent("customerText"));
    }
    final Label customerAsStringLabel = new Label("customerAsString", new Model<String>() {

      @Override
      public String getObject()
      {
        final KundeDO customer = getModelObject();
        return customerFormatter.format(customer, false);
      }
    });
    add(customerAsStringLabel);
    final SubmitLink selectButton = new SubmitLink("select") {
      @Override
      public void onSubmit()
      {
        setResponsePage(new CustomerListPage(caller, selectProperty));
      };
    };
    selectButton.setDefaultFormProcessing(false);
    add(selectButton);
    selectButton.add(new TooltipImage("selectHelp", getResponse(), WebConstants.IMAGE_KUNDE_SELECT, getString("fibu.tooltip.selectKunde")));
    final SubmitLink unselectButton = new SubmitLink("unselect") {
      @Override
      public void onSubmit()
      {
        caller.unselect(selectProperty);
      }

      @Override
      public boolean isVisible()
      {
        return KontoSelectPanel.this.getModelObject() != null;
      }
    };
    unselectButton.setDefaultFormProcessing(false);
    add(unselectButton);
    unselectButton.add(new TooltipImage("unselectHelp", getResponse(), WebConstants.IMAGE_KUNDE_UNSELECT,
        getString("fibu.tooltip.unselectKunde")));
    // DropDownChoice favorites
    final FavoritesChoicePanel<KundeDO, KundeFavorite> favoritesPanel = new FavoritesChoicePanel<KundeDO, KundeFavorite>("favorites",
        KundePrefArea.KUNDE_FAVORITE, tabIndex, "half select") {
      @Override
      protected void select(final KundeFavorite favorite)
      {
        if (favorite.getKunde() != null) {
          KontoSelectPanel.this.selectKunde(favorite.getKunde());
        }
      }

      @Override
      protected KundeDO getCurrentObject()
      {
        return KontoSelectPanel.this.getModelObject();
      }

      @Override
      protected KundeFavorite newFavoriteInstance(final KundeDO currentObject)
      {
        final KundeFavorite favorite = new KundeFavorite();
        favorite.setKunde(currentObject);
        return favorite;
      }
    };
    add(favoritesPanel);
    favoritesPanel.init();
    if (showFavorites == false) {
      favoritesPanel.setVisible(false);
    }
    return this;
  }

  /**
   * Will be called if the customer has chosen an entry of the customer favorites drop down choice.
   * @param customer
   */
  protected void selectKunde(final KundeDO customer)
  {
    setModelObject(customer);
    caller.select(selectProperty, customer.getId());
  }

  /**
   * @return The customer's raw input of customer text if given, otherwise null.
   */
  public String getKundeTextInput()
  {
    if (customerTextField != null) {
      return customerTextField.getRawInput();
    }
    return null;
  }

  @Override
  protected void convertInput()
  {
    setConvertedInput(getModelObject());
  }
}
