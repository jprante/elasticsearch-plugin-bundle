/*
 * Copyright (C) 2014 Jörg Prante
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 */
package org.xbib.elasticsearch.rest.action.isbnformat;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.support.RestStatusToXContentListener;
import org.xbib.elasticsearch.action.isbnformat.ISBNFormatAction;
import org.xbib.elasticsearch.action.isbnformat.ISBNFormatRequest;
import org.xbib.elasticsearch.action.isbnformat.ISBNFormatResponse;

import static org.elasticsearch.rest.RestRequest.Method.GET;

public class RestISBNFormatterAction extends BaseRestHandler {

    @Inject
    public RestISBNFormatterAction(Settings settings, Client client, RestController controller) {
        super(settings, controller, client);
        controller.registerHandler(GET, "/_isbn", this);
    }

    @Override
    public void handleRequest(final RestRequest request, final RestChannel channel, final Client client) {
        ISBNFormatRequest isbnFormatRequest = new ISBNFormatRequest()
                .setValue(request.param("value"));
        client.execute(ISBNFormatAction.INSTANCE, isbnFormatRequest,
                new RestStatusToXContentListener<ISBNFormatResponse>(channel));
    }
}