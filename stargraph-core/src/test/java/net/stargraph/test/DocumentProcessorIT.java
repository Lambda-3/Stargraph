package net.stargraph.test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.stargraph.core.Stargraph;
import net.stargraph.core.processors.PassageProcessor;
import net.stargraph.data.Indexable;
import net.stargraph.data.processor.Holder;
import net.stargraph.model.Document;
import net.stargraph.model.KBId;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

@SuppressWarnings("unchecked")
public final class DocumentProcessorIT {
    KBId kbId = KBId.of("dbpedia-2016", "documents");
    PassageProcessor processor;
    String text;

    @BeforeClass
    public void beforeClass() throws Exception {
        ConfigFactory.invalidateCaches();
        Config config = ConfigFactory.load();
        Stargraph core = new Stargraph();
        Config processorCfg = config.getConfig("processor").withOnlyPath(PassageProcessor.name);
        processor = new PassageProcessor(core, processorCfg);
        URI u = getClass().getClassLoader().getResource("anarchism.txt").toURI();
        Assert.assertNotNull(u);
        text = new String(Files.readAllBytes(Paths.get(u)));
    }

    @Test
    public void processTest() {
        Holder holder = new Indexable(new Document("anarchism.txt", "Anarchism", text), kbId);
        processor.run(holder);
        Assert.assertFalse(holder.isSinkable());
    }
}
