package org.xbib.elasticsearch.index.analysis.standardnumber;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.collect.Sets;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.xbib.standardnumber.ISBN;
import org.xbib.standardnumber.StandardNumber;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static org.elasticsearch.common.collect.Lists.newLinkedList;

public class Detector extends AbstractLifecycleComponent<Detector>  {

    private final static ThreadLocal<Set<StandardNumber>> stdnums = new ThreadLocal<Set<StandardNumber>>();

    @Inject
    public Detector(Settings settings) {
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
            set.addAll(types == null ?
                    StandardNumberService.create() :
                    StandardNumberService.create(types));
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
