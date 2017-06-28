package com.codebrig.jvmmechanic.agent;

import com.google.common.collect.Maps;

import java.io.*;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

/**
 * Most code copied from java.util.Properties. This class is used to remove comments from Properties
 * and to save only the updated properties when store() is called.
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class ConfigProperties extends Properties {

    private ConcurrentMap<Object, Object> updatedPropertiesMap = Maps.newConcurrentMap();
    private String configFilePath;
    private OutputStream out;

    public ConfigProperties(String configFilePath) throws IOException {
        this.configFilePath = configFilePath;
        if (new File(configFilePath).exists()) {
            load(new FileInputStream(configFilePath));
        }
    }

    public void sync() {
        try {
            if (out == null) {
                synchronized (this) {
                    out = new FileOutputStream(configFilePath);
                }
            }
            store(out);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public synchronized Object put(Object key, Object value) {
        Object currentValue = get(key);
        if (!Objects.equals(currentValue, value)) {
            super.put(key, value);
            return updatedPropertiesMap.put(key, value);
        } else {
            return super.put(key, value);
        }
    }

    void store(OutputStream out) throws IOException {
        store0(new BufferedWriter(new OutputStreamWriter(out, "8859_1")), true);
    }

    private void store0(BufferedWriter bw, boolean escUnicode) throws IOException {
        synchronized (this) {
            boolean updatedFile = !updatedPropertiesMap.isEmpty();
            for (Object keyOb : updatedPropertiesMap.keySet()) {
                String key = (String) keyOb;
                String val = (String) updatedPropertiesMap.remove(keyOb);
                key = saveConvert(key, true, escUnicode);
                /* No need to escape embedded and trailing spaces for value, hence
                 * pass false to flag.
                 */
                val = saveConvert(val, false, escUnicode);

                bw.write(key + "=" + val);
                bw.newLine();
            }

            if (updatedFile) {
                bw.flush();
            }
        }
    }

    /*
     * Converts unicodes to encoded &#92;uxxxx and escapes
     * special characters with a preceding slash
     */
    private String saveConvert(String theString, boolean escapeSpace, boolean escapeUnicode) {
        if (theString == null) {
            return null;
        }

        int len = theString.length();
        int bufLen = len * 2;
        if (bufLen < 0) {
            bufLen = Integer.MAX_VALUE;
        }
        StringBuffer outBuffer = new StringBuffer(bufLen);

        for(int x=0; x<len; x++) {
            char aChar = theString.charAt(x);
            // Handle common case first, selecting largest block that
            // avoids the specials below
            if ((aChar > 61) && (aChar < 127)) {
                if (aChar == '\\') {
                    outBuffer.append('\\'); outBuffer.append('\\');
                    continue;
                }
                outBuffer.append(aChar);
                continue;
            }
            switch(aChar) {
                case ' ':
                    if (x == 0 || escapeSpace)
                        outBuffer.append('\\');
                    outBuffer.append(' ');
                    break;
                case '\t':outBuffer.append('\\'); outBuffer.append('t');
                    break;
                case '\n':outBuffer.append('\\'); outBuffer.append('n');
                    break;
                case '\r':outBuffer.append('\\'); outBuffer.append('r');
                    break;
                case '\f':outBuffer.append('\\'); outBuffer.append('f');
                    break;
                case '=': // Fall through
                case ':': // Fall through
                case '#': // Fall through
                case '!':
                    outBuffer.append('\\'); outBuffer.append(aChar);
                    break;
                default:
                    if (((aChar < 0x0020) || (aChar > 0x007e)) & escapeUnicode ) {
                        outBuffer.append('\\');
                        outBuffer.append('u');
                        outBuffer.append(toHex((aChar >> 12) & 0xF));
                        outBuffer.append(toHex((aChar >>  8) & 0xF));
                        outBuffer.append(toHex((aChar >>  4) & 0xF));
                        outBuffer.append(toHex( aChar        & 0xF));
                    } else {
                        outBuffer.append(aChar);
                    }
            }
        }
        return outBuffer.toString();
    }

    /**
     * Convert a nibble to a hex character
     * @param   nibble  the nibble to convert.
     */
    private static char toHex(int nibble) {
        return hexDigit[(nibble & 0xF)];
    }

    /** A table of hex digits */
    private static final char[] hexDigit = {
            '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };

}
