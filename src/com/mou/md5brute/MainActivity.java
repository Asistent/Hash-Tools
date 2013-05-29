package com.mou.md5brute;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.view.View.*;
import android.text.ClipboardManager;
import java.io.*;
import android.net.*;
import android.content.Intent;
import java.util.*;
import java.security.*;
import java.nio.channels.*;
import java.nio.*;
import java.nio.charset.*;
import android.content.Context;
import android.content.DialogInterface;

public class MainActivity extends Activity
{
    /** Called when the activity is first created. */
	
	public class HASH
	{
		String algorithm;
		String password;
		public HASH(String str, String algo){
			password = str;
			algorithm = algo;
		}
		public String hexdigest()
		//Compute the hash and return the hex value
		{
			byte[] hash      = null;
			byte[] pass      = password.getBytes();
			try
			{
				hash = MessageDigest.getInstance(algorithm).digest(pass);
			}
			catch (NoSuchAlgorithmException e)
			{
				throw new Error("No hashing support in this VM.");
			}

			StringBuilder hashString = new StringBuilder();
			for (int i = 0; i < hash.length; i++)
			{
				String hex = Integer.toHexString(hash[i]);

				if (hex.length() == 1)
				{
					hashString.append('0');
					hashString.append(hex.charAt(hex.length() - 1));
				}
				else
					hashString.append(hex.substring(hex.length() - 2));
			}
			return hashString.toString();
		}
	}
	public class Brute {
		String toCrack, lastHash, lastTry, rAlgo;
		String table = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!?., ";
		Hashtable<Integer, Integer> pows = new Hashtable<Integer,Integer>();

		int tries = 0, lenght = 1;

		public Brute(String hash,String Algo){
			toCrack = hash;
			rAlgo = Algo;
			int caracteresMax = 20;
			for (int compteur = 0; compteur <= caracteresMax; compteur++){
				//make a table to know when the script tried all possibilities
				pows.put(new Integer(compteur), (int)(Math.pow(table.length(), compteur*2.1)));
			}

		}
		public void setLen(int len){
			//Just set the lenght of the next generated word
			lenght = len;
		}
		public void jump(){
			//add chars to the next word if the app tried all possibilities
			if (tries > (int)pows.get(lenght-1)){
				lenght++;
			}
		}


		private String randomString(int len) 
		{
			//generate a random string from table
			Random rnd = new Random();
			StringBuilder sb = new StringBuilder(len);
			for( int i = 0; i < len; i++ )
				sb.append(table.charAt(rnd.nextInt(table.length())));
			return sb.toString();
		}


		public boolean shot(){
			//Launch one try and return 1 if found any solutions
			tries++;
			lastTry = randomString(lenght);
			HASH hash = new HASH(lastTry, rAlgo);
			lastHash = hash.hexdigest();
			if (lastHash.equals(toCrack)){
				return true;
			}
			return false;
		}

	}
	
	
	@Override
	class iCrack extends AsyncTask<String, String, String> {
		final TextView informations = (TextView) findViewById(R.id.Informations);
		@Override
		public String doInBackground(String... params) {
			String tocrack = params[0];
			String ralgo = params[1];
			Brute force = new Brute(tocrack,ralgo);
			force.setLen(1);
			while (true) {
				force.jump();
				if (force.shot()){
					break;
				}
				else{
					publishProgress(force.lastTry, force.lastHash);
				}
			}
			return (force.lastTry);
		}
		@Override
		public void onProgressUpdate(String... results) {
			informations.setText("Testing: "+results[0]+" "+results[1]);
		}
		public void onPostExecute(String result){
			informations.setText("Cracked hash= "+result);
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			clipboard.setText(result);
			Toast.makeText(getApplicationContext(),"Copied text to clipboard",Toast.LENGTH_SHORT).show();
		}
	}
	protected void onActivityResult(int RequestCode, int ResultCode, Intent data){
		if (RequestCode==1){
			final ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			final TextView informations = (TextView) findViewById(R.id.Informations);
			try{
			final EditText input = (EditText) findViewById(R.id.ToCrack);
			String filepath = data.getDataString();
			String hash;
			MessageDigest digest = MessageDigest.getInstance(selectedalgorithm);
			filepath = filepath.split("//")[1];
			File file = new File(filepath);
			FileInputStream f = new FileInputStream(file);
			Toast.makeText(getApplicationContext(),"Loading: "+filepath,Toast.LENGTH_LONG).show();
			byte[] buffer = new byte[8192];
			int len = 0;
			while (-1 != (len = f.read(buffer))){
				digest.update(buffer,0,len);
			}
			byte[] bhash = digest.digest();
			StringBuilder hashString = new StringBuilder();
			for (int i = 0; i < bhash.length; i++)
			{
				String hex = Integer.toHexString(bhash[i]);

				if (hex.length() == 1)
				{
					hashString.append('0');
					hashString.append(hex.charAt(hex.length() - 1));
				}
				else{
					hashString.append(hex.substring(hex.length() - 2));
				}
			}
			hash = hashString.toString();
			if (hash.equals(input.getText().toString())){
				informations.setText("Hash correspond with the file!");
			}
			else{
				informations.setText("File hash= "+hash+"\nHashes does not correspond.");
			}
			Toast.makeText(getApplicationContext(),"Copied file hash to clipboard.",Toast.LENGTH_LONG).show();
			clipboard.setText(hash);
			}catch(Exception e){informations.setText("Error while reading file");}
		}
	}
	iCrack cracker;
	final Context context = this;
	int choice=0;
	String selectedalgorithm="MD5";//DEFAULT
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		final TextView informations = (TextView) findViewById(R.id.Informations);
		final EditText input = (EditText) findViewById(R.id.ToCrack);
		final Button crackit = (Button) findViewById(R.id.CrackIt);
		final Button tohash  = (Button) findViewById(R.id.tomd5);
		final Button selectfile = (Button) findViewById(R.id.selectfile);
		final Button stop = (Button) findViewById(R.id.stop);
		final Button change = (Button) findViewById(R.id.change);
		final String[] algorithms = {"MD2","MD4","MD5","SHA1","SHA256","SHA512"};
		change.setText(selectedalgorithm);
		
