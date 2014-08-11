# Elasticsearch German Analysis

A plugin that consists of a compilation of useful Elasticsearch plugins related to indexing and searching documents in german language that are not avaliable in Elasticsearch by default.

- German normalizer (Lucene)

- WordDelimiterFilter2 (Lucene)

- elasticsearch-analysis-icu (with ICU collation analyzer compatible to Lucene 5.x)

- elasticsearch-analysis-baseform

- elasticsearch-analysis-decompund

- elasticsearch-analysis-combo

- elasticsearch-langdetect

- sortform (process string forms for bibliographical sorting, taking non-sort areas into account)

- year (token filter for 4-digit sequences)

## Versions

| Elasticsearch version    | Plugin      | Release date |
| ------------------------ | ----------- | -------------|
| 1.3.1                    | 1.3.0.2     | Aug 11, 2014 |

## Installation

    ./bin/plugin -install analysis-german -url http://xbib.org/repository/org/xbib/elasticsearch/plugin/elasticsearch-analysis-german/1.3.0.2/elasticsearch-analysis-german-1.3.0.2.zip

Do not forget to restart the node after installing.

## Checksum

| File                                          | SHA1                                     |
| --------------------------------------------- | -----------------------------------------|
| elasticsearch-analysis-german-1.3.0.2.zip     | 063a89e4016af637330c5a3d7f51d84c7056b182 |

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