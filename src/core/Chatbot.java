package core;

// Core
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

// GUI
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

// Language Processing
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

// Google Translate API
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

//Wikipedia API
// Unable to implement

public class Chatbot {
	public GUI Interface;
	public Knowledge Knowledge;
	public LinkedList<String> previousResponses;

	public StanfordCoreNLP stanfordCoreNLP = Pipeline.getPipeline();

	public Chatbot() throws FileNotFoundException, ClassNotFoundException, IOException {
		previousResponses = new LinkedList<String>();
		readKnowledge();
		Interface = new GUI();
		Interface.draw();
	}

	public void learn(String input) throws FileNotFoundException, IOException, ClassNotFoundException {
		if (!(getResponse(isQuestion(input)).contains(input))) {
			getResponse(isQuestion(input)).add(input);
			saveKnowledge();
		}
	}

	public String respondTo(String input) throws FileNotFoundException, ClassNotFoundException, IOException {
		learn(input);
		return addToPreviousResponses(isQuestion(input) ? getAnswer(input) : getQuestion(input));
	}

	public String getAnswer(String input) throws FileNotFoundException, ClassNotFoundException, IOException {
		ArrayList<String> possibleResponses = checkHashMap(input);
		return !possibleResponses.isEmpty() ? getRandomResponse(possibleResponses) : getKnowledgeResponse(input);
	}

	public String getQuestion(String input) throws FileNotFoundException, ClassNotFoundException, IOException {
		if (isQuestion(getLastResponse()))
			addAnswerToHashMap(getLastResponse(), input);
		return knowledgeResponse(input);
	}

	public String getKnowledgeResponse(String input) throws FileNotFoundException, ClassNotFoundException, IOException {
		addQuestionToHashMap(input);
		return knowledgeResponse(input);
	}

	public String knowledgeResponse(String input) throws FileNotFoundException, ClassNotFoundException, IOException {
		String response = "";
		int topMatchingWords = 0;
		boolean isQuestion = isQuestion(input);
		for (int i = 0; i < getResponse(!isQuestion).size(); i++) {
			int matchingWords = compare(input, getResponse(!isQuestion).get(i));
			// this line makes sure that the bot does not repeatedly say the same thing.
			if ((matchingWords > topMatchingWords)
					&& !(getPreviousResponses().contains(getResponse(!isQuestion).get(i)))) {
				response = getResponse(!isQuestion).get(i);
				topMatchingWords = matchingWords;
			}
		}
		if (topMatchingWords == 0) {
			response = getRandomResponse(getResponse(!isQuestion));
		}
		return response;
	}

	public int compare(String input, String Knowledge) {

		int matchingWords = 0;

		CoreDocument inNLP = new CoreDocument(input);
		CoreDocument knowledgeNLP = new CoreDocument(Knowledge);
		stanfordCoreNLP.annotate(inNLP);
		stanfordCoreNLP.annotate(knowledgeNLP);
		List<CoreLabel> inputWordList = inNLP.tokens();
		List<CoreLabel> knowledgeWordList = knowledgeNLP.tokens();

		String simpleInput = "";

		for (CoreLabel inputWord : inputWordList) {
			simpleInput = simpleInput + inputWord.lemma() + " ";
		}
		inNLP = new CoreDocument(simpleInput);
		stanfordCoreNLP.annotate(inNLP);
		inputWordList = inNLP.tokens();

		for (CoreLabel inputWord : inputWordList) {

			String pos = inputWord.getString(CoreAnnotations.PartOfSpeechAnnotation.class);

			// we only care about matching nouns, verbs and adjectives
			if (!(pos.equals("JJ") || pos.equals("NN") || pos.equals("NP") || pos.equals("VV"))) {
				continue;
			}
			for (CoreLabel knowledgeWord : knowledgeWordList) {

				if (inputWord.lemma().equals(knowledgeWord.lemma())) {
	
					matchingWords++;
				}
			}
		}
		return matchingWords;
	}

