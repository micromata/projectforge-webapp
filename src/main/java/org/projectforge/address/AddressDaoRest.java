/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.projectforge.core.BaseSearchFilter;
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
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddressDaoRest.class);

  private final AddressDao addressDao;

  private final PersonalAddressDao personalAddressDao;

  public AddressDaoRest()
  {
    this.addressDao = Registry.instance().getDao(AddressDao.class);
    this.personalAddressDao = this.addressDao.getPersonalAddressDao();
  }

  /**
   * Rest-Call for {@link AddressDao#getFavoriteVCards()}. <br/>
   * If modifiedSince is given then only those addresses will be returned:
   * <ol>
   * <li>The address was changed after the given modifiedSince date, or</li>
   * <li>the address was added to the user's personal address book after the given modifiedSince date, or</li>
   * <li>the address was removed from the user's personal address book after the given modifiedSince date.</li>
   * </ol>
   * @param searchTerm
   * @param modifiedSince milliseconds since 1970 (UTC)
   * @see AddressDaoClientMain
   */
  @GET
  @Path(RestPaths.LIST)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getList(@QueryParam("search") final String searchTerm, @QueryParam("modifiedSince") final Long modifiedSince)
  {
    final AddressFilter filter = new AddressFilter(new BaseSearchFilter());
    Date modifiedSinceDate = null;
    if (modifiedSince != null) {
      modifiedSinceDate = new Date(modifiedSince);
      filter.setModifiedSince(modifiedSinceDate);
    }
    filter.setSearchString(searchTerm);
    final List<AddressDO> list = addressDao.getList(filter);

    final List<PersonalAddressDO> favorites = personalAddressDao.getList();
    final Set<Integer> favoritesSet = new HashSet<Integer>();
    if (favorites != null) {
      for (final PersonalAddressDO personalAddress : favorites) {
        if (personalAddress.isFavoriteCard() == true && personalAddress.isDeleted() == false) {
          favoritesSet.add(personalAddress.getAddressId());
        }
      }
    }
    final List<AddressObject> result = new LinkedList<AddressObject>();
    final Set<Integer> alreadyExported = new HashSet<Integer>();
    if (list != null) {
      for (final AddressDO addressDO : list) {
        if (favoritesSet.contains(addressDO.getId()) == false) {
          // Export only personal favorites due to data-protection.
          continue;
        }
        final AddressObject address = AddressDOConverter.getAddressObject(addressDO);
        result.add(address);
        alreadyExported.add(address.getId());
      }
    }
    if (modifiedSinceDate != null) {
      // Add now personal address entries which were modified since the given date (deleted or added):
      for (final PersonalAddressDO personalAddress : favorites) {
        if (alreadyExported.contains(personalAddress.getAddressId()) == true) {
          // Already exported:
        }
        if (personalAddress.getLastUpdate() != null && personalAddress.getLastUpdate().before(modifiedSinceDate) == false) {
          final AddressDO addressDO = addressDao.getById(personalAddress.getAddressId());
          final AddressObject address = AddressDOConverter.getAddressObject(addressDO);
          if (personalAddress.isFavorite() == false) {
            // This address was may-be removed by the user from the personal address book, so add this address as deleted to the result
            // list.
            address.setDeleted(true);
          }
          result.add(address);
        }
      }
    }
    final String json = JsonUtils.toJson(result);
    log.info("Rest call finished (" + result.size() + " addresses)...");
    return Response.ok(json).build();
  }
}
