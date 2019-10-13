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
package cd.go.notification.git;

import static cd.go.notification.git.Constants.PLUGIN_IDENTIFIER;

import cd.go.notification.git.executors.GetPluginConfigurationExecutor;
import cd.go.notification.git.executors.GetPluginIconExecutor;
import cd.go.notification.git.executors.GetViewRequestExecutor;
import cd.go.notification.git.executors.NotificationInterestedInExecutor;
import cd.go.notification.git.requests.StageStatusRequest;
import cd.go.notification.git.requests.ValidatePluginSettings;
import com.google.gson.Gson;
import com.sun.tools.sjavac.Log;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import static java.text.MessageFormat.format;
import java.util.HashMap;

@Extension
public class NotificationPlugin implements GoPlugin {

    private GoApplicationAccessor accessor;
    private PluginRequest pluginRequest;
    private ServerInfoRequest serverInfoRequest;
    private PluginSettings pluginSettings;
    private static final Gson GSON = new Gson();

    static final Logger LOG = Logger.getLoggerFor(NotificationPlugin.class);

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor accessor) {
        this.accessor = accessor;
        this.pluginRequest = new PluginRequest(this.accessor);
        this.serverInfoRequest = new ServerInfoRequest(this.accessor);
        this.pluginSettings = fetchPluginSettings(pluginRequest);
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) throws UnhandledRequestTypeException {
        try {
            GoPluginApiResponse response;

            LOG.debug(format("Request: {0}", request.requestName()));

            switch (Request.fromString(request.requestName())) {
                case REQUEST_GET_PLUGIN_ICON:
                    return new GetPluginIconExecutor().execute();
                case PLUGIN_SETTINGS_GET_VIEW:
                    response = new GetViewRequestExecutor().execute();
                    break;
                case PLUGIN_SETTINGS_CHANGED:
                    this.pluginSettings = fetchPluginSettings(pluginRequest);
                    HashMap<String, Object> responseJson = new HashMap<>();
                    responseJson.put("status", "success");
                    response = new DefaultGoPluginApiResponse(200, GSON.toJson(responseJson));
                    break;
                case REQUEST_NOTIFICATIONS_INTERESTED_IN:
                    response = new NotificationInterestedInExecutor().execute();
                    break;
                case REQUEST_STAGE_STATUS:
                    response = StageStatusRequest.fromJSON(request.requestBody()).executor(pluginSettings, serverInfoRequest).execute();
                    break;
                case PLUGIN_SETTINGS_GET_CONFIGURATION:
                    response = new GetPluginConfigurationExecutor().execute();
                    break;
                case PLUGIN_SETTINGS_VALIDATE_CONFIGURATION:
                    response = ValidatePluginSettings.fromJSON(request.requestBody()).executor().execute();
                    break;
                default:
                    throw new UnhandledRequestTypeException(request.requestName());
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return PLUGIN_IDENTIFIER;
    }
    
    private PluginSettings fetchPluginSettings(PluginRequest request) {
        PluginSettings settings = null;
        try {
            settings = pluginRequest.getPluginSettings();
        } catch (ServerRequestFailedException error) {
            Log.debug(error);
        }

        return settings;
    }
}
