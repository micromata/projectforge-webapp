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

package org.projectforge.web.doc;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.hibernate.criterion.Restrictions;
import org.projectforge.core.QueryFilter;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.web.user.UserEditPage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.MessagePage;

/**
 * Standard error page should be shown in production mode.
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class TutorialPage extends AbstractSecuredPage
{
  private static final String KEY_TYPE = "type";

  private static final String KEY_REF = "ref";

  private static final String TYPE_CREATE_USER = "createUser";

  private String type;

  private String reference;
  
  @SpringBean(name="userDao")
  private UserDao userDao;

  public TutorialPage(final PageParameters params)
  {
    super(params);
    type = params.getString(KEY_TYPE);
    reference = params.getString(KEY_REF);
    if (TYPE_CREATE_USER.equals(type) == true) {
      createUser();
    } else {
      setResponsePage(new MessagePage("tutorial.unknown"));
    }
  }

  private void createUser()
  {
    final String tutorialReference = "{tutorial-ref:" + reference + "}";
    final QueryFilter filter = new QueryFilter();
    filter.add(Restrictions.ilike("description", "%" + tutorialReference + "%"));
    if (CollectionUtils.isNotEmpty(userDao.internalGetList(filter))==true) {
      setResponsePage(new MessagePage("tutorial.objectAlreadyCreated"));
      return;
    }
    final PFUserDO user;
    if ("Linda".equals(reference) == true) {
      user = createUser("linda", "Evans", "Linda", "l.evans@javagurus.com", addTutorialReference("Project manager", tutorialReference));
    } else {
      setResponsePage(new MessagePage("tutorial.unknown"));
      return;
    }
    final PageParameters params = new PageParameters();
    params.put(AbstractEditPage.PARAMETER_KEY_DATA_PRESET, user);
    final UserEditPage userEditPage = new UserEditPage(params);
    setResponsePage(userEditPage);
  }

  private PFUserDO createUser(final String userName, final String lastName, final String firstName, final String email,
      final String description)
  {
    final PFUserDO userDO = new PFUserDO();
    userDO.setUsername(userName);
    userDO.setEmail(email);
    userDO.setLastname(lastName);
    userDO.setFirstname(firstName);
    userDO.setDescription(description);
    userDO.setPassword("test123");
    return userDO;
  }

  private String addTutorialReference(final String text, final String tutorialReference)
  {
    if (StringUtils.isEmpty(text) == true) {
      return tutorialReference;
    } else {
      return text + "\n" + tutorialReference;
    }
  }

  @Override
  protected String getTitle()
  {
    return "TutorialRedirectPage";
  }
}
