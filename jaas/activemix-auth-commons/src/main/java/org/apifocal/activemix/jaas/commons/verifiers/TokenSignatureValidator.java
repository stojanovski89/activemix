/*
 * Copyright (c) 2017-2020 apifocal LLC. All rights reserved.
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
package org.apifocal.activemix.jaas.commons.verifiers;

import org.apifocal.activemix.jaas.commons.Settings;
import org.apifocal.activemix.jaas.commons.TokenValidationException;
import org.apifocal.activemix.jaas.commons.TokenValidator;

import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.SignedJWT;

/**
 * Token validator which verifies if given token is signed.
 *
 * @param <T> Type of token.
 */
public class TokenSignatureValidator<T extends JWT> implements TokenValidator<T, SecurityContext> {

    public TokenSignatureValidator(Settings settings) {

    }

    @Override
    public void validate(T token, SecurityContext securityContext) throws TokenValidationException {
        if (!(token instanceof SignedJWT)) {
            throw new TokenValidationException("Requested operation requires signed token");
        }
    }

}
