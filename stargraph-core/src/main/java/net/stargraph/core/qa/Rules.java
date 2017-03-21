package net.stargraph.core.qa;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import net.stargraph.Language;
import net.stargraph.UnsupportedLanguageException;
import net.stargraph.core.qa.nli.DataModelType;
import net.stargraph.core.qa.nli.DataModelTypePattern;
import net.stargraph.core.qa.nli.QueryPlanPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class Rules {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Marker marker = MarkerFactory.getMarker("qa");

    private Map<Language, List<DataModelTypePattern>> dataModelTypePatterns;
    private Map<Language, List<QueryPlanPattern>> queryPlanPatterns;
    private Map<Language, List<Pattern>> stopPatterns;

    public Rules(Config config) {
        logger.info(marker, "Loading Rules.");
        this.dataModelTypePatterns = loadDataModelTypePatterns(Objects.requireNonNull(config));
        this.queryPlanPatterns = loadQueryPlanPatterns(Objects.requireNonNull(config));
        this.stopPatterns = loadStopPatterns(config);
    }

    public List<DataModelTypePattern> getDataModelTypeRules(Language language) {
        if (dataModelTypePatterns.containsKey(language)) {
            return dataModelTypePatterns.get(language);
        }
        throw new UnsupportedLanguageException(language);
    }

    public List<QueryPlanPattern> getQueryPlanRules(Language language) {
        if (queryPlanPatterns.containsKey(language)) {
            return queryPlanPatterns.get(language);
        }
        throw new UnsupportedLanguageException(language);
    }

    public List<Pattern> getStopRules(Language language) {
        if (stopPatterns.containsKey(language)) {
            return stopPatterns.get(language);
        }
        throw new UnsupportedLanguageException(language);
    }

    private Map<Language, List<DataModelTypePattern>> loadDataModelTypePatterns(Config config) {
        Map<Language, List<DataModelTypePattern>> rulesByLang = new HashMap<>();
        ConfigObject configObject = config.getObject("rules.syntatic-pattern");

        configObject.keySet().forEach(strLang -> {
            Language language = Language.valueOf(strLang.toUpperCase());

            rulesByLang.compute(language, (l, r) -> {

                List<DataModelTypePattern> rules = new ArrayList<>();
                List<? extends Config> patternCfg = configObject.toConfig().getConfigList(strLang);

                for (Config cfg : patternCfg) {
                    Map.Entry<String, ConfigValue> entry = cfg.entrySet().stream().findFirst().orElse(null);
                    String modelStr = entry.getKey();
                    @SuppressWarnings("unchecked")
                    List<String> patternList = (List<String>) entry.getValue().unwrapped();
                    rules.addAll(patternList.stream()
                            .map(p -> new DataModelTypePattern(p, DataModelType.valueOf(modelStr)))
                            .collect(Collectors.toList()));
                }

                logger.trace(marker, "All rules for '{}': {}", l, rules);

                logger.info(marker, "Loaded {} Data Model Type patterns for '{}'", rules.size(), l);

                return rules;
            });
        });


        return rulesByLang;
    }

    private Map<Language, List<QueryPlanPattern>> loadQueryPlanPatterns(Config config) {
        Map<Language, List<QueryPlanPattern>> rulesByLang = new HashMap<>();
        ConfigObject configObject = config.getObject("rules.planner-pattern");

        configObject.keySet().forEach(strLang -> {
            Language language = Language.valueOf(strLang.toUpperCase());

            rulesByLang.compute(language, (l, q) -> {

                List<QueryPlanPattern> plans = new ArrayList<>();

                Config langCfg = configObject.toConfig().getConfig(strLang);
                langCfg.entrySet().forEach(entry -> {
                    String patternStr = entry.getKey();
                    List<String> tripleStrList = langCfg.getStringList(patternStr);
                    plans.add(new QueryPlanPattern(patternStr, tripleStrList));
                });

                logger.info(marker, "Loaded {} Query Plan patterns for '{}'", plans.size(), l);

                return plans;
            });
        });

        return rulesByLang;
    }

    private Map<Language, List<Pattern>> loadStopPatterns(Config config) {
        Map<Language, List<Pattern>> stopByLang = new HashMap<>();
        ConfigObject configObject = config.getObject("rules.stop-pattern");

        configObject.keySet().forEach(strLang -> {
            Language language = Language.valueOf(strLang.toUpperCase());
            List<String> patternStr = configObject.toConfig().getStringList(strLang);

            stopByLang.compute(language,
                    (lang, pattern) -> patternStr.stream().map(Pattern::compile).collect(Collectors.toList()));

            logger.info(marker, "Loaded {} Stop patterns for '{}'", stopByLang.get(language).size(), language);

        });

        return stopByLang;
    }
}
