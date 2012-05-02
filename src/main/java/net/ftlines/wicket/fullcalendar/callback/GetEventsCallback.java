/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ftlines.wicket.fullcalendar.callback;

import net.ftlines.wicket.fullcalendar.EventProvider;
import net.ftlines.wicket.fullcalendar.EventSource;

import org.apache.wicket.request.Request;
import org.apache.wicket.request.handler.TextRequestHandler;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.StringValue;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class GetEventsCallback extends AbstractCallback
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GetEventsCallback.class);

  private static final String SOURCE_ID = "sid";

  public String getUrl(final EventSource source)
  {
    return getUrl(new MicroMap<String, Object>(SOURCE_ID, source.getUuid()));
  }

  @Override
  protected void respond()
  {
    try {
      final Request r = getCalendar().getRequest();
      final String sid = r.getRequestParameters().getParameterValue(SOURCE_ID).toString();
      DateTime start = parseDateTime(r, "start");
      DateTime end = parseDateTime(r, "end");
      if (getCalendar().getConfig().isIgnoreTimezone()) {
        // Convert to same DateTime in local time zone.
        final int remoteOffset = -r.getRequestParameters().getParameterValue("timezoneOffset").toInt();
        final int localOffset = DateTimeZone.getDefault().getOffset(null) / 60000;
        final int minutesAdjustment = remoteOffset - localOffset;
        start = start.plusMinutes(minutesAdjustment);
        end = end.plusMinutes(minutesAdjustment);
      }
      final EventSource source = getCalendar().getEventManager().getEventSource(sid);
      final EventProvider provider = source.getEventProvider();
      final String response = getCalendar().toJson(provider.getEvents(start, end));
      getCalendar().getRequestCycle().scheduleRequestHandlerAfterCurrent(new TextRequestHandler(response));
    } catch (final Exception ex) {
      // Happens normally after session time out. Do nothing.
      log.info("Exception after session time out? " + ex.getMessage());
    }
  }

  private DateTime parseDateTime(final Request r, final String param) {
    final StringValue sval = r.getRequestParameters().getParameterValue(param);
    if (sval.toString("").contains("-") == true) {
      return new DateTime(sval.toString());
    } else {
      return new DateTime(sval.toLong());
    }
  }
}
