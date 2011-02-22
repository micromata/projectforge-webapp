/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.user;

import java.util.Locale;
import java.util.TimeZone;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.StringHelper;
import org.projectforge.core.Configuration;
import org.projectforge.core.NumberFormatter;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
import org.projectforge.web.wicket.components.TimeZonePanel;
import org.projectforge.web.wicket.components.TooltipImage;

public abstract class UserBaseEditForm<P extends AbstractEditPage< ? , ? , ? >> extends AbstractEditForm<PFUserDO, P>
{
  private static final long serialVersionUID = 1749900024977453916L;

  public static final String[] LOCALIZATIONS = { "en", "de"};

  @SpringBean(name = "userDao")
  protected UserDao userDao;

  public UserBaseEditForm(P parentPage, PFUserDO data)
  {
    super(parentPage, data);
    this.colspan = 4;
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    add(new MaxLengthTextField("jiraUsername", new PropertyModel<String>(getData(), "jiraUsername")));
    add(new TooltipImage("jiraUsernameHelpImage", getResponse(), WebConstants.IMAGE_HELP, getString("user.jiraUsername.tooltip")));
    add(new MaxLengthTextField("email", new PropertyModel<String>(getData(), "email")));
    {
      final LabelValueChoiceRenderer<String> localeChoiceRenderer = new LabelValueChoiceRenderer<String>();
      localeChoiceRenderer.addValue("", getString("user.defaultLocale"));
      for (final String str : LOCALIZATIONS) {
        localeChoiceRenderer.addValue(str, getString("locale." + str));
      }
      @SuppressWarnings("unchecked")
      final DropDownChoice localeChoice = new DropDownChoice("localeChoice", new PropertyModel(getData(), "locale"), localeChoiceRenderer
          .getValues(), localeChoiceRenderer) {

        @Override
        protected CharSequence getDefaultChoice(Object selected)
        {
          return "";
        }

        @Override
        protected Object convertChoiceIdToChoice(final String id)
        {
          if (StringHelper.isIn(id, LOCALIZATIONS) == true) {
            return new Locale(id);
          } else {
            return null;
          }
        }
      };
      add(localeChoice);
    }
    add(new TimeZonePanel("timezoneChoice", new PropertyModel<TimeZone>(getData(), "timeZoneObject")));
    add(new MaxLengthTextField(this, "organization", getString("organization"), new PropertyModel<String>(getData(), "organization")));

    add(new MaxLengthTextField("personalPhoneIdentifiers", new PropertyModel<String>(getData(), "personalPhoneIdentifiers")));
    final WebMarkupContainer mebMobileNumbersRow = new WebMarkupContainer("mebMobileNumbersRow");
    add(mebMobileNumbersRow);
    if (Configuration.getInstance().isMebConfigured() == false) {
      mebMobileNumbersRow.setVisible(false);
    } else {
      mebMobileNumbersRow.add(new MaxLengthTextField("personalMebMobileNumbers", new PropertyModel<String>(getData(),
          "personalMebMobileNumbers")));
      mebMobileNumbersRow.add(new TooltipImage("personalMebMobileNumbersHelpImage", getResponse(), WebConstants.IMAGE_HELP,
          getString("user.personalMebMobileNumbers.tooltip")));
    }
    add(new TooltipImage("personalPhoneIdentifiersHelpImage", getResponse(), WebConstants.IMAGE_HELP,
        getString("user.personalPhoneIdentifiers.tooltip")));
    add(new RequiredMaxLengthTextField("firstname", new PropertyModel<String>(getData(), "firstname")));
    add(new RequiredMaxLengthTextField("lastname", new PropertyModel<String>(getData(), "lastname")));
    add(new MaxLengthTextArea("description", new PropertyModel<String>(getData(), "description")));
    add(new Label("lastLogin", DateTimeFormatter.instance().getFormattedDateTime(getData().getLastLogin())));
    add(new Label("loginFailures", NumberFormatter.format(getData().getLoginFailures())));
  }
}
