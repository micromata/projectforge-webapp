/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projecttree;

import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.TableTree;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.ResourceReference;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.visitors.WbsIconVisitor;
import org.projectforge.plugins.chimney.web.components.ImageLinkPanel;
import org.projectforge.plugins.chimney.web.visitors.WbsNodeEditPageVisitor;
import org.projectforge.web.wicket.AbstractSecuredPage;

/**
 * Tree table wicket componant for visualization of the (sub) tree structure of a project node (AbstractWbsNodeDO).
 * @see org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO
 * @author Sweeps <pf@byte-storm.com>
 */
public class ProjectTableTree extends TableTree<AbstractWbsNodeDO, String>
{
  private static final long serialVersionUID = 2035376224358666855L;

  public ProjectTableTree(final String id, final List<IColumn<AbstractWbsNodeDO, String>> columns,
      final ITreeProvider<AbstractWbsNodeDO> provider, final int rowsPerPage, final IModel<Set<AbstractWbsNodeDO>> state)
  {
    super(id, columns, provider, rowsPerPage, state);
  }

  @Override
  protected void onInitialize()
  {
    super.onInitialize();
    // addTopToolbar(new NavigationToolbar(this));
    // addTopToolbar(new HeadersToolbar(this));
    // addBottomToolbar(new NoRecordsToolbar(this));
  };

  @Override
  protected Component newContentComponent(final String id, final IModel<AbstractWbsNodeDO> model)
  {
    final AbstractWbsNodeDO wbsNode = model.getObject();
    final ResourceReference icon = getWbsIconRessourceFor(wbsNode);
    return new ImageLinkPanel(id, icon, wbsNode.getTitle()) {
      private static final long serialVersionUID = 1L;

      AbstractSecuredPage localEditPage = null;

      @Override
      public void onClick()
      {
        if (localEditPage == null) {
          localEditPage = WbsNodeEditPageVisitor.createEditPageFor(model.getObject());
        }
        setResponsePage(localEditPage);
      }

    };
  }

  @Override
  protected Item<AbstractWbsNodeDO> newRowItem(final String id, final int index, final IModel<AbstractWbsNodeDO> model)
  {
    return new OddEvenItem<AbstractWbsNodeDO>(id, index, model);
  }

  // @Override
  // public void renderHead(final IHeaderResponse response)
  // {
  // response.render(CssReferenceHeaderItem.forReference(new WindowsTheme()));
  // }

  private ResourceReference getWbsIconRessourceFor(final AbstractWbsNodeDO wbsNode)
  {
    final WbsIconVisitor iconVisitor = new WbsIconVisitor(false);
    wbsNode.accept(iconVisitor);
    return iconVisitor.getSelectedImageResource();
  }

}
