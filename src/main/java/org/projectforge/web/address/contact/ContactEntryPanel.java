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

package org.projectforge.web.address.contact;

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.iterator.ComponentHierarchyIterator;
import org.projectforge.address.contact.ContactEntryDO;
import org.projectforge.address.contact.ContactType;
import org.projectforge.web.wicket.components.AjaxMaxLengthEditableLabel;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.flowlayout.AjaxIconLinkPanel;
import org.projectforge.web.wicket.flowlayout.IconType;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class ContactEntryPanel extends Panel
{
  private static final long serialVersionUID = -7234382706624510638L;

  private final List<ContactEntryDO> entrys;

  private final RepeatingView entrysRepeater;

  private final WebMarkupContainer mainContainer, addNewEntryContainer;

  private final LabelValueChoiceRenderer<ContactType> formChoiceRenderer;

  private final ContactEntryDO newEntryValue;

  private final String DEFAULT_ENTRY_VALUE = "Neue Adresse";
  private final String DEFAULT_STREET_VALUE = "Strasse";
  private final String DEFAULT_ZIPCODE_VALUE= "Plz";
  private final String DEFAULT_CITY_VALUE = "Stadt";
  private final String DEFAULT_COUNTRY_VALUE = "Land";
  private final String DEFAULT_STATE_VALUE = "Bundesland";

  /**
   * @param id
   */
  public ContactEntryPanel(final String id, final List<ContactEntryDO> entrys)
  {
    super(id);
    this.entrys = entrys;
    newEntryValue = new ContactEntryDO().setStreet(DEFAULT_ENTRY_VALUE).setCity(DEFAULT_CITY_VALUE).setZipCode(DEFAULT_ZIPCODE_VALUE).setCountry(DEFAULT_COUNTRY_VALUE).setState(DEFAULT_STATE_VALUE).setContactType(ContactType.PRIVATE);
    formChoiceRenderer = new LabelValueChoiceRenderer<ContactType>(this, ContactType.values());
    mainContainer = new WebMarkupContainer("main");
    add(mainContainer.setOutputMarkupId(true));
    entrysRepeater = new RepeatingView("liRepeater");
    mainContainer.add(entrysRepeater);

    rebuildEntrys();
    addNewEntryContainer = new WebMarkupContainer("liAddNewEntry");
    mainContainer.add(addNewEntryContainer);

    init(addNewEntryContainer);
    entrysRepeater.setVisible(true);
  }

  @SuppressWarnings("serial")
  void init(final WebMarkupContainer item)
  {
    final DropDownChoice<ContactType> dropdownChoice = new DropDownChoice<ContactType>("choice", new PropertyModel<ContactType>(
        newEntryValue, "contactType"), formChoiceRenderer.getValues(), formChoiceRenderer);
    item.add(dropdownChoice);
    dropdownChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
      @Override
      protected void onUpdate(final AjaxRequestTarget target)
      {
        newEntryValue.setContactType(dropdownChoice.getModelObject());
      }
    });

    final WebMarkupContainer streetCodeDiv = new WebMarkupContainer("streetCodeDiv");
    streetCodeDiv.setOutputMarkupId(true);
    streetCodeDiv.add(new AjaxMaxLengthEditableLabel("street", new PropertyModel<String>(newEntryValue, "street")) {
      /**
       * @see org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel#onEdit(org.apache.wicket.ajax.AjaxRequestTarget)
       */
      @Override
      public void onEdit(final AjaxRequestTarget target)
      {
        super.onEdit(target);
        if (newEntryValue.getStreet().equals(DEFAULT_ENTRY_VALUE) == true)
          newEntryValue.setStreet(DEFAULT_STREET_VALUE);
      }
      /**
       * @see org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
       */
      @Override
      protected void onSubmit(final AjaxRequestTarget target)
      {
        super.onSubmit(target);
        final ComponentHierarchyIterator it = this.getPage().visitChildren(AjaxMaxLengthEditableLabel.class);
        while (it.hasNext() == true) {
          final Component c = it.next();
          if (c.getId().equals("zipCode") == true) {
            c.setVisible(true);
            target.add(mainContainer);
          }
        }
      }
    }).setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true).setVisible(true);;
    item.add(streetCodeDiv);


    final WebMarkupContainer zipCodeDiv = new WebMarkupContainer("zipCodeDiv");
    zipCodeDiv.setOutputMarkupId(true);
    zipCodeDiv.add(new AjaxMaxLengthEditableLabel("zipCode", new PropertyModel<String>(newEntryValue, "zipCode")) {
      /**
       * @see org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
       */
      @Override
      protected void onSubmit(final AjaxRequestTarget target)
      {
        super.onSubmit(target);
        final ComponentHierarchyIterator it = this.getPage().visitChildren(AjaxMaxLengthEditableLabel.class);
        while (it.hasNext() == true) {
          final Component c = it.next();
          if (c.getId().equals("city") == true) {
            c.setVisible(true);
            target.add(mainContainer);
          }
        }
      }
    }.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true).setVisible(false));
    item.add(zipCodeDiv);

    final WebMarkupContainer cityDiv = new WebMarkupContainer("cityDiv");
    cityDiv.setOutputMarkupId(true);
    cityDiv.add(new AjaxMaxLengthEditableLabel("city", new PropertyModel<String>(newEntryValue, "city")) {
      /**
       * @see org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
       */
      @Override
      protected void onSubmit(final AjaxRequestTarget target)
      {
        super.onSubmit(target);
        final ComponentHierarchyIterator it = this.getPage().visitChildren(AjaxMaxLengthEditableLabel.class);
        while (it.hasNext() == true) {
          final Component c = it.next();
          if (c.getId().equals("country") == true) {
            c.setVisible(true);
            target.add(mainContainer);
          }
        }
      }
    }.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true).setVisible(false));
    item.add(cityDiv);

    final WebMarkupContainer countryDiv = new WebMarkupContainer("countryDiv");
    countryDiv.setOutputMarkupId(true);
    countryDiv.add(new AjaxMaxLengthEditableLabel("country", new PropertyModel<String>(newEntryValue, "country")) {
      /**
       * @see org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
       */
      @Override
      protected void onSubmit(final AjaxRequestTarget target)
      {
        super.onSubmit(target);
        final ComponentHierarchyIterator it = this.getPage().visitChildren(AjaxMaxLengthEditableLabel.class);
        while (it.hasNext() == true) {
          final Component c = it.next();
          if (c.getId().equals("state") == true) {
            c.setVisible(true);
            target.add(mainContainer);
          }
        }
      }
    }.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true).setVisible(false));
    item.add(countryDiv);

    final WebMarkupContainer stateDiv = new WebMarkupContainer("stateDiv");
    stateDiv.setOutputMarkupId(true);
    stateDiv.add(new AjaxMaxLengthEditableLabel("state", new PropertyModel<String>(newEntryValue, "state")).setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true).setVisible(false));
    item.add(stateDiv);

    item.add(new AjaxIconLinkPanel("delete", IconType.REMOVE, new PropertyModel<String>(newEntryValue, "street")) {
      /**
       * @see org.projectforge.web.wicket.flowlayout.AjaxIconLinkPanel#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
       */
      @Override
      protected void onClick(final AjaxRequestTarget target)
      {
        super.onClick(target);
        final Iterator<ContactEntryDO> it = entrys.iterator();
        while (it.hasNext() == true) {
          if (it.next() == newEntryValue) {
            it.remove();
          }
        }
        rebuildEntrys();
        target.add(mainContainer);
      }
    });

  }


  /** rebuild ** ********************************* */
  @SuppressWarnings("serial")
  private void rebuildEntrys()
  {
    entrysRepeater.removeAll();
    for (final ContactEntryDO entry : entrys) {
      final WebMarkupContainer item = new WebMarkupContainer(entrysRepeater.newChildId());
      entrysRepeater.add(item);
      final DropDownChoice<ContactType> dropdownChoice = new DropDownChoice<ContactType>("choice", new PropertyModel<ContactType>(entry,
          "contactType"), formChoiceRenderer.getValues(), formChoiceRenderer);
      item.add(dropdownChoice);
      dropdownChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
        @Override
        protected void onUpdate(final AjaxRequestTarget target)
        {
          entry.setContactType(dropdownChoice.getModelObject());
        }
      });

      final WebMarkupContainer streetCodeDiv = new WebMarkupContainer("streetCodeDiv");
      streetCodeDiv.setOutputMarkupId(true);
      streetCodeDiv.add(new AjaxMaxLengthEditableLabel("street", new PropertyModel<String>(entry, "street")) {
        /**
         * @see org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel#onEdit(org.apache.wicket.ajax.AjaxRequestTarget)
         */
        @Override
        public void onEdit(final AjaxRequestTarget target)
        {
          super.onEdit(target);
          if (newEntryValue.getStreet().equals(DEFAULT_ENTRY_VALUE) == true)
            newEntryValue.setStreet(DEFAULT_STREET_VALUE);
        }
        /**
         * @see org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
         */
        @Override
        protected void onSubmit(final AjaxRequestTarget target)
        {
          super.onSubmit(target);
          final ComponentHierarchyIterator it = this.getPage().visitChildren(AjaxMaxLengthEditableLabel.class);
          while (it.hasNext() == true) {
            final Component c = it.next();
            if (c.getId().equals("zipCode") == true) {
              c.setVisible(true);
              target.add(mainContainer);
            }
          }
        }
      }).setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true).setVisible(true);
      item.add(streetCodeDiv);


      final WebMarkupContainer zipCodeDiv = new WebMarkupContainer("zipCodeDiv");
      zipCodeDiv.setOutputMarkupId(true);
      zipCodeDiv.add(new AjaxMaxLengthEditableLabel("zipCode", new PropertyModel<String>(entry, "zipCode")) {
        /**
         * @see org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
         */
        @Override
        protected void onSubmit(final AjaxRequestTarget target)
        {
          super.onSubmit(target);
          final ComponentHierarchyIterator it = this.getPage().visitChildren(AjaxMaxLengthEditableLabel.class);
          while (it.hasNext() == true) {
            final Component c = it.next();
            if (c.getId().equals("city") == true) {
              c.setVisible(true);
              target.add(mainContainer);
            }
          }
        }
      }.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true).setVisible(false));
      item.add(zipCodeDiv);

      final WebMarkupContainer cityDiv = new WebMarkupContainer("cityDiv");
      cityDiv.setOutputMarkupId(true);
      cityDiv.add(new AjaxMaxLengthEditableLabel("city", new PropertyModel<String>(entry, "city")) {
        /**
         * @see org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
         */
        @Override
        protected void onSubmit(final AjaxRequestTarget target)
        {
          super.onSubmit(target);
          final ComponentHierarchyIterator it = this.getPage().visitChildren(AjaxMaxLengthEditableLabel.class);
          while (it.hasNext() == true) {
            final Component c = it.next();
            if (c.getId().equals("country") == true) {
              c.setVisible(true);
              target.add(mainContainer);
            }
          }
        }
      }.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true).setVisible(false));
      item.add(cityDiv);

      final WebMarkupContainer countryDiv = new WebMarkupContainer("countryDiv");
      countryDiv.setOutputMarkupId(true);
      countryDiv.add(new AjaxMaxLengthEditableLabel("country", new PropertyModel<String>(entry, "country")) {
        /**
         * @see org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
         */
        @Override
        protected void onSubmit(final AjaxRequestTarget target)
        {
          super.onSubmit(target);
          final ComponentHierarchyIterator it = this.getPage().visitChildren(AjaxMaxLengthEditableLabel.class);
          while (it.hasNext() == true) {
            final Component c = it.next();
            if (c.getId().equals("state") == true) {
              c.setVisible(true);
              target.add(mainContainer);
            }
          }
        }
      }.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true).setVisible(false));
      item.add(countryDiv);

      final WebMarkupContainer stateDiv = new WebMarkupContainer("stateDiv");
      stateDiv.setOutputMarkupId(true);
      stateDiv.add(new AjaxMaxLengthEditableLabel("state", new PropertyModel<String>(entry, "state")).setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true).setVisible(false));
      item.add(stateDiv);

    }
  }
}
