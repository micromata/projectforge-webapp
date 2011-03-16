/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.fibu;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.core.CurrencyFormatter;
import org.projectforge.core.NumberFormatter;
import org.projectforge.fibu.AuftragDO;
import org.projectforge.fibu.AuftragDao;
import org.projectforge.fibu.AuftragsPositionDO;
import org.projectforge.fibu.AuftragsStatus;
import org.projectforge.fibu.RechnungCache;
import org.projectforge.fibu.RechnungsPositionVO;
import org.projectforge.web.HtmlHelper;
import org.projectforge.web.common.OutputType;
import org.projectforge.web.task.TaskFormatter;
import org.projectforge.web.user.UserFormatter;
import org.projectforge.web.user.UserPropertyColumn;
import org.projectforge.web.wicket.AbstractBasePage;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.AttributeAppendModifier;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.CurrencyPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.IListPageColumnsCreator;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;
import org.projectforge.web.wicket.WicketLocalizerAndUrlBuilder;

@ListPage(editPage = AuftragEditPage.class)
public class AuftragListPage extends AbstractListPage<AuftragListForm, AuftragDao, AuftragDO> implements IListPageColumnsCreator<AuftragDO>
{
  private static final long serialVersionUID = -8406452960003792763L;

  protected static final String[] BOOKMARKABLE_PROPERTIES = mergeStringArrays(BOOKMARKABLE_FILTER_PROPERTIES, new String[] { "year|y",
      "listType|lt", "auftragsPositionsArt|art"});

  @SpringBean(name = "auftragDao")
  private AuftragDao auftragDao;

  @SpringBean(name = "userFormatter")
  private UserFormatter userFormatter;

  @SpringBean(name = "rechnungCache")
  private RechnungCache rechnungCache;

  @SpringBean(name = "taskFormatter")
  private TaskFormatter taskFormatter;

  @SpringBean(name = "htmlHelper")
  private HtmlHelper htmlHelper;

  public AuftragListPage(PageParameters parameters)
  {
    super(parameters, "fibu.auftrag");
  }

