package sk.peterjurkovic.dril.de;

import sk.peterjurkovic.dril.de.db.WordDBAdapter;
import sk.peterjurkovic.dril.de.listener.AsyncLIstener;
import sk.peterjurkovic.dril.de.updater.CheckForUpdate;
import sk.peterjurkovic.dril.de.updater.UpdateSaver;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public final class DashboardActivity extends MainActivity implements AsyncLIstener{
	
	
	Button btnStart = null;
	
	WordDBAdapter wordAdapter = null;
	
	long countOfActiveWords = 0;
	
	private Context context;
	
	int pos = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_dashboard);
        wordAdapter = new WordDBAdapter(this);
        btnStart = (Button) findViewById(R.id.btn_start);
        
        context = this;
        
        Button btnBook = (Button) findViewById(R.id.btn_book);

        Button btn_stats = (Button) findViewById(R.id.btn_stats);
        
        Button btn_info = (Button) findViewById(R.id.btn_info);
        
        Button btn_update = (Button) findViewById(R.id.btn_update);
 
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View view) {
            	
            	if(countOfActiveWords == 0){
            		Toast.makeText(getApplicationContext(), R.string.zero_cards_alert, Toast.LENGTH_SHORT).show();
            	
            	}else{
            		Intent i = new Intent(getApplicationContext(), DrilActivity.class);
            		startActivity(i);
            	}
            }
        });
        
        btnBook.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View view) {
            		Intent i = new Intent(getApplicationContext(), BookListActivity.class);
            		startActivity(i);
            }
        });
        
        btn_stats.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View view) {
            		Intent i = new Intent(getApplicationContext(), SessionStatisticActivity.class);
            		startActivity(i);
            }
        });
        
        btn_info.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View view) {
        		Intent i = new Intent(getApplicationContext(), InfoActivity.class);
        		startActivity(i);
	        }
	    });
        
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View view) {
            	
            	CheckForUpdate chfu = new CheckForUpdate( context );
    	    	chfu.execute(); 
	        }
	    });
        
        clearPreferencies();
        ImageButton goHome = (ImageButton) findViewById(R.id.home);
        goHome.setVisibility(View.INVISIBLE);
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	updateCountOfActiveWords();  	
    }
      
    private void updateCountOfActiveWords(){
    	try{
    		countOfActiveWords = wordAdapter.getCountOfActiveWords();
    	}catch (Exception ex) {
    		ex.printStackTrace();
         } finally {
            wordAdapter.close();
        }
    }
    
    
    /* UPDATE --------------------------------- */
    @Override
	public void onCheckResponse(Integer response) {
    	switch(response){
    		case CheckForUpdate.STATE_NO_INTERNET_CONN :
    			showNoActionDialog(getResources().getString( R.string.update_no_conn));
    			break;
    		case CheckForUpdate.STATE_PARSING_ERROR :
    			showNoActionDialog(getResources().getString( R.string.update_failed));
    			break;
    		case CheckForUpdate.STATE_NO_UPDATE:
    			showNoActionDialog(getResources().getString( R.string.up_to_date));
    			break;
    		default:
    			showDownloadDialog(response);
    	}
    	
	}
    
    
    @Override
	public void onUpdatedResponse(Integer response) {
		if(response > 0){
			showNoActionDialog(getResources().getString( R.string.successfully_updated, response));
		}else{
			onCheckResponse(response);
		}
    	
	}
    
    
	/**
	 * Dialog, if updates are available
	 * 
	 * @param response
	 */
	public void showDownloadDialog(Integer response){
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder
			.setTitle(R.string.update_status)
			.setMessage(R.string.update_available)
			.setCancelable(false)
			.setNegativeButton(R.string.no,new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,int id) {
							dialog.cancel();
						}
			})
			.setPositiveButton(R.string.yes ,new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
					UpdateSaver chfu = new UpdateSaver( context );
			    	chfu.execute();
			        
				}
			  })
			;

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}
	
	
	/**
	 * If update is not available show dialog with close button only.
	 * 
	 */
	public void showNoActionDialog(String responseMsg){
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder
			.setTitle(R.string.update_status)
			.setMessage(responseMsg)
			.setCancelable(false)
			.setNegativeButton(R.string.ok,new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,int id) {
							dialog.cancel();
						}
			});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

    
    /* OPTION MENU ---------------------------- */
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    
    private void clearPreferencies(){
    	 SharedPreferences sharedPreferences = getSharedPreferences(DrilActivity.STATISTIC_ID_KEY, MODE_PRIVATE);
         SharedPreferences.Editor editor = sharedPreferences.edit();
         editor.clear().commit();
    }
    
}
