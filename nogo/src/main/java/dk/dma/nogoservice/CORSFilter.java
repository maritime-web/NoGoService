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

import org.springframework.http.HttpMethod;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Klaus Groenbaek
 *         Created 01/05/17.
 */
public class CORSFilter implements Filter {

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest httpServletRequest = (HttpServletRequest) req;
        // The headers needs to be on all responses.
        String origin = httpServletRequest.getHeader("origin");
        if (origin == null) {
            // Make the developers life simpler. The browser adds the origin but it is a pain to
            // for developers to add this header while testing.
            origin = "*";
        }
        response.setHeader("Access-Control-Allow-Origin", origin.replace("\n", ""));

        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, Accept, Authorization");

        // For options requests return immediately. Options does not require authentication so we want to return
        // here to avoid all yet unknown security risks.
        if (HttpMethod.OPTIONS.toString().equals(httpServletRequest.getMethod())) {
            return;
        }
        chain.doFilter(req, res);
    }

    public void init(FilterConfig filterConfig) {
    }

    public void destroy() {
    }

}
