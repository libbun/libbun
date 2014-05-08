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


package libbun.encode;

import libbun.common.CommonStringBuilder;
import libbun.util.BField;
import libbun.util.LibBunSystem;
import libbun.util.Var;

public final class LibBunSourceBuilder extends CommonStringBuilder {
	@BField LibBunSourceBuilder Parent;
	@BField LibBunSourceGenerator Template;

	public LibBunSourceBuilder(LibBunSourceGenerator Template, LibBunSourceBuilder Parent) {
		super();
		this.Template = Template;
		this.Parent = Parent;
	}

	public final LibBunSourceBuilder Pop() {
		this.AppendLineFeed();
		return this.Parent;
	}

	public final void AppendCode(String Source) {
		this.LastChar = '\0';
		@Var int StartIndex = 0;
		@Var int i = 0;
		while(i < Source.length()) {
			@Var char ch = LibBunSystem._GetChar(Source, i);
			if(ch == '\n') {
				if(StartIndex < i) {
					this.SourceList.add(Source.substring(StartIndex, i));
				}
				this.AppendNewLine();
				StartIndex = i + 1;
			}
			if(ch == '\t') {
				if(StartIndex < i) {
					this.SourceList.add(Source.substring(StartIndex, i));
				}
				this.Append(this.Template.Tab);
				StartIndex = i + 1;
			}
			i = i + 1;
		}
		if(StartIndex < i) {
			this.SourceList.add(Source.substring(StartIndex, i));
		}
	}

	public final int GetPosition() {
		return this.SourceList.size();
	}

	public final String CopyString(int BeginIndex, int EndIndex) {
		return LibBunSystem._SourceBuilderToString(this, BeginIndex, EndIndex);
	}

}