	public boolean isQuestion(String input) {
		if (input == null)
			return false;
		return input.contains("?");
	}

	public ArrayList<String> getResponse(boolean isQuestion) {
		if (isQuestion)
			return Knowledge.getQuestion();
		else
			return Knowledge.getAnswer();
	}

	public String getLastResponse() {
		if (getPreviousResponses().isEmpty())
			return "";
		return getPreviousResponses().getLast();
	}

	public String getRandomResponse(ArrayList<String> input) {
		return input.get((int) (Math.random() * input.size()));
	}

	public LinkedList<String> getPreviousResponses() {
		return previousResponses;
	}

	public String addToPreviousResponses(String response) {
		previousResponses.add(response);
		if (previousResponses.size() > 5)
			previousResponses.remove();
		return response;
	}

	public void addAnswerToHashMap(String question, String input) {
		if (Knowledge.getAnswerGroups().keySet().contains(question))
			if (!(checkHashMap(question).contains(input)))
				checkHashMap(question).add(input);
	}

	public void addQuestionToHashMap(String input) {
		Knowledge.getAnswerGroups().put(input, new ArrayList<String>());
	}

	public ArrayList<String> checkHashMap(String input) {
		if (Knowledge.getAnswerGroups().get(input) == null)
			return new ArrayList<String>();
		return Knowledge.getAnswerGroups().get(input);
	}

	public void setKnowledge(Knowledge Knowledge) {
		this.Knowledge = Knowledge;
	}

	public Knowledge getKnowledge() {
		return Knowledge;
	}

