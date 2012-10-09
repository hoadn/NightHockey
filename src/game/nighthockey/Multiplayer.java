package game.nighthockey;

import java.net.InetAddress;

import android.app.Activity;
import android.os.Bundle;
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
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer);
        ipAddress = (TextView)findViewById(R.id.ipAddress);
        radios = (RadioGroup)findViewById(R.id.radioGroup1);
        startGame = (Button)findViewById(R.id.buttonStart);
        connectIp = (EditText)findViewById(R.id.editText1);
        connectIp.setText("000.000.000.000");
        try{
            InetAddress ownIP=InetAddress.getLocalHost();
            System.out.println("IP of my Android := "+ownIP.getHostAddress());
            ipAddress.setText(ownIP.getHostAddress());
            }catch (Exception e){
            System.out.println("Exception caught ="+e.getMessage());
            }
        
        startGame.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				selectedButton = (RadioButton)findViewById(radios.getCheckedRadioButtonId());
				System.out.println("radio := "+selectedButton.getId());
				selectedButton.getId();
				if (selectedButton.getText().equals("Server")){
					Toast.makeText(getBaseContext(), "Start server game", Toast.LENGTH_SHORT).show();
				}
				else
					Toast.makeText(getBaseContext(), "Start client game. Address:" +connectIp.getText(), Toast.LENGTH_SHORT).show();
			}
		});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_multiplayer, menu);
        return true;
    }
}
