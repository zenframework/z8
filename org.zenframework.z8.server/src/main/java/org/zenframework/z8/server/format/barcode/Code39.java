package org.zenframework.z8.server.format.barcode;
public class Code39 {
	/// <summary>
	/// Converts an input String to the equivilant string, that need to be
	/// produced using the 'Code 3 de 9' font.
	/// </summary>
	/// <param name="value">String to be encoded</param>
	/// <returns>Encoded String start/stop characters included</returns>
	public static String encode(String value) {
		return stringToBarcode(value, false);
	}

	/// <summary>
	/// Converts an input String to the equivilant string, that need to be
	/// produced using the 'Code 3 de 9' font.
	/// </summary>
	/// <param name="value">String to be encoded</param>
	/// <param name="addChecksum">Is checksum to be added</param>
	/// <returns>Encoded String start/stop and checksum characters
	/// included</returns>
	public static String stringToBarcode(String value, boolean addChecksum) {
		// Parameters : a string
		// Return : a String which give the bar code when it is dispayed with
		// CODE128.TTF font
		// : an empty String if the supplied parameter is no good
		boolean isValid = true;
		char currentChar;
		String returnValue = "";
		int checksum = 0;
		if(value.length() > 0) {

			// Check for valid characters
			for(int CharPos = 0; CharPos < value.length(); CharPos++) {
				currentChar = value.charAt(CharPos);
				if(!((currentChar >= '0' && currentChar <= '9') || (currentChar >= 'A' && currentChar <= 'Z') || currentChar == ' ' || currentChar == '-' || currentChar == '.' || currentChar == '$' || currentChar == '/' || currentChar == '+' || currentChar == '%')) {
					isValid = false;
					break;
				}
			}
			if(isValid) {
				// Add start char
				returnValue = "*";
				// Add other chars, and calc checksum
				for(int CharPos = 0; CharPos < value.length(); CharPos++) {
					currentChar = value.charAt(CharPos);
					returnValue += currentChar;
					if(currentChar >= '0' && currentChar <= '9') {
						checksum = checksum + (int)currentChar - 48;
					} else if(currentChar >= 'A' && currentChar <= 'Z') {
						checksum = checksum + (int)currentChar - 55;
					} else {
						switch(currentChar) {
						case '-':
							checksum = checksum + (int)currentChar - 9;
							break;
						case '.':
							checksum = checksum + (int)currentChar - 9;
							break;
						case '$':
							checksum = checksum + (int)currentChar + 3;
							break;
						case '/':
							checksum = checksum + (int)currentChar - 7;
							break;
						case '+':
							checksum = checksum + (int)currentChar - 2;
							break;
						case '%':
							checksum = checksum + (int)currentChar + 5;
							break;
						case ' ':
							checksum = checksum + (int)currentChar + 6;
							break;
						}
					}
				}
				// Calculation of the checksum ASCII code
				if(addChecksum) {
					checksum = checksum % 43;
					if(checksum >= 0 && checksum <= 9) {
						returnValue += (char)(checksum + 48);
					} else if(checksum >= 10 && checksum <= 35) {
						returnValue += (char)(checksum + 55);
					} else {
						switch(checksum) {
						case 36:
							returnValue += "-";
							break;
						case 37:
							returnValue += ".";
							break;
						case 38:
							returnValue += " ";
							break;
						case 39:
							returnValue += "$";
							break;
						case 40:
							returnValue += "/";
							break;
						case 41:
							returnValue += "+";
							break;
						case 42:
							returnValue += "%";
							break;
						}
					}
				}
				// Add stop char
				returnValue += "*";
			}
		}
		return returnValue;
	}
}
