/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.gantt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.markup.MarkupType;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.AbstractItem;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.joda.time.ReadableDateTime;
import org.projectforge.plugins.chimney.activities.DependencyRelationType;
import org.projectforge.plugins.chimney.web.gantt.model.GanttActivityVisualizationType;
import org.projectforge.plugins.chimney.web.gantt.model.IGanttActivity;
import org.projectforge.plugins.chimney.web.gantt.model.IGanttDependency;
/**
 * Creates XML input for gantt charts of jsgantt (http://www.jsgantt.com/).
 * This WebPage(@see {@link WebPage}) uses text/xml markup type (@see {@link MarkupType}) to respond xml data to the client.
 * Thus the corresponding markup page is also an xml file
 * The format of the xml file is slightly changed in comparison to the original jsgantt format to support all four dependency types (begin-begin, begin-end, end-begin, end-end),
 * rather than end-begin dependencies only.
 * @author Sweeps <pf@byte-storm.com>
 */
public class GanttTaskXmlPage extends WebPage
{

  public static final String PAGE_ID = "ganttTaskXmlPage";
  /**
   * 
   */
  private static final long serialVersionUID = -2994549463635379195L;

  /**
   * MarkupType for xml output
   */
  public static final MarkupType XML_MARKUP_TYPE = new MarkupType("xml", "text/xml");


  //@Transient
  //final transient List<? extends IGanttActivity> ganttActivities;

  /**
   * creates xml data of the given gantt activities (@see {@link IGanttActivity}) for jsgantt.
   * the tree structure of the gantt activities is automatically preorder sorted,
   * to assure the correct visualization of groups (sub trees).
   * This sorting is staple so that the given order of the gantt activities in one group(subtree) remains unchanged.
   * 
   * @param activities gantt activities translated to jsgantt xml data which will be visualized on the client
   */
  public GanttTaskXmlPage(final List<? extends IGanttActivity> activities)
  {
    super(new PageParameters());
    initXmlFor(activities);
  }

  /**
   * Needed to achieve that xml rather than xhtml is send to the browser.
   * 
   * @see org.apache.wicket.markup.html.WebPage#getMarkupType()
   */
  @Override
  public MarkupType getMarkupType() {
    return XML_MARKUP_TYPE;
  };

  /*
   // at the moment it is not needed to override this method, but this may change with future versions of wicket
  @Override
  protected void configureResponse(final WebResponse response) {
    super.configureResponse(response);
    response.setContentType("text/xml");
  }
   */

  /**
   * creates xml data of the given gantt activities (@see IGanttActivity) for jsgantt.
   * the tree structure of the gantt activities is automatically preorder sorted,
   * to assure the correct visualization of groups (subtrees).
   * This sorting is staple so that the given order of the gantt activities in one group(subtree) remains unchanged.
   * 
   * @param activityList gantt activities translated to jsgantt xml data
   */
  protected void initXmlFor(final List<? extends IGanttActivity> activityList) {

    // remember all ids to avoid dependencies not contained in the Set
    final Set<Integer> allActivityIds = new HashSet<Integer>(activityList.size());
    for (final Iterator<? extends IGanttActivity> activityIt = activityList.iterator(); activityIt.hasNext(); ) {
      if (!allActivityIds.add(activityIt.next().getId())) {
        activityIt.remove();
      };
    }

    final List<GanttActivityTreeNode> ganttTreeNodes = createGanttTreeModel(activityList);

    final RepeatingView taskRepeater = new RepeatingView("taskRepeater");

    add(taskRepeater);


    AbstractItem item;
    GanttActivityTreeNode ganttNode;
    IGanttActivity ganttActivity;
    for (final Iterator<GanttActivityTreeNode> ganttNodeIt = ganttTreeNodes.iterator(); ganttNodeIt.hasNext(); ) {

      ganttNode = ganttNodeIt.next();
      ganttActivity = ganttNode.value;

      item = new AbstractItem(taskRepeater.newChildId());
      taskRepeater.add(item);

      item.add(
          new Label("taskId",
              Integer.toString(ganttActivity.getId()))
      );
      item.add(
          new Label("taskName",
              ganttNode.value.getTitle())
      );
      item.add(
          new Label("caption",
              ganttNode.value.getWbsCode())
      );
      item.add(
          new Label("startDate",
              formatReadableDateTypeForJsGantt( ganttActivity.getBeginDate()))
      );
      item.add(
          new Label("endDate",
              formatReadableDateTypeForJsGantt( ganttActivity.getEndDate()))
      );
      item.add(
          new Label("backGroundColor", "0000ff")
      );
      item.add(
          new Label("taskLink", ganttActivity.getLinkUrl()!=null?ganttActivity.getLinkUrl():"")
      );
      item.add(
          new Label("isMileStone", ganttActivity.getVisualizationType() == GanttActivityVisualizationType.MILESTONE?"1":"0" )
      );
      item.add(
          new Label("resourceName", "")
      );
      item.add(
          new Label("progressPercentage",
              Integer.toString(ganttActivity.getProgress()))
      );
      item.add(
          new Label("isGroup",
              ganttNode.hasChildren()?"1":"0")
      );
      item.add(
          new Label("parentId",
              ganttNode.value.hasParent() && allActivityIds.contains(ganttActivity.getParentId()) ?
                  Integer.toString(ganttActivity.getParentId())
                  :  ""
          )
      );
      item.add(new Label("isExpanded", "1"));
      final StringBuilder dependencyStr = new StringBuilder();
      for (final Iterator<? extends IGanttDependency> dependencyIt = ganttNode.value.predecessorDependencyIterator(); dependencyIt.hasNext(); ) {
        final IGanttDependency dependency = dependencyIt.next();
        if (allActivityIds.contains(dependency.getPredecessorId())) {
          dependencyStr.append(dependency.getPredecessorId());
          dependencyStr.append(':');
          dependencyStr.append( formatDependencyTypeForJsGantt(dependency.getDependencyRelationType()));
          dependencyStr.append(',');
        }
      }
      item.add(new Label("dependsOn", dependencyStr.toString()));
    }
  }

