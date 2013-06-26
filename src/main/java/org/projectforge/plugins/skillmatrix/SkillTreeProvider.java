/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.skillmatrix;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.projectforge.registry.Registry;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;

/**
 * @author Billy Duong (b.duong@micromata.de)
 * 
 */
public class SkillTreeProvider implements ITreeProvider<SkillNode>
{

  private static final long serialVersionUID = 7692282103462630402L;

  private transient SkillDao skillDao;

  private final SkillFilter skillFilter;

  private boolean showRootNode;

  public SkillTreeProvider(final SkillFilter skillFilter)
  {
    this.skillFilter = skillFilter;
    skillFilter.resetMatch();
  }

  /**
   * Nothing to do.
   * @see org.apache.wicket.model.IDetachable#detach()
   */
  @Override
  public void detach()
  {
  }

  /**
   * @see org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider#getRoots()
   */
  @Override
  public Iterator<SkillNode> getRoots()
  {
    return iterator(getSkillTree().getRootSkillNode().getChilds(), showRootNode);
  }

  /**
   * @see org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider#hasChildren(java.lang.Object)
   */
  @Override
  public boolean hasChildren(final SkillNode node)
  {
    if (node.isRootNode() == true) {
      // Don't show children of root node again.
      return false;
    }
    return node.hasChilds();
  }

  /**
   * @see org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider#getChildren(java.lang.Object)
   */
  @Override
  public Iterator<SkillNode> getChildren(final SkillNode node)
  {
    if (node.isRootNode() == true) {
      // Don't show children of root node again.
      return new LinkedList<SkillNode>().iterator();
    }
    return iterator(node.getChilds());
  }

  /**
   * @see org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider#model(java.lang.Object)
   */
  @Override
  public IModel<SkillNode> model(final SkillNode object)
  {
    return new SkillNodeModel(object);
  }

  private Iterator<SkillNode> iterator(final List<SkillNode> nodes)
  {
    return iterator(nodes, false);
  }

  private Iterator<SkillNode> iterator(final List<SkillNode> nodes, final boolean appendRootNode)
  {
    // ensureSkillTree();
    final SortedSet<SkillNode> list = new TreeSet<SkillNode>(new Comparator<SkillNode>() {
      /**
       * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
       */
      @Override
      public int compare(final SkillNode skillNode1, final SkillNode skillNode2)
      {
        if (skillNode1.isRootNode() == true) {
          // Show root node at last position.
          return 1;
        }
        if (skillNode2.isRootNode() == true) {
          // Show root node at last position.
          return -1;
        }
        String title1 = skillNode1.getSkill().getTitle();
        title1 = title1 != null ? title1.toLowerCase() : "";
        String title2 = skillNode2.getSkill().getTitle();
        title2 = title2 != null ? title2.toLowerCase() : "";
        return title1.compareTo(title2);
      }
    });
    if (appendRootNode == true) {
      if (skillFilter.match(getSkillTree().getRootSkillNode(), null, null) == true) {
        list.add(getSkillTree().getRootSkillNode());
      }
    }
    if (nodes == null || nodes.isEmpty() == true) {
      return list.iterator();
    }
    final PFUserDO user = PFUserContext.getUser();
    for (final SkillNode node : nodes) {

      // TODO rewrite matching, no nodes are added

      final boolean isMatch = skillFilter.match(node, getSkillDao(), user);
      final boolean hasAccess = getSkillDao().hasSelectAccess(user, node.getSkill(), false);

      //      if (isMatch == true && hasAccess == true) {
      //        list.add(node);
      //      }
      list.add(node);
    }
    return list.iterator();
  }

  /**
   * @param showRootNode the showRootNode to set
   * @return this for chaining.
   */
  public SkillTreeProvider setShowRootNode(final boolean showRootNode)
  {
    this.showRootNode = showRootNode;
    return this;
  }

  /**
   * @return the skillTree
   */
  private SkillTree getSkillTree()
  {
    return getSkillDao().getSkillTree();
  }

  /**
   * A {@link Model} which uses an id to load its {@link Foo}.
   * 
   * If {@link Foo}s were {@link Serializable} you could just use a standard {@link Model}.
   * 
   * @see #equals(Object)
   * @see #hashCode()
   */
  private static class SkillNodeModel extends LoadableDetachableModel<SkillNode>
  {
    private static final long serialVersionUID = 1L;

    private final Integer id;

    private transient SkillTree skillTree;

    public SkillNodeModel(final SkillNode skillNode)
    {
      super(skillNode);
      id = skillNode.getId();
    }

    @Override
    protected SkillNode load()
    {
      if (skillTree == null) {
        skillTree = Registry.instance().getDao(SkillDao.class).getSkillTree();
      }
      return skillTree.getSkillNodeById(id);
    }

    /**
     * Important! Models must be identifyable by their contained object.
     */
    @Override
    public boolean equals(final Object obj)
    {
      if (obj instanceof SkillNodeModel) {
        return ((SkillNodeModel) obj).id.equals(id);
      }
      return false;
    }

    /**
     * Important! Models must be identifyable by their contained object.
     */
    @Override
    public int hashCode()
    {
      return id.hashCode();
    }
  }

  /**
   * @return the skillDao
   */
  public SkillDao getSkillDao()
  {
    if (skillDao == null) {
      skillDao = Registry.instance().getDao(SkillDao.class);
    }
    return skillDao;
  }

  /**
   * @param skillDao the skillDao to set
   */
  public void setSkillDao(final SkillDao skillDao)
  {
    this.skillDao = skillDao;
  }

}
