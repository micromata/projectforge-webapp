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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.address.contact.ContactEntryDO;
import org.projectforge.address.contact.ContactType;
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

  private final String DEFAULT_ENTRY_VALUE = "Adresse";

  /**
   * @param id
   */
  public ContactEntryPanel(final String id, final List<ContactEntryDO> entrys)
  {
    super(id);
    this.entrys = entrys;
    newEntryValue = new ContactEntryDO().setCity(DEFAULT_ENTRY_VALUE).setContactType(ContactType.PRIVATE);
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

    item.add(new AjaxIconLinkPanel("delete", IconType.REMOVE, new PropertyModel<String>(newEntryValue, "city")) {
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

    item.add(new TextField<String>("street", new PropertyModel<String>(newEntryValue, "street")));
    item.add(new TextField<String>("zipCode", new PropertyModel<String>(newEntryValue, "zipCode")));
    item.add(new TextField<String>("city", new PropertyModel<String>(newEntryValue, "city")));
    item.add(new TextField<String>("country", new PropertyModel<String>(newEntryValue, "country")));
    item.add(new TextField<String>("state", new PropertyModel<String>(newEntryValue, "state")));

  }

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

      item.add(new AjaxIconLinkPanel("delete", IconType.REMOVE, new PropertyModel<String>(entry, "city")) {
        /**
         * @see org.projectforge.web.wicket.flowlayout.AjaxIconLinkPanel#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
         */
        @Override
        protected void onClick(final AjaxRequestTarget target)
        {
          super.onClick(target);
          final Iterator<ContactEntryDO> it = entrys.iterator();
          while (it.hasNext() == true) {
            if (it.next() == entry) {
              it.remove();
            }
          }
          rebuildEntrys();
          target.add(mainContainer);
        }
      });

      item.add(new TextField<String>("street", new PropertyModel<String>(entry, "street")));
      item.add(new TextField<String>("zipCode", new PropertyModel<String>(entry, "zipCode")));
      item.add(new TextField<String>("city", new PropertyModel<String>(entry, "city")));
      item.add(new TextField<String>("country", new PropertyModel<String>(entry, "country")));
      item.add(new TextField<String>("state", new PropertyModel<String>(entry, "state")));

    }
  }
}