	public void saveKnowledge() throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("Knowledge.dat"));
		out.writeObject(getKnowledge());
		out.close();
	}

	public void readKnowledge() throws FileNotFoundException, IOException, ClassNotFoundException {
		if (new File("Knowledge.dat").exists()) {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream("Knowledge.dat"));
			setKnowledge((Knowledge) in.readObject());
			in.close();
		} else {// use defaults
			setKnowledge(new Knowledge());
			learn("yes");
			learn("how are you?");
		}
	}

	public void displayKnowledge() {
		System.out.println(Knowledge.getQuestion().toString());
		System.out.println(Knowledge.getAnswer().toString());
		System.out.println(Knowledge.getAnswerGroups().toString());
	}

	public String[] parseIntoWords(String s) {
		if (s == null || s.equals(""))
			return new String[0];
		return s.split(" ");
	}

	public String removePunctuation(String str) {
		String temp = "";
		char[] arr = str.toLowerCase().toCharArray();
		for (char elem : arr)
			if (((int) elem >= 97 && (int) elem <= 122) | (int) elem == 32)
				temp += elem;
		return temp;
	}

	public class GUI {
		public int inputCount;
		public JFrame Window;
		public JPanel Panel;
		public JPanel PanelHistory;
		public JTextField UserInArea;
		public JLabel BotOutArea;
		public SpellChecker SpellChecker;

		public GUI() {
			Window = new JFrame();
			Panel = new JPanel();
			PanelHistory = new JPanel();
			UserInArea = new JTextField(35);
			BotOutArea = new JLabel("Chatbot says: ");
		}

		public void draw() {
			inputCount = 0;
			Panel.setBorder(BorderFactory.createEmptyBorder());
			Panel.setLayout(new GridLayout(10, 1));
			PanelHistory.setBorder(BorderFactory.createTitledBorder("Recent History"));
			PanelHistory.setLayout(new GridLayout(30, 1));
			UserInArea.setName("type here");
			SpellChecker = new SpellChecker();
			UserInArea.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent userPressedEnter) {
					String input = UserInArea.getText().toLowerCase();

					if (input.equals("stop"))
						System.exit(0);

					if (input.equals("###")) {
						displayKnowledge();
						BotOutArea.setText("Chatbot says: Displayed knowledge in console");
					} else if (!input.isEmpty()) {
						try {
							input = SpellChecker.checkUserInput(input);
							String botOut = respondTo(input);
							BotOutArea.setText("Chatbot says: " + botOut);
							UserInArea.setText("");
							PanelHistory.add(new JLabel(input));
							PanelHistory.add(new JLabel(botOut));
							if (inputCount < 13)
								inputCount++;
							else {
								PanelHistory.remove(0);
								PanelHistory.remove(0);
							}
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

			});
			Panel.add(UserInArea, BorderLayout.BEFORE_FIRST_LINE);
			Panel.add(BotOutArea, BorderLayout.AFTER_LAST_LINE);
			Window.add(Panel, BorderLayout.WEST);
			Window.add(PanelHistory, BorderLayout.EAST);
			Window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			Window.setTitle("Chatbot GUI");
			Window.setPreferredSize(new Dimension(700, 500));
			Window.pack();
			Window.setVisible(true);
		}
	}

	public class SpellChecker {
		public Levenshtein MatchStrengthTester;
		public String[] KnownWords;

		public SpellChecker() {
			MatchStrengthTester = new Levenshtein();
			KnownWords = getParsedKnowledge();
		}

		public String checkUserInput(String input) {
			boolean tackQuestionMarkOnEndOfInput = false;
			if (isQuestion(input))
				tackQuestionMarkOnEndOfInput = true;
			input = removePunctuation(input);
			String[] words = parseIntoWords(input);
			String currentWord, correctedInput = "";
			for (int i = 0; i < words.length; i++) {
				currentWord = words[i];
				if (isSpellingMistake(currentWord))
					currentWord = correctSpelling(currentWord);
				correctedInput += currentWord + " ";
			}
			
			if(tackQuestionMarkOnEndOfInput) {
				correctedInput = correctedInput.substring(0,correctedInput.length() - 1) + "?";
			}else {
				correctedInput = correctedInput.substring(0,correctedInput.length() - 1);
			}
			
			return correctedInput;

		public boolean isSpellingMistake(String word) {
			for (int i = 0; i < KnownWords.length; i++)
				if (word.equals(KnownWords[i]))
					return false;
			return true;
		}

		public String correctSpelling(String word) {
			return findClosestMatchingSupportedWord(word);
		}

		public String findClosestMatchingSupportedWord(String word) {
			int minMatchStrengthSoFar = 100;
			String bestMatchingWordSoFar = "";
			int matchStrength = 0;
			int currentMatchStrength = 0;

			for (int i = 0; i < KnownWords.length; i++) {
				currentMatchStrength = MatchStrengthTester.distance(word.toLowerCase(), KnownWords[i].toLowerCase());
				if (currentMatchStrength < minMatchStrengthSoFar) {
					minMatchStrengthSoFar = currentMatchStrength;
					bestMatchingWordSoFar = KnownWords[i];
				}
			}

			if (matchStrength <= 2 && bestMatchingWordSoFar.length() > 0
					&& Math.abs(word.length() - bestMatchingWordSoFar.length()) < 3) {
				System.out.println("Spelling mistake detected: replaced " + word + " with " + bestMatchingWordSoFar);
				return bestMatchingWordSoFar;
			}
			return word;
		}

		public String[] getParsedKnowledge() {
			return parseIntoWords(removePunctuation(Knowledge.getQuestion().toString()
					+ Knowledge.getAnswer().toString() + Knowledge.getAnswerGroups().toString()));
		}

		public class Levenshtein {

			public int distance(CharSequence lhs, CharSequence rhs) {
				return distance(lhs, rhs, 1, 1, 1, 1);
			}

			public int distance(CharSequence source, CharSequence target, int deleteCost, int insertCost,
					int replaceCost, int swapCost) {

				if (2 * swapCost < insertCost + deleteCost) {
					throw new IllegalArgumentException("Unsupported cost assignment");
				}
				if (source.length() == 0) {
					return target.length() * insertCost;
				}
				if (target.length() == 0) {
					return source.length() * deleteCost;
				}
				int[][] table = new int[source.length()][target.length()];
				Map<Character, Integer> sourceIndexByCharacter = new HashMap<Character, Integer>();
				if (source.charAt(0) != target.charAt(0)) {
					table[0][0] = Math.min(replaceCost, deleteCost + insertCost);
				}
				sourceIndexByCharacter.put(source.charAt(0), 0);
				for (int i = 1; i < source.length(); i++) {
					int deleteDistance = table[i - 1][0] + deleteCost;
					int insertDistance = (i + 1) * deleteCost + insertCost;
					int matchDistance = i * deleteCost + (source.charAt(i) == target.charAt(0) ? 0 : replaceCost);
					table[i][0] = Math.min(Math.min(deleteDistance, insertDistance), matchDistance);
				}
				for (int j = 1; j < target.length(); j++) {
					int deleteDistance = (j + 1) * insertCost + deleteCost;
					int insertDistance = table[0][j - 1] + insertCost;
					int matchDistance = j * insertCost + (source.charAt(0) == target.charAt(j) ? 0 : replaceCost);
					table[0][j] = Math.min(Math.min(deleteDistance, insertDistance), matchDistance);
				}
				for (int i = 1; i < source.length(); i++) {
					int maxSourceLetterMatchIndex = source.charAt(i) == target.charAt(0) ? 0 : -1;
					for (int j = 1; j < target.length(); j++) {
						Integer candidateSwapIndex = sourceIndexByCharacter.get(target.charAt(j));
						int jSwap = maxSourceLetterMatchIndex;
						int deleteDistance = table[i - 1][j] + deleteCost;
						int insertDistance = table[i][j - 1] + insertCost;
						int matchDistance = table[i - 1][j - 1];
						if (source.charAt(i) != target.charAt(j)) {
							matchDistance += replaceCost;
						} else {
							maxSourceLetterMatchIndex = j;
						}
						int swapDistance;
						if (candidateSwapIndex != null && jSwap != -1) {
							int iSwap = candidateSwapIndex;
							int preSwapCost;
							if (iSwap == 0 && jSwap == 0) {
								preSwapCost = 0;
							} else {
								preSwapCost = table[Math.max(0, iSwap - 1)][Math.max(0, jSwap - 1)];
							}
							swapDistance = preSwapCost + (i - iSwap - 1) * deleteCost + (j - jSwap - 1) * insertCost
									+ swapCost;
						} else {
							swapDistance = Integer.MAX_VALUE;
						}
						table[i][j] = Math.min(Math.min(Math.min(deleteDistance, insertDistance), matchDistance),
								swapDistance);
					}
					sourceIndexByCharacter.put(source.charAt(i), i);
				}
				return table[source.length() - 1][target.length() - 1];
			}

		}

		public String GoogleTranslate (String inputLang, String outputLang, String inputText) {
			String translation;

			// Unfortunately, I was unable to implement this feature.

			/** The logic I was trying to was this:
			 * 1. Connect to Google Translate API
			 * 2. Get input language, desired output language, and input text.
			 * 3. Send the text to the API
			 * 4. API will return translated text
			 * 5. Store the translated text in a variable "translation"
			 * 6. Return the translated text
			 * 
			 **/
			return translation;
		}

		public String Wikipedia (String search){
			String response;

			// Unfortunately, I was unable to implement this feature either.

			/** The logic I was trying to was similar to Google Translate API:
			 * 1. Connect to Wikipedia API
			 * 2. Authentication
			 * 3. Send the search text to API
			 * 4. API will return article
			 * 5. Extract first two lines from the article
			 * 6. Store the extracted text in "response" varaible
			 * 7. Return the text
			 * 
			 **/

			return response;
		}
	}
}