  /**
   * converts a dependency relation type to a string used in jsgantt xml format
   * @param dependencyType type to convert
   * @return string for jsgantt xml data
   */
  private String formatDependencyTypeForJsGantt(final DependencyRelationType dependencyType) {
    switch (dependencyType) {
      case BEGIN_BEGIN: return "bb";
      case END_END:     return "ee";
      case BEGIN_END:   return "be";
      default :         return "eb";
    }

  }

  /**
   * converts a joda readable date time to a date string used in jsgantt xml data
   * @param date date to convert
   * @return string representation of date used in jsgantt xml data
   */
  private String formatReadableDateTypeForJsGantt(final ReadableDateTime date) {
    if (date==null)
      return "";
    return date.getDayOfMonth()+"/"+date.getMonthOfYear()+"/"+date.getYear();
  }


  /**
   * Helper Datastructure to sort ganttactivities by preorder and to gather information about the tree structure of activities
   * 
   * @author Sweeps <pf@byte-storm.com>
   */
  private static class GanttActivityTreeNode {
    IGanttActivity value;
    Set<GanttActivityTreeNode> children;
    public GanttActivityTreeNode (final IGanttActivity value) {
      this.value = value;
      children = new LinkedHashSet<GanttActivityTreeNode>();
    }
    public boolean addChild(final GanttActivityTreeNode child) {
      return children.add(child);
    }
    public boolean hasChildren() {
      return children.size() > 0;
    }
  }

  /**
   * creates a forest of GanttActivityTreeNodes for the given list of gantt activities
   * @param ganttActivities plain gantt activities
   * @return root nodes of the forest of preorder sorted trees of the given gantt activities
   */
  public static List<GanttActivityTreeNode> createGanttTreeModel(final List<? extends IGanttActivity> ganttActivities) {
    final List<GanttActivityTreeNode> resultList = new ArrayList<GanttActivityTreeNode>(ganttActivities.size());

    final Set<GanttActivityTreeNode> rootsSet = createGanttForest(ganttActivities);
    forrestPreOrder(rootsSet, resultList);

    return resultList;
  };


  /**
   * converts a list of gantt activities to a tree structure consisting of GanttActivityTreeNodes.
   * 
   * @param ganttActivities plain gantt activities
   * @return tree structure consisting of GanttActivityTreeNodes
   */
  protected static Set<GanttActivityTreeNode> createGanttForest(final List<? extends IGanttActivity> ganttActivities) {

    final Map<Integer, GanttActivityTreeNode> treeNodeMap = new LinkedHashMap<Integer, GanttActivityTreeNode>(ganttActivities.size());
    final Set<GanttActivityTreeNode> rootsSet = new LinkedHashSet<GanttActivityTreeNode>(ganttActivities.size());

    IGanttActivity ga;
    GanttActivityTreeNode gaNode, parentGaNode;
    // create roots for all gantt activities
    for (final Iterator<? extends IGanttActivity> gaIt = ganttActivities.iterator(); gaIt.hasNext(); ) {
      ga = gaIt.next();
      gaNode = new GanttActivityTreeNode(ga);
      treeNodeMap.put(ga.getId(), gaNode);
      rootsSet.add(gaNode);
    }

    // build forest
    for (final Iterator<GanttActivityTreeNode> gaNodeIt = treeNodeMap.values().iterator(); gaNodeIt.hasNext(); ) {
      gaNode = gaNodeIt.next();
      ga = gaNode.value;
      if (ga.hasParent() && treeNodeMap.containsKey(ga.getParentId())) {
        parentGaNode = treeNodeMap.get(ga.getParentId());
        parentGaNode.addChild(gaNode);
        rootsSet.remove(gaNode);
      }
    }

    return rootsSet;
  }


  /**
   * sorts all single trees in a forest of GanttActivityTreeNodes by preorder.
   * @param rootsSet set of all roots in the forest
   * @param list result list of preorder sorted GanttActivityTreeNodes
   */
  private static void forrestPreOrder(final Set<GanttActivityTreeNode> rootsSet, final List<GanttActivityTreeNode> list) {

    GanttActivityTreeNode gaNode;
    for (final Iterator<GanttActivityTreeNode> gaNodeIt = rootsSet.iterator(); gaNodeIt.hasNext(); ) {
      gaNode = gaNodeIt.next();
      preOrder(gaNode, list);
    }
  }

  /**
   * sorts a tree of GanttActivityTreeNodes by preorder.
   * @param node root node of a tree structure consisting of GanttActivityTreeNodes
   * @param list result list of preorder sorted GanttActivityTreeNodes
   */
  private static void preOrder(final GanttActivityTreeNode node, final List<GanttActivityTreeNode> list) {
    list.add(node);
    for (final Iterator<GanttActivityTreeNode> childIt=node.children.iterator(); childIt.hasNext(); ) {
      preOrder(childIt.next(), list);
    }
  }

}
