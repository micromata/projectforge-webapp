/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.stripes;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.projectforge.access.AccessException;
import org.projectforge.common.ExceptionHelper;
import org.projectforge.core.UserException;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.core.BaseActionBean;
import org.projectforge.web.wicket.ErrorPage;

import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.exception.ExceptionHandler;

@Deprecated
public class MyExceptionHandler implements ExceptionHandler
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MyExceptionHandler.class);

  /** Doesn't have to do anything... */
  public void init(Configuration configuration) throws Exception
  {
  }

  public void handle(Throwable throwable, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {

    // TransactionUtil.rollback(); // rollback any tx in progress
    // if (AppProperties.isDevMode()) {
    // throw new StripesServletException(throwable);
    BaseActionBean bean = (BaseActionBean) request.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN);

    if (bean != null) {
      // Does not work: public void handle(UserException ex, HttpServletRequest request, HttpServletResponse response) throws
      // ServletException, IOException),
      // so the following ugly if-else-cascade is used:
      if (throwable instanceof UserException) {
        UserException ex = (UserException) throwable;
        bean.addGlobalError(ex.getI18nKey(), ex.getParams(PFUserContext.getResourceBundle()));
        log.info(ex.toString() + ExceptionHelper.getFilteredStackTrace(ex, ErrorPage.ONLY4NAMESPACE));
      } else if (throwable instanceof AccessException) {
        AccessException ex = (AccessException) throwable;
        if (ex.getParams() != null) {
          bean.addGlobalError(ex.getI18nKey(), ex.getParams());
        } else {
          bean.addGlobalError(ex.getI18nKey(), ex.getMessageArgs(PFUserContext.getResourceBundle()));
        }
        log.info(ex.toString() + ExceptionHelper.getFilteredStackTrace(ex, ErrorPage.ONLY4NAMESPACE));
      } else if (throwable instanceof ServletException) {
        String messageNumber = String.valueOf(System.currentTimeMillis());
        log.error("Message #" + messageNumber + ": " + throwable.getMessage(), throwable);
        Throwable rootCause = ((ServletException) throwable).getRootCause();
        if (rootCause != null) {
          log.error("Message #" + messageNumber + " rootCause: " + rootCause.getMessage(), rootCause);
        }
        bean.addGlobalError(UserException.I18N_KEY_PLEASE_CONTACT_DEVELOPER_TEAM, messageNumber);
      } else {
        String messageNumber = String.valueOf(System.currentTimeMillis());
        log.error("Message #" + messageNumber + ": " + throwable.getMessage(), throwable);
        bean.addGlobalError(UserException.I18N_KEY_PLEASE_CONTACT_DEVELOPER_TEAM, messageNumber);
      }
      try {
        bean.getContext().getSourcePageResolution().execute(request, response);
        return;
      } catch (Exception ex) {
        log.error("Exception encountered " + ex, ex);
      }
    }
    ResourceBundle bundle = BaseActionBean.getResourceBundle(PFUserContext.getLocale());
    String msg = null;
    if (throwable instanceof UserException) {
      Object[] args = ((UserException) throwable).getParams(bundle);
      msg = getMessage(request, ((UserException) throwable).getI18nKey(), args);
    }
    log.info(throwable + ExceptionHelper.getFilteredStackTrace(throwable, ErrorPage.ONLY4NAMESPACE));
    request.setAttribute("exception", throwable);
    request.setAttribute("message", msg);
    request.getRequestDispatcher("/error.jsp").forward(request, response);
  }

  private String getMessage(HttpServletRequest request, String i18nKey, Object[] args)
  {
    ResourceBundle bundle = BaseActionBean.getResourceBundle(PFUserContext.getLocale());
    if (args != null) {
      return MessageFormat.format(bundle.getString(i18nKey), args);
    }
    return bundle.getString(i18nKey);
  }
}
