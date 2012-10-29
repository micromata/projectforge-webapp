/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.NumberHelper;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
import org.projectforge.plugins.teamcal.admin.TeamCalDao;
import org.projectforge.user.PFUserContext;

import com.vaynberg.wicket.select2.Response;
import com.vaynberg.wicket.select2.TextChoiceProvider;

/**
 * Provider class for multipleChoice.
 * 
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 *
 */
public class TeamCalChoiceProvider extends TextChoiceProvider<TeamCalDO>
{

  private static final long serialVersionUID = -8310756569504320965L;

  private static int RESULT_PAGE_SIZE = 20;

  @SpringBean
  private TeamCalDao teamCalDao;

  public TeamCalChoiceProvider()
  {
    Injector.get().inject(this);
  }

  /**
   * @see com.vaynberg.wicket.select2.TextChoiceProvider#getDisplayText(java.lang.Object)
   */
  @Override
  protected String getDisplayText(final TeamCalDO teamCal)
  {
    return teamCal.getTitle();
  }

  /**
   * @see com.vaynberg.wicket.select2.TextChoiceProvider#getId(java.lang.Object)
   */
  @Override
  protected Object getId(final TeamCalDO choice)
  {
    return choice.getId();
  }

  /**
   * @see com.vaynberg.wicket.select2.ChoiceProvider#query(java.lang.String, int, com.vaynberg.wicket.select2.Response)
   */
  @Override
  public void query(String term, final int page, final Response<TeamCalDO> response)
  {
    // add all access groups
    final boolean ownTeamCals = true;
    final List<TeamCalDO> fullAccessTeamCals = getTeamCalDao().getTeamCalsByAccess(PFUserContext.getUser(), ownTeamCals,
        TeamCalDao.FULL_ACCESS_GROUP, TeamCalDao.READONLY_ACCESS_GROUP, TeamCalDao.MINIMAL_ACCESS_GROUP);
    final List<TeamCalDO> result = new ArrayList<TeamCalDO>();
    term = term.toLowerCase();

    final int offset = page * RESULT_PAGE_SIZE;

    int matched = 0;
    boolean hasMore = false;
    for (final TeamCalDO teamCal : fullAccessTeamCals) {
      if (result.size() == RESULT_PAGE_SIZE) {
        hasMore = true;
        break;
      }
      if (teamCal.getTitle().toLowerCase().contains(term) == true
          || teamCal.getOwner().getFullname().toLowerCase().contains(term) == true
          || teamCal.getOwner().getUsername().toLowerCase().contains(term) == true) {
        matched++;
        if (matched > offset) {
          result.add(teamCal);
        }
      }
    }
    response.addAll(result);
    response.setHasMore(hasMore);
  }

  /**
   * @see com.vaynberg.wicket.select2.ChoiceProvider#toChoices(java.util.Collection)
   */
  @Override
  public Collection<TeamCalDO> toChoices(final Collection<String> ids)
  {
    final List<TeamCalDO> list = new ArrayList<TeamCalDO>();
    if (ids == null) {
      return list;
    }
    for (final String id : ids) {
      final Integer teamCalId = NumberHelper.parseInteger(id);
      if (teamCalId == null) {
        continue;
      }
      final TeamCalDO teamCal = getTeamCalDao().getById(teamCalId);
      if (teamCal != null) {
        list.add(teamCal);
      }
    }
    return list;
  }

  private TeamCalDao getTeamCalDao() {
    return teamCalDao;
  }

}
