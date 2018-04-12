
 
/*-------------------------------------------------------------------------+
|                                                                          |
| Copyright 2005-2011 The ConQAT Project                                   |
|                                                                          |
| Licensed under the Apache License, Version 2.0 (the "License");          |
| you may not use this file except in compliance with the License.         |
| You may obtain a copy of the License at                                  |
|                                                                          |
|    http://www.apache.org/licenses/LICENSE-2.0                            |
|                                                                          |
| Unless required by applicable law or agreed to in writing, software      |
| distributed under the License is distributed on an "AS IS" BASIS,        |
| WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. |
| See the License for the specific language governing permissions and      |
| limitations under the License.                                           |
+-------------------------------------------------------------------------*/
package org.conqat.lib.scanner;

/**
 * Class for CS tokens generated by the CS scanner. This does only add the
 * language identification to the base class.
 * <p>
 * NOTE: This class was automatically generated. DO NOT MODIFY.
 * 
 * @see org.conqat.lib.scanner.Token
 * @see org.conqat.lib.scanner.CSScanner
 */
public class CSToken extends Token {

	/**
	 * Create new CS token.
	 * 
	 * @param type
	 *            token type
	 * @param offset
	 *            number of characters before token in its origin
	 * @param lineNumber
	 *            line number
	 * @param text
	 *            original text
	 * @param originId
	 *            origin id
	 */
	/* package */CSToken(ETokenType type, int offset, int lineNumber,
			String text, String originId) {
		super(type, offset, lineNumber, text, originId);
	}

	/** {@inheritDoc} */
	@Override
	public ELanguage getLanguage() {
		return ELanguage.CS;
	}

	/** {@inheritDoc} */
	@Override
	public CSToken newToken(ETokenType type, int offset, int lineNumber,
			String text, String originId) {
		return new CSToken(type, offset, lineNumber, text, originId);
	}
}