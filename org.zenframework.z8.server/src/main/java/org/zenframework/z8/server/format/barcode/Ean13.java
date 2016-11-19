package org.zenframework.z8.server.format.barcode;

//font - ean13.ttf
public class Ean13 {
	static public String encode(String value) {
		return createEAN13Code(getFullCode(value));
	}

	static private String getFullCode(String code) {
		int chetVal = 0, nechetVal = 0;

		for(int index = 0; index < 6; index++) {
			chetVal += Integer.valueOf(code.substring(index * 2 + 1, index * 2 + 2)).intValue();
			nechetVal += Integer.valueOf(code.substring(index * 2, index * 2 + 1)).intValue();
		}

		chetVal *= 3;
		int controlNumber = 10 - (chetVal + nechetVal) % 10;
		if(controlNumber == 10)
			controlNumber = 0;

		return code + String.valueOf(controlNumber);
	}

	static private String digitToUpperCase(String digit) {
		String letters = "ABCDEFGHIJ";
		int position = Integer.valueOf(digit).intValue();
		return letters.substring(position, position + 1);
	}

	static private String digitToLowerCase(String digit) {
		String letters = "abcdefghij";
		int position = Integer.valueOf(digit).intValue();
		return letters.substring(position, position + 1);
	}

	static private String createEAN13Code(String rawCode) {
		int firstFlag = Integer.valueOf(rawCode.substring(0, 1)).intValue();

		String leftString = rawCode.substring(1, 7);
		String rightString = rawCode.substring(7);

		String rightCode = "";
		String leftCode = "";

		for(int i = 0; i < 6; i++)
			rightCode += digitToLowerCase(rightString.substring(i, i + 1));

		if(firstFlag == 0)
			leftCode = "#!" + leftString.substring(0, 1) + leftString.substring(1, 2) + leftString.substring(2, 3) + leftString.substring(3, 4) + leftString.substring(4, 5) + leftString.substring(5);
		else if(firstFlag == 1)
			leftCode = "$!" + leftString.substring(0, 1) + leftString.substring(1, 2) + digitToUpperCase(leftString.substring(2, 3)) + leftString.substring(3, 4) + digitToUpperCase(leftString.substring(4, 5)) + digitToUpperCase(leftString.substring(5));
		else if(firstFlag == 2)
			leftCode = "%!" + leftString.substring(0, 1) + leftString.substring(1, 2) + digitToUpperCase(leftString.substring(2, 3)) + digitToUpperCase(leftString.substring(3, 4)) + leftString.substring(4, 5) + digitToUpperCase(leftString.substring(5));
		else if(firstFlag == 3)
			leftCode = "&!" + leftString.substring(0, 1) + leftString.substring(1, 2) + digitToUpperCase(leftString.substring(2, 3)) + digitToUpperCase(leftString.substring(3, 4)) + digitToUpperCase(leftString.substring(4, 5)) + leftString.substring(5);
		else if(firstFlag == 4)
			leftCode = "'!" + leftString.substring(0, 1) + digitToUpperCase(leftString.substring(1, 2)) + leftString.substring(2, 3) + leftString.substring(3, 4) + digitToUpperCase(leftString.substring(4, 5)) + digitToUpperCase(leftString.substring(5));
		else if(firstFlag == 5)
			leftCode = "(!" + leftString.substring(0, 1) + digitToUpperCase(leftString.substring(1, 2)) + digitToUpperCase(leftString.substring(2, 3)) + leftString.substring(3, 4) + leftString.substring(4, 5) + digitToUpperCase(leftString.substring(5));
		else if(firstFlag == 6)
			leftCode = ")!" + leftString.substring(0, 1) + digitToUpperCase(leftString.substring(1, 2)) + digitToUpperCase(leftString.substring(2, 3)) + digitToUpperCase(leftString.substring(3, 4)) + leftString.substring(4, 5) + leftString.substring(5);
		else if(firstFlag == 7)
			leftCode = "*!" + leftString.substring(0, 1) + digitToUpperCase(leftString.substring(1, 2)) + leftString.substring(2, 3) + digitToUpperCase(leftString.substring(3, 4)) + leftString.substring(4, 5) + digitToUpperCase(leftString.substring(5));
		else if(firstFlag == 8)
			leftCode = "+!" + leftString.substring(0, 1) + digitToUpperCase(leftString.substring(1, 2)) + leftString.substring(2, 3) + digitToUpperCase(leftString.substring(3, 4)) + digitToUpperCase(leftString.substring(4, 5)) + leftString.substring(5);
		else if(firstFlag == 9)
			leftCode = ",!" + leftString.substring(0, 1) + digitToUpperCase(leftString.substring(1, 2)) + digitToUpperCase(leftString.substring(2, 3)) + leftString.substring(3, 4) + digitToUpperCase(leftString.substring(4, 5)) + leftString.substring(5);

		return leftCode + "-" + rightCode + "!";
	}
}
