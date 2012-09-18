/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal;

import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.user.GroupDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.user.GroupSelectPanel;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.JodaDatePanel;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

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

  @SuppressWarnings("unused")
  private String templateName; // Used by Wicket

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  private boolean access = false;

  private JodaDatePanel datePanel;

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

    gridBuilder.newGrid8();

    // checking visibility rights
    if (isNew() == true || data.getOwner() == null && data.getMinimalAccessGroup() == null) {
      access = true;
    } else {
      if (new TeamCalRight().hasUpdateAccess(getUser(), data, data) == true)
        access = true;
      else
        if (accessCheck(data.getReadOnlyAccessGroup()) == true)
          access = false;
        else
          if (accessCheck(data.getMinimalAccessGroup()) == true) {
            final TeamCalDO newTeamCalDO = new TeamCalDO();
            newTeamCalDO.setId(data.getId());
            newTeamCalDO.setMinimalAccessGroup(data.getMinimalAccessGroup());
            newTeamCalDO.setOwner(data.getOwner());
            data = newTeamCalDO;
            access = false;
          } else
            access = false;
    }

    // set title
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.teamcal.title"));
      final RequiredMaxLengthTextField title = new RequiredMaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data,
          "title"));
      if (isNew() == true) {
        title.add(WicketUtils.setFocus());
      }
      fs.add(title);
      if (access == false)
        title.setEnabled(false);
    }

    // set description
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.teamcal.description"));
      final MaxLengthTextArea descr = new MaxLengthTextArea(fs.getTextAreaId(), new PropertyModel<String>(data, "description"));
      fs.add(descr).setAutogrow();
      if (access == false)
        descr.setEnabled(false);
    }

    // set owner
    {
      if (data.getOwner() == null)
        data.setOwner(getUser());
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.teamcal.owner")).setLabelFor(this);
      fs.add(new Label(fs.newChildId(), data.getOwner().getUsername() + ""));
    }

    // set access groups
    gridBuilder.newGrid8();
    {
      // set full access group chooser
      final FieldsetPanel fsFullAccess = gridBuilder.newFieldset(getString("plugins.teamcal.fullAccess"), true);
      final PropertyModel<GroupDO> model = new PropertyModel<GroupDO>(data, "fullAccessGroup");
      final GroupSelectPanel fullAccess = new GroupSelectPanel(fsFullAccess.newChildId(), model,
          parentPage, "fullAccessGroupId");
      fsFullAccess.add(fullAccess);
      fsFullAccess.setLabelFor(fullAccess);
      fullAccess.init();
      if (access == false)
        fullAccess.setEnabled(false);

      // set read-only access chooser
      final FieldsetPanel fsReadOnly = gridBuilder.newFieldset(getString("plugins.teamcal.readOnlyAccess"), true);
      final GroupSelectPanel readOnly = new GroupSelectPanel(fsReadOnly.newChildId(), new PropertyModel<GroupDO>(data, "readOnlyAccessGroup"),
          parentPage, "readOnlyAccessGroupId");
      fsReadOnly.add(readOnly);
      fsReadOnly.setLabelFor(readOnly);
      readOnly.init();
      if (access == false)
        readOnly.setEnabled(false);

      // set minimal access chooser
      final FieldsetPanel fsMinimal = gridBuilder.newFieldset(getString("plugins.teamcal.minimalAccess"), true);
      final GroupSelectPanel minimalAccess = new GroupSelectPanel(fsMinimal.newChildId(), new PropertyModel<GroupDO>(data, "minimalAccessGroup"),
          parentPage, "minimalAccessGroupId");
      fsMinimal.add(minimalAccess);
      fsMinimal.setLabelFor(minimalAccess);
      fsMinimal.addHelpIcon(getString("plugins.teamcal.minimalAccess.hint"));
      minimalAccess.init();
      if (access == false)
        minimalAccess.setEnabled(false);
    }
  }

  private boolean accessCheck(final GroupDO group) {
    if (group != null) {
      final Collection<Integer> groups = userGroupCache.getUserGroups(getUser());
      final Iterator<Integer> it = groups.iterator();
      while (it.hasNext()){
        final int id = it.next();
        if (id == 0 || group.getId() == id)
          return true;
      }
    }
    return false;
  }

  /**
   * to be continued.
   * multiple group selection.
   */
  /**
  @SuppressWarnings("serial")
  private void addAssignedGroups(final boolean adminAccess)
  {
    final FieldsetPanel fs = gridBuilder.newFieldset("FullAccessGroup", true).setLabelSide(false);
    final List<KeyValueBean<Integer, String>> fullList = new ArrayList<KeyValueBean<Integer, String>>();

    // list of all available groups
    final List<GroupDO> result = Login.getInstance().getAllGroups();
    GroupDO buffer = null; // check for double entries.
    for (final GroupDO group : result) {
      if (buffer == null) {
        fullList.add(new KeyValueBean<Integer, String>(group.getId(), group.getName()));
        buffer = group;
      } else
        // don't add if group already added.
        if (group.getId() != buffer.getId()) {
          fullList.add(new KeyValueBean<Integer, String>(group.getId(), group.getName()));
          buffer = group;
        }
    }

    // relation teamcal to group
    final TeamCalAccessDO teamCalAccess = new TeamCalAccessDO();
    final Set<GroupDO> assignedGroups = teamCalAccess.getFullAccessGroups();
    final List<Integer> assignedGroupList = new ArrayList<Integer>();
    if (assignedGroups != null)
      for (final GroupDO g : assignedGroups)
        assignedGroupList.add(g.getId());

    groups = new TwoListHelper<Integer, String>(fullList, assignedGroupList);
    groups.sortLists();

    valuesToUnassignChoice = new ListMultipleChoice<Integer>(fs.getListChoiceId());
    valuesToUnassignChoice.setModel(new PropertyModel<Collection<Integer>>(this, "valuesToUnassign"));
    // set row height - unassigned items
    WicketUtils.setHeight(valuesToUnassignChoice, ROW_HEIGHT);
    WicketUtils.setPercentSize(valuesToUnassignChoice, ROW_HEIGHT_PERCENT);
    fs.add(valuesToUnassignChoice);

    valuesToAssignChoice = new ListMultipleChoice<Integer>(fs.getListChoiceId());
    valuesToAssignChoice.setModel(new PropertyModel<Collection<Integer>>(this, "valuesToAssign"));
    // set row height - assigned items
    WicketUtils.setHeight(valuesToAssignChoice, ROW_HEIGHT);
    WicketUtils.setPercentSize(valuesToAssignChoice, ROW_HEIGHT_PERCENT);
    fs.add(valuesToAssignChoice);

    //    fs.add(valuesToAssignChoice);
    if (adminAccess == true) {

      // ASSIGN
      valuesToAssign = new ArrayList<Integer>();
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

      // UNASSIGN
      valuesToUnassign = new ArrayList<Integer>();
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
    valuesToUnassignChoice.setChoiceRenderer(valuesToUnassignChoiceRenderer);
    valuesToUnassignChoice.setChoices(valuesToUnassignChoiceRenderer.getValues());
  }*/

  /**
   * @see org.projectforge.web.wicket.AbstractEditForm#getLogger()
   */
  @Override
  protected Logger getLogger()
  {
    return log;
  }

  /**
   * @return the datePanel
   */
  public JodaDatePanel getDatePanel()
  {
    return datePanel;
  }

  /**
   * @param datePanel the datePanel to set
   * @return this for chaining.
   */
  public void setDatePanel(final JodaDatePanel datePanel)
  {
    this.datePanel = datePanel;
  }
}
