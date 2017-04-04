/* Copyright (c) 2011 Danish Maritime Authority.
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
package dk.dma.nogoservice;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Klaus Groenbaek
 *         Created 04/04/17.
 */
public class RequestUtils {
    /**
     * Get the URL where this application is deployed
     * @param request a request to this context
     * @return the url
     */
    public static String getContextURL(HttpServletRequest request) {
        if (usePort(request)) {
            return String.format("%s://%s:%d%s",request.getScheme(),  request.getServerName(), request.getServerPort(), request.getContextPath());
        } else {
            return String.format("%s://%s%s",request.getScheme(),  request.getServerName(), request.getContextPath());
        }

    }

    private static boolean usePort(HttpServletRequest request) {
        return ("https".equals(request.getScheme()) && request.getServerPort() != 443) ||
                ("http".equals(request.getScheme()) && request.getServerPort() != 80);
    }

    private RequestUtils() {
    }
}
