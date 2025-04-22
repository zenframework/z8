package org.zenframework.z8.server.crypto;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class Base34 {
	static private char[] Digits = {
		'1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C',
		'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'P',
		'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
	};

	static private Map<Character, Integer> Numbers = new HashMap<Character, Integer>();

	static {
		Numbers.put('1', 0); Numbers.put('2', 1); Numbers.put('3', 2); Numbers.put('4', 3); Numbers.put('5', 4); Numbers.put('6', 5); Numbers.put('7', 6); Numbers.put('8', 7); Numbers.put('9', 8); Numbers.put('A', 9); Numbers.put('B', 10); Numbers.put('C', 11);
		Numbers.put('D', 12); Numbers.put('E', 13); Numbers.put('F', 14); Numbers.put('G', 15); Numbers.put('H', 16); Numbers.put('I', 17); Numbers.put('J', 18); Numbers.put('K', 19); Numbers.put('L', 20); Numbers.put('M', 21); Numbers.put('N', 22); Numbers.put('P', 23);
		Numbers.put('Q', 24); Numbers.put('R', 25); Numbers.put('S', 26); Numbers.put('T', 27); Numbers.put('U', 28); Numbers.put('V', 29); Numbers.put('W', 30); Numbers.put('X', 31); Numbers.put('Y', 32); Numbers.put('Z', 33);
	};

	static public String encode(long number) {
		return encode((int)number);
	}

	static public String encode(int number) {
		if(number < 0)
			throw new RuntimeException("Base34.encode(number): number < 0");

		if(number == 0)
			return "" + Digits[0];

		String base34 = "";

		while(number != 0) {
			int remainder = number % 34;
			base34 = Digits[remainder] + base34;
			number = (number - remainder) / 34;
		}

		return base34;
	}

	static public int decode(String base34) {
		if(base34.isEmpty())
			throw new RuntimeException("Base34.decode(base34): base34 is empty");

		base34 = base34.toUpperCase();

		int number = 0;

		for(int i = base34.length() - 1; i >= 0; i -= 1) {
			Integer remainder = Numbers.get(base34.charAt(i));
			if(remainder == null)
				throw new RuntimeException("Base34.decode - unacceptable symbol");

			number += remainder * Math.round(Math.pow(34, base34.length() - 1 - i));
		}

		return number;
	}

	static public String random() {
		/* Base34 211111 - ZZZZZZ */

		int range = 1499368991;
		int min = 45435424;
		return Base34.encode(Math.round((range * Math.random())) + min);
	}

	static public string z8_encode(integer number) {
		return new string(encode(number.getInt()));
	}

	static public integer z8_decode(string base34) {
		return new integer(decode(base34.get()));
	}

	static public string z8_random() {
		return new string(random());
	}
}