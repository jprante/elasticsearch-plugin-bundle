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

| Elasticsearch version    | Plugin        | Release date |
| ------------------------ | ------------- | -------------|
| 2.1.1                    | 2.1.1.0       | Dec 21, 2015 |
| 2.1.0                    | 2.1.0.0       | Nov 27, 2015 |
| 2.0.0                    | 2.0.0.0       | Oct 28, 2015 |
| 1.6.0                    | 1.6.0.0       | Jun 30, 2015 |
| 1.5.2                    | 1.5.2.1       | Jun 30, 2015 |
| 1.5.2                    | 1.5.2.0       | Apr 27, 2015 |
| 1.5.1                    | 1.5.1.0       | Apr 23, 2015 |
| 1.5.0                    | 1.5.0.0       | Mar 31, 2015 |
| 1.4.4                    | 1.4.4.0       | Apr 26, 2015 |
| 1.4.0                    | 1.4.0.6       | Feb 23, 2015 |
| 1.4.0                    | 1.4.0.5       | Jan 28, 2015 |
| 1.4.0                    | 1.4.0.4       | Jan 19, 2015 |
| 1.4.0                    | 1.4.0.3       | Dec 16, 2014 |
| 1.4.0                    | 1.4.0.1       | Nov 10, 2014 |

## Installation

### Elasticsearch 2.x

    ./bin/plugin install http://xbib.org/repository/org/xbib/elasticsearch/plugin/elasticsearch-plugin-bundle/2.1.1.0/elasticsearch-plugin-bundle-2.1.1.0-plugin.zip

### Elasticsearch 1.x

    ./bin/plugin -install bundle -url http://xbib.org/repository/org/xbib/elasticsearch/plugin/elasticsearch-plugin-bundle/1.6.0.0/elasticsearch-plugin-bundle-1.6.0.0-plugin.zip

Do not forget to restart the node after installing.

## Project docs

