/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.poll.result;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class SideWaysPanel extends Panel
{
  private static final long serialVersionUID = -1790378825815834050L;

  private RepeatingView rowRepeaters;

  // private final WebMarkupContainer additionalContent;
  private final RepeatingView columnRepeater;

  private WebMarkupContainer rowContainer;

  private int column;

  private int row;

  private final int rows;

  private final int columns;

  /**
   * @param id
   * @param allEvents
   */
  public SideWaysPanel(String id, int columns, int rows)
  {
    super(id);

    column = 0;
    row = 1;
    this.columns = columns;
    this.rows = rows;

    WebMarkupContainer columnContainer = new WebMarkupContainer("elementContainer");

    columnRepeater = new RepeatingView("columnContainer");

    columnContainer.add(columnRepeater);
    add(columnContainer);

    newColumn();

  }

  private void newColumn()
  {
    rowContainer = new WebMarkupContainer(columnRepeater.newChildId());
    columnRepeater.add(rowContainer);

    rowRepeaters = new RepeatingView("rowRepeater");
    rowContainer.add(rowRepeaters);
  }

  public void addLabels(String startDate, String endDate)
  {
    if (column != columns) {
      if (row == rows) {
        row = 1;
        column++;
        newColumn();
      }

      rowRepeaters.add(addContainer(rowRepeaters.newChildId(), startDate, endDate));
      row++;
    }
  }

  private WebMarkupContainer addContainer(String id, String startDate, String endDate)
  {
    WebMarkupContainer container = new WebMarkupContainer(id);
    container.add(new Label("startDate", startDate));
    container.add(new Label("endDate", endDate));
    return container;
  }

  public void addIndividual(WebMarkupContainer container)
  {
    // additionalContent.add(container);
  }
}
