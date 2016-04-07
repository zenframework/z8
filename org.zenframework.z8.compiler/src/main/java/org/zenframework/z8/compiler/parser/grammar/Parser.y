	int yyxi;

	final int endStack = 100;
	final int newState = 101;

	final int YYMAXDEPTH = 1000;
	final int YYREDMAX  = 1000;
	final int PCYYFLAG = -1000;
	final int WAS0ERR = 0;
	final int WAS1ERR = 1;
	final int WAS2ERR = 2;
	final int WAS3ERR = 3;

	Token yylval = new Token();
	Token yyval = new Token();

	Token[] s = new Token[YYMAXDEPTH];     /* value stack */
	int pcyyerrfl = 0;           /* error flag */
	int pcyyerrct = 0;           /* error count */
	
	int[] redseq = new int[YYREDMAX];
	int redcnt = 0;
	int pcyytoken = -1;          /* input token */

	int[] statestack = new int[YYMAXDEPTH]; /* state stack */
	int j, m;              /* working index */
	int s1;
	int tmpstate = 0, tmptoken = -1, n;

	int ssPos = -1;
	int sp = -1;

	Parser parser;

	Lexer getLexer()
	{
		return parser.getLexer();
	}
	
	void recover()
	{
		if(yylval.getId() != 0)
		{
			pcyytoken = -1;
			pcyyerrfl = WAS0ERR;
		}
	}
		
	void error(IPosition position, String message)
	{
		parser.getCompilationUnit().error(position, message);
	}

	void error()
	{
		error(yylval);
	}

	void error(IToken token)
	{
		error(token, null);
	}

	void error(String message)
	{
		error(getLexer().previousToken(), message);
	}

	void error(IToken token, String message)
	{
		if(token != null && token.getPosition() != null) // empty or fully commented file
		{
			if(message == null)
			{
				message = "', delete this token";
			}
			else
			{
				message = "', " + message;
			}
		
			parser.getCompilationUnit().error(token.getPosition(), "Syntax error on token '" + getLexer().getString(token.getPosition()) + message);
		}
		else if(token != null && token.getId() == 0)
		{
			token = getLexer().previousToken();
			
			if(token.getPosition() != null)
			{
				parser.getCompilationUnit().error(token.getPosition(), "Syntax error on token '" + getLexer().getString(token.getPosition()) + "', unexpected end of file");
			}
		}
	}

	int lex()
	{
		yylval = getLexer().nextToken();
		return yylval.getId(); 
	}

	int parse(Parser parser)
	{
		this.parser = parser;
		
		int nResult = endStack;

		while(true)
		{
		
//	endStack:
			if(nResult == endStack)
			{
				/* push stack */
				if(++ssPos - YYMAXDEPTH > 0)
				{
					error(); //"pcyacc internal stack overflow"
					return(1);
				}
				statestack[ssPos] = tmpstate;
				++sp;
				s[sp] = yyval;
			}

//	newState:
			n = yypact[tmpstate];
			if (n <= PCYYFLAG)
			{
				nResult = doDefault();
				if(nResult < endStack)
					return nResult;
				continue; // defaultact; /*  a simple state */
			}

			if(pcyytoken < 0)
			{
				if((pcyytoken = lex()) < 0)
					pcyytoken = 0;
			}
		  
			if((n += pcyytoken) < 0 || n >= YYLAST)
			{
				nResult = doDefault();
				if(nResult < endStack)
					return nResult;
				continue; // defaultact;
			}

			if(yychk[n=yyact[n]] == pcyytoken)
			{ /* a shift */
				pcyytoken = -1;
				yyval = yylval;
				tmpstate = n;
				if(pcyyerrfl > 0)
					--pcyyerrfl;
				continue; // enstack;
			}

			nResult = doDefault();

			if(nResult < endStack)
				return nResult;
		}
	}

	int doDefault()
	{
		if ((n=yydef[tmpstate]) == -2) 
		{
			if (pcyytoken < 0)
			{
				if((pcyytoken = lex()) < 0)
					pcyytoken = 0;
			}
			for(yyxi = 0; (yyexca[yyxi] != -1) || (yyexca[yyxi + 1] != tmpstate); yyxi += 2)
			{
			}
			while(yyexca[yyxi += 2] >= 0)
				if(yyexca[yyxi] == pcyytoken)
					break;
			if((n = yyexca[yyxi + 1]) < 0)
			{ /* an accept action */
			return (0);
			}
		}

		if(n == 0)
		{
			/* error situation */
			switch (pcyyerrfl) {
			case WAS0ERR:          /* an error just occurred */
				error();
	            ++pcyyerrct;
			case WAS1ERR:
			case WAS2ERR:           /* try again */
				pcyyerrfl = WAS3ERR;
				/* find a state for a legal shift action */
				while (ssPos >= 0) 
				{
					n = yypact[statestack[ssPos]] + YYERRCODE;
					if(n >= 0 && n < YYLAST && yychk[yyact[n]] == YYERRCODE)
					{
						tmpstate = yyact[n];  /* simulate a shift of "error" */
						return endStack;//break enstack;
					}
					n = yypact[statestack[ssPos]];

					/* the current yyps has no shift on "error", pop stack */
					--ssPos;
					--sp;
				}
				return(1);
			case WAS3ERR:  /* clobber input char */
				if (pcyytoken == 0) 
					return 1; /* quit */
				pcyytoken = -1;
				return newState;
			} /* switch */
		} /* if */


		/* reduction, given a production n */
		ssPos -= yyr2[n];
		s1 = sp;
		sp -= yyr2[n];
		yyval = s[sp + 1];
		m = n;
		/* find next state from goto table */
		n = yyr1[n];
		j = yypgo[n] + statestack[ssPos] + 1;
		if(j >= YYLAST || yychk[ tmpstate = yyact[j] ] != -n) 
			tmpstate = yyact[yypgo[n]];
	
		switch (m) { 
			$A
		}
		return endStack;
	}
}