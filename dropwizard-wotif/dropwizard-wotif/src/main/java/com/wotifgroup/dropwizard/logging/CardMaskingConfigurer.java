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

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.encoder.EncoderBase;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import com.google.common.base.Optional;
import com.wotifgroup.logging.logback.CardMaskingLayoutWrappingEncoder;
import io.dropwizard.logging.AsyncAppender;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class CardMaskingConfigurer {

    public void addCardMasking(final Logger root) {
        for (OutputStreamAppender outputStreamAppender : getOutputStreamAppenders(root)) {
            for (LayoutWrappingEncoder lwe : getLayoutWrappingEncoder(outputStreamAppender).asSet()) {
                addCardMarkingEncoder(outputStreamAppender, lwe);
            }
        }
     }

    private void addCardMarkingEncoder(OutputStreamAppender outputStreamAppender, LayoutWrappingEncoder lwe) {

        final CardMaskingLayoutWrappingEncoder encoder = createCardMaskingEncoder(lwe);

        try {

            lwe.close();

            final Field outputStreamField = EncoderBase.class.getDeclaredField("outputStream");
            outputStreamField.setAccessible(true);

            final OutputStream outputStream = (OutputStream) outputStreamField.get(lwe);
            encoder.init(outputStream);

            outputStreamField.setAccessible(false);
            outputStreamAppender.setEncoder(encoder);

            lwe.stop();
        } catch (Exception ex) {
            System.err.println("Fatal error trying to add card masking filter.");
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }

    }

    private CardMaskingLayoutWrappingEncoder createCardMaskingEncoder(LayoutWrappingEncoder lwe) {
        final CardMaskingLayoutWrappingEncoder encoder = new CardMaskingLayoutWrappingEncoder();
        encoder.setCharset(lwe.getCharset());
        encoder.setContext(lwe.getContext());
        encoder.setLayout(lwe.getLayout());
        encoder.setImmediateFlush(lwe.isImmediateFlush());
        return encoder;
    }

    private Optional<LayoutWrappingEncoder> getLayoutWrappingEncoder(final OutputStreamAppender outputStreamAppender) {
        final Encoder encoder = outputStreamAppender.getEncoder();
        if (encoder instanceof LayoutWrappingEncoder) {
            return Optional.of((LayoutWrappingEncoder) encoder);
        }
        return Optional.absent();
    }

    private Collection<OutputStreamAppender> getOutputStreamAppenders(final Logger logger) {
        final Collection<OutputStreamAppender> appenders = new HashSet<>();
        final Iterator<Appender<ILoggingEvent>> i = logger.iteratorForAppenders();
        while (i.hasNext()) {
            final Appender<ILoggingEvent> a = i.next();
            appenders.addAll(getOutputStreamAppender(a).asSet());
        }
        return appenders;
    }

    private Optional<OutputStreamAppender> getOutputStreamAppender(Appender<ILoggingEvent> a) {
        if (a instanceof OutputStreamAppender) {
            return Optional.of((OutputStreamAppender)a);
        }
        if (a instanceof AsyncAppender) {
            final Appender<ILoggingEvent> delegate = ((AsyncAppender) a).getDelegate();
            return getOutputStreamAppender(delegate);
        }
        return Optional.absent();
    }

}
