# A plugin bundle for Elastisearch

A plugin that consists of a compilation of useful Elasticsearch plugins related to indexing and searching documents.

- German normalizer: taken from Lucene

- WordDelimiterFilter2: taken from Lucene

- ICU: improved with ICU collation analyzer compatible to Lucene 5.x and rbbi ICU rule file support in ICU tokenizer

- baseform: index also base forms of words (german, english)

- decompound: decompose words if possible (german) 

- combo: apply more than one analyzer on a field

- langdetect: find language code of detected languages

- standardnumber: standard number entity recognition

- hyphen: token filter for shingling and combining hyphenated words (german: Bindestrichwörter), the opposite of the decompound token filter

- sortform: process string forms for bibliographical sorting, taking non-sort areas into account

- year: token filter for 4-digit sequences

- reference: 

## Versions

| Elasticsearch version    | Plugin      | Release date |
| ------------------------ | ----------- | -------------|
| 1.4.0                    | 1.4.0.1     | Nov 10, 2014 |

## Installation

    ./bin/plugin -install bundle -url http://xbib.org/repository/org/xbib/elasticsearch/plugin/elasticsearch-plugin-bundle/1.4.0.1/elasticsearch-plugin-bundle-1.4.0.1-plugin.zip

Do not forget to restart the node after installing.

## Checksum

| File                                                 | SHA1                                     |
| ---------------------------------------------------- | -----------------------------------------|
|      |  |

## Project docs

The Maven project site is available at [Github](http://jprante.github.io/elasticsearch-plugin-bundle)

## Issues

All feedback is welcome! If you find issues, please post them at [Github](https://github.com/jprante/elasticsearch-plugin-bundle/issues)

# License

elasticsearch-plugin-bundle - a compilation of useful plugins for Elasticsearch

Copyright (C) 2014 Jörg Prante

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