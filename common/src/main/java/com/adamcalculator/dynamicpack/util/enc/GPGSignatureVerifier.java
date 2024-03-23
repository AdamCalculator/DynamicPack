/*
 * This file is part of Arduino.
 *
 * Copyright 2015 Arduino LLC (http://www.arduino.cc/)
 *
 * Arduino is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * As a special exception, you may use this file as part of a free software
 * library without restriction.  Specifically, if other files instantiate
 * templates or use macros or inline functions from this file, or you compile
 * this file and link it with other files to produce an executable, this
 * file does not by itself cause the resulting executable to be covered by
 * the GNU General Public License.  This exception does not however
 * invalidate any other reasons why the executable file might be covered by
 * the GNU General Public License.
 *
 *
 * --- 2024.03.16 ---
 * This version of file Modified by adam.
 */
package com.adamcalculator.dynamicpack.util.enc;


import com.adamcalculator.dynamicpack.Support;
import com.adamcalculator.dynamicpack.util.Out;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.compress.utils.IOUtils;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentVerifierBuilderProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class GPGSignatureVerifier {

    private GPGSignatureVerifier() {
    }

    public static boolean verify(InputStream signedFileInputStream, InputStream signatureIs, String base64publicKey) throws IOException {
        if (Support._isSkipGPGVerify()) {
            return Support._getDefaultVerifySkipResult();
        }
        try {
            PGPObjectFactory pgpObjectFactory = new PGPObjectFactory(signatureIs, new BcKeyFingerprintCalculator());

            Object nextObject;
            try {
                nextObject = pgpObjectFactory.nextObject();
                if (!(nextObject instanceof PGPSignatureList)) {
                    return false;
                }
            } catch (IOException e) {
                return false;
            }
            PGPSignatureList pgpSignatureList = (PGPSignatureList) nextObject;
            assert pgpSignatureList.size() == 1;
            PGPSignature pgpSignature = pgpSignatureList.get(0);

            PGPPublicKey pgpPublicKey = readPublicKey(new Base64InputStream(new ByteArrayInputStream(base64publicKey.getBytes(StandardCharsets.UTF_8))));

            pgpSignature.init(new BcPGPContentVerifierBuilderProvider(), pgpPublicKey);
            pgpSignature.update(IOUtils.toByteArray(signedFileInputStream));

            return pgpSignature.verify();

        } catch (PGPException e) {
            throw new IOException(e);

        } finally {
            IOUtils.closeQuietly(signedFileInputStream);
        }
    }

    private static PGPPublicKey readPublicKey(InputStream input) throws IOException, PGPException {
        PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(input),
                new BcKeyFingerprintCalculator());

        Iterator keyRingIter = pgpPub.getKeyRings();
        while (keyRingIter.hasNext()) {
            PGPPublicKeyRing keyRing = (PGPPublicKeyRing) keyRingIter.next();

            Iterator keyIter = keyRing.getPublicKeys();
            Object o = null;
            boolean b = false;
            while (keyIter.hasNext()) {
                var key = (PGPPublicKey) keyIter.next();
                Out.println("key:" + Hex.encodeHexString(key.getFingerprint()).toUpperCase());
                if (!b) {
                    o = key;
                    b = true;
                }
            }
            return (PGPPublicKey) o;
        }

        throw new IllegalArgumentException("Can't find encryption key in key ring.");
    }

}