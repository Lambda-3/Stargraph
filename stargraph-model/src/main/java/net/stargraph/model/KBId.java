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

/**
 * Identity for a tuple (kb, model)
 */
public final class KBId {

    private String id;
    private String model;

    private KBId(String id, String model) {
        this.id = id;
        this.model = model;
    }

    public static KBId of(String id, String model) {
        if (id == null || model == null || id.isEmpty() || model.isEmpty()) {
            throw new IllegalArgumentException(String.format("id=%s, type=%s", id, model));
        }
        return new KBId(id, model);
    }

    public String getModelPath() {
        return String.format("kb.%s.model.%s", id, model);
    }

    public String getKBPath() {
        return String.format("kb.%s", id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KBId kbId = (KBId) o;

        return id.equals(kbId.id) && model.equals(kbId.model);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + model.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s.%s", id, model);
    }

    public String getId() {
        return id;
    }

    public String getModel() {
        return model;
    }

}
