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

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.fibu.AuftragDO;
import org.projectforge.fibu.PaymentScheduleDO;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.converter.CurrencyConverter;
import org.projectforge.web.wicket.flowlayout.AjaxIconLinkPanel;
import org.projectforge.web.wicket.flowlayout.IconType;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class PaymentSchedulePanel extends Panel
{

  private static final long serialVersionUID = 2669766778018430028L;

  private RepeatingView entrysRepeater;

  private WebMarkupContainer mainContainer, addNewEntryContainer;

  private PaymentScheduleDO newEntryValue;

  //  private final String DEFAULT_ENTRY_VALUE = "Neue Adresse";
  //  private final String DEFAULT_STREET_VALUE = "Strasse";
  //  private final String DEFAULT_ZIPCODE_VALUE= "Plz";
  //  private final String DEFAULT_CITY_VALUE = "Stadt";
  //  private final String DEFAULT_COUNTRY_VALUE = "Land";
  //  private final String DEFAULT_STATE_VALUE = "Bundesland";
  //
  //  private Component city;
  //  private Component zipCode;
  //  private Component country;
  //  private Component state;
  private Component delete;

  private final IModel<AuftragDO>  model;

  /**
   * @param id
   */
  public PaymentSchedulePanel(final String id, final IModel<AuftragDO>  model)
  {
    super(id);
    this.model = model;
  }

  /**
   * @see org.apache.wicket.Component#onInitialize()
   */
  @Override
  protected void onInitialize()
  {
    super.onInitialize();
    newEntryValue = new PaymentScheduleDO().setAmount(BigDecimal.ZERO).setAuftrag(model.getObject());
    mainContainer = new WebMarkupContainer("main");
    add(mainContainer.setOutputMarkupId(true));
    entrysRepeater = new RepeatingView("liRepeater");
    mainContainer.add(entrysRepeater);

    rebuildEntries();
    addNewEntryContainer = new WebMarkupContainer("liAddNewEntry");
    mainContainer.add(addNewEntryContainer);

    init(addNewEntryContainer);
    entrysRepeater.setVisible(true);
  }

  /********************************** init ** ********************************* */
  @SuppressWarnings("serial")
  void init(final WebMarkupContainer item)
  {

    // scheduleDate
    final DatePanel datePanel = new DatePanel("scheduleDate", new PropertyModel<Date>(newEntryValue, "scheduleDate"),
        DatePanelSettings.get().withTargetType(java.sql.Date.class));
    item.add(datePanel);

    // amount
    final TextField<String> amount = new TextField<String>("amount", new PropertyModel<String>(newEntryValue, "amount")) {
      @SuppressWarnings({ "rawtypes", "unchecked"})
      @Override
      public IConverter getConverter(final Class type)
      {
        return new CurrencyConverter();
      }
    };
    item.add(amount);

    // comment
    item.add(new MaxLengthTextArea("comment", new PropertyModel<String>(newEntryValue, "comment")));

    // reached
    item.add(new CheckBox("reached", new PropertyModel<Boolean>(newEntryValue, "reached")));

    final WebMarkupContainer deleteDiv = new WebMarkupContainer("deleteDiv");
    deleteDiv.setOutputMarkupId(true);
    deleteDiv.add( delete = new AjaxIconLinkPanel("delete", IconType.REMOVE, new PropertyModel<String>(newEntryValue, "comment")) {
      /**
       * @see org.projectforge.web.wicket.flowlayout.AjaxIconLinkPanel#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
       */
      @Override
      protected void onClick(final AjaxRequestTarget target)
      {
        super.onClick(target);
        final Iterator<PaymentScheduleDO> it = model.getObject().getPaymentSchedules().iterator();
        while (it.hasNext() == true) {
          if (it.next() == newEntryValue) {
            it.remove();
          }
        }
        rebuildEntries();
        target.add(mainContainer);
      }
    });
    item.add(deleteDiv);
    delete.setVisible(false);
  }

  /********************************** rebuild ** ********************************* */
  @SuppressWarnings("serial")
  public void rebuildEntries()
  {

    final Set<PaymentScheduleDO> entries = model.getObject().getPaymentSchedules();
    if ( entries != null) {
      entrysRepeater.removeAll();

      for (final PaymentScheduleDO entry : entries) {

        final WebMarkupContainer item = new WebMarkupContainer(entrysRepeater.newChildId());
        entrysRepeater.add(item);

        // scheduleDate
        final DatePanel datePanel = new DatePanel("scheduleDate", new PropertyModel<Date>(entry, "scheduleDate"),
            DatePanelSettings.get().withTargetType(java.sql.Date.class));
        item.add(datePanel);

        // amount
        final TextField<String> amount = new TextField<String>("amount", new PropertyModel<String>(newEntryValue, "amount")) {
          @SuppressWarnings({ "rawtypes", "unchecked"})
          @Override
          public IConverter getConverter(final Class type)
          {
            return new CurrencyConverter();
          }
        };
        item.add(amount);

        // comment
        item.add(new MaxLengthTextArea("comment", new PropertyModel<String>(entry, "comment")));

        // reached
        item.add(new CheckBox("reached", new PropertyModel<Boolean>(entry, "reached")));


        final WebMarkupContainer deleteDiv = new WebMarkupContainer("deleteDiv");
        deleteDiv.setOutputMarkupId(true);
        deleteDiv.add( delete = new AjaxIconLinkPanel("delete", IconType.REMOVE, new PropertyModel<String>(entry, "comment")) {
          /**
           * @see org.projectforge.web.wicket.flowlayout.AjaxIconLinkPanel#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
           */
          @Override
          protected void onClick(final AjaxRequestTarget target)
          {
            super.onClick(target);
            final Iterator<PaymentScheduleDO> it = model.getObject().getPaymentSchedules().iterator();
            while (it.hasNext() == true) {
              if (it.next() == entry) {
                it.remove();
              }
            }
            rebuildEntries();
            target.add(mainContainer);
          }
        });
        item.add(deleteDiv);
        delete.setVisible(true);
      }
    }
  }
}
