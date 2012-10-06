package sk.peterjurkovic.dril.de.listener;

public interface OnEditWordListener {
	
	void saveEditedWord(long wordId, String question, String answer);

	
}
