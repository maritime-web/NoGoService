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
package dk.dma.nogoservice.service;

import com.google.common.io.BaseEncoding;
import com.google.common.io.CharStreams;
import lombok.SneakyThrows;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.representations.AccessToken;
import org.keycloak.util.TokenUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * creates KeyCloak access tokens, that matches the public key in the keycloak-test.json configuration
 *
 * @author Klaus Groenbaek
 *         Created 01/05/17.
 */
@Component
public class TestTokenProvider {

    private final PrivateKey privateKey;

    @SneakyThrows({IOException.class, GeneralSecurityException.class})
    public TestTokenProvider() {
        ClassPathResource resource = new ClassPathResource("/privateKey.base64");

        try (InputStream inputStream = resource.getInputStream()) {
            String base64 = CharStreams.toString(new InputStreamReader(inputStream, StandardCharsets.US_ASCII));
            byte[] bytes = BaseEncoding.base64().decode(base64);
            privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(bytes));
        }

    }


    @SneakyThrows
    public String getToken() {
        AccessToken token = new AccessToken();
        token.id("111");
        token.issuer("http://localhost:8080/auth/realms/test");
        token.addAccess("foo").addRole("admin");
        token.addAccess("bar").addRole("user");
        token.setSubject("test");
        token.type(TokenUtil.TOKEN_TYPE_BEARER);

        String encoded = new JWSBuilder()
                .jsonContent(token)
                .rsa256(privateKey);

        JWSInput input = new JWSInput(encoded);

        return input.getWireString();
    }


}
