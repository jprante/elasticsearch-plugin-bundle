package org.xbib.elasticsearch.index.analysis.hyphen;

import org.apache.lucene.analysis.standard.StandardTokenizerInterface;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

%%

%class HyphenTokenizerImpl
%implements StandardTokenizerInterface
%unicode 3.0
%integer
%function getNextToken
%pack
%char
%buffer 4096

%{

public static final int ALPHANUM          = HyphenTokenizer.ALPHANUM;
public static final int ALPHANUM_COMP     = HyphenTokenizer.ALPHANUM_COMP;
public static final int NUM               = HyphenTokenizer.NUM;
public static final int CJ                = HyphenTokenizer.CJ;

public static final String [] TOKEN_TYPES = HyphenTokenizer.TOKEN_TYPES;

public final int yychar() {
    return yychar;
}

public final void getText(CharTermAttribute t) {
  t.copyBuffer(zzBuffer, zzStartRead, zzMarkedPos-zzStartRead);
}

public final void setBufferSize(int numChars) {
  throw new UnsupportedOperationException();
}

%}

THAI = [\u0E00-\u0E59]

// basic word: a sequence of digits & letters (includes Thai to enable ThaiAnalyzer to function)
ALPHANUM   = ({LETTER}|{THAI}|[:digit:])+

// keep hyphenated / composed word fragments together for the token filter.
// internal apostrophes: O'Reilly, you're, O'Reilly's
// use a post-filter to remove possessives
// acronyms: U.S.A., I.B.M., etc. with dropped last punctuation char.
// company names like AT&T
// emails, product identifiers, hostnames
ADJUNCT = "-" | "'" | "&" | "@" | "_" | "."
ALPHANUM_COMP = {ALPHANUM} {ADJUNCT} {ALPHANUM} ({ADJUNCT} {ALPHANUM})*

// floating point, serial, model numbers, ip addresses, etc.
// every other segment must have at least one digit
NUM        = ({ALPHANUM} {P} {HAS_DIGIT}
           | {HAS_DIGIT} {P} {ALPHANUM}
           | {ALPHANUM} ({P} {HAS_DIGIT} {P} {ALPHANUM})+
           | {HAS_DIGIT} ({P} {ALPHANUM} {P} {HAS_DIGIT})+
           | {ALPHANUM} {P} {HAS_DIGIT} ({P} {ALPHANUM} {P} {HAS_DIGIT})+
           | {HAS_DIGIT} {P} {ALPHANUM} ({P} {HAS_DIGIT} {P} {ALPHANUM})+)

// punctuation for numbers
P           = ("_"|"-"|"/"|"."|",")

// at least one digit
HAS_DIGIT  = ({LETTER}|[:digit:])* [:digit:] ({LETTER}|[:digit:])*

// From the JFlex manual: "the expression that matches everything of <a> not matched by <b> is !(!<a>|<b>)"
LETTER     = !(![:letter:]|{CJ})

// Chinese and Japanese (but NOT Korean, which is included in [:letter:])
CJ = [\u3100-\u312f\u3040-\u309F\u30A0-\u30FF\u31F0-\u31FF\u3300-\u337f\u3400-\u4dbf\u4e00-\u9fff\uf900-\ufaff\uff65-\uff9f]

%%

{ALPHANUM}    { return ALPHANUM; }
{ALPHANUM_COMP} { return ALPHANUM_COMP; }
{NUM}         { return NUM; }
{CJ}          { return CJ; }

/** Ignore the rest */
[^]         { /* Break so we don't hit fall-through warning: */ break;/* ignore */ }