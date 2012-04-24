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

package net.ftlines.wicket.fullcalendar;

import java.util.UUID;

import net.ftlines.wicket.fullcalendar.callback.AjaxConcurrency;
import net.ftlines.wicket.fullcalendar.callback.ClickedEvent;
import net.ftlines.wicket.fullcalendar.callback.DateRangeSelectedCallback;
import net.ftlines.wicket.fullcalendar.callback.DroppedEvent;
import net.ftlines.wicket.fullcalendar.callback.EventClickedCallback;
import net.ftlines.wicket.fullcalendar.callback.EventDroppedCallback;
import net.ftlines.wicket.fullcalendar.callback.EventResizedCallback;
import net.ftlines.wicket.fullcalendar.callback.GetEventsCallback;
import net.ftlines.wicket.fullcalendar.callback.ResizedEvent;
import net.ftlines.wicket.fullcalendar.callback.SelectedRange;
import net.ftlines.wicket.fullcalendar.callback.View;
import net.ftlines.wicket.fullcalendar.callback.ViewDisplayCallback;

import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.template.PackageTextTemplate;
import org.apache.wicket.util.template.TextTemplate;

public class FullCalendar extends AbstractFullCalendar
{
	private static final TextTemplate EVENTS = new PackageTextTemplate(FullCalendar.class, "FullCalendar.events.tpl");

	private final Config config;
	private EventDroppedCallback eventDropped;
	private EventResizedCallback eventResized;
	private GetEventsCallback getEvents;
	private DateRangeSelectedCallback dateRangeSelected;
	private EventClickedCallback eventClicked;
	private ViewDisplayCallback viewDisplay;

	public FullCalendar(String id, Config config)
	{
		super(id);
		this.config = config;
		setVersioned(false);
	}

	public Config getConfig()
	{
		return config;
	}

	public EventManager getEventManager()
	{
		return new EventManager(this);
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		for (EventSource source : config.getEventSources())
		{
			String uuid = UUID.randomUUID().toString().replaceAll("[^A-Za-z0-9]", "");
			source.setUuid(uuid);
		}
		setupCallbacks();
	}

	private void setupCallbacks()
	{
		if (getEvents != null)
			return;

		getEvents = new GetEventsCallback();
		add(getEvents);
		for (final EventSource source : config.getEventSources())
		{
			source.setEventsModel(new AbstractReadOnlyModel<String>() {
				@Override
				public String getObject() {
					return EVENTS.asString(new MicroMap("url", getEvents.getUrl(source)));
				}
			});
		}

		if (Strings.isEmpty(config.getEventClick()))
		{
			add(eventClicked = new EventClickedCallback()
			{
				@Override
				protected void onClicked(ClickedEvent event, CalendarResponse response)
				{
					onEventClicked(event, response);
				}
			});
			config.setEventClickModel(eventClicked.getHandlerScript());
		}

		if (Strings.isEmpty(config.getSelect()))
		{
			add(dateRangeSelected = new DateRangeSelectedCallback(config.isIgnoreTimezone())
			{
				@Override
				protected void onSelect(SelectedRange range, CalendarResponse response)
				{
					FullCalendar.this.onDateRangeSelected(range, response);
				}
			});
			config.setSelectModel(dateRangeSelected.getHandlerScript());
		}

		if (Strings.isEmpty(config.getEventDrop()))
		{
			add(eventDropped = new EventDroppedCallback()
			{

				@Override
				protected boolean onEventDropped(DroppedEvent event, CalendarResponse response)
				{
					return FullCalendar.this.onEventDropped(event, response);
				}
			});
			config.setEventDropModel(eventDropped.getHandlerScript());
		}

		if (Strings.isEmpty(config.getEventResize()))
		{
			add(eventResized = new EventResizedCallback()
			{

				@Override
				protected boolean onEventResized(ResizedEvent event, CalendarResponse response)
				{
					return FullCalendar.this.onEventResized(event, response);
				}

			});

			config.setEventResizeModel(eventResized.getHandlerScript());
		}

		if (Strings.isEmpty(config.getViewDisplay()))
		{
			add(viewDisplay = new ViewDisplayCallback()
			{
				@Override
				protected void onViewDisplayed(View view, CalendarResponse response)
				{
					FullCalendar.this.onViewDisplayed(view, response);
				}
			});
			config.setViewDisplayModel(viewDisplay.getHandlerScript());
		}

		getPage().dirty();
	}


	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		String configuration = "$(\"#" + getMarkupId() + "\").fullCalendarExt(";
		configuration += Json.toJson(config);
		configuration += ");";

		response.renderOnDomReadyJavaScript(configuration);
	}

	protected boolean onEventDropped(DroppedEvent event, CalendarResponse response)
	{
		return false;
	}

	protected boolean onEventResized(ResizedEvent event, CalendarResponse response)
	{
		return false;
	}

	protected void onDateRangeSelected(SelectedRange range, CalendarResponse response)
	{

	}

	protected void onEventClicked(ClickedEvent event, CalendarResponse response)
	{

	}

	protected void onViewDisplayed(View view, CalendarResponse response)
	{

	}

	public AjaxConcurrency getAjaxConcurrency()
	{
		return AjaxConcurrency.QUEUE;
	}
}
