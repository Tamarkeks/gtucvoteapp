package com.example.godspower.gtucvote.util;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.x509.RSAPublicKeyStructure;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.encodings.OAEPEncoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;

/**
 * RSA-OAEP modification for the brute force verification.
 * 
 * @version 25.04.2013
 */
public class Crypto {

	private static String lastRandom;

	public static byte[] encrypt(String data, String random, String strkey)
			throws IOException, InvalidCipherTextException {
		return encrypt(data.getBytes(Util.ENCODING), new SecureRandomWrapper(
				random.getBytes(Util.ENCODING)), strkey);
	}

	public static byte[] encrypt(byte[] data, SecureRandomWrapper random,
			String strkey) throws IOException, InvalidCipherTextException {

		RSAKeyParameters key = readKey(strkey);

		OAEPEncoding engine = new OAEPEncoding(new RSAEngine());
		engine.init(true, new ParametersWithRandom(key, random));

		int bsz = engine.getInputBlockSize();
		byte[] res = Hex.encode(engine.processBlock(data, 0,
				Math.min(bsz, data.length)));

		lastRandom = new String(random.getLastBytes(), Util.ENCODING);
		return res;
	}

	private static RSAKeyParameters readKey(String pemstr) throws IOException {
		PemReader reader = null;
		PemObject pem;
		try {
			StringReader rr = new StringReader(pemstr);
			reader = new PemReader(rr);
			pem = reader.readPemObject();
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

		ASN1InputStream stream = null;
		ASN1Sequence seq;
		try {
			stream = new ASN1InputStream(pem.getContent());
			seq = (ASN1Sequence) stream.readObject();

			Enumeration enm = seq.getObjects();
			enm.nextElement();

			stream = new ASN1InputStream(
					((DERBitString) enm.nextElement()).getBytes());
			seq = (ASN1Sequence) stream.readObject();
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
		RSAPublicKeyStructure pks = new RSAPublicKeyStructure(seq);
		return new RSAKeyParameters(false, pks.getModulus(),
				pks.getPublicExponent());
	}

	public static String getLastRandom() {
		return lastRandom;
	}
}