/**
* @file MainActivity.java
* @author Michael Jenisch
* @last updated 26.1.2014
*
*
*/
package de.h_da.nfc_inventur_app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

public class MainActivity extends FragmentActivity {

	private NfcAdapter mAdapter;
	private PendingIntent pendingIntent;
	private IntentFilter[] intentFA;
	private String[][] techListsa;
	private List<String> idlist = new ArrayList<String>();
	private List<String> namelist = new ArrayList<String>();
	static final int DIALOG_Item = 2;
	ListView listview;
	int itemausg = 0;
	boolean benutzer = false;
	UDPInterface io;
	UDPDiscovery udpdis;
	private String dbvname;
	private String dbnname;
	private String dbemail;
	private String dbgname;
	private String dbstatus;
	private boolean doppelt = false;
	private String alterTyp = "startwert";
	String IPadresse;
	
	@Override
	/** App Erzeugung */
	public void onCreate(Bundle savedInstanceState) {

		/** Netzwerkverbindung wird hergestellt */
		//udpdis = new UDPDiscovery();
		//IPadresse = udpdis.Bootstrap(6667);

		IPadresse = "192.168.2.107";

		/**Fehlerabfrage für IPadressen-Ermittlung*/
		if (!IPadresse.contains("TIMEOUT") && !IPadresse.contains("EXCEPTION")) {
			io = new UDPInterface();
			io.setLocalEndPoint(6666);
			io.setRemoteEndPoint(IPadresse, 6666);
		} else if (IPadresse.contains("TIMEOUT") || IPadresse.contains("EXCEPTION")) {
			showDialogFehler("Keine Netzwerkverbindung.");
			namelist.add(IPadresse);

		}

		super.onCreate(savedInstanceState);

		/** Layout wird geladen */
		setContentView(R.layout.activity_main);

		mAdapter = NfcAdapter.getDefaultAdapter(this);

		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		IntentFilter tag = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);

		intentFA = new IntentFilter[] { tag, ndef };

