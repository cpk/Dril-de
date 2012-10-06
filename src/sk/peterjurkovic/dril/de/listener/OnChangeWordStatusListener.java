package sk.peterjurkovic.dril.de.listener;

public interface OnChangeWordStatusListener {
	
	void activeWord(long wordId);

	void deactiveWord(long wordId);
}
