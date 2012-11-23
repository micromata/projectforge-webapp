/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.admin;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.access.AccessChecker;
import org.projectforge.user.GroupDO;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.WebConfiguration;
import org.projectforge.web.user.GroupSelectPanel;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.JodaDatePanel;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
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

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  @SpringBean(name = "userDao")
  private UserDao userDao;

  @SpringBean(name = "accessChecker")
  protected AccessChecker accessChecker;

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
    final TeamCalRight right = new TeamCalRight();
    if (isNew() == true || data.getOwner() == null) {
      access = true;
    } else {
      if (right.hasUpdateAccess(getUser(), data, data) == true)
        access = true;
      else if (right.hasAccessGroup(data.getReadOnlyAccessGroup(), userGroupCache, getUser()) == true)
        access = false;
      else if (right.hasAccessGroup(data.getMinimalAccessGroup(), userGroupCache, getUser()) == true) {
        final TeamCalDO newTeamCalDO = new TeamCalDO();
        newTeamCalDO.setId(data.getId());
        newTeamCalDO.setMinimalAccessGroup(data.getMinimalAccessGroup());
        newTeamCalDO.setOwner(data.getOwner());
        newTeamCalDO.setTitle(data.getTitle());
        data = newTeamCalDO;
        access = false;
      } else access = false;
    }

    // set title
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.teamcal.title"));
      final RequiredMaxLengthTextField title = new RequiredMaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, "title"));
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

    gridBuilder.newGrid8();
    if (access == true) {
      // set access groups
      {
        // set full access group chooser
        final FieldsetPanel fsFullAccess = gridBuilder.newFieldset(getString("plugins.teamcal.fullAccess"), true);
        final PropertyModel<GroupDO> model = new PropertyModel<GroupDO>(data, "fullAccessGroup");
        final GroupSelectPanel fullAccess = new GroupSelectPanel(fsFullAccess.newChildId(), model, parentPage, "fullAccessGroupId");
        fsFullAccess.add(fullAccess);
        fsFullAccess.setLabelFor(fullAccess);
        fullAccess.init();
        if (access == false)
          fullAccess.setEnabled(false);

        // set read-only access chooser
        final FieldsetPanel fsReadOnly = gridBuilder.newFieldset(getString("plugins.teamcal.readOnlyAccess"), true);
        final GroupSelectPanel readOnly = new GroupSelectPanel(fsReadOnly.newChildId(), new PropertyModel<GroupDO>(data,
            "readOnlyAccessGroup"), parentPage, "readOnlyAccessGroupId");
        fsReadOnly.add(readOnly);
        fsReadOnly.setLabelFor(readOnly);
        readOnly.init();
        if (access == false)
          readOnly.setEnabled(false);

        // set minimal access chooser
        final FieldsetPanel fsMinimal = gridBuilder.newFieldset(getString("plugins.teamcal.minimalAccess"), true);
        final GroupSelectPanel minimalAccess = new GroupSelectPanel(fsMinimal.newChildId(), new PropertyModel<GroupDO>(data,
            "minimalAccessGroup"), parentPage, "minimalAccessGroupId");
        fsMinimal.add(minimalAccess);
        fsMinimal.setLabelFor(minimalAccess);
        fsMinimal.addHelpIcon(getString("plugins.teamcal.minimalAccess.hint"));
        minimalAccess.init();
        if (access == false)
          minimalAccess.setEnabled(false);
      }
    }
    if (accessChecker.isRestrictedUser() == false && WebConfiguration.isDevelopmentMode() == true) {
      final FieldsetPanel fsSubscribe = gridBuilder.newFieldset(getString("plugins.teamcal.subscribe"), true).setNoLabelFor();
      final ExternalLink iCalExportLink = new ExternalLink(IconLinkPanel.LINK_ID, createICalTarget());
      final IconLinkPanel exportICalButtonPanel = new IconLinkPanel(fsSubscribe.newChildId(), IconType.SUBSCRIPTION,
          getString("plugins.teamcal.subscribe"), iCalExportLink).setLight();
      fsSubscribe.add(exportICalButtonPanel);
      if (isNew() == true)
        fsSubscribe.setVisible(false);
    }
  }

  /**
   * create ics export url
   */
  private String createICalTarget()
  {
    final PFUserDO user = PFUserContext.getUser();
    final String authenticationKey = userDao.getAuthenticationToken(user.getId());
    final String contextPath = WebApplication.get().getServletContext().getContextPath();
    final String iCalTarget = contextPath
        + "/export/ProjectForge.ics?timesheetUser="
        + user.getUsername()
        + "&token="
        + authenticationKey
        + "&teamCals="
        + this.getData().getId()
        + "&timesheetRequired="
        + false;
    return iCalTarget;
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
