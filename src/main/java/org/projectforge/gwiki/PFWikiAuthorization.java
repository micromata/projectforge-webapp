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

package org.projectforge.gwiki;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.wicket.WicketApplication;

import de.micromata.genome.gwiki.auth.GWikiSimpleUser;
import de.micromata.genome.gwiki.auth.GWikiSimpleUserConfig;
import de.micromata.genome.gwiki.model.AuthorizationFailedException;
import de.micromata.genome.gwiki.model.GWikiAuthorization;
import de.micromata.genome.gwiki.model.GWikiElementInfo;
import de.micromata.genome.gwiki.page.GWikiContext;
import de.micromata.genome.gwiki.umgmt.GWikiUserServeElementFilterEvent;
import de.micromata.genome.util.runtime.CallableX;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class PFWikiAuthorization implements GWikiAuthorization
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PFWikiAuthorization.class);

  public static final String USER_KEY = "org.projectforge.gwiki.User";

  public static GWikiSimpleUserConfig defaultConfig;

  static {
    defaultConfig = new GWikiSimpleUserConfig();
    defaultConfig.getUsers().put("gwikisu", new GWikiSimpleUser("gwikisu", "gwiki", "genome@micromata.de", "+*"));
    defaultConfig.getUsers().put("anon", new GWikiSimpleUser("anon", "anon", "genome@micromata.de", "GWIKI_VIEWPAGES"));
    defaultConfig.getUsers().put("gwikiadmin", new GWikiSimpleUser("gwikiadmin", "gwiki", "genome@micromata.de", "+*,-GWIKI_DEVELOPER"));
    defaultConfig.getUsers()
        .put("gwikideveloper", new GWikiSimpleUser("gwikideveloper", "gwiki", "genome@micromata.de", "+*,-GWIKI_ADMIN"));
  }

  public boolean initThread(final GWikiContext wikiContext)
  {
    GWikiSimpleUser su = getUser(wikiContext);
    if (su == null || StringUtils.equals(su.getUser(), "anon") == true) {
      return false;
    }
    GWikiUserServeElementFilterEvent.setUser(su);
    return true;
  }

  public void clearThread(final GWikiContext wikiContext)
  {
    GWikiUserServeElementFilterEvent.setUser(null);
  }

  public void ensureAllowTo(final GWikiContext ctx, final String right)
  {
    if (isAllowTo(ctx, right) == true) {
      return;
    }
    AuthorizationFailedException.failRight(ctx, right);
  }

  public GWikiSimpleUser getUser(final GWikiContext ctx)
  {
    final PFUserDO user = PFUserContext.getUser();
    if (user == null) {
      return defaultConfig.getUser("anon");
    }
    GWikiSimpleUser su = (GWikiSimpleUser) user.getAttribute(USER_KEY);
    if (su != null) {
      return su;
    }
    su = new GWikiSimpleUser();
    su.setEmail(user.getEmail());
    su.setUser(user.getUsername());
    user.setAttribute(USER_KEY, su);
    return su;
  }

  public String getCurrentUserEmail(final GWikiContext ctx)
  {
    return getUser(ctx).getEmail();
  }

  public Locale getCurrentUserLocale(final GWikiContext ctx)
  {
    return PFUserContext.getLocale();
  }

  public String getCurrentUserName(final GWikiContext ctx)
  {
    return getUser(ctx).getUser();
  }

  public String getEffectiveRight(GWikiContext ctx, GWikiElementInfo ei, String pageRight)
  {
    log.warn("Unsupported: getEffectiveRight: ctx=" + ctx + ", ei=" + ", pageRight=" + pageRight);
    return "";
  }

  public String getUserProp(final GWikiContext ctx, final String key)
  {
    if ("skin".equals(key) == true) {
      final String id = ctx.getCurrentElement().getElementInfo().getId();
      if (id != null && id.startsWith("admin/") == false && id.startsWith("edit/") == false) {
        return "pf";
      }
    }
    String val = ctx.getCookie(key);
    if (val != null) {
      return val;
    }
    final GWikiSimpleUser su = getUser(ctx);
    if (su == null) {
      return null;
    }
    return su.getProps().get(key);
  }

  public void setUserProp(final GWikiContext ctx, final String key, final String value, final boolean persist)
  {
    final GWikiSimpleUser su = getUser(ctx);
    if (su == null) {
      return;
    }
    su.getProps().put(key, value);
    if (persist == true) {
      ctx.setCookie(key, value);
    }
  }

  public boolean isAllowTo(GWikiContext ctx, String right)
  {
    return WicketApplication.isDevelopmentModus();
  }

  public boolean isAllowToCreate(GWikiContext ctx, GWikiElementInfo ei)
  {
    return WicketApplication.isDevelopmentModus();
  }

  public boolean isAllowToEdit(GWikiContext ctx, GWikiElementInfo ei)
  {
    return WicketApplication.isDevelopmentModus();
  }

  public boolean isAllowToView(GWikiContext ctx, GWikiElementInfo ei)
  {
    // TODO: test if user is allowed to 'view' the space
    final String space = extractSpace(ei);

    return WicketApplication.isDevelopmentModus();
  }

  public boolean login(GWikiContext ctx, String user, String password)
  {
    throw new UnsupportedOperationException();
  }

  public void logout(GWikiContext ctx)
  {
    throw new UnsupportedOperationException();
  }

  public boolean needAuthorization(GWikiContext ctx)
  {
    return WicketApplication.isDevelopmentModus();
  }

  public <T> T runAsSu(GWikiContext wikiContext, CallableX<T, RuntimeException> callback)
  {
    GWikiSimpleUser su = GWikiUserServeElementFilterEvent.getUser();
    try {
      GWikiUserServeElementFilterEvent.setUser(defaultConfig.getUser("gwikisu"));
      return callback.call();
    } finally {
      GWikiUserServeElementFilterEvent.setUser(su);
    }
  }

  public <T> T runAsUser(String user, GWikiContext wikiContext, CallableX<T, RuntimeException> callback)
  {
    GWikiSimpleUser su = defaultConfig.getUser(user);
    if (su == null) {
      throw new AuthorizationFailedException("User doesn't exits: " + user);
    }
    GWikiSimpleUser pu = GWikiUserServeElementFilterEvent.getUser();
    try {
      GWikiUserServeElementFilterEvent.setUser(su);
      return callback.call();
    } finally {
      GWikiUserServeElementFilterEvent.setUser(pu);
    }
  }

  public boolean runIfAuthentificated(GWikiContext wikiContext, CallableX<Void, RuntimeException> callback)
  {
    return WicketApplication.isDevelopmentModus();
  }

  private final String extractSpace(final GWikiElementInfo ei)
  {
    if (ei.getId().indexOf('/') != -1) {
      return ei.getId().substring(0, ei.getId().indexOf('/'));
    }

    return ei.getId();
  }

  @Override
  public <T> T runWithRight(GWikiContext wikiContext, String addRight, CallableX<T, RuntimeException> callback)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T> T runWithRights(GWikiContext wikiContext, String[] addRights, CallableX<T, RuntimeException> callback)
  {
    // TODO Auto-generated method stub
    return null;
  }

}
