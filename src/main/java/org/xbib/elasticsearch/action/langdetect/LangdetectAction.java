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
package org.xbib.elasticsearch.action.langdetect;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

public class LangdetectAction extends Action<LangdetectRequest, LangdetectResponse, LangdetectRequestBuilder> {

    public static final String NAME = "langdetect";

    public static final LangdetectAction INSTANCE = new LangdetectAction();

    private LangdetectAction() {
        super(NAME);
    }

    @Override
    public LangdetectRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new LangdetectRequestBuilder(client);
    }

    @Override
    public LangdetectResponse newResponse() {
        return new LangdetectResponse();
    }

}
