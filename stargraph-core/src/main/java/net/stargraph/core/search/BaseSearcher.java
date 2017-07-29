package net.stargraph.core.search;

/*-
 * ==========================License-Start=============================
 * stargraph-core
 * --------------------------------------------------------------------
 * Copyright (C) 2017 Lambda^3
 * --------------------------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ==========================License-End===============================
 */

import net.stargraph.core.Stargraph;
import net.stargraph.model.KBId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Objects;

public abstract class BaseSearcher implements Searcher {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected Marker marker = MarkerFactory.getMarker("search");
    protected Stargraph stargraph;
    protected KBId kbId;
    private boolean running;

    public BaseSearcher(KBId kbId, Stargraph stargraph) {
        this.stargraph = Objects.requireNonNull(stargraph);
        this.kbId = Objects.requireNonNull(kbId);
    }

    @Override
    public synchronized final void start() {
        if (running) {
            throw new IllegalStateException("Already started!");
        }
        onStart();
        running = true;
    }

    @Override
    public synchronized final void stop() {
        if (!running) {
            logger.error(marker, "Searcher already stopped.");
        } else {
            try {
                onStop();
                running = false;
            }
            catch (Exception e) {
                logger.error(marker, "Fail to stop.", e);
            }

        }
    }

    protected void onStart() {
        // Specific implementation detail
    }

    protected void onStop() {
        // Specific implementation detail
    }

}