		/** unterstützte Tag-Arten */
		this.techListsa = new String[][] {
				new String[] { MifareUltralight.class.getName(),
						Ndef.class.getName(), NfcA.class.getName() },
				new String[] { MifareClassic.class.getName(),
						Ndef.class.getName(), NfcA.class.getName() },
				new String[] { MifareUltralight.class.getName(),
						NdefFormatable.class.getName(), NfcA.class.getName() },
				new String[] { Ndef.class.getName(), NfcV.class.getName() },
				new String[] { NfcF.class.getName() } };

	}

	@Override
	/**Wird ausgeführt wenn der Benutzer die App verlässt */
	public void onPause() {
		//udpdis.close();
		super.onPause();
		if (mAdapter != null) {
			mAdapter.disableForegroundDispatch(this);
		}
	}

	@Override
	/**Wird ausgeführt wenn der Benutzer die App erneut aufruft */
	public void onResume() {
		super.onResume();
		mAdapter.enableForegroundDispatch(this, pendingIntent, intentFA,
				techListsa);
	}

	@Override
	/** Wird aktiv wenn ein neuer Tag eingelesen wird*/
	public void onNewIntent(Intent intent) {
		
		String antwort;
		String Typ;
		String dbid;
		String dbbeschreibung;
		String Status;


		Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

		/** auslesen der TagID */
		byte[] byteid = tagFromIntent.getId();
		
		/** Aufruf der TagID umcodierung */
		String id = ByteArrayToHexString(byteid);

		/** Tag bereits in der ID-Liste vorhanden ? */
		doppelt = idlist.contains(id);
		
		/** Abfrage der Tag-Informationen in der Datenbank */
		//antwort = io.getTagInfo(id);

		/** Für Test der App ohne DB Anbindung */
		String id2 = "56:4C:A7:9B:FA:1E:00";

		if (id.contentEquals(id2)) {
			antwort = ("RECIVED;;User;;11;;Muster;;Max;;Student;;max@hda.de");
		} else {

			antwort = ("RECIVED;;Geraet;;112;;scanner;;scanner;;false");
		} 
		
		// usertag:
		// User;Tagid;Name;Nachname;Beschreibung;Email;
		// gerät:
		// Gerät;Tagid;Name;Beschreibung;status fales = nichtaugeliehen

		/** Aufteilung des Antwortstring aus der Datenbank */
		String[] daten = antwort.split(";;");

		Status = daten[0];
		Typ = daten[1];

		/** Antwort Fehlerabfrage */
		if (Status.contains("RECIVED")) {

			/** Abfrage ob Tag doppelt eingescannt wurde. */
			if (doppelt == false) {

				/** Abfrage nach User-Tag */
				if (Typ.contains("User") && !alterTyp.contains("User")) {
					dbid = daten[2];
					dbvname = daten[4];
					dbnname = daten[3];
					dbbeschreibung = daten[5];
					dbemail = daten[6];
					alterTyp = daten[1];
					idlist.add(id);
				
				/** Abfrage von Geräte-Tag */
				} else if (Typ.contains("Geraet") && alterTyp.contains("User")) {
					dbid = daten[2];
					dbgname = daten[3];
					dbbeschreibung = daten[4];
					dbstatus = daten[5];
					status();
					idlist.add(id);
					
				/** Abfangen eines unbekannten Tags */
				} else if (Typ.contains("Unbekannt") && alterTyp.contains("User")) {
					showDialogFehler("Unbekannte ID. Bitte an der Basisstation eintragen.");
				
				/**Abfangen von Einlogversuchen während ein User noch angemeldet ist */
				} else if (Typ.contains("User") && alterTyp.contains("User")) {
					showDialogFehler("Bitte erst den aktuellen Nutzer ausloggen.");
				}

				benutzer = alterTyp.contains("User");

			}

			/** Aufrufen der Fehlermeldung wenn kein User angemeldet ist */
			if (benutzer == false) {

				showDialogFehler("Bitte Benutzer anmelden");

			}

			/**Aufrufen der Fehlermeldung wenn keine Netzwerkverbindung vorhanden ist*/
		} else if (Status.contains("TIMEOUT")) {
			showDialogFehler("Keine Netzwerkverbindung.");

		}

		/** Aufrufen der Fehlermeldung wenn ein Tag doppelt eingelsen wird */
		if (doppelt == true && !Typ.contains("User")) {
			showDialogFehler("Gerät wurde bereits erfasst.");

			/** Versand der eingelesenen Daten, nach dem erneuten einlesen des
			 *  Benutzer-Tags, des aktuell eingelogten Benutzers.*/
		} else if (doppelt == true && Typ.contains("User")) {

			idlist.add(id);
			io.sendTransaction(idlist);
			benutzer = false;
			idlist.clear();
			namelist.clear();
			alterTyp = "clear";
		}

		/** Aufruf der Anzeigefunktion */
		anzeige();

	}

	/** Wird aktiv wenn der Button Legende gedrückt wird */
	public void ButtonOnClick(View v) {
		switch (v.getId()) {
		case R.id.button_legende:
			showDialogFehler("+ Gerät wird zurückgegeben\n- Gerät wird ausgeliehen");
			break;
		}
	}

	/** Wandelt die von dem Tag empfangene ID in einen String um */
	public String ByteArrayToHexString(byte[] byteid) {
		int i, j, in;
		String[] hex = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A",
				"B", "C", "D", "E", "F" };
		String out = "";
		String oute;

		for (j = 0; j < byteid.length; ++j) {
			in = (int) byteid[j] & 0xff;
			i = (in >> 4) & 0x0f;
			out += hex[i];
			i = in & 0x0f;
			out += hex[i];
			out += ":";
		}
		oute = out.substring(0, out.length() - 1);

		/** Sorgt für einheitliche ID-Länge */
		if (byteid.length == 4) {

			oute += ":FA:1E:00";
		}

		return oute;
	}

	/** Erstellt die Anzeige nach dem Einlesen eines Tags */
	public void anzeige() {

		TextView emailanzeigefeld = (TextView) findViewById(R.id.textView_Emailanzeige);
		TextView TextAnzahl = (TextView) findViewById(R.id.textView_anzahl);
		TextView Textname = (TextView) findViewById(R.id.textView_Benutzername);

		/** Abfrage ob ein Benutzer angemeldet ist */
		if (benutzer == true) {

			Textname.setText(dbvname + " " + dbnname);
			emailanzeigefeld.setText(dbemail);

		} else {

			Textname.setText("Erwarte Benutzer");
			emailanzeigefeld.setText("");
		}

		TextAnzahl.setText("" + namelist.size());

		/** Befühlt die Liste */

		listview = (ListView) findViewById(R.id.listView_geraete);

		listview.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, namelist));
		listview.setItemsCanFocus(true);

		/** Ermöglicht das Auswählen eines Objektes in der Liste */
		listview.setOnItemClickListener(new OnItemClickListener() {
			@SuppressWarnings("deprecation")
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				itemausg = position;
				showDialog(DIALOG_Item);

			}
		});

	}

	/** Fügt den Listenobjekten die Statusanzeige hinzu */
	public void status() {

		String ausleihen = "- ";
		String zurueckgeben = "+ ";

		if (dbstatus.contains("false")) {
			dbgname = ausleihen.concat(dbgname);
		} else if (dbstatus.contains("true")) {
			dbgname = zurueckgeben.concat(dbgname);
		}
		namelist.add(dbgname);

	}

	/** Ruft die Fehler Dialoge und den Legenden Dialog auf*/
	public void showDialogFehler(String text_uebergabe) {

		FragmentManager fmf = getSupportFragmentManager();
		DialogKlasseFehler dialog_fehler = new DialogKlasseFehler();
		dialog_fehler.GetText(text_uebergabe);
		dialog_fehler.show(fmf, "dialog_fehler");
	}

	@Override
	/** Erstellt den Item loeschen Dialog*/
	public Dialog onCreateDialog(int did) {

		AlertDialog.Builder dia;
		AlertDialog alert;
		
		dia = new AlertDialog.Builder(this);

		switch (did) {

		case DIALOG_Item:

			dia.setMessage("Gerät aus Liste löschen ?")
					.setCancelable(false)
					.setPositiveButton("Ja",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									idlist.remove(itemausg + 1);
									namelist.remove(itemausg);
									anzeige();

								}
							})
					.setNegativeButton("Nein",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {

									dialog.cancel();
								}
							});

			break;
		}

		alert = dia.create();
		return alert;

	}

}