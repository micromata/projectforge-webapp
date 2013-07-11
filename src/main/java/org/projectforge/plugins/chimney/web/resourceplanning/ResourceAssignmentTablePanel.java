/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.resourceplanning;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.joda.time.Period;
import org.projectforge.plugins.chimney.resourceplanning.ResourceAssignmentDO;
import org.projectforge.plugins.chimney.resources.ImageResources;
import org.projectforge.plugins.chimney.web.components.ChimneyJodaPeriodConverter;
import org.projectforge.user.PFUserContext;

/**
 * A panel with a table to display dependency relations with delete links for a given activity.
 * The method {@link #deleteDependency(int)} must be overridden in order to delete an item.
 * 
 * @author Sweeps <pf@byte-storm.com>
 */
public abstract class ResourceAssignmentTablePanel extends Panel
{
  private static final long serialVersionUID = 7314987482274995921L;

  public ResourceAssignmentTablePanel(final String id, final List<ResourceAssignmentDO> resAssignments)
  {
    super(id, null);

    final ListView<ResourceAssignmentDO> rows = new ListView<ResourceAssignmentDO>("row", resAssignments) {
      private static final long serialVersionUID = 1L;

      @Override
      protected void populateItem(final ListItem<ResourceAssignmentDO> item)
      {
        final ResourceAssignmentDO ra = item.getModelObject();

        item.add(new Label("user", ra.getUser().getFullname()));
        item.add(new Label("effort", getEffortOf(ra)));
        addDeleteLinkTo(item);
      }

      private String getEffortOf(final ResourceAssignmentDO ra)
      {
        final ChimneyJodaPeriodConverter<Period> periodConverter = new ChimneyJodaPeriodConverter<Period>();
        return periodConverter.convertToString(ra.getPlannedEffort(), PFUserContext.getLocale());
      }
    };
    add(rows);
  }

  private void addDeleteLinkTo(final ListItem<ResourceAssignmentDO> item)
  {
    // add delete link and icon
    final Link<Void> deleteLink = new Link<Void>("delete_link") {
      private static final long serialVersionUID = 1L;
      @Override
      public void onClick()
      {
        deleteRessourceAssignment(item.getModelObject().getId());
      }
    };
    //deleteLink.add(WicketUtils.javaScriptConfirmDialogOnClick(getLocalizedMessage(
    //    "plugins.chimney.projectlist.projectconfirmdelete", project.getTitle())));
    deleteLink.add(new Image("delete_icon", ImageResources.DELETE_SMALL_IMAGE));
    item.add(deleteLink);
  }

  /**
   * Must be overridden to delete a resource assignment
   * @param dependencyId The id of the resource assignment the user clicked the delete link for
   */
  public abstract void deleteRessourceAssignment(int resourceAssignmentId);

}
