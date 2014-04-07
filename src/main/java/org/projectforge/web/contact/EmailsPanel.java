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

package org.projectforge.web.contact;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.projectforge.contact.EmailValue;
import org.projectforge.plugins.teamcal.event.TeamEventAttendeeDO;
import org.projectforge.web.wicket.components.AjaxMaxLengthEditableLabel;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class EmailsPanel extends Panel
{

  private static final long serialVersionUID = -7950224503861575606L;

  private final List<EmailValue> emails;

  private final RepeatingView emailsRepeater;

  private final WebMarkupContainer mainContainer;

  /**
   * @param id
   */
  public EmailsPanel(final String id, final List<EmailValue> emails)
  {
    super(id);
    this.emails = emails;
    mainContainer = new WebMarkupContainer("main");
    add(mainContainer.setOutputMarkupId(true));
    emailsRepeater = new RepeatingView("liRepeater");
    mainContainer.add(emailsRepeater);
    rebuildEmails();
    final WebMarkupContainer item = new WebMarkupContainer("liAddNewEmail");
    mainContainer.add(item);
    item.add(new EmailEditableLabel("editableLabel", Model.of(new EmailValue()), true));
    emailsRepeater.setVisible(true);
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
            return EmailsPanel.this.getString("plugins.teamcal.event.addNewAttendee");
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
            email.setContactType(null);
            return;
          } else {
            email.setEmail(object);
            email.setContactType(null);
          }
        }
      }, TeamEventAttendeeDO.URL_MAX_LENGTH);
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
      if (lastEntry == true) {
        if (StringUtils.isBlank(email.getEmail()) == true) {
          // Do nothing.
          super.onSubmit(target);
          return;
        }
        final EmailValue clone = new EmailValue();
        clone.setContactType(email.getContactType()).setEmail(email.getEmail());
        emails.add(clone);
        rebuildEmails();
        target.add(mainContainer);
      } else if (StringUtils.isBlank(email.getEmail()) == true && StringUtils.isBlank(email.getContactType()) == true) {
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

    @Override
    protected FormComponent<String> newEditor(final MarkupContainer parent, final String componentId, final IModel<String> model)
    {

      final FormComponent<String> form = super.newEditor(parent, componentId, model);
      // form.add(new AutoCompleteBehavior<String>(new PFAutoCompleteRenderer()) {
      // private static final long serialVersionUID = 1L;
      //
      // @Override
      // protected Iterator<String> getChoices(final String input)
      // {
      // final List<String> list = new LinkedList<String>();
      // list.add("Kai Reinhard");
      // list.add("Horst xy");
      // list.add("k.reinhard@micromata.de");
      // list.add("h.xy@irgendwas.de");
      // return list.iterator();
      // }
      // });
      return form;
    }
  }

  private void rebuildEmails()
  {
    emailsRepeater.removeAll();
    int count = 0;
    final int size = emails.size();
    for (final EmailValue email : emails) {
      final WebMarkupContainer item = new WebMarkupContainer(emailsRepeater.newChildId());
      emailsRepeater.add(item);
      if ( count == size)
        item.add(new EmailEditableLabel("editableLabel", Model.of(email), true));
      else
        item.add(new EmailEditableLabel("editableLabel", Model.of(email), false));
      count++;
    }
  }
}
