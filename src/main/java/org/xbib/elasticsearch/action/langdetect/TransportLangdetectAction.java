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

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.TransportAction;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;
import org.xbib.elasticsearch.common.langdetect.LangdetectService;
import org.xbib.elasticsearch.common.langdetect.Language;
import org.xbib.elasticsearch.common.langdetect.LanguageDetectionException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransportLangdetectAction extends TransportAction<LangdetectRequest, LangdetectResponse> {

    private final static Map<String, LangdetectService> services = new HashMap<>();

    @Inject
    public TransportLangdetectAction(Settings settings, ThreadPool threadPool,
                                     ActionFilters actionFilters,
                                     IndexNameExpressionResolver indexNameExpressionResolver,
                                     TransportService transportService) {
        super(settings, LangdetectAction.NAME, threadPool, actionFilters, indexNameExpressionResolver, transportService.getTaskManager());
        services.put("", new LangdetectService(settings));
    }

    @Override
    protected void doExecute(LangdetectRequest request, ActionListener<LangdetectResponse> listener) {
        String profile = request.getProfile();
        if (profile == null) {
            profile = "";
        }
        if (!services.containsKey(profile)) {
            services.put(profile, new LangdetectService(settings, profile));
        }
        // detectAll() is not thread-safe
        synchronized (services) {
            try {
                List<Language> langs = services.get(profile).detectAll(request.getText());
                listener.onResponse(new LangdetectResponse().setLanguages(langs).setProfile(request.getProfile()));
            } catch (LanguageDetectionException e) {
                listener.onFailure(e);
            }
        }
    }
}
