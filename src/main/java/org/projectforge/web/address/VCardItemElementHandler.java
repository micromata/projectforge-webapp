/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.address;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.vcard.Group;
import net.fortuna.ical4j.vcard.Parameter;
import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.Property.Id;

import org.apache.commons.lang.StringUtils;

/**
 * Handle vCard item entries.<br />
 * {@link net.fortuna.ical4j.vcard} is not possible to handle item elements.
 * 
 * @author Maximilian Lauterbach (m.lauterbach@micromata.de)
 *
 */
public class VCardItemElementHandler
{
  private final ArrayList<Property> itemList;

  public VCardItemElementHandler(final FileInputStream fis){
    final DataInputStream in = new DataInputStream(fis);
    final BufferedReader br = new BufferedReader(new InputStreamReader(in));
    itemList = new ArrayList<Property>();

    //Read File Line By Line
    try {
      String strLine;
      while ((strLine = br.readLine()) != null)   {
        // looking for a item entry
        if (strLine.startsWith("item") && !strLine.contains("X-AB")) {

          // dissect the line by char
          final String str[] = StringUtils.splitByCharacterType(strLine);

          /*
           * ignore "item" + "number" + "." (example: "item2.") cause is not needed.
           * at index = 3 is the GroupId
           */
          final int n = 3;

          // set Property.Id
          final Id id = getItemId(str[n]);

          final ArrayList<Parameter> param = new ArrayList<Parameter>();

          boolean startSignFound = false;

          String valueCache = "";
          for (int i = n; i < str.length; i++){
            // looking for parameters
            if (str[i].equals("WORK") || str[i].equals("HOME")){
              param.add(getParameter(str[i]));
            }

            /*
             * looking for start sign.
             * usually ":" but sometimes addresses starts with ":;;"
             */
            if (str[i].equals(":;;") || str[i].equals(":") || str[i].equals(":;") && !startSignFound){
              startSignFound = true;
            } else
              if (startSignFound) {
                // terminate unwanted signs.
                if(str[i].equals(";") || str[i].equals(";;") || str[i].equals(".;"))
                  valueCache = valueCache + ";";
                else
                  valueCache = valueCache + str[i];
              }
          }

          final String finalValue = valueCache;
          // set property with group at index = 3
          @SuppressWarnings("serial")
          final Property property = new Property(new Group(str[n]), id, param) {
            @Override
            public void validate() throws ValidationException
            {
            }

            @Override
            public String getValue()
            {
              return finalValue;
            }
          };
          itemList.add(property);

        }
      }
      //      for (final Property p : itemList){
      //        System.out.println("propterty val: " + p.getValue() + " ;;; id: " + p.getId() + " ;;; parameter: " + p.getParameters(Parameter.Id.TYPE));
      //      }

      in.close();
    } catch (final IOException ex) {
      //      log.fatal("Exception encountered " + ex, ex);
    }
  }

  /**
   * Get list of "item" elements as {@link net.fortuna.ical4j.vcard.Property}
   * 
   * @return ArrayList<Property>
   */
  public ArrayList<Property> getItemList(){
    return itemList;
  }

  /**
   * @param parameter
   * @return
   */
  @SuppressWarnings("serial")
  private Parameter getParameter(final String param)
  {
    return new Parameter(Parameter.Id.TYPE) {

      @Override
      public String getValue()
      {
        return param;
      }
    };
  }

  /**
   * @param string
   * @return
   */
  private Id getItemId(final String string)
  {
    Id found = null;
    for (final Id id : Id.values())
      if(id.toString().equals(string)){
        found = id;
        break;
      }
    return found;
  }
}

