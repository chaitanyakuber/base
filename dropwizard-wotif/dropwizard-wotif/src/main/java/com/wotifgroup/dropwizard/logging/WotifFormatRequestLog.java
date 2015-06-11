/*
 * #%L
 * dropwizard-wotif
 * %%
 * Copyright (C) 2015 Wotif Group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.wotifgroup.dropwizard.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import io.dropwizard.jetty.Slf4jRequestLog;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.DateCache;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

/**
 * Custom request log formatter: overrides org.eclipse.jetty.server.AbstractNCSARequestLog#log to customise the request logs in a
 * Wotif standards compliant format.
 */
public class WotifFormatRequestLog extends Slf4jRequestLog {
    private static final String TIME_ZONE_ID = "GMT+10";

    // org.eclipse.jetty.server.AbstractNCSARequestLog#_buffers is private, so is replicated here
    private static ThreadLocal<StringBuilder> buffers = new ThreadLocal<StringBuilder>() {
        @Override
        protected StringBuilder initialValue() {
            return new StringBuilder(256);
        }
    };
    // org.eclipse.jetty.server.AbstractNCSARequestLog#_logDateCache is private, so is replicated here
    private final DateCache logDateCache;

    private final Set<String> cookies;

    public WotifFormatRequestLog(AppenderAttachableImpl<ILoggingEvent> appenders, List<String> cookies) {
        super(appenders, TimeZone.getTimeZone(TIME_ZONE_ID));
        logDateCache = new DateCache("dd/MMM/yyyy:HH:mm:ss Z", Locale.getDefault());
        logDateCache.setTimeZoneID(TIME_ZONE_ID);
        this.cookies = new HashSet<>(cookies);
    }

    @Override
    public void log(Request request, Response response) {
        try {
            if (!isEnabled()) {
                return;
            }

            StringBuilder buf = buffers.get();
            buf.setLength(0);

            String addr = request.getHeader(HttpHeader.X_FORWARDED_FOR.toString());

            if (addr == null) {
                addr = request.getRemoteAddr();
            }

            buf.append(addr);
            buf.append(" - ");
            Authentication authentication = request.getAuthentication();
            if (authentication instanceof Authentication.User) {
                buf.append(((Authentication.User) authentication).getUserIdentity().getUserPrincipal().getName());
            } else {
                buf.append("-");
            }

            buf.append(" [");
            buf.append(logDateCache.format(request.getTimeStamp()));

            buf.append("] \"");
            buf.append(request.getMethod());
            buf.append(' ');
            buf.append(request.getUri().toString());
            buf.append(' ');
            buf.append(request.getProtocol());
            buf.append("\" ");

            int status = response.getStatus();
            if (status <= 0) {
                status = 404;
            }
            buf.append((char) ('0' + ((status / 100) % 10)));
            buf.append((char) ('0' + ((status / 10) % 10)));
            buf.append((char) ('0' + (status % 10)));

            long responseLength = response.getLongContentLength();

            if (responseLength < 0) {
                responseLength = response.getContentCount();
            }

            if (responseLength >= 0) {
                buf.append(' ');
                if (responseLength > 99999) {
                    buf.append(responseLength);
                } else {
                    if (responseLength > 9999) {
                        buf.append((char) ('0' + ((responseLength / 10000) % 10)));
                    }
                    if (responseLength > 999) {
                        buf.append((char) ('0' + ((responseLength / 1000) % 10)));
                    }
                    if (responseLength > 99) {
                        buf.append((char) ('0' + ((responseLength / 100) % 10)));
                    }
                    if (responseLength > 9) {
                        buf.append((char) ('0' + ((responseLength / 10) % 10)));
                    }
                    buf.append((char) ('0' + (responseLength) % 10));
                }
                buf.append(' ');
            } else {
                buf.append(" - ");
            }

            logExtended(request, response, buf);

            long now = System.currentTimeMillis();

            // Duplicate response time for compatibility with legacy format, which included both dispatch time and response time.
            // Dispatch time was actually removed from Request in Jetty 8, but the fallback has always been response time so I
            // suspect that it was always just two of the same value anyway. I suspect that it's because it was a mostly redundant
            // value that it was removed, but I can't find any discussion around its removal. The closest I've found is this:
            // http://git.eclipse.org/c/jetty/org.eclipse.jetty.project.git/commit/jetty-server/src/main/java/org/eclipse \
            // /jetty/server/AbstractNCSARequestLog.java?id=c33db24d948b69fbe4b96cc9b52d80ca1580e3f7
            buf.append(' ');
            buf.append(now - request.getTimeStamp());

            buf.append(' ');
            buf.append(now - request.getTimeStamp());

            // Wotif customisation: include requestId
            buf.append(' ');
            buf.append(request.getAttribute("requestId"));

            // Wotif customisation: include session cookies
            buf.append(" \"");
            if (request.getCookies() != null) {
                boolean firstCookie = true;
                for (Cookie cookie : request.getCookies()) {
                    if (cookies.contains(cookie.getName())) {
                        if (!firstCookie) {
                            buf.append("; ");
                        }
                        buf.append(cookie.getName());
                        buf.append('=');
                        buf.append(cookie.getValue());
                        if (firstCookie) {
                            firstCookie = false;
                        }
                    }
                }
            }
            buf.append('"');

            String log = buf.toString();
            write(log);
        } catch (IOException e) {
            LOG.warn(e);
        }
    }

}
