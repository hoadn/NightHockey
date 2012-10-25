package nighthockey.game;

import game.nighthockey.R;

import java.net.InetAddress;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class Multiplayer extends Activity {
	TextView ipAddress;
	RadioGroup radios;
	RadioButton selectedButton;
	Button startGame;
	EditText connectIp;
	
	/* When connection is created into server from client handle message */
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			Toast.makeText(getBaseContext(), "connected", Toast.LENGTH_SHORT).show();
			
			Intent intent = new Intent(getBaseContext(), NightHockeyActivity.class);
			startActivity(intent);
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer);
        ipAddress = (TextView)findViewById(R.id.ipAddress);
        radios = (RadioGroup)findViewById(R.id.radioGroup1);
        startGame = (Button)findViewById(R.id.buttonStart);
        connectIp = (EditText)findViewById(R.id.editText1);
        connectIp.setText("192.168.1.31");
        
        try{
            InetAddress ownIP=InetAddress.getLocalHost();
            ipAddress.setText(ownIP.getHostAddress());
        } catch (Exception e){}
        
        startGame.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				NightHockeyActivity.ONLINE_GAME = true;
				selectedButton = (RadioButton)findViewById(radios.getCheckedRadioButtonId());
				NetworkHandler net = NetworkHandler.getInstance();
				
				if (selectedButton.getText().equals("Server")){
					net.startServer();
					NightHockeyActivity.SERVER = true;
					startGame.setEnabled(false);
				}
				else{
					Toast.makeText(getBaseContext(), "Start client game. Address:" +connectIp.getText(), Toast.LENGTH_SHORT).show();
					NightHockeyActivity.SERVER = false;
					net.connectClient(connectIp.getText().toString());	
				}	
				
				waitConnection();
			}
		});
    }
    
    /* Wait until client has connected to server and send message after that */
    private void waitConnection() {
		Thread background = new Thread(new Runnable() {
			@Override
			public void run() {
				boolean running = true;
				
				while(running) {
					try { Thread.sleep(500); } catch (Exception e) {}
					
					NetworkHandler net = NetworkHandler.getInstance();
					if(net.isConnected()) {
						handler.sendMessage(handler.obtainMessage());
						running = false;
					}
				}
			}
		});

		background.start();	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_multiplayer, menu);
        return true;
    }
}
