/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projecttree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.projectforge.plugins.chimney.activities.DependencyRelationDO;
import org.projectforge.plugins.chimney.activities.WbsActivityDO;
import org.projectforge.plugins.chimney.resources.ImageResources;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;

/**
 * A panel with a table to display dependency relations with delete links for a given activity.
 * The method {@link #deleteDependency(int)} must be overridden in order to delete an item.
 * 
 * @author Sweeps <pf@byte-storm.com>
 */
public abstract class DependencyTablePanel extends Panel
{
  private static final long serialVersionUID = 7314987482274995921L;
  private final IModel<WbsActivityDO> activityModel;

  public DependencyTablePanel(final String id, final IModel<WbsActivityDO> model, final Comparator<DependencyRelationDO> sortComparator)
  {
    super(id, model);
    this.activityModel = model;

    final List<DependencyRelationDO> predecessors = getSortedPredecessorList(sortComparator);

    final ListView<DependencyRelationDO> rows = new ListView<DependencyRelationDO>("row", predecessors) {
      private static final long serialVersionUID = 1L;

      @Override
      protected void populateItem(final ListItem<DependencyRelationDO> item)
      {
        final DependencyRelationDO dep = item.getModelObject();
        final AbstractWbsNodeDO predecessor = dep.getPredecessor().getWbsNode();

        item.add(new Label("wbs", predecessor.getWbsCode()));
        item.add(new Label("type", getString(dep.getType().getI18nKey())));
        item.add(new Label("name", predecessor.getTitle()));
        addDeleteLinkTo(item);
      }

    };
    add(rows);
  }

  private void addDeleteLinkTo(final ListItem<DependencyRelationDO> item)
  {
    // add delete link and icon
    final Link<Void> deleteLink = new Link<Void>("delete_link") {
      private static final long serialVersionUID = 1L;
      @Override
      public void onClick()
      {
        deleteDependency(item.getModelObject().getId());
      }
    };
    //deleteLink.add(WicketUtils.javaScriptConfirmDialogOnClick(getLocalizedMessage(
    //    "plugins.chimney.projectlist.projectconfirmdelete", project.getTitle())));
    deleteLink.add(new Image("delete_icon", ImageResources.DELETE_SMALL_IMAGE));
    item.add(deleteLink);
  }

  private List<DependencyRelationDO> getSortedPredecessorList(final Comparator<DependencyRelationDO> sortComparator)
  {
    final Set<DependencyRelationDO> predSet = activityModel.getObject().getPredecessorRelations();
    final ArrayList<DependencyRelationDO> sortedList = new ArrayList<DependencyRelationDO>(predSet);
    Collections.sort(sortedList, sortComparator);
    return sortedList;
  }

  /**
   * Must be overridden to delete a dependency
   * @param dependencyId The id of the dependency the user clicked the delete link for
   */
  public abstract void deleteDependency(int dependencyId);

}
