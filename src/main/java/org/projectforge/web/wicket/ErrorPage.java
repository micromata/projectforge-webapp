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

package org.projectforge.web.wicket;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.access.AccessException;
import org.projectforge.common.ExceptionHelper;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationParam;
import org.projectforge.core.InternalErrorException;
import org.projectforge.core.ProjectForgeException;
import org.projectforge.core.SendFeedback;
import org.projectforge.core.UserException;
import org.projectforge.user.PFUserContext;

/**
 * Standard error page should be shown in production mode.
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class ErrorPage extends AbstractSecuredPage
{
  private static final long serialVersionUID = -637809894879133209L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ErrorPage.class);

  public static final String ONLY4NAMESPACE = "org.projectforge";

  private String title;

  String errorMessage, messageNumber;

  @SpringBean(name = "sendFeedback")
  private SendFeedback sendFeedback;

  private final ErrorForm form;

  private boolean showFeedback;

  /**
   * Get internationalized message inclusive the message params if exists.
   * @param securedPage Needed for localization.
   * @param exception
   * @param doLog If true, then a log entry with level INFO will be produced.
   * @return
   */
  public static String getExceptionMessage(final AbstractSecuredBasePage securedPage, final ProjectForgeException exception,
      final boolean doLog)
  {
    // Feedbackpanel!
    if (exception instanceof UserException) {
      final UserException ex = (UserException) exception;
      if (doLog == true) {
        log.info(ex.toString() + ExceptionHelper.getFilteredStackTrace(ex, ONLY4NAMESPACE));
      }
      return securedPage.translateParams(ex.getI18nKey(), ex.getMsgParams(), ex.getParams());
    } else if (exception instanceof InternalErrorException) {
      final InternalErrorException ex = (InternalErrorException) exception;
      if (doLog == true) {
        log.info(ex.toString() + ExceptionHelper.getFilteredStackTrace(ex, ONLY4NAMESPACE));
      }
      return securedPage.translateParams(ex.getI18nKey(), ex.getMsgParams(), ex.getParams());
    } else if (exception instanceof AccessException) {
      final AccessException ex = (AccessException) exception;
      if (doLog == true) {
        log.info(ex.toString() + ExceptionHelper.getFilteredStackTrace(ex, ONLY4NAMESPACE));
      }
      if (ex.getParams() != null) {
        return securedPage.getLocalizedMessage(ex.getI18nKey(), ex.getParams());
      } else {
        return securedPage.translateParams(ex.getI18nKey(), ex.getMessageArgs(), ex.getParams());
      }
    }
    throw new UnsupportedOperationException("For developer: Please add unknown ProjectForgeException here!", exception);
  }

  public ErrorPage()
  {
    this(null);
  }

  public ErrorPage(final Throwable throwable)
  {
    super(null);
    errorMessage = getString("errorpage.unknownError");
    messageNumber = null;
    Throwable rootCause = null;
    showFeedback = true;
    if (throwable != null) {
      rootCause = ExceptionHelper.getRootCause(throwable);
      if (rootCause instanceof ProjectForgeException) {
        errorMessage = getExceptionMessage(this, (ProjectForgeException) rootCause, true);
      } else if (throwable instanceof ServletException) {
        messageNumber = String.valueOf(System.currentTimeMillis());
        log.error("Message #" + messageNumber + ": " + throwable.getMessage(), throwable);
        if (rootCause != null) {
          log.error("Message #" + messageNumber + " rootCause: " + rootCause.getMessage(), rootCause);
        }
        errorMessage = getLocalizedMessage(UserException.I18N_KEY_PLEASE_CONTACT_DEVELOPER_TEAM, messageNumber);
      } else if (throwable instanceof PageExpiredException) {
        log.info("Page expired (session time out).");
        showFeedback = false;
        errorMessage = getString("message.wicket.pageExpired");
        title = getString("message.title");
      } else {
        messageNumber = String.valueOf(System.currentTimeMillis());
        log.error("Message #" + messageNumber + ": " + throwable.getMessage(), throwable);
        errorMessage = getLocalizedMessage(UserException.I18N_KEY_PLEASE_CONTACT_DEVELOPER_TEAM, messageNumber);
      }
    }
    form = new ErrorForm(this);
    final String receiver = Configuration.getInstance().getStringValue(ConfigurationParam.FEEDBACK_E_MAIL);
    form.data.setReceiver(receiver);
    form.data.setMessageNumber(messageNumber);
    form.data.setMessage(throwable != null ? throwable.getMessage() : "");
    form.data.setStackTrace(throwable != null ? ExceptionHelper.printStackTrace(throwable) : "");
    form.data.setSender(PFUserContext.getUser().getFullname());
    final String subject = "ProjectForge-Error #" + form.data.getMessageNumber() + " from " + form.data.getSender();
    form.data.setSubject(subject);
    if (rootCause != null) {
      form.data.setRootCause(rootCause.getMessage());
      form.data.setRootCauseStackTrace(ExceptionHelper.printStackTrace(rootCause));
    }
    final boolean visible = showFeedback == true && messageNumber != null && StringUtils.isNotBlank(receiver);
    body.add(form);
    if (visible == true) {
      form.init();
    }
    form.setVisible(visible);
    final Label errorMessageLabel = new Label("errorMessage", errorMessage);
    body.add(errorMessageLabel.setVisible(errorMessage != null));
    final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
    feedbackPanel.setOutputMarkupId(true);
    body.add(feedbackPanel);
  }

  void cancel()
  {
    setResponsePage(WicketUtils.getDefaultPage());
  }

  void sendFeedback()
  {
    log.info("Send feedback.");
    boolean result = false;
    try {
      result = sendFeedback.send(form.data);
    } catch (final Throwable ex) {
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
    return title != null ? title : getString("errorpage.title");
  }

  /**
   * @see org.apache.wicket.Component#isVersioned()
   */
  @Override
  public boolean isVersioned()
  {
    return false;
  }

  /**
   * @see org.apache.wicket.Page#isErrorPage()
   */
  @Override
  public boolean isErrorPage()
  {
    return true;
  }
}
