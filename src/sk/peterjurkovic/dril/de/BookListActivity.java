package sk.peterjurkovic.dril.de;

import sk.peterjurkovic.dril.de.adapter.BookAdapter;
import sk.peterjurkovic.dril.de.db.BookDBAdapter;
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
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class BookListActivity extends ListActivity {
	
	public static final String TAG = "BookListActivity";
	
	private static final int REQUEST_ADD_BOOK = 0;
	private static final int REQUEST_EDIT_BOOK = 1;
	
	public static final int MENU_VIEW_ID = Menu.FIRST +1;
	public static final int MENU_EDIT_ID = Menu.FIRST+2;
	public static final int MENU_DELETE_ID = Menu.FIRST+3;
	
	BookAdapter bookAdapter;
	BookDBAdapter bookDBAdapter;
	
	protected ProgressBar booKProgressBar;
	protected ListView listView;
	protected TextView bookProgressBarLabel;
	protected TextView emptyList;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.book_list_activity);
	    registerForContextMenu( getListView() );
	    ImageButton goHome = (ImageButton) findViewById(R.id.home);
        goHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity( new Intent(BookListActivity.this, DashboardActivity.class) );
            }
        });
        booKProgressBar = (ProgressBar)findViewById(R.id.booKProgress);
        bookProgressBarLabel = (TextView)findViewById(R.id.booKProgressLabel);
        emptyList = (TextView)findViewById(android.R.id.empty);
        listView = (ListView)findViewById(android.R.id.list);
        bookDBAdapter = new BookDBAdapter(this);
        updateList();
	}
	
	protected void showList(){
		listView.setVisibility(View.VISIBLE);
		emptyList.setVisibility(View.VISIBLE);
		booKProgressBar.setVisibility(View.GONE);
		bookProgressBarLabel.setVisibility(View.GONE);
	}
	protected void showLoader(){
		listView.setVisibility(View.GONE);
		emptyList.setVisibility(View.GONE);
		booKProgressBar.setVisibility(View.VISIBLE);
		bookProgressBarLabel.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		showLectureList(id);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
        case MENU_DELETE_ID:
        		deleteBook(info.id);
            return true;
        case MENU_EDIT_ID:
        		onEditBookClicked(info.id);
            return true;
        case MENU_VIEW_ID:
        		showLectureList(info.id);
        return true;
            
        default:
                return super.onContextItemSelected(item);
        }
	}
		
		
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
	    super.onCreateContextMenu(menu, v, menuInfo);
	    menu.add(Menu.NONE, MENU_VIEW_ID, Menu.NONE, R.string.view);
	    menu.add(Menu.NONE, MENU_EDIT_ID, Menu.NONE, R.string.edit);
	    menu.add(Menu.NONE, MENU_DELETE_ID, Menu.NONE, R.string.delete);
	}
	    
	
	public void updateList() {
		new LoadData(this).execute();
	}
	  
	  
	public void deleteBook(long id){
        Boolean deleted = false;
        try {
        	deleted = bookDBAdapter.deleteBook(id);
		} catch (Exception e) {
			Log.e(TAG, "Can not delete book", e);
		}
	    
	    if(deleted){
	        Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show();
	        updateList();
	    } else{
	        Toast.makeText(this, R.string.book_not_deleted, Toast.LENGTH_SHORT).show();
	    }
	}
  
	  
	 /**
	 * Show form for add book.
	 *  
	 * @param View
	 */
	 public void onAddBookClicked(View v){ 
	    Intent i = new Intent(this, AddBookActivity.class);
	    startActivityForResult(i, REQUEST_ADD_BOOK);
	 }
			
			
			
	@Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data){
	 	String bookName = null;
	 	
	 	if(resultCode != RESULT_OK) return;
	 	
	 	switch(requestCode){
	 		
	 		case REQUEST_ADD_BOOK :
	 			bookName = data.getStringExtra(AddBookActivity.EXTRA_BOOK_NAME);
	 			onSaveNewBook(bookName);
	 		break;
	 		case REQUEST_EDIT_BOOK :
	 			bookName = data.getStringExtra(EditBookActivity.EXTRA_BOOK_NAME);
	 			long bookId =  data.getLongExtra( EditBookActivity.EXTRA_BOOK_ID, -1 ); 
	 			onSaveEditedBook( bookId , bookName);
	 		break;
	 		default:
	 			throw new Error("requestCode: " + requestCode + 
	 							" is not implemented in onActivityResult");
	 	} 
        super.onActivityResult(requestCode, resultCode, data);
    }
			
			
			
	public void onSaveEditedBook(long bookId, String bookName) {
		if(bookId == -1) throw new Error("Unable save edited book.");
		boolean result = false;
		try {
			result = bookDBAdapter.editBook(bookId , bookName);
		} catch (Exception e) {
			Log.e(TAG, "Can not edit book", e);
		}
		
		if(result){
		    updateList();
		    Toast.makeText(this, R.string.saved_ok, Toast.LENGTH_LONG).show();
	
		}else{
            Toast.makeText(this, R.string.saved_no, Toast.LENGTH_LONG).show();
        }
	}
	
	
	public void onSaveNewBook(String bookName) {
		long id = -1;
		try {
			id = bookDBAdapter.insertBook(bookName);
		} catch (Exception e) {
			Log.e(TAG, "Can not save book", e);
		}
		
		if(id > -1){
		    updateList();
		    Toast.makeText(this, R.string.book_added, Toast.LENGTH_LONG).show();		   
		}else{
            Toast.makeText(this, R.string.book_not_added, Toast.LENGTH_LONG).show();
        }
	}
	
	public void onEditBookClicked(long bookId) {
		Intent i = new Intent(this,  EditBookActivity.class);
		i.putExtra(EditBookActivity.EXTRA_BOOK_ID, bookId);
		startActivityForResult(i, REQUEST_EDIT_BOOK);
	}
	
	public void showLectureList(long id){
		Intent intent = new Intent(this, LectureListActivity.class);
		intent.putExtra( LectureListActivity.EXTRA_BOOK_ID , id);
		startActivity(intent);
	}
	
	public void deactiveAllCards(){
		WordDBAdapter wordDBAdapter = new WordDBAdapter(this);
		try {
			wordDBAdapter.deactiveAll();
		} catch (Exception e) {
			Log.d(TAG, "ERROR: " + e.getMessage());
		} finally {
			wordDBAdapter.close();
		}
		updateList();
		Toast.makeText(this, R.string.words_deactived, Toast.LENGTH_LONG).show();		   
		
	}
	
	@Override
	protected void onDestroy() {
		super.onStop();
		closeAdapterCursor();
	}
	

	private void closeAdapterCursor(){
		try {
			if(bookAdapter != null){
				if(!bookAdapter.getCursor().isClosed())
					bookAdapter.getCursor().close();
			}
			if(bookDBAdapter != null)
					bookDBAdapter.close();
		} catch (Exception e) {
			Log.d(TAG, e.getMessage());
		}
	}
	
	
	
	/* LOADING CURSOR DATA IN BACKGROUND -------------- */
	private class LoadData extends AsyncTask<Void, Void, Cursor>{
		
		Context context;
		
		public LoadData(Context context){
			this.context = context;
		}
		@Override
		protected void onPreExecute(){     
			showLoader();
		}
		
		@Override
		protected Cursor doInBackground(Void... params) {
			Cursor cursor = null;
			try {
				cursor = bookDBAdapter.getBooks();
			} catch (Exception e) {
				Log.e(TAG, "Can not retrive books", e);
			}
			return cursor;
		}
		
		@Override
        protected void onPostExecute(Cursor cursor){
			showList();
			bookAdapter = new BookAdapter(context, cursor, 0);
			setListAdapter(bookAdapter);
			bookAdapter.notifyDataSetChanged();
		}
		
	}
	
	/* OPTION MENU ---------------------------------------- */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.book_list, menu);
	return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.menu_about2:
	        startActivity(new Intent(this, AboutActivity.class));
	        return true;
	    case R.id.deactive_all:
	    	deactiveAllCards();
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
    /* ENDOPTION MENU ---------------------------------------- */
}
