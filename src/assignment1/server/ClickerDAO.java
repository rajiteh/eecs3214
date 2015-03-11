package assignment1.server;

import java.util.Map;

abstract class ClickerDAO {
	protected Integer choiceCount;
	public ClickerDAO(Integer choiceCount) {
		this.choiceCount = choiceCount;
		// TODO Auto-generated constructor stub
	}

	public Integer getChoiceCount() {
		return this.choiceCount;
	}
	abstract boolean userExists(String username);
	abstract boolean incrementChoice(String username, int choice) throws IllegalArgumentException;
	abstract Map<String, Integer[]> getAnswers();
}
