package org.zenframework.z8.server.format.barcode;

// font - code128.ttf
public class Code128 {
	public static String encode(String value) {
		int charPos, minCharPos;
		int currentChar, checksum;
		boolean isTableB = true, isValid = true;
		String returnValue = "";

		if(value.length() > 0) {

			// Check for valid characters
			for(int charCount = 0; charCount < value.length(); charCount++) {
				// currentChar = char.GetNumericValue(value, charPos);
				currentChar = value.charAt(charCount);
				if(!(currentChar >= 32 && currentChar <= 126)) {
					isValid = false;
					break;
				}
			}

			// Barcode is full of ascii characters, we can now process it
			if(isValid) {
				charPos = 0;
				while(charPos < value.length()) {
					if(isTableB) {
						// See if interesting to switch to table C
						// yes for 4 digits at start or end, else if 6 digits
						if(charPos == 0 || charPos + 4 == value.length())
							minCharPos = 4;
						else
							minCharPos = 6;

						minCharPos = isNumber(value, charPos, minCharPos);

						if(minCharPos < 0) {
							// Choice table C
							if(charPos == 0) {
								// Starting with table C
								returnValue += (char)205; // char.ConvertFromUtf32(205);
							} else {
								// Switch to table C
								returnValue += (char)199;
							}
							isTableB = false;
						} else {
							if(charPos == 0) {
								// Starting with table B
								returnValue = String.valueOf((char)204); // char.ConvertFromUtf32(204);
							}

						}
					}

					if(!isTableB) {
						// We are on table C, try to process 2 digits
						minCharPos = 2;
						minCharPos = isNumber(value, charPos, minCharPos);
						if(minCharPos < 0) // OK for 2 digits, process it
						{
							currentChar = Integer.parseInt(value.substring(charPos, charPos + 2));
							currentChar = currentChar < 95 ? currentChar + 32 : currentChar + 100;
							returnValue = returnValue + (char)currentChar;
							charPos += 2;
						} else {
							// We haven't 2 digits, switch to table B
							returnValue += (char)200;
							isTableB = true;
						}
					}
					if(isTableB) {
						// Process 1 digit with table B
						returnValue += value.charAt(charPos);
						charPos++;
					}
				}

				// Calculation of the checksum
				checksum = 0;
				for(int loop = 0; loop < returnValue.length(); loop++) {
					currentChar = returnValue.charAt(loop);
					currentChar = currentChar < 127 ? currentChar - 32 : currentChar - 100;
					if(loop == 0)
						checksum = currentChar;
					else
						checksum = (checksum + (loop * currentChar)) % 103;
				}

				// Calculation of the checksum ASCII code
				checksum = checksum < 95 ? checksum + 32 : checksum + 100;
				// Add the checksum and the STOP
				returnValue = returnValue + checksum + 206;
			}
		}

		return returnValue;
	}

	private static int isNumber(String inputValue, int charPos, int minCharPos) {
		minCharPos--;
		if(charPos + minCharPos < inputValue.length()) {
			while(minCharPos >= 0) {
				if(inputValue.charAt(charPos + minCharPos) < 48 || inputValue.charAt(charPos + minCharPos) > 57)
					break;
				minCharPos--;
			}
		}
		return minCharPos;
	}
}
