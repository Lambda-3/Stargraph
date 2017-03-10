package net.stargraph.core;

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

import java.io.InputStream;
import java.util.Properties;

public final class Version {
    private static String codeName = "Stargraph";
    private static String buildVersion = "Stargraph DEV";
    private static String buildNumber = String.valueOf(System.currentTimeMillis()); //defaults to timestamp

    static {
        try (InputStream is = ClassLoader.getSystemResourceAsStream("build.properties")) {
            Properties properties = new Properties();
            properties.load(is);

            String code = properties.getProperty("code-name");
            if (code != null && !code.isEmpty()) {
                codeName = code;
            }

            String version = properties.getProperty("version");
            if (version != null && !version.isEmpty()) {
                buildVersion = version.replace("stargraph-core", "");
            }

            String gitSHA1 = properties.getProperty("git-sha-1");
            if (gitSHA1 != null && !gitSHA1.isEmpty()) {
                buildNumber = gitSHA1;
            }
        } catch (Exception e) {
            System.err.println("Can't read buildNumber.properties");
        }
    }

    public static String getCodeName() {
        return codeName;
    }

    public static String getBuildVersion() {
        return buildVersion;
    }

    public static String getBuildNumber() {
        return buildNumber;
    }
}
