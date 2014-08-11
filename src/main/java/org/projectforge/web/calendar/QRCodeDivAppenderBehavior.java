package org.projectforge.web.calendar;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.border.BorderBehavior;

/**
 * Appends a div html element to an existing panel.
 * 
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 */
public class QRCodeDivAppenderBehavior extends BorderBehavior
{

  @Override
  public void bind(Component component)
  {
    super.bind(component);
    component.setOutputMarkupId(true);
  }

  @Override
  public void renderHead(Component component, IHeaderResponse response)
  {
    super.renderHead(component, response);
    response.render(JavaScriptHeaderItem.forUrl("scripts/qrcode.js"));
    response.render(JavaScriptHeaderItem.forScript("$(function() {\n" +
        "var urlComponent = $('#" + component.getMarkupId() + "');\n" +
          "if (urlComponent.val() != undefined && urlComponent.val().length > 0) {\n" +
            "var qrCode = new QRCode(urlComponent.siblings('.pf_qrcode')[0], {width: 250, height: 250});\n" +
            "qrCode.makeCode(urlComponent.val());\n" +
          "}\n" +
        "});", "qrCode" + System.currentTimeMillis()));
  }
}
