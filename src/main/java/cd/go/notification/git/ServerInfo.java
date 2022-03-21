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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

// Implement any settings that your plugin needs
public class ServerInfo {

    private static final Gson GSON = new GsonBuilder().
            excludeFieldsWithoutExposeAnnotation().
            create();

    @Expose
    @SerializedName("server_id")
    private String serverId;

    @Expose
    @SerializedName("site_url")
    private String siteUrl;

    @Expose
    @SerializedName("secure_site_url")
    private String secureSiteUrl;

    public static ServerInfo fromJSON(String json) {
        return GSON.fromJson(json, ServerInfo.class);
    }

    public String serverId() {
        return serverId;
    }

    public String siteUrl() {
        return siteUrl;
    }

    public String secureSiteUrl() {
        return secureSiteUrl;
    }
}