  @SuppressWarnings("serial")
  @Override
  public List<IColumn<AuftragDO>> createColumns(final WebPage returnToPage, final boolean sortable)
  {
    final List<IColumn<AuftragDO>> columns = new ArrayList<IColumn<AuftragDO>>();
    final CellItemListener<AuftragDO> cellItemListener = new CellItemListener<AuftragDO>() {
      public void populateItem(Item<ICellPopulator<AuftragDO>> item, String componentId, IModel<AuftragDO> rowModel)
      {
        final AuftragDO auftrag = rowModel.getObject();
        if (auftrag.getAuftragsStatus() == null) {
          // Should not occur:
          return;
        }
        final boolean isDeleted = auftrag.isDeleted() == true
            || auftrag.getAuftragsStatus().isIn(AuftragsStatus.ABGELEHNT, AuftragsStatus.ERSETZT) == true;
        final StringBuffer cssStyle = getCssStyle(auftrag.getId(), isDeleted);
        if (isDeleted) {
          // Do nothing further.
        } else if (auftrag.isAbgeschlossenUndNichtVollstaendigFakturiert() == true) {
          cssStyle.append("font-weight:bold; color: red;");
        } else if (auftrag.getAuftragsStatus().isIn(AuftragsStatus.BEAUFTRAGT, AuftragsStatus.LOI) == true) {
          cssStyle.append("color: green;");
        } else if (auftrag.getAuftragsStatus().isIn(AuftragsStatus.ESKALATION) == true) {
          cssStyle.append("font-weight:bold; color: red;");
        }
        if (cssStyle.length() > 0) {
          item.add(new AttributeModifier("style", true, new Model<String>(cssStyle.toString())));
        }
      }
    };
    columns.add(new CellItemListenerPropertyColumn<AuftragDO>(new Model<String>(getString("fibu.auftrag.nummer.short")), "nummer",
        "nummer", cellItemListener) {
      @SuppressWarnings("unchecked")
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        AuftragDO auftrag = (AuftragDO) rowModel.getObject();
        item.add(new ListSelectActionPanel(componentId, rowModel, AuftragEditPage.class, auftrag.getId(), AuftragListPage.this, String
            .valueOf(auftrag.getNummer())));
        cellItemListener.populateItem(item, componentId, rowModel);
        addRowClick(item);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<AuftragDO>(getString("fibu.kunde"), "kundeAsString", "kundeAsString", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<AuftragDO>(getString("fibu.projekt"), "projekt.name", "projekt.name", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<AuftragDO>(getString("fibu.auftrag.titel"), "titel", "titel", cellItemListener));
    columns.add(new AbstractColumn<AuftragDO>(new Model<String>(getString("fibu.auftrag.positions"))) {
      public void populateItem(Item<ICellPopulator<AuftragDO>> cellItem, String componentId, IModel<AuftragDO> rowModel)
      {
        final AuftragDO auftrag = rowModel.getObject();
        final List<AuftragsPositionDO> list = auftrag.getPositionen();
        final StringBuffer buf = new StringBuffer();
        if (list != null) {
          buf.append("<span style=\"font-style: italic;\" title=\"").append(NumberFormatter.format(auftrag.getPersonDays())).append(" ")
              .append(getString("projectmanagement.personDays.short")).append(" - ");
          final Iterator<AuftragsPositionDO> it = list.iterator();
          while (it.hasNext() == true) {
            final AuftragsPositionDO pos = it.next();
            buf.append("#").append(pos.getNumber()).append(": ");
            if (pos.getPersonDays() != null && pos.getPersonDays().compareTo(BigDecimal.ZERO) != 0) {
              buf.append("(").append(NumberFormatter.format(pos.getPersonDays())).append(" ").append(
                  getString("projectmanagement.personDays.short")).append(") ");
            }
            buf.append(CurrencyFormatter.format(pos.getNettoSumme()));
            if (StringUtils.isNotBlank(pos.getTitel()) == true) {
              buf.append(": ").append(HtmlHelper.escapeXml(pos.getTitel()));
            }
            buf.append(": ");
            if (pos.getTaskId() != null) {
              buf.append(taskFormatter.getTaskPath(pos.getTaskId(), true, OutputType.HTML));
            } else {
              buf.append(getString("fibu.auftrag.position.noTaskGiven"));
            }
            if (pos.getStatus() != null) {
              buf.append(", ").append(getString(pos.getStatus().getI18nKey()));
            }
            if (it.hasNext() == true) {
              buf.append("<br/>");
            }
          }
          buf.append("\">");
          htmlHelper.appendImageTag(new WicketLocalizerAndUrlBuilder(getResponse()), buf, htmlHelper.getInfoImage());
          buf.append("#").append(list.size());
          buf.append("</span>");
        }
        final Label label = new Label(componentId, new Model<String>(buf.toString()));
        label.setEscapeModelStrings(false);
        cellItem.add(label);
        cellItemListener.populateItem(cellItem, componentId, rowModel);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<AuftragDO>(getString("projectmanagement.personDays.short"), "personDays", "personDays",
        cellItemListener) {
      @Override
      public void populateItem(Item<ICellPopulator<AuftragDO>> item, String componentId, IModel<AuftragDO> rowModel)
      {
        item.add(new Label(componentId, NumberFormatter.format(rowModel.getObject().getPersonDays())));
        item.add(new AttributeAppendModifier("style", new Model<String>("text-align: right;")));
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    columns
        .add(new CellItemListenerPropertyColumn<AuftragDO>(getString("fibu.common.reference"), "referenz", "referenz", cellItemListener));
    columns.add(new UserPropertyColumn<AuftragDO>(getString("contactPerson"), "contactPerson.fullname", "contactPerson", cellItemListener)
        .withUserFormatter(userFormatter));
    columns.add(new CellItemListenerPropertyColumn<AuftragDO>(getString("fibu.auftrag.datum"), "angebotsDatum", "angebotsDatum",
        cellItemListener));
    // columns
    // .add(new CellItemListenerPropertyColumn<AuftragDO>(new Model<String>(getString("fibu.auftrag.bindungsFrist")), "bindungsFrist",
    // "bindungsFrist", cellItemListener));
    columns.add(new CurrencyPropertyColumn<AuftragDO>(getString("fibu.auftrag.nettoSumme"), "nettoSumme", "nettoSumme", cellItemListener));
    columns.add(new CurrencyPropertyColumn<AuftragDO>(getString("fibu.fakturiert"), "fakturiertSum", "fakturiertSum", cellItemListener)
        .setSuppressZeroValues(true));
    columns
        .add(new CellItemListenerPropertyColumn<AuftragDO>(new Model<String>(getString("fibu.rechnungen")), null, null, cellItemListener) {
          @Override
          public void populateItem(Item<ICellPopulator<AuftragDO>> item, String componentId, IModel<AuftragDO> rowModel)
          {
            final AuftragDO auftrag = rowModel.getObject();
            final Set<RechnungsPositionVO> orderPositions = rechnungCache.getRechnungsPositionVOSetByAuftragId(auftrag.getId());
            if (CollectionUtils.isEmpty(orderPositions) == true) {
              item.add(AbstractBasePage.createInvisibleDummyComponent(componentId));
            } else {
              final InvoicePositionsPanel panel = new InvoicePositionsPanel(componentId) {
                protected void onBeforeRender()
                {
                  super.onBeforeRender();
                  init(orderPositions);
                };
              };
              item.add(panel);
            }
            cellItemListener.populateItem(item, componentId, rowModel);
          }
        });
    columns.add(new CellItemListenerPropertyColumn<AuftragDO>(new Model<String>(getString("status")), "auftragsStatusAsString",
        "auftragsStatusAsString", cellItemListener));
    return columns;
  }

  @Override
  protected void init()
  {
    dataTable = createDataTable(createColumns(this, true), "nummer", false);
    form.add(dataTable);
  }

  @Override
  public void refresh()
  {
    super.refresh();
    form.refresh();
  }

  @Override
  protected AuftragListForm newListForm(AbstractListPage< ? , ? , ? > parentPage)
  {
    return new AuftragListForm(this);
  }

  @Override
  protected AuftragDao getBaseDao()
  {
    return auftragDao;
  }

  @Override
  public List<AuftragDO> getList()
  {
    if (list == null) {
      list = super.getList();
      auftragDao.calculateInvoicedSum(list);
    }
    return list;
  }

  @Override
  protected IModel<AuftragDO> getModel(AuftragDO object)
  {
    return new DetachableDOModel<AuftragDO, AuftragDao>(object, getBaseDao());
  }

  @Override
  protected String[] getBookmarkableFilterProperties()
  {
    return BOOKMARKABLE_PROPERTIES;
  }
}
