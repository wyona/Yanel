/*
 * Copyright 2006 Wyona
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.wyona.org/licenses/APACHE-LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wyona.yanel.servlet.communication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import java.io.UnsupportedEncodingException;

/**
 * This class wraps a HttpServlerRequest and does parameter decoding.
 * 
 * TODO: implement getParameterValues() and getParameterMap()
 */
public class HttpRequest extends HttpServletRequestWrapper {
    public static String form_encoding = "UTF-8";
    public static String container_encoding = "ISO-8859-1";
    
    public HttpRequest(HttpServletRequest request) {
        super(request);
    }
    
    public String getParameter(String name) {
        String value = super.getParameter(name);
        if (form_encoding == null || container_encoding == null || value == null) {
            return value;
        }
        // Form and container encoding are equal, skip expensive value decoding
        if (container_encoding.equals(form_encoding)) {
            return value;
        }
        return decode(value);
    }

    private static String decode(String str) {
        if (str == null) return null;
        try {
            if (container_encoding == null)
                container_encoding = "ISO-8859-1";
            byte[] bytes = str.getBytes(container_encoding);
            return new String(bytes, form_encoding);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Unsupported Encoding Exception", uee);
        }
    }
}
