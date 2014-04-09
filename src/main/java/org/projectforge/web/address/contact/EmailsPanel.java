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

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.iterator.ComponentHierarchyIterator;
import org.projectforge.address.contact.ContactType;
import org.projectforge.address.contact.EmailValue;
import org.projectforge.web.wicket.components.AjaxMaxLengthEditableLabel;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class EmailsPanel extends Panel
{

  private static final long serialVersionUID = -7950224503861575606L;

  private final List<EmailValue> emails;

  private final RepeatingView emailsRepeater;

  private final WebMarkupContainer mainContainer;

  private final LabelValueChoiceRenderer<ContactType> formChoiceRenderer;

  private final EmailValue emailValue;
  /**
   * @param id
   */
  public EmailsPanel(final String id, final List<EmailValue> emails)
  {
    super(id);
    this.emails = emails;
    emailValue = new EmailValue().setEmail("E-Mail").setContactType(ContactType.PRIVATE);
    formChoiceRenderer = new LabelValueChoiceRenderer<ContactType>(this, ContactType.values());
    mainContainer = new WebMarkupContainer("main");
    add(mainContainer.setOutputMarkupId(true));
    emailsRepeater = new RepeatingView("liRepeater");
    mainContainer.add(emailsRepeater);

    rebuildEmails();
    final WebMarkupContainer item = new WebMarkupContainer("liAddNewEmail");
    mainContainer.add(item);

    init(item);
    emailsRepeater.setVisible(true);
  }

  void init(final WebMarkupContainer item) {
    // new PropertyModel<ContactType>( emailValue, "contactType")
    item.add(new DropDownChoice<ContactType>("choice", Model.of(new EmailValue().getContactType()), formChoiceRenderer.getValues(),  formChoiceRenderer));
    item.add(new EmailEditableLabel("editableLabel", Model.of(new EmailValue()), true));
  }

  @SuppressWarnings("serial")
  class EmailEditableLabel extends AjaxMaxLengthEditableLabel
  {
    private IModel<EmailValue> emailModel;

    private boolean lastEntry;

    EmailEditableLabel(final String id, final IModel<EmailValue> emailModel, final boolean lastEntry)
    {
      super("editableLabel", new Model<String>() {

        /**
         * @see org.apache.wicket.model.Model#getObject()
         */
        @Override
        public String getObject()
        {
          if (lastEntry == true) {
            return EmailsPanel.this.getString("email");
          }
          final EmailValue email = emailModel.getObject();
          return email.getEmail();
        }

        /**
         * @see org.apache.wicket.model.Model#setObject(java.io.Serializable)
         */
        @Override
        public void setObject(final String object)
        {
          final EmailValue email = emailModel.getObject();
          if (StringUtils.isBlank(object) == true) {
            email.setEmail(null);
            //email.setContactType(null);
            return;
          } else {
            email.setEmail(object);
            //email.setContactType(null);
          }
        }
      }, 255);
      this.emailModel = emailModel;
      this.lastEntry = lastEntry;
      setType(String.class);
    }


    /**
     * @return the emailModel
     */
    EmailValue getEmail()
    {
      return emailModel.getObject();
    }

    /**
     * @see org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel#onEdit(org.apache.wicket.ajax.AjaxRequestTarget)
     */
    @Override
    public void onEdit(final AjaxRequestTarget target)
    {
      super.onEdit(target);
    }

    /**
     * @see org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
     */
    @Override
    protected void onSubmit(final AjaxRequestTarget target)
    {
      final EmailValue email = emailModel.getObject();


      final ComponentHierarchyIterator iter = EmailsPanel.this.visitChildren(DropDownChoice.class);
      while (iter.hasNext() == true) {
        final DropDownChoice drop = (DropDownChoice) iter.next();
        final IModel m = drop.getModel();
        final IModel<String> s = drop.getLabel();
      }
      if (lastEntry == true) {
        if (StringUtils.isBlank(email.getEmail()) == true) {
          // Do nothing.
          super.onSubmit(target);
          return;
        }
        final EmailValue clone = new EmailValue();
        clone.setEmail(email.getEmail()).setContactType(email.getContactType());
        emails.add(clone);
        rebuildEmails();
        target.add(mainContainer);
      } else if (StringUtils.isBlank(email.getEmail()) == true) {
        final Iterator<EmailValue> it = emails.iterator();
        while (it.hasNext() == true) {
          if (it.next() == emailModel.getObject()) {
            it.remove();
          }
        }
        rebuildEmails();
        target.add(mainContainer);
      }
      super.onSubmit(target);
    }
  }

  private void rebuildEmails()
  {
    emailsRepeater.removeAll();
    for (final EmailValue email : emails) {
      final WebMarkupContainer item = new WebMarkupContainer(emailsRepeater.newChildId());
      emailsRepeater.add(item);
      // new PropertyModel<ContactType>( email, "contactType")
      item.add(new DropDownChoice<ContactType>("choice", Model.of(email.getContactType()), formChoiceRenderer.getValues(),  formChoiceRenderer));
      item.add(new EmailEditableLabel("editableLabel", Model.of(email), false));
    }
  }
}