		change.setOnClickListener(new OnClickListener(){
			public void onClick(View p1){
				new AlertDialog.Builder(context)
				.setSingleChoiceItems(algorithms,choice,null)
				.setPositiveButton("Ok",new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog,int whichButton){
						dialog.dismiss();
						int pos = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
						choice = pos;
						selectedalgorithm = algorithms[pos];
						change.setText(selectedalgorithm);
					}
				}).show();
			}
		});
		
		stop.setOnClickListener(new OnClickListener(){
			public void onClick(View p1){
				try{
					cracker.cancel(true);
					Toast.makeText(getApplicationContext(),"Stopped",Toast.LENGTH_SHORT).show();
					
				}catch(Exception e){
					Toast.makeText(getApplicationContext(),"Nothing to stop",Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		selectfile.setOnClickListener(new OnClickListener() {
			public void onClick(View p1){
				try{
					cracker.cancel(true);
				}catch(Exception e){}
				Intent i = new Intent(Intent.ACTION_GET_CONTENT);
				i.setType("file/*");
				startActivityForResult(i,1);
			}
		});
		
		
		crackit.setOnClickListener(new OnClickListener() {
			public void onClick(View p1) {
				try{
					cracker.cancel(true);
				}catch(Exception e){}
				cracker = new iCrack();
				String Hash = input.getText().toString();
				if (Hash.length() >= 32){
					cracker.execute(Hash,selectedalgorithm);
				}
				else{
					informations.setText("Invalid hash");
				}
			}
		});
		tohash.setOnClickListener(new OnClickListener() {
			public void onClick(View p1) {
				try{
					cracker.cancel(true);
				}catch(Exception e){}
				HASH hash = new HASH(input.getText().toString(), selectedalgorithm);
				String shash = hash.hexdigest();
				informations.setText("Hash= "+shash);
				ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				clipboard.setText(shash);
				Toast.makeText(getApplicationContext(),"Copied hash to clipboard",Toast.LENGTH_SHORT).show();
			}
		});
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.layout.menu, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()){
			case R.id.emailme:
			    Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("message/rfc822");
				i.putExtra(Intent.EXTRA_EMAIL, new String[]{"arnaudalies.py@gmail.com"});
				i.putExtra(Intent.EXTRA_SUBJECT,"Feedback of md5 tools");
				try{
					startActivity(Intent.createChooser(i,"Send mail..."));
				}
				catch (android.content.ActivityNotFoundException e){
					Toast.makeText(getApplicationContext(),"no email client found...", Toast.LENGTH_SHORT).show();
				}
				return true;

			case R.id.rateit:
			    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.mou.md5brute")));
				return true;
				
			case R.id.donate:
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.mou.donate")));
				return true;
			default:
			    return false;
		}
	}
}
