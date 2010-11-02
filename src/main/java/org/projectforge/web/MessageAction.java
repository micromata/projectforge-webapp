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

package org.projectforge.web;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.projectforge.web.core.BaseActionBean;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.LocalizableMessage;
import net.sourceforge.stripes.action.Message;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

/**
 * Generic message page.<br/>
 * Redirect to this page with a localized message by using method getForwardResolution. If this method is not useable (e. g. in jsp code)
 * you can also post the message i18 key as request parameter.
 */
@UrlBinding("/secure/Message.action")
public class MessageAction extends BaseActionBean
{
  public static final String MSG_NOT_YET_IMPLEMENTED = "message.notYetImplemented";
  
  /**
   * Do only use, if you call this message out side java code, e. g. in jsp code. Otherwise the
   * message should be set via static method getForwardResolution.
   */
  public static final String MSG_REQ_PARAM_NAME = "msgKey";
  
  private String message;

  /**
   * Creates a forward resolution for this message action which displays the given message. Do post message key as
   * request parameter only if this method is not reachable (e. g. in jsp code).
   * @param msgKey
   * @param parameter
   * @return
   */
  public static ForwardResolution getForwardResolution(BaseActionBean action, String msgKey, Object... parameter)
  {
    action.getContext().getMessages().add(new LocalizableMessage(msgKey, parameter));
    return new ForwardResolution(MessageAction.class);
  }

  @DefaultHandler
  public Resolution init()
  {
    List<Message> messages = getContext().getMessages();
    for (Message m : messages) {
      message = m.getMessage(getContext().getLocale());
    }
    if (StringUtils.isNotBlank(getContext().getRequestParameter(MSG_REQ_PARAM_NAME)) == true) {
      message = getLocalizedString(getContext().getRequestParameter(MSG_REQ_PARAM_NAME));
    }
    return new ForwardResolution("/WEB-INF/jsp/message.jsp");
  }

  public String getMessage()
  {
    return message;
  }
}
