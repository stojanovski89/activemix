/*
 * Copyright (c) 2017-2018 apifocal LLC. All rights reserved.
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
package org.apifocal.amix.jaas.token.verifiers;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.SignedJWT;
import org.apifocal.amix.jaas.token.Settings;
import org.apifocal.amix.jaas.token.TokenValidationException;
import org.apifocal.amix.jaas.token.TokenValidator;
import org.apifocal.amix.jaas.token.verifiers.nimbus.DirectoryJWKSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.security.Key;
import java.util.List;

/**
 * Validator of token signer information.
 */
public class TokenSignerValidator implements TokenValidator<SignedJWT, SecurityContext> {

    private final Logger logger = LoggerFactory.getLogger(TokenSignerValidator.class);
    private final File directory;

    public TokenSignerValidator(Settings settings) {
        this.directory = settings.stringOption("keys")
            .map(File::new)
            .filter(File::exists)
            .filter(File::isDirectory)
            .orElseThrow(() -> new IllegalStateException("TokenSignerValidator requires keys property pointing to authorized keys directory"));
    }

    @Override
    public void validate(SignedJWT token, SecurityContext securityContext) throws TokenValidationException {
        JWSHeader header = token.getHeader();
        JWSAlgorithm algorithm = header.getAlgorithm();

        JWKSource<SecurityContext> jwkSource = new DirectoryJWKSource<>(directory);
        try {
            List<Key> keys = new JWSVerificationKeySelector<>(algorithm, jwkSource)
                .selectJWSKeys(header, null);

            DefaultJWSVerifierFactory verifierFactory = new DefaultJWSVerifierFactory();
            int success = 0;
            for (Key key : keys) {
                try {
                    JWSVerifier jwsVerifier = verifierFactory.createJWSVerifier(header, key);
                    jwsVerifier.verify(header, token.getSigningInput(), token.getSignature());
                    success++;
                } catch (JOSEException e) {
                    logger.info("Invalid signature found", e);
                }
            }

            if (success == 0) {
                throw new TokenValidationException("Could not verify signature. No matching keys found.");
            }
        } catch (KeySourceException e) {
            throw new TokenValidationException("Invalid token signature", e);
        }
    }

}