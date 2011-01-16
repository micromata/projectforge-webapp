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

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.common.RecentQueue;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserXmlPreferencesCache;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractSelectPanel;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteTextField;
import org.projectforge.web.wicket.components.TooltipImage;

/**
 * This panel shows the actual user and buttons for select/unselect user.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class UserSelectPanel extends AbstractSelectPanel<PFUserDO>
{
  private static final long serialVersionUID = -7114401036341110814L;

  private static final String USER_PREF_KEY_RECENT_USERS = "UserSelectPanel:recentUsers";

  private boolean defaultFormProcessing = false;

  @SpringBean(name = "userDao")
  private UserDao userDao;

  @SpringBean(name = "userXmlPreferencesCache")
  protected UserXmlPreferencesCache userXmlPreferencesCache;

  private RecentQueue<String> recentUsers;

  private PFAutoCompleteTextField<PFUserDO> userTextField;

  // Only used for detecting changes:
  private PFUserDO currentUser;

  private String label;

  private WebMarkupContainer spanContainer;

  /**
   * Label is assumed as "user" translation.
   * @param id
   * @param model
   * @param caller
   * @param selectProperty
   */
  public UserSelectPanel(final String id, final IModel<PFUserDO> model, final ISelectCallerPage caller, final String selectProperty)
  {
    this(id, model, null, caller, selectProperty);
  }

  /**
   * @param id
   * @param model
   * @param label Only needed for validation messages (feed back).
   * @param caller
   * @param selectProperty
   */

  /**
   * @param id
   * @param model
   * @param caller
   * @param selectProperty
   */
  public UserSelectPanel(final String id, final IModel<PFUserDO> model, final String label, final ISelectCallerPage caller,
      final String selectProperty)
  {
    super(id, model, caller, selectProperty);
    this.label = label;
  }

  /**
   * Should be called before init() method. If true, then the validation will be done after submitting.
   * @param defaultFormProcessing
   */
  public void setDefaultFormProcessing(boolean defaultFormProcessing)
  {
    this.defaultFormProcessing = defaultFormProcessing;
  }

  @SuppressWarnings("serial")
  public UserSelectPanel init()
  {
    super.init();
    userTextField = new PFAutoCompleteTextField<PFUserDO>("userField", getModel()) {
      @Override
      protected List<PFUserDO> getChoices(final String input)
      {
        final BaseSearchFilter filter = new BaseSearchFilter();
        filter.setSearchFields("username", "firstname", "lastname", "email");
        filter.setSearchString(input);
        final List<PFUserDO> list = userDao.getList(filter);
        return list;
      }

      @Override
      protected List<String> getRecentUserInputs()
      {
        return getRecentUsers().getRecents();
      }

      @Override
      protected String formatLabel(final PFUserDO user)
      {
        if (user == null) {
          return "";
        }
        return formatUser(user);
      }

      @Override
      protected String formatValue(final PFUserDO user)
      {
        if (user == null) {
          return "";
        }
        return user.getUsername() + ": " + user.getFullname();
      }

      @Override
      protected String getTooltip()
      {
        final PFUserDO user = getModel().getObject();
        if (user == null) {
          return null;
        }
        return user.getFullname() + ", " + user.getEmail();
      }

      @Override
      protected void convertInput()
      {
        final PFUserDO user = (PFUserDO) getConverter(getType()).convertToObject(getInput(), getLocale());
        setConvertedInput(user);
        if (user != null && (currentUser == null || user.getId() != currentUser.getId())) {
          getRecentUsers().append(formatUser(user));
        }
        currentUser = user;
      }

      @Override
      public IConverter getConverter(final Class< ? > type)
      {
        return new IConverter() {
          @Override
          public Object convertToObject(final String value, final Locale locale)
          {
            if (StringUtils.isEmpty(value) == true) {
              getModel().setObject(null);
              return null;
            }
            final int ind = value.indexOf(": ");
            final String username = ind >= 0 ? value.substring(0, ind) : value;
            final PFUserDO user = userDao.getUserGroupCache().getUser(username);
            if (user == null) {
              error(getString("user.panel.error.usernameNotFound"));
            }
            getModel().setObject(user);
            return user;
          }

          @Override
          public String convertToString(final Object value, final Locale locale)
          {
            if (value == null) {
              return "";
            }
            final PFUserDO user = (PFUserDO) value;
            return user.getUsername();
          }
        };
      }
    };
    currentUser = getModelObject();
    userTextField.enableTooltips().withLabelValue(true).withMatchContains(true).withMinChars(2).withAutoSubmit(false).withWidth(400);
    userTextField.setLabel(new Model<String>() {
      @Override
      public String getObject()
      {
        if (label != null) {
          return label;
        } else {
          return getString("user");
        }
      }
    });
    spanContainer = new WebMarkupContainer("span");
    add(spanContainer);
    spanContainer.add(userTextField);
    final SubmitLink selectMeButton = new SubmitLink("selectMe") {
      @Override
      public void onSubmit()
      {
        caller.select(selectProperty, PFUserContext.getUserId());
        markTextFieldModelAsChanged();
      }

      @Override
      public boolean isVisible()
      {
        // Is visible if no user is given or the given user is not the current logged in user.
        PFUserDO user = getModelObject();
        return user == null || user.getId().equals(PFUserContext.getUser().getId()) == false;
      }
    };
    spanContainer.add(selectMeButton);
    selectMeButton.setDefaultFormProcessing(defaultFormProcessing);
    selectMeButton.add(new TooltipImage("selectMeHelp", getResponse(), WebConstants.IMAGE_USER_SELECT_ME, getString("tooltip.selectMe")));
    return this;
  }

  private void markTextFieldModelAsChanged()
  {
    userTextField.modelChanged();
    final PFUserDO user = getModelObject();
    if (user != null) {
      getRecentUsers().append(formatUser(user));
    }
  }

  public UserSelectPanel withAutoSubmit(final boolean autoSubmit)
  {
    userTextField.withAutoSubmit(autoSubmit);
    return this;
  }

  @Override
  public Component getClassModifierComponent()
  {
    return spanContainer;
  }
  
  @Override
  protected void convertInput()
  {
    setConvertedInput(getModelObject());
  }

  @SuppressWarnings("unchecked")
  private RecentQueue<String> getRecentUsers()
  {
    if (this.recentUsers == null) {
      this.recentUsers = (RecentQueue<String>) userXmlPreferencesCache.getEntry(USER_PREF_KEY_RECENT_USERS);
    }
    if (this.recentUsers == null) {
      this.recentUsers = new RecentQueue<String>();
      userXmlPreferencesCache.putEntry(USER_PREF_KEY_RECENT_USERS, this.recentUsers, true);
    }
    return this.recentUsers;
  }

  private String formatUser(final PFUserDO user)
  {
    if (user == null) {
      return "";
    }
    return user.getUsername() + " (" + user.getFullname() + ", " + user.getEmail() + ")";
  }
}
