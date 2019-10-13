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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;

public class FieldUrl extends Field {

    public FieldUrl(String key, String displayName, String defaultValue, Boolean required, Boolean secure, String displayOrder) {
        super(key, displayName, defaultValue, required, secure, displayOrder);
    }

    @Override
    public String doValidate(String input) {
        if (StringUtils.isBlank(input)) {
            return this.displayName + " must not be blank.";
        }

        if (!validateUrl(input)) {
            return "URL is malformed.";
        }

        return null;
    }

    public static boolean validateUrl(String url) {
        UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS | UrlValidator.ALLOW_ALL_SCHEMES);
        return urlValidator.isValid(url);
    }

}
