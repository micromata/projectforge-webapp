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
import org.projectforge.address.contact.SocialMediaType;
import org.projectforge.address.contact.SocialMediaValue;
import org.projectforge.web.wicket.components.AjaxMaxLengthEditableLabel;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.flowlayout.AjaxIconLinkPanel;
import org.projectforge.web.wicket.flowlayout.IconType;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class SocialMediaPanel extends Panel
{

  private static final long serialVersionUID = -7631249461414483163L;

  @SpringBean(name = "contactDao")
  private ContactDao contactDao;

  private List<SocialMediaValue> ims = null;

  private  RepeatingView imsRepeater;

  private  WebMarkupContainer mainContainer, addNewImContainer;

  private  LabelValueChoiceRenderer<ContactType> contactChoiceRenderer;

  private  LabelValueChoiceRenderer<SocialMediaType> imChoiceRenderer;

  private  SocialMediaValue newImValue;

  private final String DEFAULT_IM_VALUE = "Benutzer";

  private Component delete;

  private final PropertyModel<String> model;

  /**
   * @param id
   */
  public SocialMediaPanel(final String id, final PropertyModel<String> model)
  {
    super(id);
    this.model = model;
    if (StringUtils.isNotBlank(model.getObject()) == true) {
      ims = contactDao.readSocialMediaValues(model.getObject());
    }
  }

  /**
   * @see org.apache.wicket.Component#onInitialize()
   */
  @Override
  protected void onInitialize()
  {
    super.onInitialize();
    if (ims == null) {
      ims = new ArrayList<SocialMediaValue>();
    }
    newImValue = new SocialMediaValue().setUser(DEFAULT_IM_VALUE).setContactType(ContactType.BUSINESS).setImType(SocialMediaType.AIM);
    contactChoiceRenderer = new LabelValueChoiceRenderer<ContactType>(this, ContactType.values());
    imChoiceRenderer = new LabelValueChoiceRenderer<SocialMediaType>(this, SocialMediaType.values());
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
        model.setObject(contactDao.getSocialMediaValuesAsXml(ims));
      }
    });

    final DropDownChoice<SocialMediaType> imChoice = new DropDownChoice<SocialMediaType>("imChoice", new PropertyModel<SocialMediaType>(
        newImValue, "imType"), imChoiceRenderer.getValues(), imChoiceRenderer);
    item.add(imChoice);
    imChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
      @Override
      protected void onUpdate(final AjaxRequestTarget target)
      {
        newImValue.setImType(imChoice.getModelObject());
        model.setObject(contactDao.getSocialMediaValuesAsXml(ims));
      }
    });

    item.add(new AjaxMaxLengthEditableLabel("editableLabel", new PropertyModel<String>(newImValue, "user")) {
      @Override
      protected void onSubmit(final AjaxRequestTarget target)
      {
        super.onSubmit(target);
        if (StringUtils.isNotBlank(newImValue.getUser()) == true && newImValue.getUser().equals(DEFAULT_IM_VALUE) == false) {
          ims.add(new SocialMediaValue().setUser(newImValue.getUser()).setContactType(newImValue.getContactType()).setImType(newImValue.getImType()));
          model.setObject(contactDao.getSocialMediaValuesAsXml(ims));
        }
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
        final Iterator<SocialMediaValue> it = ims.iterator();
        while (it.hasNext() == true) {
          if (it.next() == newImValue) {
            it.remove();
          }
        }
        rebuildIms();
        model.setObject(contactDao.getSocialMediaValuesAsXml(ims));
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
    for (final SocialMediaValue im : ims) {
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
          model.setObject(contactDao.getSocialMediaValuesAsXml(ims));
        }
      });

      final DropDownChoice<SocialMediaType> imChoice = new DropDownChoice<SocialMediaType>("imChoice", new PropertyModel<SocialMediaType>(
          im, "imType"), imChoiceRenderer.getValues(), imChoiceRenderer);
      item.add(imChoice);
      imChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
        @Override
        protected void onUpdate(final AjaxRequestTarget target)
        {
          im.setImType(imChoice.getModelObject());
          model.setObject(contactDao.getSocialMediaValuesAsXml(ims));
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
          final Iterator<SocialMediaValue> it = ims.iterator();
          while (it.hasNext() == true) {
            if (it.next() == im) {
              it.remove();
            }
          }
          rebuildIms();
          model.setObject(contactDao.getSocialMediaValuesAsXml(ims));
          target.add(mainContainer);
        }
      });
      item.add(deleteDiv);
    }
  }
}
