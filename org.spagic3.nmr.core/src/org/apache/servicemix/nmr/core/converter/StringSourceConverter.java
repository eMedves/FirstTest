package org.apache.servicemix.nmr.core.converter;

import org.apache.servicemix.nmr.core.util.StringSource;


public class StringSourceConverter {


    public static String toString(StringSource source) {
        return source.getText();
    }
}
