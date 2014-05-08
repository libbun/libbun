// ***************************************************************************
// Copyright (c) 2013-2014, Libbun project authors. All rights reserved.
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// *  Redistributions of source code must retain the above copyright notice,
//    this list of conditions and the following disclaimer.
// *  Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
// TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
// PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
// OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
// WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
// OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// **************************************************************************

package libbun.parser.common;

import libbun.util.BArray;
import libbun.util.BField;
import libbun.util.LibBunSystem;
import libbun.util.Var;

public final class BunLogger {

	public BArray<String>  ReportedErrorList = new BArray<String>(new String[10]);

	public final void log(String Message) {
		this.ReportedErrorList.add(Message);
	}

	public final static void _LogErrorExit(BToken Token, String Message) {
		if(Token != null && Token.Source != null) {
			Message = Token.Source.FormatErrorMarker("error", Token.StartIndex, Message);
			Token.Source.logger.log(Message);
		}
		else {
			LibBunSystem._Exit(1, Message);
		}
	}

	public final static String _LogError(BToken Token, String Message) {
		if(Token != null && Token.Source != null) {
			Message = Token.Source.FormatErrorMarker("error", Token.StartIndex, Message);
			Token.Source.logger.log(Message);
			@Var int loc = Message.indexOf("\n");
			if(loc > 0) {
				Message = Message.substring(0, loc);
			}
		}
		return Message;
	}

	public final static void _LogWarning(BToken Token, String Message) {
		if(Token != null) {
			Message = Token.Source.FormatErrorMarker("warning", Token.StartIndex, Message);
			Token.Source.logger.log(Message);
		}
	}

	public final static void _LogInfo(BToken Token, String Message) {
		if(Token != null && Token.Source != null) {
			Message = Token.Source.FormatErrorMarker("info", Token.StartIndex, Message);
			Token.Source.logger.log(Message);
		}
	}

	public final static void _LogDebug(BToken Token, String Message) {
		if(Token != null && Token.Source != null) {
			Message = Token.Source.FormatErrorMarker("debug", Token.StartIndex, Message);
			Token.Source.logger.log(Message);
		}
	}

	public final String[] GetReportedErrors() {
		@Var BArray<String> List = this.ReportedErrorList;
		this.ReportedErrorList = new BArray<String>(new String[10]);
		return List.CompactArray();
	}

	public final void OutputErrorsToStdErr() {
		@Var String[] Messages = this.GetReportedErrors();
		@Var int i = 0;
		while(i < Messages.length) {
			LibBunSystem._PrintLine(Messages[i]);
			i = i + 1;
		}
	}

}

