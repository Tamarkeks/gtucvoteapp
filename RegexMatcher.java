package com.example.godspower.gtucvote.util;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

/**
 * Regular expressions matcher.
 * 
 * @version 16.05.2013
 */
public class RegexMatcher {

	public final static boolean isElevenDigets(String s) {
		return s.matches("\\d{11}");
	}

	public final static boolean IsOneDigit(String s) {
		return s.matches("\\d{1}");
	}

	public final static boolean IsOneOrTwoDigits(String s) {
		return s.matches("\\d{1,2}");
	}

	public final static boolean IsCandidateNumber(String s) {
		return s.matches("\\d{1,10}.\\d{1,11}");
	}

	public final static boolean IsFortyCharacters(String s) {
		if (s.length() == 40) {
			return true;
		}
		return false;
	}
	
	public static boolean isCorrectQR(String value){
		if(value.matches("^\\w{40}\n(\\w{1,28}\t([A-Fa-f0-9]){40}\n){1,5}")){
			return true;
		}	
		return false;
	}

	public final static boolean Is256Bytes(String s) {
		if (s.length() == 256) {
			return true;
		}
		return false;
	}

	public final static boolean Is512Bytes(String s) {
		if (s.length() == 512) {
			return true;
		}
		return false;
	}

	public static boolean isValidUTF8(final byte[] bytes) {
		try {
			Charset.availableCharsets().get("UTF-8").newDecoder()
					.decode(ByteBuffer.wrap(bytes));
		} catch (CharacterCodingException e) {
			return false;
		}
		return true;
	}

	public final static boolean IsLessThan101UtfChars(String s) {
		if (s.length() < 101 && isValidUTF8(s.getBytes())) {
			return true;
		}
		return false;
	}
}
