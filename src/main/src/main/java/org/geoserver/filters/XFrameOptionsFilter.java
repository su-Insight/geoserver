/* (c) 2017 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.geoserver.platform.GeoServerExtensions;

/**
 * Simple filter to set X-Frame-Options header to prevent click jacking attacks. This filter is
 * controlled by two system properties: <br>
 * - geoserver.xframe.shouldSetPolicy: controls whether the X-Frame-Options filter should be set at
 * all. Default is true. <br>
 * - geoserver.xframe.policy: controls what the set the X-Frame-Options header to. Default is
 * SAMEORIGIN valid options are DENY, SAMEORIGIN and ALLOW-FROM [uri] <br>
 * These properties can be set via command line -D arg, web.xml init or environment variable.
 */
public class XFrameOptionsFilter implements Filter {

    private static final boolean DEFAULT_SHOULD_SET_POLICY = true;
    private static final String DEFAULT_FRAME_POLICY = "SAMEORIGIN";
    private static final String X_FRAME_OPTIONS = "X-Frame-Options";

    /** The system property to set whether the X-Frame-Options header should be set */
    public static final String GEOSERVER_XFRAME_SHOULD_SET_POLICY =
            "geoserver.xframe.shouldSetPolicy";

    /** The system property for the value of the X-Frame-Options header */
    public static final String GEOSERVER_XFRAME_POLICY = "geoserver.xframe.policy";

    /** The system property to set whether the X-Content-Type-Options header should be set */
    public static final String GEOSERVER_XCONTENT_TYPE_SHOULD_SET_POLICY =
            "geoserver.xContentType.shouldSetPolicy";

    /**
     * Whether the X-Frame-Option header should be set at all. Check this on the fly for easier
     * testing and in order to potentially make this a GUI controlled option in the future.
     */
    private static boolean shouldSetPolicy() {
        boolean shouldSetPolicy = DEFAULT_SHOULD_SET_POLICY;
        if (StringUtils.isNotEmpty(
                GeoServerExtensions.getProperty(GEOSERVER_XFRAME_SHOULD_SET_POLICY))) {
            shouldSetPolicy =
                    Boolean.parseBoolean(
                            GeoServerExtensions.getProperty(GEOSERVER_XFRAME_SHOULD_SET_POLICY));
        }

        return shouldSetPolicy;
    }

    private static String getFramePolicy() {
        String framePolicy = DEFAULT_FRAME_POLICY;
        if (StringUtils.isNotEmpty(GeoServerExtensions.getProperty(GEOSERVER_XFRAME_POLICY))) {
            framePolicy = GeoServerExtensions.getProperty(GEOSERVER_XFRAME_POLICY);
        }

        return framePolicy;
    }

    /**
     * Whether the X-Content-Type-Options header should be set at all. Check this on the fly for
     * easier testing and in order to potentially make this a GUI controlled option in the future.
     */
    private static boolean shouldSetContentTypePolicy() {
        boolean shouldSetPolicy = true;
        if (StringUtils.isNotEmpty(
                GeoServerExtensions.getProperty(GEOSERVER_XCONTENT_TYPE_SHOULD_SET_POLICY))) {
            shouldSetPolicy =
                    Boolean.parseBoolean(
                            GeoServerExtensions.getProperty(
                                    GEOSERVER_XCONTENT_TYPE_SHOULD_SET_POLICY));
        }
        return shouldSetPolicy;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        if (shouldSetPolicy()) {
            httpResponse.setHeader(X_FRAME_OPTIONS, getFramePolicy());
        }
        if (shouldSetContentTypePolicy()) {
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
