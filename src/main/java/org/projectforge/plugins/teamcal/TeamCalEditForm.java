/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.user.GroupDO;
import org.projectforge.user.GroupDao;
import org.projectforge.web.user.GroupSelectPanel;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.WicketUtils;
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

  @SpringBean(name = "teamCalDao")
  private TeamCalDao teamCalDao;

  @SpringBean(name = "groupDao")
  private GroupDao groupDao;

  @SuppressWarnings("unused")
  private String templateName; // Used by Wicket

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

    // set title
    {
      // TODO i18nKey!
      final FieldsetPanel fs = gridBuilder.newFieldset("Titel");
      final RequiredMaxLengthTextField title = new RequiredMaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data,
          "title"));
      if (isNew() == true) {
        title.add(WicketUtils.setFocus());
      }
      fs.add(title);
    }

    // set description
    {
      // TODO i18nKey!
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("description"));
      fs.add(new MaxLengthTextArea(fs.getTextAreaId(), new PropertyModel<String>(data, "description"))).setAutogrow();
    }

    // set owner
    {
      // TODO i18nKey!
      data.setOwner(getUser());
      final FieldsetPanel fs = gridBuilder.newFieldset(Model.of("Ersteller").getObject());
      fs.add(new Label(fs.newChildId(), getUser().getUsername() + ""));
    }

    // set access groups
    gridBuilder.newGrid8();
    {
      // set full access group chooser
      final FieldsetPanel fsFullAccess = gridBuilder.newFieldset(Model.of("AccessGroup").getObject(), true);
      final PropertyModel<GroupDO> model = new PropertyModel<GroupDO>(data, "fullAccessGroup");
      final GroupSelectPanel fullAccess = new GroupSelectPanel(fsFullAccess.newChildId(), model,
          parentPage, "fullAccessGroupId");
      fsFullAccess.add(fullAccess);
      fsFullAccess.setLabelFor(fullAccess);
      fullAccess.init();
      
      // set read-only access chooser
      final FieldsetPanel fsReadOnly = gridBuilder.newFieldset(Model.of("ReadOnlyGroup").getObject(), true);
      final GroupSelectPanel readOnly = new GroupSelectPanel(fsReadOnly.newChildId(), new PropertyModel<GroupDO>(data, "readOnlyAccessGroup"),
          parentPage, "readOnlyAccessGroupId");
      fsReadOnly.add(readOnly);
      fsReadOnly.setLabelFor(readOnly);
      readOnly.init();
      
      // set minimal access chooser
      final FieldsetPanel fsMinimal = gridBuilder.newFieldset(Model.of("MinimalAccessGroup").getObject(), true);
      final GroupSelectPanel minimalAccess = new GroupSelectPanel(fsMinimal.newChildId(), new PropertyModel<GroupDO>(data, "minimalAccessGroup"),
          parentPage, "minimalAccessGroupId");
      fsMinimal.add(minimalAccess);
      fsMinimal.setLabelFor(minimalAccess);
      minimalAccess.init();
    }
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

}
