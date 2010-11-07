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

package org.projectforge.web.user;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.hibernate.Hibernate;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserFavorite;
import org.projectforge.user.UserPrefArea;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractSelectPanel;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.components.FavoritesChoicePanel;
import org.projectforge.web.wicket.components.TooltipImage;


/**
 * This panel shows the actual user and buttons for select/unselect user.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class UserSelectPanel extends AbstractSelectPanel<PFUserDO>
{
  private static final long serialVersionUID = -7114401036341110814L;

  private boolean defaultFormProcessing = false;

  /**
   * @param id
   * @param model
   * @param caller
   * @param selectProperty
   */
  public UserSelectPanel(final String id, final IModel<PFUserDO> model, final ISelectCallerPage caller, final String selectProperty)
  {
    super(id, model, caller, selectProperty);
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
    final Label userAsStringLabel = new Label("userAsString", new Model<String>() {
      @Override
      public String getObject()
      {
        final PFUserDO user = getModelObject();
        Hibernate.initialize(user);
        if (user != null) {
          return user.getFullname();
        }
        return "";
      }
    });
    add(userAsStringLabel);
    final SubmitLink selectButton = new SubmitLink("select") {
      public void onSubmit()
      {
        setResponsePage(new UserListPage(caller, selectProperty));
      };
    };
    selectButton.setDefaultFormProcessing(defaultFormProcessing);
    add(selectButton);
    selectButton.add(new TooltipImage("selectHelp", getResponse(), WebConstants.IMAGE_USER_SELECT, getString("tooltip.selectUser")));
    final SubmitLink unselectButton = new SubmitLink("unselect") {
      @Override
      public void onSubmit()
      {
        caller.unselect(selectProperty);
      }

      @Override
      public boolean isVisible()
      {
        return isRequired() == false && getModelObject() != null;
      }
    };
    unselectButton.setDefaultFormProcessing(defaultFormProcessing);
    add(unselectButton);
    unselectButton
        .add(new TooltipImage("unselectHelp", getResponse(), WebConstants.IMAGE_USER_UNSELECT, getString("tooltip.unselectUser")));
    final SubmitLink selectMeButton = new SubmitLink("selectMe") {
      @Override
      public void onSubmit()
      {
        caller.select(selectProperty, PFUserContext.getUserId());
      }

      @Override
      public boolean isVisible()
      {
        // Is visible if no user is given or the given user is not the current logged in user.
        PFUserDO user = getModelObject();
        return user == null || user.getId().equals(PFUserContext.getUser().getId()) == false;
      }
    };
    add(selectMeButton);
    selectMeButton.setDefaultFormProcessing(defaultFormProcessing);
    selectMeButton.add(new TooltipImage("selectMeHelp", getResponse(), WebConstants.IMAGE_USER_SELECT_ME, getString("tooltip.selectMe")));
    // DropDownChoice favorites
    final FavoritesChoicePanel<PFUserDO, UserFavorite> favoritesPanel = new FavoritesChoicePanel<PFUserDO, UserFavorite>("favorites", UserPrefArea.USER_FAVORITE, tabIndex) {
      @Override
      protected void select(final UserFavorite favorite)
      {
        if (favorite.getUser() != null) {
          UserSelectPanel.this.selectUser(favorite.getUser());
        }
      }

      @Override
      protected PFUserDO getCurrentObject()
      {
        return UserSelectPanel.this.getModelObject();
      }

      @Override
      protected UserFavorite newFavoriteInstance(final PFUserDO currentObject)
      {
        final UserFavorite favorite = new UserFavorite();
        favorite.setUser(currentObject);
        return favorite;
      }
    };
    add(favoritesPanel);
    favoritesPanel.init();
    if (showFavorites == false) {
      favoritesPanel.setVisible(false);
    }
    return this;
  }

  /**
   * Will be called if the user has chosen an entry of the user favorites drop down choice.
   * @param user
   */
  protected void selectUser(final PFUserDO user)
  {
    setModelObject(user);
    caller.select(selectProperty, user.getId());
  }

  @Override
  protected void convertInput()
  {
    setConvertedInput(getModelObject());
  }
}
