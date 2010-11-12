package org.wyona.yanel.servlet.toolbar;

import javax.servlet.http.HttpServletRequest;

import org.wyona.yanel.core.Resource;
import org.wyona.yanel.core.map.Map;

/**
 * Generates toolbar markup, to be merged with the HTML stream generated by
 * views.
 */
public interface YanelToolbar {

    /**
     * Generates markup to be inserted immediately after the first {@literal
     * <head>} tag.
     * 
     * @param resource the resource being accessed
     * @param request the request being serviced
     * @param map Yanel's map of URLs to realms
     * @param reservedPrefix Yanel's URL prefix for accessing global resources
     * @return XHTML markup
     * @throws YanelToolbarException if something goes wrong
     */
    String getToolbarHeader(Resource resource, HttpServletRequest request, Map map, String reservedPrefix);

    /**
     * Generates markup to be inserted immediately after the first {@literal
     * <body>} tag.
     * 
     * @param resource the resource being accessed
     * @param request the request being serviced
     * @param map Yanel's map of URLs to realms
     * @param reservedPrefix Yanel's URL prefix for accessing global resources
     * @return XHTML markup
     * @throws YanelToolbarException if something goes wrong
     */
    String getToolbarBodyStart(Resource resource, HttpServletRequest request, Map map, String reservedPrefix);

    /**
     * Generates markup to be inserted immediately after the last {@literal
     * </body>} tag.
     * 
     * @param resource the resource being accessed
     * @param request the request being serviced
     * @param map Yanel's map of URLs to realms
     * @param reservedPrefix Yanel's URL prefix for accessing global resources
     * @return XHTML markup
     * @throws YanelToolbarException if something goes wrong
     */
    String getToolbarBodyEnd(Resource resource, HttpServletRequest request, Map map, String reservedPrefix);

}