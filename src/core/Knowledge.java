package core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Knowledge implements Serializable {
	
	private HashMap<String, ArrayList<String>> answerGroups;
	private ArrayList<String> question;
	private ArrayList<String> answer;

	public Knowledge() {
		setAnswerGroups(new HashMap<String, ArrayList<String>>());
		setQuestion(new ArrayList<String>());
		setAnswer(new ArrayList<String>());
	}

	public ArrayList<String> getQuestion() {
		return question;
	}

	public void setQuestion(ArrayList<String> question) {
		this.question = question;
	}

	public ArrayList<String> getAnswer() {
		return answer;
	}

	public void setAnswer(ArrayList<String> answer) {
		this.answer = answer;
	}

	public HashMap<String, ArrayList<String>> getAnswerGroups() {
		return answerGroups;
	}

	public void setAnswerGroups(HashMap<String, ArrayList<String>> answerGroups) {
		this.answerGroups = answerGroups;
	}
}