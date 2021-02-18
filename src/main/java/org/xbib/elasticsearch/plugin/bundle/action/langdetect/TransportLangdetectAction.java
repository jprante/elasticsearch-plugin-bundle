package org.xbib.elasticsearch.plugin.bundle.action.langdetect;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.TransportAction;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.transport.TransportService;
import org.xbib.elasticsearch.plugin.bundle.common.langdetect.LangdetectService;
import org.xbib.elasticsearch.plugin.bundle.common.langdetect.Language;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Transport action for language detection response.
 */
public class TransportLangdetectAction extends TransportAction<LangdetectRequest, LangdetectResponse> {

    private static final Map<String, LangdetectService> services = new HashMap<>();

    private final Settings settings;

    @Inject
    public TransportLangdetectAction(Settings settings,
                                     ActionFilters actionFilters,
                                     TransportService transportService) {
        super(LangdetectAction.NAME, actionFilters, transportService.getLocalNodeConnection(), transportService.getTaskManager());
        this.settings = settings;
        services.put("", new LangdetectService(settings));
    }

    @Override
    protected void doExecute(Task task, LangdetectRequest request, ActionListener<LangdetectResponse> listener) {
        String profile = request.getProfile();
        if (profile == null) {
            profile = "";
        }
        if (!services.containsKey(profile)) {
            services.put(profile, new LangdetectService(settings, profile));
        }
        List<Language> langs = services.get(profile).detectAll(request.getText());
        listener.onResponse(new LangdetectResponse().setLanguages(langs).setProfile(request.getProfile()));
    }
}
