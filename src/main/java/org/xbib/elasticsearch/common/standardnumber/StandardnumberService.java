package org.xbib.elasticsearch.common.standardnumber;

import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.xbib.elasticsearch.action.isbnformat.ISBNFormatResponse;
import org.xbib.elasticsearch.index.mapper.standardnumber.StandardnumberMapper;
import org.xbib.standardnumber.ISBN;
import org.xbib.standardnumber.NoSuchStandardNumberException;
import org.xbib.standardnumber.StandardNumber;

import java.util.Collection;
import java.util.LinkedList;

/**
 *
 */
public class StandardnumberService extends AbstractLifecycleComponent {

    private static final String[] DEFAULT_STANDARD_NUMBERS =
            { "isbn", "issn", "ismn", "isni",  "orcid", "ppn", "zdb" };

    @Inject
    public StandardnumberService(Settings settings) {
        super(settings);
    }

    /**
     * Called from StandardnumberTokenFilterFactory.
     * @param standardNumberTypeParser the type parser
     */
    public void setStandardNumberTypeParser(StandardnumberMapper.TypeParser standardNumberTypeParser) {
        standardNumberTypeParser.setService(this);
    }

    @Override
    protected void doStart() {
        // nothing to do
    }

    @Override
    protected void doStop() {
        // nothing to do
    }

    @Override
    protected void doClose() {
        // nothing to do
    }

    /**
     * Called from {@link org.xbib.elasticsearch.index.analysis.standardnumber.StandardnumberTokenFilter}.
     * @param settings settings
     * @param content content
     * @return a collection of variants of the detected standard number or an empty collection
     */
    public Collection<CharSequence> lookup(Settings settings, CharSequence content) {
        String[] stdnums = settings.getAsArray("standardnumbers",
                DEFAULT_STANDARD_NUMBERS);
        if (stdnums.length == 0) {
            stdnums = DEFAULT_STANDARD_NUMBERS;
        }
        Collection<CharSequence> variants = new LinkedList<>();
        for (String stdnum : stdnums) {
            try {
                StandardNumber standardNumber = StandardNumber.getInstance(stdnum);
                if (standardNumber instanceof ISBN) {
                    handleISBN((ISBN) standardNumber, content, variants);
                } else {
                    standardNumber = standardNumber.set(content).normalize();
                    if (standardNumber.isValid()) {
                        for (String variant : standardNumber.getTypedVariants()) {
                            if (variant != null) {
                                variants.add(variant);
                            }
                        }
                    }
                }
            } catch (NoSuchStandardNumberException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return variants;
    }

    public void handle(String value, ISBNFormatResponse isbnFormatResponse) {
        ISBN isbn = new ISBN();
        isbn = (ISBN)isbn.set(value).normalize().verify();
        isbnFormatResponse.setIsbn10(isbn.ean(false).normalizedValue());
        isbnFormatResponse.setIsbn10Formatted(isbn.ean(false).format());
        isbnFormatResponse.setIsbn13(isbn.ean(true).normalizedValue());
        isbnFormatResponse.setIsbn13Formatted(isbn.ean(true).format());
    }

    private void handleISBN(ISBN isbn, CharSequence content, Collection<CharSequence> variants) {
        ISBN normalizedISBN = (ISBN)isbn.set(content).normalize();
        if (normalizedISBN.isValid()) {
            if (!normalizedISBN.isEAN()) {
                // create variants: ISBN, ISBN normalized, ISBN-13, ISBN-13 normalized
                variants.add(normalizedISBN.ean(false).format());
                variants.add(normalizedISBN.ean(false).normalizedValue());
                StandardNumber normalizedEAN = normalizedISBN.ean(true).set(content).normalize();
                if (normalizedEAN.isValid()) {
                    variants.add(normalizedEAN.format());
                    variants.add(normalizedEAN.normalizedValue());
                }
            } else {
                // 2 variants, do not create ISBN-10 for an ISBN-13
                variants.add(normalizedISBN.ean(true).format());
                variants.add(normalizedISBN.ean(true).normalizedValue());
            }
        }
    }
}
