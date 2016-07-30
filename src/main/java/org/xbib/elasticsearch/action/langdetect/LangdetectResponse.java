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

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.StatusToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.RestStatus;
import org.xbib.elasticsearch.common.langdetect.Language;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.rest.RestStatus.OK;

public class LangdetectResponse extends ActionResponse implements StatusToXContent {

    private String profile;

    private List<Language> languages = new ArrayList<>();

    public LangdetectResponse() {
    }

    public String getProfile() {
        return profile;
    }

    public LangdetectResponse setProfile(String profile) {
        this.profile = profile;
        return this;
    }

    public List<Language> getLanguages() {
        return languages;
    }

    public LangdetectResponse setLanguages(List<Language> languages) {
        this.languages = languages;
        return this;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        if (!Strings.isNullOrEmpty(profile)) {
            builder.field("profile", profile);
        }
        builder.startArray("languages");
        for (Language lang : languages) {
            builder.startObject().field("language", lang.getLanguage())
                    .field("probability", lang.getProbability()).endObject();
        }
        builder.endArray();
        return builder;
    }

    @Override
    public RestStatus status() {
        return OK;
    }
}
