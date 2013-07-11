/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.components;

import org.apache.commons.lang.Validate;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;

/**
 * A panel with a modal select window for selecting a wbs node.
 * It mainly consists of a form field with a label that contains a node path, e.g. Project -> Subtask -> Workpackage
 * and a link to change the wbs node.
 * @author Sweeps <pf@byte-storm.com>
 */
public class WbsNodeSelectPanel extends FormComponentPanel<AbstractWbsNodeDO>
{
  private static final long serialVersionUID = 8517756771791956045L;
  private ModalWindow modalSelect;
  private Label selectedWbsNodeLabel;
  private final IModel< ? extends AbstractWbsNodeDO> target;
  private final String label;
  private WbsNodeModalSelectWindowPanel modalPanel;

  /**
   * Constructor for a wbs node select panel form field with parent-child validity check
   * @param id wicket id
   * @param wbsModel model of the currently selected wbs node
   * @param targetModel model of the target node for which this select window selects the wbs node for
   * @param label Label for the form field
   */
  public WbsNodeSelectPanel(final String id, final IModel<AbstractWbsNodeDO> wbsModel, final IModel<? extends AbstractWbsNodeDO> targetModel, final String label) {
    super(id, wbsModel);
    Validate.notNull(wbsModel);
    Validate.notNull(targetModel);
    Validate.notNull(targetModel.getObject());
    this.target = targetModel;
    this.label = label;

    init();
  }

  /**
   * Constructor for a wbs node select panel form field without parent-child validity check
   * @param id wicket id
   * @param wbsModel model of the currently selected wbs node
   * @param label Label for the form field
   */
  public WbsNodeSelectPanel(final String id, final IModel<AbstractWbsNodeDO> wbsModel, final String label) {
    super(id, wbsModel);
    Validate.notNull(wbsModel);
    this.target = null;
    this.label = label;

    init();
  }

  /**
   * @return the WbsNodeModalSelectWindowPanel backing node selection
   */
  public WbsNodeModalSelectWindowPanel getModalWindowPanel()
  {
    return modalPanel;
  }

  private void init()
  {
    addFormLabels();
    addModalSelectWindow();
    addOpenModalWindowLink();
  }

  private void addFormLabels()
  {
    selectedWbsNodeLabel = new Label("selected_node", new Model<String>(null){
      private static final long serialVersionUID = 1L;

      @Override
      public String getObject()
      {
        return getWbsNodePathString();
      }
    });
    selectedWbsNodeLabel.setOutputMarkupId(true);
    add(selectedWbsNodeLabel);

    final WebMarkupContainer labelTag = new WebMarkupContainer("selectionLabel");
    labelTag.add(AttributeModifier.replace("for", selectedWbsNodeLabel.getMarkupId()));
    labelTag.add(new Label("field_label", new Model<String>(label)));
    add(labelTag);
  }

  private void addModalSelectWindow()
  {
    modalSelect = new ModalWindow("modal");
    modalPanel = new WbsNodeModalSelectWindowPanel(modalSelect.getContentId(), getModel(), target, modalSelect);
    modalSelect.setCookieName("modalSelectWindow");
    modalSelect.setContent(modalPanel);
    modalSelect.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
      private static final long serialVersionUID = 1L;
      @Override
      public void onClose(final AjaxRequestTarget target)
      {
        // modal window closed, update the label on the form
        target.add(selectedWbsNodeLabel);
        onModalWindowClosed(getModel(), target);
      }
    });
    add(modalSelect);
  }

  /**
   * Can be overridden to perform actions after the modal window has been closed.
   * @param model Model of the currently selected wbs node
   * @param target Target to perform Ajax Updates
   */
  protected void onModalWindowClosed(final IModel<AbstractWbsNodeDO> model, final AjaxRequestTarget target) {

  }

  private void addOpenModalWindowLink()
  {
    add(new AjaxLink<Void>("open_modal") {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(final AjaxRequestTarget target)
      {
        modalSelect.show(target);
      }

    });
  }

  protected String getWbsNodePathString()
  {
    final StringBuilder sb = new StringBuilder();
    AbstractWbsNodeDO node = getModelObject();
    if (node == null)
      return null;
    sb.insert(0, node.getTitle());
    while (node.getParent() != null) {
      node = node.getParent();
      sb.insert(0, node.getTitle()+" > ");
    }
    return sb.toString();
  }

  @Override
  protected void convertInput()
  {
    setConvertedInput(getModelObject());
  }
}
