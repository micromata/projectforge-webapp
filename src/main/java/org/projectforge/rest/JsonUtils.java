/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.rest;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

/**
 * Serialization and deserialization for rest calls.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class JsonUtils
{
  public static String toJson(final Object obj)
  {
    return createGson().toJson(obj);
  }

  public static <T> T fromJson(final String json, final Class<T> classOfT) throws JsonSyntaxException
  {
    return createGson().fromJson(json, classOfT);

  }

  public static <T> T fromJson(final String json, final Type typeOfT) throws JsonSyntaxException
  {
    return createGson().fromJson(json, typeOfT);
  }

  private static Gson createGson()
  {
    return new GsonBuilder().registerTypeAdapter(Date.class, new UTCDateTypeAdapter()).create();
  }

  private static class UTCDateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date>
  {
    private final DateFormat dateFormat;

    private UTCDateTypeAdapter()
    {
      dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
      dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public synchronized JsonElement serialize(final Date date, final Type type, final JsonSerializationContext jsonSerializationContext)
    {
      synchronized (dateFormat) {
        final String dateFormatAsString = dateFormat.format(date);
        return new JsonPrimitive(dateFormatAsString);
      }
    }

    @Override
    public synchronized Date deserialize(final JsonElement jsonElement, final Type type,
        final JsonDeserializationContext jsonDeserializationContext)
    {
      try {
        synchronized (dateFormat) {
          return dateFormat.parse(jsonElement.getAsString());
        }
      } catch (final ParseException e) {
        throw new JsonSyntaxException(jsonElement.getAsString(), e);
      }
    }
  }
}
