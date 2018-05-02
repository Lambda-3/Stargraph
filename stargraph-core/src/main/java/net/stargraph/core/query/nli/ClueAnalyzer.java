package net.stargraph.core.query.nli;

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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.stargraph.core.query.Analyzers;
import net.stargraph.core.query.annotator.Annotator;
import net.stargraph.core.query.annotator.Word;
import net.stargraph.query.Language;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class ClueAnalyzer {

	private Matcher matcher;

   	public enum Gender {MALE_PERSON, FEMALE_PERSON, PERSON, NEUTRAL, COLLECTIVE}
	public enum AbstractAnswerType {PERSON, LOCATION, NUMBER, TIME, DATE}

    private Map<String,Gender> AbstractAnswerType = new HashMap<>();
	private Map<String,Gender> pronouns = new HashMap<>();
	private Set<String> determiners = new HashSet<>();
	private List<String> tokens = new ArrayList<>();
	private List<String> posPattern = new ArrayList<>();

	private Annotator annotator;

	public ClueAnalyzer() {

		buildPronounList();
		buildDeterminerList();
	}

	public String getAnswerType(String clueText) {

		String lat = "";
		parse(clueText);
		if(hasPronoun(clueText))
			lat = getPronominalAnswerType(clueText);
		else
			lat = getNominalAnswerType(clueText);

		return lat;
	}

	public String getNominalAnswerType(String clueText){

		if(hasDeterminer(clueText)) {
			long detIndex = 0;
			long nounIndex = 0;
			detIndex = detectDeterminerIndex(clueText);

			for(long index = detIndex; index < tokens.size() ; index++) {
				String pos = getPOSByIndex(index);
				if (pos.equals("NN") || pos.equals("NNS")){
					nounIndex = index;
					break;
				}
			}

			return getTextByIndexRange(detIndex, nounIndex);
		}

		if(hasPronoun(clueText)){
			for(String word : clueText.split(" ")){
				if(pronouns.containsKey(word)) {
					return pronouns.get(word).toString();
				}
			}
		}

		return "";
	}

	public String getPronominalAnswerType(String clueText){

		for(String word : tokens){
			if(pronouns.containsKey(word.toLowerCase())) {
				return pronouns.get(word.toLowerCase()).toString();
			}
		}

		return "OTHER";
	}

	public boolean isClue(String expression){

		parse(expression);

		if(expression.contains("?"))
			return false;

		for(String word : tokens){
			if(determiners.contains(word.toLowerCase()) || pronouns.containsKey(word.toLowerCase()))
				return true;
		}

		return false;
	}

	public boolean entityIsInClue(String entity, String clueText){

		if(clueText.contains(entity))
			return true;

		return false;
	}

	private void parse(String clueText){

		Config config = ConfigFactory.load().getConfig("stargraph");
		annotator = Analyzers.createAnnotatorFactory(config).create();
		List<Word> words = annotator.run(Language.EN, clueText);

		this.posPattern = words.stream().map(Word::getPosTagString).collect(Collectors.toList());
		this.tokens = words.stream().map(Word::getText).collect(Collectors.toList());
	}

	public long getWordIndex(String word){

		long i = 0;
		for(String elem : tokens) {
			i++;
			if(elem.equals(word))
				return i;
		}

		return -1;
	}

	public long getPOSIndex(String pos){

		long i = 0;
		for(String elem : posPattern) {
			i++;
			if(elem.equals(pos))
				return i;
		}

		return -1;
	}

	public String getTextByIndexRange(long start, long end){

		String out = "";
		long i = 0;
		for(String elem : tokens) {
			if(i >= start && i <= end)
				out += elem + " ";
			i++;
		}
		return out.trim();
	}

	public String getWordByIndex(long index){

		String out = "";
		long i = 0;
		for(String elem : tokens) {
			if(i == index)
				return elem;
			i++;
		}
		return "";
	}

	public String getPOSByIndex(long index){

		String out = "";
		long i = 0;
		for(String elem : posPattern) {
			if(i == index)
				return elem;
			i++;
		}
		return "";
	}

	private boolean hasPronoun(String clueText){

		boolean hasPronoun = false;
		for(String word : tokens){
			if(pronouns.containsKey(word.toLowerCase()))
				hasPronoun = true;
		}

		return hasPronoun;
	}

	private boolean hasDeterminer(String clueText){

		boolean hasDeterminer = false;
		for(String word : clueText.split(" ")){
			if(determiners.contains(word))
				hasDeterminer = true;
		}

		return hasDeterminer;
	}

	private void buildPronounList(){

		pronouns.put("his", Gender.PERSON);
		pronouns.put("her", Gender.PERSON);
		pronouns.put("it", Gender.NEUTRAL);
		pronouns.put("their", Gender.COLLECTIVE);
		pronouns.put("I", Gender.NEUTRAL);
		pronouns.put("he", Gender.PERSON);
		pronouns.put("she", Gender.PERSON);

	}

	private void buildDeterminerList(){

		determiners.add("this");
		determiners.add("these");
	}

	private long detectDeterminerIndex(String clueText) {

		long i = -1;
		for(String det : determiners) {
			i = getWordIndex(det);
			if(i >= 0)
				return i;
		}

		return i;
	}

}
