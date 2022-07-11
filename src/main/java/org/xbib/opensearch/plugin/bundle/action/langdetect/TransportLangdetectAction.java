package org.xbib.opensearch.plugin.bundle.action.langdetect;

import org.opensearch.action.ActionListener;
import org.opensearch.action.support.ActionFilters;
import org.opensearch.action.support.TransportAction;
import org.opensearch.common.inject.Inject;
import org.opensearch.common.settings.Settings;
import org.opensearch.tasks.Task;
import org.opensearch.transport.TransportService;
import org.xbib.opensearch.plugin.bundle.common.langdetect.LangdetectService;
import org.xbib.opensearch.plugin.bundle.common.langdetect.Language;

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
        super(LangdetectAction.NAME, actionFilters, transportService.getTaskManager());
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
