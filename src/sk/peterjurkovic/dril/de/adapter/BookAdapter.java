package sk.peterjurkovic.dril.de.adapter;

import sk.peterjurkovic.dril.de.R;
import sk.peterjurkovic.dril.de.db.BookDBAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class BookAdapter extends CursorAdapter {
	
	public BookAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
	}
	
	@Override
	public void bindView(View oldView, Context ctx, Cursor c) {
                		 
		int bookNameIndex = c.getColumnIndex(BookDBAdapter.BOOK_NAME);
		int lectureCountIndex = c.getColumnIndex(BookDBAdapter.LECTURES_COUNT);
		int wordCountIntex = c.getColumnIndex(BookDBAdapter.WORD_COUNT);
		int activedWordcountIndex = c.getColumnIndex(BookDBAdapter.ACTIVE_WORD_COUNT);
		
		((TextView) oldView.findViewById(R.id.adapter_book_name)).
										setText(c.getString(bookNameIndex));
		Resources res = ctx.getResources();		
		((TextView) oldView.findViewById(R.id.adapter_book_descr)).setText(
				res.getString(
						R.string.lecture_count, 
						c.getInt(lectureCountIndex),
						c.getInt(wordCountIntex),
						c.getInt(activedWordcountIndex)
					));
		
	}

	@Override
	public View newView(Context ctx, Cursor c, ViewGroup root) {
		LayoutInflater inflater = LayoutInflater.from(ctx);
		View view = inflater.inflate(R.layout.book, root, false);
		bindView(view, ctx, c);
		return view;
	}

}
