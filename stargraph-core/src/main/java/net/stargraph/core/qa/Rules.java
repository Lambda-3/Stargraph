package net.stargraph.core.qa;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import net.stargraph.Language;
import net.stargraph.UnsupportedLanguageException;
import net.stargraph.core.qa.nli.DataModelType;
import net.stargraph.core.qa.nli.QueryPlan;
import net.stargraph.core.qa.nli.SyntaticRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.*;

public final class Rules {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Marker marker = MarkerFactory.getMarker("qa");

    private Map<Language, List<SyntaticRule>> syntaticRules;
    private Map<Language, List<QueryPlan>> queryPlans;

    public Rules(Config config) {
        logger.info(marker, "Loading Rules.");
        this.syntaticRules = loadRules(Objects.requireNonNull(config));
        this.queryPlans = loadPlans(Objects.requireNonNull(config));
    }

    public List<SyntaticRule> getSyntaticRules(Language language) {
        if (syntaticRules.containsKey(language)) {
            return syntaticRules.get(language);
        }
        throw new UnsupportedLanguageException(language);
    }

    public List<QueryPlan> getQueryPlans(Language language) {
        if (queryPlans.containsKey(language)) {
            return queryPlans.get(language);
        }
        throw new UnsupportedLanguageException(language);
    }

    private Map<Language, List<SyntaticRule>> loadRules(Config config) {
        Map<Language, List<SyntaticRule>> rulesByLang = new HashMap<>();
        ConfigObject configObject = config.getObject("syntatic-patterns");

        configObject.keySet().forEach(strLang -> {
            Language language = Language.valueOf(strLang.toUpperCase());

            rulesByLang.compute(language, (l, r) -> {

                List<SyntaticRule> rules = new ArrayList<>();
                List<? extends Config> patternCfg = configObject.toConfig().getConfigList(strLang);

                for (Config cfg : patternCfg) {
                    Map.Entry<String, ConfigValue> entry = cfg.entrySet().stream().findFirst().orElse(null);
                    String patternStr = entry.getKey();
                    String modelStr = (String) entry.getValue().unwrapped();
                    rules.add(new SyntaticRule(patternStr, DataModelType.valueOf(modelStr)));
                }

                logger.info(marker, "Loaded {} syntatic patterns for '{}'", rules.size(), l);

                return rules;
            });
        });


        return rulesByLang;
    }

    private Map<Language, List<QueryPlan>> loadPlans(Config config) {
        Map<Language, List<QueryPlan>> plansByLang = new HashMap<>();
        ConfigObject configObject = config.getObject("planner-patterns");

        configObject.keySet().forEach(strLang -> {
            Language language = Language.valueOf(strLang.toUpperCase());

            plansByLang.compute(language, (l, q) -> {

                List<QueryPlan> plans = new ArrayList<>();

                Config langCfg = configObject.toConfig().getConfig(strLang);
                langCfg.entrySet().forEach(entry -> {
                    String patternStr = entry.getKey();
                    List<String> tripleStrList = langCfg.getStringList(patternStr);
                    plans.add(new QueryPlan(patternStr, tripleStrList));
                });

                logger.info(marker, "Loaded {} plans patterns for '{}'", plans.size(), l);

                return plans;
            });
        });

        return plansByLang;
    }
}
