/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.integration;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.plugins.teamcal.TeamCalChoiceProvider;
import org.projectforge.plugins.teamcal.TeamCalDO;
import org.projectforge.plugins.teamcal.TeamCalDao;
import org.projectforge.web.calendar.CalendarFilter;
import org.projectforge.web.calendar.CalendarForm;
import org.projectforge.web.calendar.CalendarPage;
import org.projectforge.web.common.MultiChoiceListHelper;
import org.projectforge.web.wicket.flowlayout.DivType;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.GridBuilder;

import com.vaynberg.wicket.select2.Select2MultiChoice;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 */
public class TeamCalCalendarForm extends CalendarForm
{

  private static final long serialVersionUID = -5838203593605203398L;

  @SpringBean(name = "teamCalDao")
  private TeamCalDao teamCalDao;

  private MultiChoiceListHelper<TeamCalDO> multipleTeamCalList;

  private TeamCalCalendarFilter filter;

  private static final String USERPREF_KEY = "TeamCalendarForm.userPrefs";

  /**
   * @param parentPage
   */
  public TeamCalCalendarForm(final CalendarPage parentPage)
  {
    super(parentPage);
  }

  /**
   * @see org.projectforge.web.calendar.CalendarForm#onAfterInit(org.projectforge.web.wicket.flowlayout.GridBuilder,
   *      org.projectforge.web.wicket.flowlayout.FieldsetPanel)
   */
  @SuppressWarnings("unchecked")
  @Override
  protected void onAfterInit(final GridBuilder gridBuilder, final FieldsetPanel fs)
  {
    final List<TeamCalDO> list = teamCalDao.getTeamCalsByAccess(getUser(), TeamCalDao.FULL_ACCESS_GROUP, TeamCalDao.READONLY_ACCESS_GROUP,
        TeamCalDao.MINIMAL_ACCESS_GROUP);
    gridBuilder.newColumnPanel(DivType.COL_75);

    // get users last filter settings
    // TODO Max das Ding vielleicht mit in den Filter legen?
    multipleTeamCalList = ((MultiChoiceListHelper<TeamCalDO>) parentPage.getUserPrefEntry(USERPREF_KEY));
    if (multipleTeamCalList == null) {
      multipleTeamCalList = new MultiChoiceListHelper<TeamCalDO>().setComparator(new IdComparator()).setFullList(list);
      parentPage.putUserPrefEntry(USERPREF_KEY, multipleTeamCalList, true);
    }

    final FieldsetPanel listFieldSet = gridBuilder.newFieldset(getString("plugins.teamevent.teamCal"), true);
    // TODO schon ausgewählte teamcals -> aus teamcaldao z.b. full_access_groups
    // if (assignedTeamCals != null) {
    // for (final TeamCalDO cals : assignedTeamCals) {
    // multipleTeamCalList.addOriginalAssignedItem(cals).assignItem(cals);
    // }
    // }
    final TeamCalChoiceProvider teamProvider = new TeamCalChoiceProvider();
    final Select2MultiChoice<TeamCalDO> teamCalChoice = new Select2MultiChoice<TeamCalDO>(fs.getSelect2MultiChoiceId(),
        new PropertyModel<Collection<TeamCalDO>>(this.multipleTeamCalList, "assignedItems"), teamProvider);
    teamCalChoice.add(new AjaxFormComponentUpdatingBehavior("onChange") {
      private static final long serialVersionUID = 1L;

      @Override
      protected void onUpdate(final AjaxRequestTarget target)
      {
        if (multipleTeamCalList.getAssignedItems().isEmpty() == false) {
          filter.setAssignedtItems(multipleTeamCalList.getAssignedItems());
          setResponsePage(TeamCalCalendarForm.this.getParentPage());
        }
      }
    });
    listFieldSet.add(teamCalChoice);
  }

  @Override
  public CalendarFilter getFilter()
  {
    if(this.filter == null && super.getFilter() != null) {
      return super.getFilter();
    }
    return filter;
  }

  @Override
  protected void setFilter(final CalendarFilter filter)
  {
    if (filter instanceof TeamCalCalendarFilter) {
      this.filter = (TeamCalCalendarFilter) filter;
    }
    super.setFilter(filter);
  }

  /**
   * compare ids
   * 
   * @author Maximilian Lauterbach (m.lauterbach@micromata.de)
   * 
   */
  private class IdComparator implements Comparator<TeamCalDO>, Serializable
  {

    private static final long serialVersionUID = 5501418454944208820L;

    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(final TeamCalDO arg0, final TeamCalDO arg1)
    {
      final Integer n1 = arg0.getId() != null ? arg0.getId() : 0;

      final Integer n2 = arg1.getId() != null ? arg1.getId() : 0;

      return n1.compareTo(n2);
    }
  }

  /**
   * @return the multipleTeamCalList
   */
  public MultiChoiceListHelper<TeamCalDO> getMultipleTeamCalList()
  {
    return multipleTeamCalList;
  }

  /**
   * @param multipleTeamCalList the multipleTeamCalList to set
   * @return this for chaining.
   */
  public void setMultipleTeamCalList(final MultiChoiceListHelper<TeamCalDO> multipleTeamCalList)
  {
    this.multipleTeamCalList = multipleTeamCalList;
  }

}
