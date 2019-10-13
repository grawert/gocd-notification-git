/*
 * Copyright 2018 ThoughtWorks, Inc.
 * Copyright 2019 Uwe Grawert <grawert@b1-systems.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cd.go.notification.git.executors;

import java.util.Arrays;
import java.util.HashMap;

import cd.go.notification.git.PluginSettings;
import cd.go.notification.git.RequestExecutor;
import cd.go.notification.git.ServerInfo;
import cd.go.notification.git.ServerInfoRequest;
import cd.go.notification.git.ServerRequestFailedException;
import cd.go.notification.git.StageStateMessage;
import cd.go.notification.git.requests.StageStatusRequest;
import cd.go.notification.git.requests.StageStatusRequest.Pipeline;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.apache.cxf.jaxrs.client.WebClient;
import static java.text.MessageFormat.format;

public class StageStatusRequestExecutor implements RequestExecutor {

    private static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    static final Logger LOG = Logger.getLoggerFor(StageStatusRequestExecutor.class);

    private final StageStatusRequest request;
    private final PluginSettings pluginSettings;
    private final ServerInfo serverInfo;

    public StageStatusRequestExecutor(StageStatusRequest request, PluginSettings pluginSettings, ServerInfoRequest serverInfoRequest) throws ServerRequestFailedException {
        this.request = request;
        this.pluginSettings = pluginSettings;
        this.serverInfo = serverInfoRequest.getServerInfo();
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        HashMap<String, Object> responseJson = new HashMap<>();
        try {
            if (pluginSettings == null) {
                throw new Exception("Plugin is not configured");
            }

            sendNotification();
            responseJson.put("status", "success");
        } catch (Exception error) {

            LOG.warn(format("Sending notification failed: {0}", error.getMessage()));

            responseJson.put("status", "failure");
            responseJson.put("messages", Arrays.asList(error.getMessage()));
        }

        return new DefaultGoPluginApiResponse(200, GSON.toJson(responseJson));
    }

    protected void sendNotification() throws Exception {
        Pipeline pipeline = request.pipeline;

        for (StageStatusRequest.BuildCause buildCause : pipeline.buildCause) {
            Map material = buildCause.material;
            String materialType = (String) material.get("type");

            if (materialType.equalsIgnoreCase("git")) {
                Map gitConfig = (Map) material.get("git-configuration");
                String url = (String) gitConfig.get("url");
                String state = pipeline.stage.state;
                String result = pipeline.stage.result;
                String revision = buildCause.modifications.get(0).revision;

                if (isTargetUrl(url)) {
                    String trackbackUrl = getTrackbackUrl(pipeline);
                    updateStatus(url, revision, state, result, trackbackUrl);
                }
            }
        }
    }

    void updateStatus(String serverUrl, String revision, String state, String result, String trackbackUrl) throws Exception {
        String authorizationHeader = "token " + pluginSettings.authToken();
        WebClient webClient = WebClient.create(getStatusApiUrl(serverUrl, revision));
        webClient.authorization(authorizationHeader);
        webClient.accept(MediaType.APPLICATION_JSON);
        webClient.type(MediaType.APPLICATION_JSON);

        StageStateMessage stateMessage = new StageStateMessage();
        HashMap<String, String> body = new HashMap<>();
        body.put("state", stateMessage.getStateMessage(state));
        body.put("context", revision);
        body.put("target_url", trackbackUrl);
        body.put("description", result);

        javax.ws.rs.core.Response response = webClient.post(GSON.toJson(body));

        if (response.getStatus() < 200 || response.getStatus() >= 300) {
            String reason = response.getStatusInfo().getReasonPhrase();
            String errorMessage = String.format("%s: %s", serverUrl, reason);
            throw new Exception(errorMessage);
        }
    }

    boolean isTargetUrl(String url) throws MalformedURLException {
        URL materialUrl = new URL(url);
        URL serverUrl = new URL(pluginSettings.serverUrl());

        return materialUrl.getHost().equalsIgnoreCase(serverUrl.getHost());
    }

    public String stripGitSuffixFromUrl(String gitUrl) {
        return gitUrl.replace(".git", "");
    }

    String getStatusApiUrl(String materialUrl, String commitId) throws ServerRequestFailedException, MalformedURLException {
        String serverUrl = pluginSettings.serverUrl();
        String path = stripGitSuffixFromUrl(new URL(materialUrl).getPath());
        String statusApiUrl = String.format("%s/api/v1/repos%s/statuses/%s", serverUrl, path, commitId);

        return statusApiUrl;
    }

    String getTrackbackUrl(StageStatusRequest.Pipeline pipeline) {
        String pipelineInstance = String.format("%s/%s/%s/%s", pipeline.name,
                pipeline.counter, pipeline.stage.name, pipeline.stage.counter);
        String trackbackURL = String.format("%s/go/pipelines/%s",
                serverInfo.siteUrl(), pipelineInstance);

        return trackbackURL;
    }
}
