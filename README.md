# Elasticsearch German Analysis

A plugin that consists of a compilation of useful Elasticsearch plugins related to indexing and searching documents in german language that are not avaliable in Elasticsearch by default.

- German normalizer: taken from Lucene

- WordDelimiterFilter2: taken from Lucene

- elasticsearch-analysis-icu: improved with ICU collation analyzer compatible to Lucene 5.x and rbbi ICU rule file support in ICU tokenizer

- elasticsearch-analysis-baseform: index also base forms of words (german, english)

- elasticsearch-analysis-decompound: decompose words if possible (german) 

- elasticsearch-analysis-combo: apply more than one analyzer on a field

- elasticsearch-langdetect: index language code of detected languages

- hyphen: token filter for shingling and combining hyphenated words (german: Bindestrichwörter), the opposite of the decompound token filter

- sortform: process string forms for bibliographical sorting, taking non-sort areas into account

- year: token filter for 4-digit sequences

## Versions

| Elasticsearch version    | Plugin      | Release date |
| ------------------------ | ----------- | -------------|
| 1.3.2                    | 1.3.2.1     | Sep 11, 2014 |
| 1.3.1                    | 1.3.0.2     | Aug 11, 2014 |

## Installation

    ./bin/plugin -install analysis-german -url http://xbib.org/repository/org/xbib/elasticsearch/plugin/elasticsearch-analysis-german/1.3.2.1/elasticsearch-analysis-german-1.3.2.1-plugin.zip

Do not forget to restart the node after installing.

## Checksum

| File                                                 | SHA1                                     |
| ---------------------------------------------------- | -----------------------------------------|
| elasticsearch-analysis-german-1.3.2.1-plugin.zip     | 40c99842a7300041e2e8f594715adbe353d1b0d2 |
| elasticsearch-analysis-german-1.3.0.2-plugin.zip     | 063a89e4016af637330c5a3d7f51d84c7056b182 |

## Project docs

The Maven project site is available at [Github](http://jprante.github.io/elasticsearch-analysis-german)

## Issues

All feedback is welcome! If you find issues, please post them at [Github](https://github.com/jprante/elasticsearch-analysis-german/issues)

# License

elasticsearch-analysis-german - a compilation of useful plugins for german

Copyright (C) 2013 Jörg Prante

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.