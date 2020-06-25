/**
 * Copyright 2018-2020 Dynatrace LLC
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

package com.dynatrace.openkit.providers;

import com.dynatrace.openkit.core.configuration.HTTPClientConfiguration;
import com.dynatrace.openkit.protocol.HTTPClient;

/**
 * Interface for providing an HTTP client. Mostly needed for testing purposes.
 */
public interface HTTPClientProvider {

    /**
     * Returns an HTTPClient based on the provided configuration.
     */
    HTTPClient createClient(HTTPClientConfiguration configuration);
}
