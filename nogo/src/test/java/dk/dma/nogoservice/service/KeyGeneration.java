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

import java.security.KeyPair;
import java.security.KeyPairGenerator;

/**
 * Main used to generate KeyCloak info
 *
 * @author Klaus Groenbaek
 *         Created 01/05/17.
 */
public class KeyGeneration {

    public static void main(String[] args) throws Exception {

        KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

        System.out.println("Private bytes: ");
        System.out.println(BaseEncoding.base64().encode(keyPair.getPrivate().getEncoded()));

        System.out.println("Public bytes: ");
        System.out.println(BaseEncoding.base64().encode(keyPair.getPublic().getEncoded()));

    }

}
