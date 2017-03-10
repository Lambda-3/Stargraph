package net.stargraph.rank;

/*-
 * ==========================License-Start=============================
 * stargraph-model
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

import net.stargraph.StarGraphException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.stargraph.rank.Threshold.ThresholdType.MIN;

public final class ThresholdFilter {
    private static Logger logger = LoggerFactory.getLogger(ThresholdFilter.class);
    private static Marker marker = MarkerFactory.getMarker("rank");


    public static Scores filter(Scores inputScores, Threshold threshold) {
        if (Objects.requireNonNull(inputScores).isEmpty()) {
            logger.warn(marker, "Nothing to filter.");
            return inputScores;
        }

        Scores filtered = new Scores();

        switch (threshold.type) {
            case AUTO:

                if (inputScores.size() < 3) {
                    return inputScores;
                }

                Iterator<Score> it = inputScores.iterator();
                Score previous = it.next();
                double max = Double.MIN_VALUE;

                while (it.hasNext()) {
                    Score curr = it.next();
                    double diff = previous.getValue() - curr.getValue();
                    if (diff > max) {
                        max = diff;
                    }
                    previous = curr;
                }

                logger.debug(marker, "AUTO threshold value is {}", max);

                it = inputScores.iterator();
                previous = it.next();
                filtered.add(previous);

                while (it.hasNext()) {
                    Score curr = it.next();
                    double diff = previous.getValue() - curr.getValue();
                    if (diff < max) {
                        filtered.add(curr);
                    } else {
                        break;
                    }
                    previous = curr;
                }

                break;

            case MIN:
            case MAX:
                filtered = new Scores(inputScores.stream().
                        filter(s -> threshold.type == MIN ? s.getValue() > threshold.value : s.getValue() < threshold.value)
                        .collect(Collectors.toList()));
                break;

            default:
                throw new StarGraphException("Unknown Threshold Type");
        }

        if (filtered.isEmpty()) {
            logger.warn(marker, "Nothing left. All filtered by ({})", threshold);
        }

        logger.trace(marker, "After {}: {}", threshold, filtered);

        return filtered;
    }

}
