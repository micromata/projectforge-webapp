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

package org.projectforge.web.wicket;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationParam;
import org.projectforge.core.SendFeedback;
import org.projectforge.core.SendFeedbackData;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextArea;
import org.projectforge.web.wicket.components.SingleButtonPanel;


/**
 * Standard error page should be shown in production mode.
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class FeedbackPage extends AbstractSecuredPage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FeedbackPage.class);

  @SpringBean(name = "sendFeedback")
  private SendFeedback sendFeedback;

  private SendFeedbackData data;

  @SuppressWarnings("serial")
  public FeedbackPage(final PageParameters parameters)
  {
    super(null);
    data = new SendFeedbackData();
    Form<ErrorPageData> form = new Form<ErrorPageData>("form");
    final String receiver = Configuration.getInstance().getStringValue(ConfigurationParam.FEEDBACK_E_MAIL);
    body.add(form);
    form.add(new FeedbackPanel("feedback").setOutputMarkupId(true));
    data.setReceiver(receiver);
    data.setSender(PFUserContext.getUser().getFullname());
    data.setSubject("Feedback from " + data.getSender());
    form.add(new Label("receiver", receiver));
    form.add(new Label("sender", data.getSender()));
    final Component textareaField = new RequiredMaxLengthTextArea("description", new PropertyModel<String>(data, "description"), 4000);
    form.add(textareaField);
    textareaField.add(new FocusOnLoadBehavior());
    final Button cancelButton = new Button("button", new Model<String>(getString("cancel"))) {
      @Override
      public final void onSubmit()
      {
        cancel();
      }
    };
    cancelButton.setDefaultFormProcessing(false); // No validation of the form.
    form.add(new SingleButtonPanel("cancel", cancelButton));
    final Button sendButton = new Button("button", new Model<String>(getString("feedback.send.title"))) {
      @Override
      public final void onSubmit()
      {
        sendFeedback();
      }
    };
    form.add(new SingleButtonPanel("send", sendButton));
    form.setDefaultButton(sendButton);
  }

  private void cancel()
  {
    setResponsePage(WicketUtils.getDefaultPage());
  }

  private void sendFeedback()
  {
    log.info("Send feedback.");
    boolean result = false;
    try {
      result = sendFeedback.send(data);
    } catch (Throwable ex) {
      log.error(ex.getMessage(), ex);
      result = false;
    }
    final MessagePage messagePage = new MessagePage(new PageParameters());
    if (result == true) {
      messagePage.setMessage(getString("feedback.mailSendSuccessful"));
    } else {
      messagePage.setMessage(getString("mail.error.exception"));
      messagePage.setWarning(true);
    }
    setResponsePage(messagePage);
  }

  @Override
  protected String getTitle()
  {
    return getString("feedback.send.title");
  }

  /**
   * @see org.apache.wicket.Component#isVersioned()
   */
  @Override
  public boolean isVersioned()
  {
    return false;
  }
}
