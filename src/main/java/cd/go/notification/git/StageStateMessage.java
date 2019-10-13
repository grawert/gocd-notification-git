/*
 * Copyright 2019 Uwe Grawert <grawert@b1-systems.de>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cd.go.notification.git;

import java.util.HashMap;

/*
 * Translate GoCD stage state to Gitea state message
*/
public class StageStateMessage {

    HashMap<String, String> states;

    public StageStateMessage() {
        states = new HashMap<>();
        states.put("Passed", "success");
        states.put("Failed", "failure");
        states.put("Building", "pending");
        states.put("Scheduled", "pending");
    }

    public String getStateMessage(String result) {
        String state = states.get(result);

        if (state == null) {
            return "warning";
        }
        
        return state;
    }
}
