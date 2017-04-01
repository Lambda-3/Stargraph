package net.stargraph.core.query.annotator;

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

public final class EnglishPOSSet extends PartOfSpeechSet {

    public static POSTag CC = new POSTag("CC");
    public static POSTag CD = new POSTag("CD");
    public static POSTag DT = new POSTag("DT");
    public static POSTag EX = new POSTag("EX");
    public static POSTag FW = new POSTag("FW");
    public static POSTag IN = new POSTag("IN");

    public static POSTag JJ = new POSTag("JJ");
    public static POSTag JJR = new POSTag("JJR");
    public static POSTag JJS = new POSTag("JJS");
    public static POSTag LS = new POSTag("LS");
    public static POSTag MD = new POSTag("MD");
    public static POSTag NN = new POSTag("NN");

    public static POSTag NNS = new POSTag("NNS");
    public static POSTag NNP = new POSTag("NNP");
    public static POSTag NNPS = new POSTag("NNPS");
    public static POSTag PDT = new POSTag("PDT");
    public static POSTag POS = new POSTag("POS");
    public static POSTag PRP = new POSTag("PRP");

    public static POSTag PRP$ = new POSTag("PRP$");
    public static POSTag RB = new POSTag("RB");
    public static POSTag RBR = new POSTag("RBR");
    public static POSTag RBS = new POSTag("RBS");
    public static POSTag RP = new POSTag("RP");
    public static POSTag SYM = new POSTag("SYM");

    public static POSTag TO = new POSTag("TO");
    public static POSTag UH = new POSTag("UH");
    public static POSTag VB = new POSTag("VB");
    public static POSTag VBD = new POSTag("VBD");
    public static POSTag VBG = new POSTag("VBG");
    public static POSTag VBN = new POSTag("VBN");

    public static POSTag VBP = new POSTag("VBP");
    public static POSTag VBZ = new POSTag("VBZ");
    public static POSTag WDT = new POSTag("WDT");
    public static POSTag WP = new POSTag("WP");
    public static POSTag WP$ = new POSTag("WP$");
    public static POSTag WRB = new POSTag("WRB");
    public static POSTag PUNCT = new POSTag(".");

    private EnglishPOSSet() {
        add(CC);
        add(CD);
        add(DT);
        add(EX);
        add(FW);
        add(IN);
        add(JJ);
        add(JJR);
        add(JJS);
        add(LS);
        add(MD);
        add(NN);
        add(NNS);
        add(NNP);
        add(NNPS);
        add(PDT);
        add(POS);
        add(PRP);
        add(PRP$);
        add(RB);
        add(RBR);
        add(RBS);
        add(RP);
        add(SYM);
        add(TO);
        add(UH);
        add(VB);
        add(VBD);
        add(VBG);
        add(VBN);
        add(VBP);
        add(VBZ);
        add(WDT);
        add(WP);
        add(WP$);
        add(WRB);
        add(PUNCT);
    }

    private static EnglishPOSSet instance;

    static EnglishPOSSet getInstance() {
        if (instance == null) {
            instance = new EnglishPOSSet();
        }
        return instance;
    }
}
