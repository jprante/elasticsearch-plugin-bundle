package org.xbib.elasticsearch.plugin.bundle.index.analysis.hyphen;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

%%

%class HyphenTokenizerImpl
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

protected static final String[] TOKEN_TYPES = HyphenTokenizer.TOKEN_TYPES;

public final long yychar() {
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

// Unicode Pd = Punctuation Dash Category
PUNCT_DASH = [\u002D\u058A\u05BE\u1400\u1806\u2010\u2011\u2012\u2013\u2014\u2015\u2E17\u2E1A\u2E3A\u2E3B\u2E40\u301C\u3030\u30A0\uFE31\uFE32\uFE58]

// keep hyphenated / composed word fragments together for the token filter.
// internal apostrophes: O'Reilly, you're, O'Reilly's
// use a post-filter to remove possessives
// acronyms: U.S.A., I.B.M., etc. with dropped last punctuation char.
// company names like AT&T
// emails, product identifiers, hostnames
ADJUNCT = ({PUNCT_DASH} | "'" | "&" | "@" | "_" | "+")+
ALPHANUM_COMP = {ALPHANUM} {ADJUNCT}
              | {ALPHANUM} {ADJUNCT} {ALPHANUM} ({ADJUNCT} {ALPHANUM})*
              | {ALPHANUM} "." {ALPHANUM} ("." {ALPHANUM})*

// floating point, serial, model numbers, ip addresses, etc.
// every other segment must have at least one digit
NUM        = ({ALPHANUM} {P} {HAS_DIGIT}
           | {HAS_DIGIT} {P} {ALPHANUM}
           | {ALPHANUM} ({P} {HAS_DIGIT} {P} {ALPHANUM})+
           | {HAS_DIGIT} ({P} {ALPHANUM} {P} {HAS_DIGIT})+
           | {ALPHANUM} {P} {HAS_DIGIT} ({P} {ALPHANUM} {P} {HAS_DIGIT})+
           | {HAS_DIGIT} {P} {ALPHANUM} ({P} {HAS_DIGIT} {P} {ALPHANUM})+)

// punctuation for mathematical formulas
P           = ("_"|\u002D|"/"|"."|","|[\u2200-\u22FF])

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