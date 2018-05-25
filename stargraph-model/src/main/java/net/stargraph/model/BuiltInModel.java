package net.stargraph.model;

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

import java.io.Serializable;

/**
 * Naming conventions for the fact based KBs.
 */
public enum BuiltInModel {

    FACT("facts", Fact.class),
    ENTITY("entities", ResourceEntity.class),
    PROPERTY("relations", PropertyEntity.class);

    public Class cls;
    public String modelId;

    BuiltInModel(String modelId, Class cls) {
        this.modelId = modelId;
        this.cls = cls;
    }

    @SuppressWarnings("unchecked")
    public static Class<Serializable> getModelClass(String modelName) {
        // This should change to support user's models.

        for (BuiltInModel entry : BuiltInModel.values()) {
            if (entry.modelId.equals(modelName)) {
                return entry.cls;
            }
        }

        throw new StarGraphException("No Class registered for model: '" + modelName + "'");
    }
}
