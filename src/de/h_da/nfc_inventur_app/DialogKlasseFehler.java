/**
* @file DialogKlasseFehler.java
* @author Michael Jenisch
* @last updated 26.1.2014
*
*
*/
package de.h_da.nfc_inventur_app;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class DialogKlasseFehler extends DialogFragment {

	    private String text;
        
		public void GetText(String text_uebergabe){
        	
		text = text_uebergabe;
        	
        }
	
		public DialogKlasseFehler () {
            // Empty constructor required for DialogFragment
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            
        	View view = inflater.inflate(R.layout.dialog_fehler, container);
            TextView dialogtext = (TextView) view.findViewById(R.id.textview_dialogtext);
            dialogtext.setText(text);
            
            if(text.equals("+ Gerät wird zurückgegeben\n- Gerät wird ausgeliehen")) 
            {getDialog().setTitle("Legende");}  
            else
            {getDialog().setTitle("Fehler");}
        
            ((Button) view.findViewById(R.id.okDialogButton)).setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
			DialogKlasseFehler.this.dismiss();
			return true;
            }});
   
            return view;
        }
    }  
