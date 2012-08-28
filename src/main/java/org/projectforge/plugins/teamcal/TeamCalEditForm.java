/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.KeyValueBean;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.user.GroupDO;
import org.projectforge.user.GroupDao;
import org.projectforge.user.Login;
import org.projectforge.web.common.TwoListHelper;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
import org.projectforge.web.wicket.flowlayout.DivType;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.IconLinkPanel;
import org.projectforge.web.wicket.flowlayout.IconType;

/**
 * Creates a top form-panel to add filter functions or other options.
 * 
 * @author Maximilian Lauterbach (m.lauterbach@micromata.de)
 *
 */
public class TeamCalEditForm extends AbstractEditForm<TeamCalDO, TeamCalEditPage>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamCalEditForm.class);

  private static final long serialVersionUID = 1379614008604844519L;

  @SpringBean(name = "teamCalDao")
  private TeamCalDao teamCalDao;

  @SpringBean(name = "groupDao")
  private GroupDao groupDao;

  @SuppressWarnings("unused")
  private String templateName; // Used by Wicket

  //  private GroupDao groupDao;

  private TwoListHelper<Integer, String> groups;

  private ListMultipleChoice<Integer> valuesToAssignChoice;

  private ListMultipleChoice<Integer> valuesToUnassignChoice;

  private final List<Integer> valuesToAssign = new ArrayList<Integer>();

  private final List<Integer> valuesToUnassign = new ArrayList<Integer>();

  /**
   * @param parentPage
   * @param data
   */
  public TeamCalEditForm(final TeamCalEditPage parentPage, final TeamCalDO data)
  {
    super(parentPage, data);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditForm#init()
   */
  @Override
  protected void init()
  {
    super.init();

    gridBuilder.newGrid16();

    // set title
    {
      final FieldsetPanel fs = gridBuilder.newFieldset("Titel", true).setLabelSide(true);
      final RequiredMaxLengthTextField title = new RequiredMaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data,
          "title"));
      if (isNew() == true) {
        title.add(WicketUtils.setFocus());
      }
      fs.add(title);
    }

    // set description
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("description"));
      fs.add(new MaxLengthTextArea(fs.getTextAreaId(), new PropertyModel<String>(data, "description"))).setAutogrow();
    }

    // set owner
    {
      data.setOwner(getUser());
      final FieldsetPanel fs = gridBuilder.newFieldset(Model.of("Ersteller").getObject());
      fs.add(new Label(fs.newChildId(), getUser().getUsername() + ""));
    }

    // set access groups
    gridBuilder.newColumnsPanel().newColumnPanel(DivType.COL_50);
    {
      addAssignedGroups(true);
    }
  }

  @SuppressWarnings("serial")
  private void addAssignedGroups(final boolean adminAccess)
  {
    final FieldsetPanel fs = gridBuilder.newFieldset("Berechtigungsgruppe", true).setLabelSide(false);
    final List<KeyValueBean<Integer, String>> fullList = new ArrayList<KeyValueBean<Integer, String>>();
    final BaseSearchFilter bsf = new BaseSearchFilter();
    bsf.setSearchString("group.name");
    //    bsf.setDeleted(false);
    //    bsf.setMaxRows(20);
    bsf.setSearchFields("name");
    //    bsf.setUseModificationFilter(false);

    // list of all available groups
    final List<GroupDO> result = Login.getInstance().getAllGroups();
    for (final GroupDO group : result) {
      fullList.add(new KeyValueBean<Integer, String>(group.getId(), group.getName()));
    }

    final TeamCalGroupCache cache = new TeamCalGroupCache();
    final List<Integer> assignedGroups = cache.getAssignedGroups(teamCalDao.newInstance());

    groups = new TwoListHelper<Integer, String>(fullList, assignedGroups);
    //    if (parentPage.tutorialGroupsToAdd != null) {
    //      groups.assign(parentPage.tutorialGroupsToAdd);
    //    }
    groups.sortLists();
    final ListMultipleChoice<Integer> valuesToUnassignChoice = new ListMultipleChoice<Integer>(fs.getListChoiceId());
    valuesToUnassignChoice.setModel(new PropertyModel<Collection<Integer>>(this, "valuesToUnassign"));
    WicketUtils.setHeight(valuesToUnassignChoice, 50);
    WicketUtils.setPercentSize(valuesToUnassignChoice, 45);
    fs.add(valuesToUnassignChoice);
    if (adminAccess == true) {
      final List<Integer> valuesToAssign = new ArrayList<Integer>();
      final List<Integer> valuesToUnassign = new ArrayList<Integer>();
      fs.add(new IconLinkPanel(fs.newChildId(), IconType.CIRCLE_ARROW_WEST, getString("tooltip.assign"), new SubmitLink(
          IconLinkPanel.LINK_ID) {
        @Override
        public void onSubmit()
        {
          //          accessChecker.checkIsLoggedInUserMemberOfAdminGroup();
          groups.assign(valuesToAssign);
          valuesToAssign.clear();
          refreshGroupLists();
        };
      }));
      fs.add(new IconLinkPanel(fs.newChildId(), IconType.CIRCLE_ARROW_EAST, getString("tooltip.unassign"), new SubmitLink(
          IconLinkPanel.LINK_ID) {
        @Override
        public void onSubmit()
        {
          //          accessChecker.checkIsLoggedInUserMemberOfAdminGroup();
          groups.unassign(valuesToUnassign);
          valuesToUnassign.clear();
          refreshGroupLists();
        };
      }));
      valuesToAssignChoice = new ListMultipleChoice<Integer>(fs.getListChoiceId());
      valuesToAssignChoice.setModel(new PropertyModel<Collection<Integer>>(this, "valuesToAssign"));
      WicketUtils.setHeight(valuesToAssignChoice, 50);
      WicketUtils.setPercentSize(valuesToAssignChoice, 45);
      fs.add(valuesToAssignChoice);
      fs.setNowrap();
    }
    refreshGroupLists();
  }

  private void refreshGroupLists()
  {
    final LabelValueChoiceRenderer<Integer> valuesToAssignChoiceRenderer = new LabelValueChoiceRenderer<Integer>();
    for (final KeyValueBean<Integer, String> group : this.groups.getUnassignedItems()) {
      valuesToAssignChoiceRenderer.addValue(group.getKey(), group.getValue());
    }
    valuesToAssignChoice.setChoiceRenderer(valuesToAssignChoiceRenderer);
    valuesToAssignChoice.setChoices(valuesToAssignChoiceRenderer.getValues());
    final LabelValueChoiceRenderer<Integer> valuesToUnassignChoiceRenderer = new LabelValueChoiceRenderer<Integer>();
    for (final KeyValueBean<Integer, String> group : this.groups.getAssignedItems()) {
      valuesToUnassignChoiceRenderer.addValue(group.getKey(), group.getValue());
    }
    //    valuesToUnassignChoice.setChoiceRenderer(valuesToUnassignChoiceRenderer);
    //    valuesToUnassignChoice.setChoices(valuesToUnassignChoiceRenderer.getValues());
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditForm#getLogger()
   */
  @Override
  protected Logger getLogger()
  {
    return log;
  }

  /**
   * @return the valuesToAssign
   */
  public List<Integer> getValuesToAssign()
  {
    return valuesToAssign;
  }

  /**
   * @return the valuesToUnassign
   */
  public List<Integer> getValuesToUnassign()
  {
    return valuesToUnassign;
  }

}
