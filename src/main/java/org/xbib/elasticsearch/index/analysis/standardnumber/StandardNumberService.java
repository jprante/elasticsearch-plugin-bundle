package org.xbib.elasticsearch.index.analysis.standardnumber;

import org.xbib.standardnumber.ARK;
import org.xbib.standardnumber.DOI;
import org.xbib.standardnumber.EAN;
import org.xbib.standardnumber.GTIN;
import org.xbib.standardnumber.IBAN;
import org.xbib.standardnumber.ISAN;
import org.xbib.standardnumber.ISBN;
import org.xbib.standardnumber.ISMN;
import org.xbib.standardnumber.ISNI;
import org.xbib.standardnumber.ISSN;
import org.xbib.standardnumber.ISTC;
import org.xbib.standardnumber.ISWC;
import org.xbib.standardnumber.ORCID;
import org.xbib.standardnumber.PPN;
import org.xbib.standardnumber.StandardNumber;
import org.xbib.standardnumber.UPC;
import org.xbib.standardnumber.ZDB;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.elasticsearch.common.collect.Lists.newLinkedList;

public class StandardNumberService {

    public static StandardNumber create(String type) {
        switch (type.toLowerCase()) {
            case "ark" : return new ARK();
            case "doi" : return new DOI();
            case "ean" : return new EAN();
            case "gtin": return new GTIN();
            case "iban": return new IBAN();
            case "isan": return new ISAN();
            case "isbn": return new ISBN();
            case "ismn": return new ISMN();
            case "isni": return new ISNI();
            case "issn": return new ISSN();
            case "istc": return new ISTC();
            case "iswc": return new ISWC();
            case "orcid": return new ORCID();
            case "ppn": return new PPN();
            case "upc": return new UPC();
            case "zdb": return new ZDB();
        }
        return null;
    }

    public static Collection<StandardNumber> create(Collection<String> types) {
        List<StandardNumber> stdnums = newLinkedList();
        for (String type : types) {
            stdnums.add(create(type));
        }
        return stdnums;
    }

    // do not contains ISTC and SICI by default, too broad character pattern filter mangles up everything.
    public static Collection<StandardNumber> create() {
        StandardNumber[] array = new StandardNumber[] {
                new ARK(),
                new DOI(),
                new EAN(),
                new GTIN(),
                new IBAN(),
                new ISAN(),
                new ISBN(),
                new ISMN(),
                new ISNI(),
                new ISSN(),
                new ISWC(),
                new ORCID(),
                new PPN(),
                new UPC(),
                new ZDB()
        };
        return Arrays.asList(array);
    }

}
