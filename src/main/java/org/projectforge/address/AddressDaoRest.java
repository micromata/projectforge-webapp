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

package org.projectforge.address;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.projectforge.registry.Registry;
import org.projectforge.rest.JsonUtils;
import org.projectforge.rest.RestPaths;
import org.projectforge.rest.objects.AddressObject;
import org.projectforge.web.rest.converter.AddressDOConverter;

/**
 * REST-Schnittstelle für {@link AddressDao}
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@Path(RestPaths.ADDRESS)
public class AddressDaoRest
{
  private final AddressDao addressDao;

  public AddressDaoRest()
  {
    this.addressDao = Registry.instance().getDao(AddressDao.class);
  }

  /**
   * Rest-Call for {@link AddressDao#getFavoriteVCards()}
   * 
   * @param searchTerm
   */
  @GET
  @Path(RestPaths.LIST)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getList(@QueryParam("search") final String searchTerm)
  {
    final AddressFilter filter = new AddressFilter();
    filter.setSearchString(searchTerm);
    final List<AddressDO> list = addressDao.getList(filter);
    final Set<Integer> favorites = addressDao.getFavorites();
    final List<AddressObject> result = new LinkedList<AddressObject>();
    if (list != null) {
      for (final AddressDO addressDO : list) {
        if (favorites.contains(addressDO.getId()) == false) {
          // Export only personal favorites due to data-protection.
          continue;
        }
        final AddressObject address = AddressDOConverter.getAddressObject(addressDO);
        result.add(address);
      }
    }
    final String json = JsonUtils.toJson(result);
    return Response.ok(json).build();
  }

}
