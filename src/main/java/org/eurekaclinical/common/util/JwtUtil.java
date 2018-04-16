package org.eurekaclinical.common.util;

/*-
 * #%L
 * Eureka! Clinical Common
 * %%
 * Copyright (C) 2016 - 2018 Emory University
 * %%
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
 * #L%
 */

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * This class provides methods to encode and decode JWT tokens.
 *
 * @author hrathod
 */
public final class JwtUtil {

    private JwtUtil () {
        // no instantiation
    }

    public static String createToken (String secret, int expirationMinutes, String username, String[] roles)
            throws UnsupportedEncodingException {

        Algorithm algorithm = Algorithm.HMAC256(secret);
        Instant expirationInstant = ZonedDateTime.now().plusMinutes(expirationMinutes).toInstant();
        return JWT.create()
                .withIssuer("eurekaclinical")
                .withExpiresAt(Date.from(expirationInstant))
                .withClaim("username", username)
                .withArrayClaim("roles", roles)
                .sign(algorithm);
    }

    public static DecodedJWT verifyAndDecode (String token, String secret) throws UnsupportedEncodingException {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm)
                .build();
        DecodedJWT decoded = verifier.verify(token);

        if (decoded.getExpiresAt().before(Date.from(ZonedDateTime.now().toInstant()))) {
            throw new JWTVerificationException("Token expired");
        }

        return decoded;
    }
}

