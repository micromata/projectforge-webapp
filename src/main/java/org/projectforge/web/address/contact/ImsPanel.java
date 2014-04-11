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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.address.contact.ContactDao;
import org.projectforge.address.contact.ContactType;
import org.projectforge.address.contact.InstantMessagingType;
import org.projectforge.address.contact.InstantMessagingValue;
import org.projectforge.web.wicket.components.AjaxMaxLengthEditableLabel;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.flowlayout.AjaxIconLinkPanel;
import org.projectforge.web.wicket.flowlayout.IconType;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class ImsPanel extends Panel
{

  private static final long serialVersionUID = -7631249461414483163L;

  @SpringBean(name = "contactDao")
  private ContactDao contactDao;

  private List<InstantMessagingValue> ims = null;

  private final RepeatingView imsRepeater;

  private final WebMarkupContainer mainContainer, addNewImContainer;

  private final LabelValueChoiceRenderer<ContactType> contactChoiceRenderer;

  private final LabelValueChoiceRenderer<InstantMessagingType> imChoiceRenderer;

  private final InstantMessagingValue newImValue;

  private final String DEFAULT_IM_VALUE = "Instant Messaging";

  private Component delete;

  /**
   * @param id
   */
  public ImsPanel(final String id, final String imsXmlString)
  {
    super(id);
    if (StringUtils.isNotBlank(imsXmlString) == true)
      ims = contactDao.readImValues(imsXmlString);
    if (ims == null)
      ims = new ArrayList<InstantMessagingValue>();
    newImValue = new InstantMessagingValue().setUser(DEFAULT_IM_VALUE).setContactType(ContactType.BUSINESS).setImType(InstantMessagingType.AIM);
    contactChoiceRenderer = new LabelValueChoiceRenderer<ContactType>(this, ContactType.values());
    imChoiceRenderer = new LabelValueChoiceRenderer<InstantMessagingType>(this, InstantMessagingType.values());
    mainContainer = new WebMarkupContainer("main");
    add(mainContainer.setOutputMarkupId(true));
    imsRepeater = new RepeatingView("liRepeater");
    mainContainer.add(imsRepeater);

    rebuildIms();
    addNewImContainer = new WebMarkupContainer("liAddNewIm");
    mainContainer.add(addNewImContainer);

    init(addNewImContainer);
    imsRepeater.setVisible(true);
  }

  public String getImsAsXmlString() {
    return contactDao.getImValuesAsXml(ims);
  }

  @SuppressWarnings("serial")
  void init(final WebMarkupContainer item)
  {
    final DropDownChoice<ContactType> contactChoice = new DropDownChoice<ContactType>("choice", new PropertyModel<ContactType>(
        newImValue, "contactType"), contactChoiceRenderer.getValues(), contactChoiceRenderer);
    item.add(contactChoice);
    contactChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
      @Override
      protected void onUpdate(final AjaxRequestTarget target)
      {
        newImValue.setContactType(contactChoice.getModelObject());
      }
    });

    final DropDownChoice<InstantMessagingType> imChoice = new DropDownChoice<InstantMessagingType>("imChoice", new PropertyModel<InstantMessagingType>(
        newImValue, "imType"), imChoiceRenderer.getValues(), imChoiceRenderer);
    item.add(imChoice);
    imChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
      @Override
      protected void onUpdate(final AjaxRequestTarget target)
      {
        newImValue.setImType(imChoice.getModelObject());
      }
    });

    item.add(new AjaxMaxLengthEditableLabel("editableLabel", new PropertyModel<String>(newImValue, "user")) {
      @Override
      protected void onSubmit(final AjaxRequestTarget target)
      {
        super.onSubmit(target);
        ims.add(new InstantMessagingValue().setUser(newImValue.getUser()).setContactType(newImValue.getContactType()).setImType(newImValue.getImType()));
        newImValue.setUser(DEFAULT_IM_VALUE);
        rebuildIms();
        target.add(mainContainer);
      }
    });

    final WebMarkupContainer deleteDiv = new WebMarkupContainer("deleteDiv");
    deleteDiv.setOutputMarkupId(true);
    deleteDiv.add( delete = new AjaxIconLinkPanel("delete", IconType.REMOVE, new PropertyModel<String>(newImValue, "user")) {
      /**
       * @see org.projectforge.web.wicket.flowlayout.AjaxIconLinkPanel#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
       */
      @Override
      protected void onClick(final AjaxRequestTarget target)
      {
        super.onClick(target);
        final Iterator<InstantMessagingValue> it = ims.iterator();
        while (it.hasNext() == true) {
          if (it.next() == newImValue) {
            it.remove();
          }
        }
        rebuildIms();
        target.add(mainContainer);
      }
    });
    item.add(deleteDiv);
    delete.setVisible(false);
  }

  @SuppressWarnings("serial")
  private void rebuildIms()
  {
    imsRepeater.removeAll();
    for (final InstantMessagingValue im : ims) {
      final WebMarkupContainer item = new WebMarkupContainer(imsRepeater.newChildId());
      imsRepeater.add(item);
      final DropDownChoice<ContactType> contactChoice = new DropDownChoice<ContactType>("choice", new PropertyModel<ContactType>(im,
          "contactType"), contactChoiceRenderer.getValues(), contactChoiceRenderer);
      item.add(contactChoice);
      contactChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
        @Override
        protected void onUpdate(final AjaxRequestTarget target)
        {
          im.setContactType(contactChoice.getModelObject());
        }
      });

      final DropDownChoice<InstantMessagingType> imChoice = new DropDownChoice<InstantMessagingType>("imChoice", new PropertyModel<InstantMessagingType>(
          im, "imType"), imChoiceRenderer.getValues(), imChoiceRenderer);
      item.add(imChoice);
      imChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
        @Override
        protected void onUpdate(final AjaxRequestTarget target)
        {
          im.setImType(imChoice.getModelObject());
        }
      });

      item.add(new AjaxMaxLengthEditableLabel("editableLabel", new PropertyModel<String>(im, "user")));

      final WebMarkupContainer deleteDiv = new WebMarkupContainer("deleteDiv");
      deleteDiv.setOutputMarkupId(true);
      deleteDiv.add(new AjaxIconLinkPanel("delete", IconType.REMOVE, new PropertyModel<String>(im, "user")) {
        /**
         * @see org.projectforge.web.wicket.flowlayout.AjaxIconLinkPanel#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
         */
        @Override
        protected void onClick(final AjaxRequestTarget target)
        {
          super.onClick(target);
          final Iterator<InstantMessagingValue> it = ims.iterator();
          while (it.hasNext() == true) {
            if (it.next() == im) {
              it.remove();
            }
          }
          rebuildIms();
          target.add(mainContainer);
        }
      });
      item.add(deleteDiv);
    }
  }
}
