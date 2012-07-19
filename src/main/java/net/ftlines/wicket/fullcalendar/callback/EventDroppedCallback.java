/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package net.ftlines.wicket.fullcalendar.callback;

import net.ftlines.wicket.fullcalendar.CalendarResponse;
import net.ftlines.wicket.fullcalendar.Event;
import net.ftlines.wicket.fullcalendar.EventSource;
import net.ftlines.wicket.fullcalendar.EventSourceNotFoundException;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.Request;

public abstract class EventDroppedCallback extends AbstractAjaxCallbackWithClientsideRevert implements CallbackWithHandler
{
  private static final long serialVersionUID = 9220878749378414280L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EventDroppedCallback.class);

  private static final String CALLBACK_PRE_SCRIPT = "var triggerAjaxEvent = function (which) { ";

  private static final String MOVE_SAVE = CalendarDropMode.MOVE_SAVE.getI18nKey();
  private static final String MOVE_EDIT = CalendarDropMode.MOVE_EDIT.getI18nKey();
  private static final String COPY_SAVE = CalendarDropMode.COPY_SAVE.getI18nKey();
  private static final String COPY_EDIT = CalendarDropMode.COPY_EDIT.getI18nKey();

  private static final String CALLBACK_POST_SCRIPT = "}; $.contextMenu.create("
      + "["
      + "{ '" + MOVE_SAVE + "' : function(menuItem,menu) { triggerAjaxEvent('" + CalendarDropMode.MOVE_SAVE.getAjaxTarget() + "'); } },"
      + "{ '" + COPY_SAVE + "' : function(menuItem,menu) { triggerAjaxEvent('" + CalendarDropMode.COPY_SAVE.getAjaxTarget() + "'); } },"
      + "$.contextMenu.separator,"
      + "{ '" + MOVE_EDIT + "' : function(menuItem,menu) { triggerAjaxEvent('" + CalendarDropMode.MOVE_EDIT.getAjaxTarget() + "'); } },"
      + "{ '" + COPY_EDIT + "' : function(menuItem,menu) { triggerAjaxEvent('" + CalendarDropMode.COPY_EDIT.getAjaxTarget() + "'); } }"
      + "],"
      + "{hideCallback: function () {this.menu.remove(); revertFunc(); console.log(revertFunc);} }"
      + ").show(this, originalEvent);";

  @Override
  protected String configureCallbackScript(final String script, final String urlTail)
  {

    return CALLBACK_PRE_SCRIPT
        + script.replace(urlTail, "&eventId='+event.id+'&sourceId='+event.source.data."
            + EventSource.Const.UUID
            + "+'&dayDelta='+dayDelta+'&minuteDelta='+minuteDelta+'&allDay='+allDay+'&which='+which+'")
            + i18nCallbackScript(CALLBACK_POST_SCRIPT);
  }

  /**
   * @param callbackPostScript
   * @return
   */
  private String i18nCallbackScript(final String callbackPostScript)
  {
    String result = callbackPostScript;
    final Component component = getComponent();
    result = result.replace(MOVE_SAVE, component.getString(MOVE_SAVE));
    result = result.replace(MOVE_EDIT, component.getString(MOVE_EDIT));
    result = result.replace(COPY_SAVE, component.getString(COPY_SAVE));
    result = result.replace(COPY_EDIT, component.getString(COPY_EDIT));
    return result;
  }

  public IModel<String> getHandlerScript()
  {
    return new AbstractReadOnlyModel<String>() {
      private static final long serialVersionUID = -3975663195244168222L;

      @Override
      public String getObject()
      {
        return "function(event, dayDelta, minuteDelta, allDay, revertFunc, originalEvent) { " + getCallbackScript() + "}";
      }
    };
  }

  @Override
  protected boolean onEvent(final AjaxRequestTarget target)
  {
    try {
      final Request r = getCalendar().getRequest();
      final String eventId = r.getRequestParameters().getParameterValue("eventId").toString();
      final String sourceId = r.getRequestParameters().getParameterValue("sourceId").toString();

      final EventSource source = getCalendar().getEventManager().getEventSource(sourceId);
      final Event event = source.getEventProvider().getEventForId(eventId);

      final int dayDelta = r.getRequestParameters().getParameterValue("dayDelta").toInt();
      final int minuteDelta = r.getRequestParameters().getParameterValue("minuteDelta").toInt();
      final boolean allDay = r.getRequestParameters().getParameterValue("allDay").toBoolean();

      return onEventDropped(new DroppedEvent(source, event, dayDelta, minuteDelta, allDay), new CalendarResponse(getCalendar(), target));
    } catch (final EventSourceNotFoundException ex) {
      // Happens normally after session time out. Do nothing.
      log.info("Exception after session time out? " + ex.getMessage());
      return false;
    }
  }

  protected abstract boolean onEventDropped(DroppedEvent event, CalendarResponse response);

  @Override
  protected String getRevertScript()
  {
    return "revertFunc();";
  }

}