The Maven project site is available at [Github](http://jprante.github.io/elasticsearch-plugin-bundle)

## Issues

All feedback is welcome! If you find issues, please post them at [Github](https://github.com/jprante/elasticsearch-plugin-bundle/issues)

# Examples

## German normalizer

    {
        "index":{
            "analysis":{
                "filter":{
                    "umlaut":{
                        "type":"german_normalize"
                    }
                },
                "tokenizer" : {
                    "umlaut" : {
                       "type":"standard",
                       "filter" : "umlaut"
                    }
                }
            }
        }
    }

## WordDelimiterFilter2

    {
        "index":{
            "analysis":{
                "filter" : {
                    "wd" : {
                       "type" : "worddelimiter2",
                       "generate_word_parts" : true,
                       "generate_number_parts" : true,
                       "catenate_all" : true,
                       "split_on_case_change" : true,
                       "split_on_numerics" : true,
                       "stem_english_possessive" : true
                    }
                }
            }
        }
    }

## ICU

    {
        "index":{
            "analysis":{
                "analyzer" : {
                    "icu_german_collate" : {
                       "type" : "icu_collation",
                       "language" : "de",
                       "country" : "DE",
                       "strength" : "primary",
                       "rules" : "& ae , a\u0308 & AE , A\u0308& oe , o\u0308 & OE , O\u0308& ue , u\u0308 & UE , u\u0308"
                    }
                }
            }
        }
    }

    {
        "index":{
            "analysis":{
                "char_filter" : {
                    "my_icu_folder" : {
                       "type" : "icu_folding"
                    }
                }
            }
        }
    }

    {
        "index":{
            "analysis":{
                "tokenizer" : {
                    "my_icu_tokenizer" : {
                       "type" : "icu_tokenizer"
                    },
                    "my_hyphen_icu_tokenizer" : {
                       "type" : "icu_tokenizer",
                       "rulefiles" : "Latn:icu/Latin-dont-break-on-hyphens.rbbi"
                    }
                }
            }
        }
    }


## Baseform

    {
     "index":{
        "analysis":{
            "filter":{
                "baseform":{
                    "type" : "baseform",
                    "language" : "de"
                }
            },
            "tokenizer" : {
                "baseform" : {
                   "type" : "standard",
                   "filter" : [ "baseform", "unique" ]
                }
            }
        }
     }
    }



# Example

In the mapping, us a token filter of type "decompound"::

  {
     "index":{
        "analysis":{
            "filter":{
                "decomp":{
                    "type" : "decompound"
                }
            },
            "tokenizer" : {
                "decomp" : {
                   "type" : "standard",
                   "filter" : [ "decomp" ]
                }
            }
        }
     }
  }

"Die Jahresfeier der Rechtsanwaltskanzleien auf dem Donaudampfschiff hat viel Ökosteuer gekostet" will be tokenized into 
"Die", "Die", "Jahresfeier", "Jahr", "feier", "der", "der", "Rechtsanwaltskanzleien", "Recht", "anwalt", "kanzlei", "auf", "auf", "dem",  "dem", "Donaudampfschiff", "Donau", "dampf", "schiff", "hat", "hat", "viel", "viel", "Ökosteuer", "Ökosteuer", "gekostet", "gekosten"

It is recommended to add the `Unique token filter <http://www.elasticsearch.org/guide/reference/index-modules/analysis/unique-tokenfilter.html>`_ to skip tokens that occur more than once.

Also the Lucene german normalization token filter is provided::

      {
        "index":{
            "analysis":{
                "filter":{
                    "umlaut":{
                        "type":"german_normalize"
                    }
                },
                "tokenizer" : {
                    "umlaut" : {
                       "type":"standard",
                       "filter" : "umlaut"
                    }            
                }
            }
        }
      }

The input "Ein schöner Tag in Köln im Café an der Straßenecke" will be tokenized into 
"Ein", "schoner", "Tag", "in", "Koln", "im", "Café", "an", "der", "Strassenecke".

# Threshold

The decomposing algorithm knows about a threshold when to assume words as decomposed successfully or not.
If the threshold is too low, words could silently disappear from being indexed. In this case, you have to adapt the
threshold so words do no longer disappear.

The default threshold value is 0.51. You can modify it in the settings::

      {
         "index" : {
            "analysis" : {
                "filter" : {
                    "decomp" : {
                        "type" : "decompound",
                        "threshold" : 0.51
                    }
                },
                "tokenizer" : {
                    "decomp" : {
                       "type" : "standard",
                       "filter" : [ "decomp" ]
                    }
                }
            }
         }
      }
      
# Subwords
      
Sometimes only the decomposed subwords should be indexed. For this, you can use the parameter `"subwords_only": true`

      {
         "index" : {
            "analysis" : {
                "filter" : {
                    "decomp" : {
                        "type" : "decompound",
                        "subwords_only" : true
                    }
                },
                "tokenizer" : {
                    "decomp" : {
                       "type" : "standard",
                       "filter" : [ "decomp" ]
                    }
                }
            }
         }
      }

# References


The decompunder is a derived work of ASV toolbox http://asv.informatik.uni-leipzig.de/asv/methoden

Copyright (C) 2005 Abteilung Automatische Sprachverarbeitung, Institut für Informatik, Universität Leipzig

The Compact Patricia Trie data structure can be found in 

*Morrison, D.: Patricia - practical algorithm to retrieve information coded in alphanumeric. Journal of ACM, 1968, 15(4):514–534*

The compound splitter used for generating features for document classification is described in

*Witschel, F., Biemann, C.: Rigorous dimensionality reduction through linguistically motivated feature selection for text categorization. Proceedings of NODALIDA 2005, Joensuu, Finland*

The base form reduction step (for Norwegian) is described in

*Eiken, U.C., Liseth, A.T., Richter, M., Witschel, F. and Biemann, C.: Ord i Dag: Mining Norwegian Daily Newswire. Proceedings of FinTAL, Turku, 2006, Finland*



## Langdetect

    curl -XDELETE 'localhost:9200/test'

    curl -XPUT 'localhost:9200/test'

    curl -XPOST 'localhost:9200/test/article/_mapping' -d '
    {
      "article" : {
        "properties" : {
           "content" : { "type" : "langdetect" }
        }
      }
    }
    '

    curl -XPUT 'localhost:9200/test/article/1' -d '
    {
      "title" : "Some title",
      "content" : "Oh, say can you see by the dawn`s early light, What so proudly we hailed at the twilight`s last gleaming?"
    }
    '

    curl -XPUT 'localhost:9200/test/article/2' -d '
    {
      "title" : "Ein Titel",
      "content" : "Einigkeit und Recht und Freiheit für das deutsche Vaterland!"
    }
    '

    curl -XPUT 'localhost:9200/test/article/3' -d '
    {
      "title" : "Un titre",
      "content" : "Allons enfants de la Patrie, Le jour de gloire est arrivé!"
    }
    '

    curl -XGET 'localhost:9200/test/_refresh'

    curl -XPOST 'localhost:9200/test/_search' -d '
    {
       "query" : {
           "term" : {
                "content.lang" : "en"
           }
       }
    }
    '
    curl -XPOST 'localhost:9200/test/_search' -d '
    {
       "query" : {
           "term" : {
                "content.lang" : "de"
           }
       }
    }
    '

    curl -XPOST 'localhost:9200/test/_search' -d '
    {
       "query" : {
           "term" : {
                "content.lang" : "fr"
           }
       }
    }
    '

## Standardnumber

    {
       "index" : {
          "analysis" : {
              "filter" : {
                  "standardnumber" : {
                      "type" : "standardnumber"
                  }
              },
              "analyzer" : {
                  "standardnumber" : {
                      "tokenizer" : "whitespace",
                      "filter" : [ "standardnumber", "unique" ]
                  }
              }
          }
       }
    }

## Hyphen

    {
        "index":{
            "analysis":{
                "tokenizer" : {
                    "my_icu_tokenizer" : {
                       "type" : "icu_tokenizer",
                       "rulefiles" : "Latn:icu/Latin-dont-break-on-hyphens.rbbi"
                    },
                    "my_hyphen_tokenizer" : {
                        "type" : "hyphen"
                    }
                }
            }
        }
    }


## Sortform

    {
        "index":{
            "analysis": {
                "analyzer" : {
                    "german_phonebook_with_sortform" : {
                       "type" : "sortform",
                       "language" : "de",
                       "country" : "DE",
                       "strength" : "quaternary",
                       "alternate" : "shifted",
                       "rules" : "& ae , a\u0308 & AE , A\u0308 & oe , o\u0308 & OE , O\u0308 & ue , u\u0308 & UE , u\u0308 & ss , \u00df",
                       "filter" : [
                           "sortform"
                       ]
                    }
                }
            }
        }
    }


## Crypt mapper

    {
        "someType" : {
            "_source" : {
                "enabled": false
            },
            "properties" : {
                "someField":{ "type" : "crypt", "algo": "SHA-512" }
            }
        }
    }

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