/*
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

import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.request.DefaultGoApiRequest;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;

import static cd.go.notification.git.Constants.PLUGIN_IDENTIFIER;
import static cd.go.notification.git.Constants.PLUGIN_SETTINGS_PROCESSOR_API_VERSION;

/**
 * Instances of this class know how to send messages to the GoCD Server.
 */
public class ServerInfoRequest {

    private final GoApplicationAccessor accessor;

    public ServerInfoRequest(GoApplicationAccessor accessor) {
        this.accessor = accessor;
    }

    public ServerInfo getServerInfo() throws ServerRequestFailedException {
        DefaultGoApiRequest request = new DefaultGoApiRequest(Constants.REQUEST_SERVER_GET_SERVER_INFO, PLUGIN_SETTINGS_PROCESSOR_API_VERSION, PLUGIN_IDENTIFIER);
        GoApiResponse response = accessor.submit(request);

        if (response.responseCode() != 200) {
            throw ServerRequestFailedException.getPluginSettings(response);
        }

        return ServerInfo.fromJSON(response.responseBody());
    }
}
