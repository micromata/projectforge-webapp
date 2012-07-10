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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.Request;

public abstract class EventDroppedCallback extends AbstractAjaxCallbackWithClientsideRevert implements CallbackWithHandler
{
  private static final long serialVersionUID = 9220878749378414280L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EventDroppedCallback.class);

  private static final String CALLBACK_PRE_SCRIPT = "var triggerAjaxEvent = function (which) { ";

  private static final String CALLBACK_POST_SCRIPT = "}; $.contextMenu.create("
      + "["
      + "{ '${calendar.dd.move.save}' : function(menuItem,menu) { triggerAjaxEvent('MoveSave'); } },"
      + "{'${calendar.dd.copy.save}' : function(menuItem,menu) { triggerAjaxEvent('CopySave'); } },"
      + "$.contextMenu.separator,"
      + "{'${calendar.dd.move.edit}' : function(menuItem,menu) { triggerAjaxEvent('MoveEdit'); } },"
      + "{'${calendar.dd.copy.edit}' : function(menuItem,menu) { triggerAjaxEvent('CopyEdit'); } }"
      + "],"
      + "{hideCallback: function () {this.menu.remove(); revertFunc();} }"
      + ").show(this, originalEvent);";

  @Override
  protected String configureCallbackScript(final String script, final String urlTail)
  {
    return CALLBACK_PRE_SCRIPT
        + script.replace(urlTail, "&eventId='+event.id+'&sourceId='+event.source.data."
            + EventSource.Const.UUID
            + "+'&dayDelta='+dayDelta+'&minuteDelta='+minuteDelta+'&allDay='+allDay+'&which='+which+'")
            + CALLBACK_POST_SCRIPT;
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
