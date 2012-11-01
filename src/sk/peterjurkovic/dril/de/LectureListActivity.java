package sk.peterjurkovic.dril.de;

import sk.peterjurkovic.dril.de.adapter.LectureAdapter;
import sk.peterjurkovic.dril.de.db.LectureDBAdapter;
import sk.peterjurkovic.dril.de.db.WordDBAdapter;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LectureListActivity extends ListActivity{
	
	public static final String EXTRA_BOOK_ID= "bookId";
	
	public static final String TAG = "LectureListActivity";
	
	private static final int REQUEST_ADD_LECTURE = 0;
	private static final int REQUEST_EDIT_LECTURE = 1;
	
	public static final int MENU_VIEW_ID = Menu.FIRST +1;
	public static final int MENU_EDIT_ID = Menu.FIRST+2;
	public static final int MENU_DELETE_ID = Menu.FIRST+3;
	public static final int MENU_ACTIVE_RANDOM = Menu.FIRST+4;
	public static final int MENU_ACTIVE_LECTURE = Menu.FIRST+5;
	public static final int MENU_DEACTIVE_LECTURE = Menu.FIRST+6;
	public static final int MENU_IMPORT = Menu.FIRST+7;
	

	
	private long bookId;	
	LectureAdapter lectureAdapter;
	LectureDBAdapter lectureDbAdapter;
	
	protected ProgressBar lectureProgressBar;
	protected ListView listView;
	protected TextView lectureProgressBarLabel;
	protected TextView emptyList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.lecture_list_activity);

	    /* add new lecture listener */
	    Button addNewLecture = (Button)findViewById(R.id.addNewLecture);
	    addNewLecture.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onAddLectureClicked();
			}
	    });
	    registerForContextMenu( getListView() );
	    
	    ImageButton goHome = (ImageButton) findViewById(R.id.home);
        goHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity( new Intent(LectureListActivity.this, DashboardActivity.class) );
            }
        });
        bookId = (long) getIntent().getLongExtra( EXTRA_BOOK_ID, 0);
        if(bookId != 0){
	        lectureProgressBar = (ProgressBar)findViewById(R.id.lectureProgress);
	        lectureProgressBarLabel = (TextView)findViewById(R.id.lectureProgressLabel);
	        emptyList = (TextView)findViewById(android.R.id.empty);
	        listView = (ListView)findViewById(android.R.id.list);
	        lectureDbAdapter = new LectureDBAdapter(this);
		    ((TextView)findViewById(R.id.lectureListLabel)).setText( getBookName(bookId) );
		    updateList();
        }else {
			Log.d(TAG, "ERR BookId is not set.");
		}    
	}
	
	protected void showList(){
		listView.setVisibility(View.VISIBLE);
		emptyList.setVisibility(View.VISIBLE);
		lectureProgressBar.setVisibility(View.GONE);
		lectureProgressBarLabel.setVisibility(View.GONE);
	}
	protected void showLoader(){
		listView.setVisibility(View.GONE);
		emptyList.setVisibility(View.GONE);
		lectureProgressBar.setVisibility(View.VISIBLE);
		lectureProgressBarLabel.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		startWordActivity(id);
	}

	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
        case MENU_DELETE_ID:
        	deleteLecture(info.id);
            return true;
        case MENU_EDIT_ID:
        	onEditLectureClicked(info.id);
            return true;
        case MENU_VIEW_ID:
        	startWordActivity(info.id);
        	return true;
        case MENU_ACTIVE_RANDOM :
        	activeRandomWords(info.id,  10 ); // 10 - count of words
        return true;
        case MENU_ACTIVE_LECTURE :
        	activeAllWordInLecture(info.id); 
        return true;
        case MENU_DEACTIVE_LECTURE :
        	deactiveAllWordInLecture(info.id); 
        return true; 
        case MENU_IMPORT :
        	importWords(info.id); 
        return true; 
        default:
                return super.onContextItemSelected(item);
        }
	}
	
	
	private void importWords(long lectureId) {
		Intent i = new Intent(this,  ImportActivity.class);
		i.putExtra(EditLectureActivity.EXTRA_LECTURE_ID, lectureId);
		i.putExtra(EditLectureActivity.EXTRA_LECTURE_NAME, lectureId);
		startActivity(i);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
	    super.onCreateContextMenu(menu, v, menuInfo);
	    menu.setHeaderTitle(R.string.options);
	    menu.add(Menu.NONE, MENU_VIEW_ID, Menu.NONE, R.string.view);
	    menu.add(Menu.NONE, MENU_EDIT_ID, Menu.NONE, R.string.edit);
	    menu.add(Menu.NONE, MENU_DELETE_ID, Menu.NONE, R.string.delete);
	    menu.add(Menu.NONE, MENU_ACTIVE_RANDOM, Menu.NONE, R.string.active_ten);
	    menu.add(Menu.NONE, MENU_ACTIVE_LECTURE, Menu.NONE, R.string.active_lecture);
	    menu.add(Menu.NONE, MENU_DEACTIVE_LECTURE, Menu.NONE, R.string.deactive_lecture);
	    menu.add(Menu.NONE, MENU_IMPORT, Menu.NONE, R.string.import_words);
	}
	
	
	
	@Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data){
        
	 	String lectureName = null;
	 	
	 	if(resultCode != RESULT_OK) return;
	 	
	 	switch(requestCode){
	 		case REQUEST_ADD_LECTURE :
	 			lectureName = data.getStringExtra(EditLectureActivity.EXTRA_LECTURE_NAME);
	 			onSaveNewLecture(lectureName);
	 		break;
	 		case REQUEST_EDIT_LECTURE :
	 			lectureName = data.getStringExtra(EditLectureActivity.EXTRA_LECTURE_NAME);
	 			long lectureId =  data.getLongExtra( EditLectureActivity.EXTRA_LECTURE_ID, -1 ); 
	 			onSaveEditedLecture( lectureId , lectureName);
	 		break;
	 		default:
	 			throw new Error("requestCode: " + requestCode + 
	 							" is not implemented in onActivityResult");
	 	} 
        super.onActivityResult(requestCode, resultCode, data);
    }
	
	
	
	public void onSaveEditedLecture(long lectureId, String lectureName) {		
		if(lectureId == -1) throw new Error("Unable save edited lecture.");
		boolean result = false;
		try {
			result = lectureDbAdapter.editLecture(lectureId , lectureName);
		} catch (Exception e) {
			Log.d(TAG, "ERROR: " + e.getMessage());
		} 
		if(result){
		    updateList();
		    Toast.makeText(this, R.string.saved_ok, Toast.LENGTH_LONG).show();
		}else{
            Toast.makeText(this, R.string.saved_no, Toast.LENGTH_LONG).show();
        }
	}
		
	 
	/**
	 * Start new activity for result.
	 * 
	 * EXTRA DATA long lectureId, key: EXTRA_LECTURE_ID
	 *  
	 * @param lectureId
	 */
	public void onEditLectureClicked(long lectureId) {
		Intent i = new Intent(this,  EditLectureActivity.class);
		i.putExtra(EditLectureActivity.EXTRA_LECTURE_ID, lectureId);
		startActivityForResult(i, REQUEST_EDIT_LECTURE);
	}
	
	/**
	 * Update list of items
	 * 
	 */
	public void updateList() {
		new LoadData(this, bookId).execute();
	}
	
	
	/**
	 * 
	 * Remove lecture from database by given id of lecture and show result message.
	 * 
	 * @param long id of lecture
	 */
	public void deleteLecture(long id){
        Boolean deleted = false;
	    try{
	    	deleted = lectureDbAdapter.deleteLecture(id);
	    } catch (Exception e) {
			Log.d(TAG, "ERROR: " + e.getMessage());
		} 
        if(deleted){
            Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show();
            updateList();
        } else{
            Toast.makeText(this, R.string.lecture_not_deleted, Toast.LENGTH_SHORT).show();
        }
    }
	
	
	/**
	 * Create new lecture and show result message. 
	 * After response show Toast message.
	 * 
	 * @param String name of new lecture.
	 */
	public void onSaveNewLecture(String lectureName) {
		long id = -1;
		try {
			id = lectureDbAdapter.insertLecture(bookId, lectureName);
		} catch (Exception e) {
			Log.d(TAG, "ERROR: " + e.getMessage());
		} 
		if(id > -1){
		    updateList();
		    Toast.makeText(this, R.string.lecture_added, Toast.LENGTH_LONG).show();		   
		}else{
            Toast.makeText(this, R.string.lecture_not_added, Toast.LENGTH_LONG).show();
        }
	}
	
	public void onAddLectureClicked() { 
	    Intent i = new Intent(this, AddLectureActivity.class);
	    startActivityForResult(i, REQUEST_ADD_LECTURE);
	}
	
	
	
	public String getBookName( long bookId ){
		String bookName = "";
		try{
			bookName = lectureDbAdapter.getBookNameByLecture(bookId);
		}catch(Exception e){
			Log.d(TAG, "ERROR: "+e.getMessage());
		}
		Log.d(TAG, "Book name: " + bookName);
		return bookName;
	}
	
	public void startWordActivity(long lectureId){
		Intent i = new Intent(this,  WordActivity.class);
		i.putExtra( WordActivity.LECTURE_ID_EXTRA, lectureId);
		startActivity(i);
	}
	
	
	public void activeRandomWords(long lectureId, int countOfwordToActivate ){
        WordDBAdapter wordDbAdapter = new WordDBAdapter(this);
 	    try{
 	    	wordDbAdapter.activateWordRandomly(lectureId, countOfwordToActivate);
 	    } catch (Exception e) {
 			Log.d(TAG, "ERROR: " + e.getMessage());
 		} finally {
 			wordDbAdapter.close();
 		}
    	Toast.makeText(this, R.string.activated, Toast.LENGTH_SHORT).show();
    	updateList();
	}
	
	
	public void activeAllWordInLecture(long lectureId){
		WordDBAdapter wordDbAdapter = new WordDBAdapter(this);
		boolean deactivated = false;
 	    try{
 	    	deactivated = wordDbAdapter.changeWordActivity(lectureId, WordDBAdapter.STATUS_ACTIVE);
 	    } catch (Exception e) {
 			Log.d(TAG, "ERROR: " + e.getMessage());
 		} finally {
 			wordDbAdapter.close();
 		}
 	    if(deactivated)	updateList();
 	    Toast.makeText(this, R.string.activated, Toast.LENGTH_SHORT).show();
	}
	
	
	
	public void deactiveAllWordInLecture(long lectureId){
		WordDBAdapter wordDbAdapter = new WordDBAdapter(this);
		boolean deactivated = false;
 	    try{
 	    	deactivated = wordDbAdapter.changeWordActivity(lectureId, WordDBAdapter.STATUS_DEACTIVE);
 	    } catch (Exception e) {
 			Log.d(TAG, "ERROR: " + e.getMessage());
 		} finally {
 			wordDbAdapter.close();
 		}
 	    if(deactivated)	updateList();
 	    Toast.makeText(this, R.string.words_deactived, Toast.LENGTH_SHORT).show();
	}
	
	
	@Override
	protected void onDestroy() {
		super.onStop();
		closeAdapterCursor();
	}
	
	private void closeAdapterCursor(){
		try {
			if(lectureAdapter != null){
				if(!lectureAdapter.getCursor().isClosed())
					lectureAdapter.getCursor().close();
			}
			if(lectureDbAdapter != null){
				lectureDbAdapter.close();
			}
		} catch (Exception e) {
			Log.d(TAG, e.getMessage());
		}
	}
	
	/* LOADING CURSOR DATA IN BACKGROUND -------------- */
	private class LoadData extends AsyncTask<Void, Void, Cursor>{
		
		Context context;
		long bookId;
		
		public LoadData(Context context, long bookId){
			this.context = context;
			this.bookId = bookId;
		}
		
		@Override
		protected void onPreExecute(){  
			Log.d(TAG, "Loading data..");
			showLoader();
		}
		
		@Override
		protected Cursor doInBackground(Void... params) {
			Cursor cursor = null;
			try {
				cursor = lectureDbAdapter.getLecturesByBookId( this.bookId );
			} catch (Exception e) {
				Log.d(TAG, e.getMessage());
			}
			return cursor;
		}
		
		@Override
        protected void onPostExecute(Cursor cursor){
			showList();
			lectureAdapter = new LectureAdapter(context, cursor, 0);
			setListAdapter(lectureAdapter);
			lectureAdapter.notifyDataSetChanged();
			if(cursor != null)
				Log.d(TAG, "Loaded ! count: " + cursor.getCount());
			else
				Log.d(TAG, "can not load data");
		}
		
	}
	
	/* OPTION MENU ---------------------------------------- */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.main_menu, menu);
	return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.menu_about:
	        startActivity(new Intent(this, AboutActivity.class));
	    	Log.d("MAINACTIVITY", "starting abotu...");
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
    /* ENDOPTION MENU ---------------------------------------- */
}
