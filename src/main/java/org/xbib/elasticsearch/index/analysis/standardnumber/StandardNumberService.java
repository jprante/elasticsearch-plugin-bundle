/*
 * Copyright (C) 2014 Jörg Prante
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 */
package org.xbib.elasticsearch.index.analysis.standardnumber;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.collect.Sets;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
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
import java.util.Set;

import static org.elasticsearch.common.collect.Lists.newLinkedList;

public class StandardnumberService extends AbstractLifecycleComponent<StandardnumberService>  {

    private final static ThreadLocal<Set<StandardNumber>> stdnums = new ThreadLocal<Set<StandardNumber>>();

    @Inject
    public StandardnumberService(Settings settings) {
        super(settings);
    }

    @Override
    protected void doStart() throws ElasticsearchException {
    }

    @Override
    protected void doStop() throws ElasticsearchException {
    }

    @Override
    protected void doClose() throws ElasticsearchException {
    }

    protected Collection<StandardNumber> getStdNums() {
        if (stdnums.get() == null) {
            String[] s = settings.getAsArray("number_types", null);
            Set<String> types = s != null ? Sets.newTreeSet(Arrays.asList(s)) : null;
            Set<StandardNumber> set = Sets.newLinkedHashSet();
            set.addAll(types == null ? create() : create(types));
            stdnums.set(set);
        }
        return stdnums.get();
    }

    public Collection<StandardNumber> detect(CharSequence content) {
        Collection<StandardNumber> candidates = newLinkedList();
        for (StandardNumber stdnum : getStdNums()) {
            stdnum.reset();
            try {
                candidates.add(stdnum.set(content).normalize().verify());
            } catch (NumberFormatException e) {
                // skip
            }
        }
        return candidates;
    }

    public Collection<CharSequence> lookup(CharSequence content) {
        Collection<CharSequence> variants = newLinkedList();
        for (StandardNumber stdnum : getStdNums()) {
            stdnum.reset();
            if (stdnum instanceof ISBN) {
                handleISBN((ISBN) stdnum, content, variants);
            } else {
                stdnum = stdnum.set(content).normalize();
                if (stdnum.isValid()) {
                    for (String s : stdnum.getTypedVariants()) {
                        if (s != null) {
                            variants.add(s);
                        }
                    }
                }
            }
        }
        return variants;
    }


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

    private void handleISBN(ISBN stdnum, CharSequence content, Collection<CharSequence> variants) throws NumberFormatException {
        ISBN isbn = stdnum.set(content).normalize();
        if (isbn.isValid()) {
            if (!isbn.isEAN()) {
                // create variants: ISBN, ISBN normalized, ISBN-13, ISBN-13 normalized
                variants.add(isbn.ean(false).format());
                variants.add(isbn.ean(false).normalizedValue());
                isbn = isbn.ean(true).set(content).normalize();
                if (isbn.isValid()) {
                    variants.add(isbn.format());
                    variants.add(isbn.normalizedValue());
                }
            } else {
                // 2 variants, do not create ISBN-10 for an ISBN-13
                variants.add(isbn.ean(true).format());
                variants.add(isbn.ean(true).normalizedValue());
            }
        }
    }

}
