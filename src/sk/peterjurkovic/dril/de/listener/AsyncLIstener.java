package sk.peterjurkovic.dril.de.listener;


public interface AsyncLIstener {
	void onCheckResponse(Integer response);
	void onUpdatedResponse(Integer response);
}
