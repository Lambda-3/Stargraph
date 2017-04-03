package net.stargraph.test;

import net.stargraph.core.Namespace;
import net.stargraph.core.Stargraph;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public final class NamespaceTest {

    private Namespace ns;

    @BeforeClass
    public void beforeClass() {
        Stargraph core = new Stargraph();
        this.ns = core.getNamespace("obama");
    }

    @Test
    public void isFromMainNamespaceTest() {
        String uri = "http://dbpedia.org/resource/BO";
        Assert.assertTrue(ns.isFromMainNS(uri));
    }

    @Test
    public void isNotFromMainNamespaceTest() {
        String uri = "http://dbpedia.org/resource/Category:ABC";
        Assert.assertFalse(ns.isFromMainNS(uri));
    }

}
