package game.nighthockey;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class NightHockeyActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_night_hockey);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_night_hockey, menu);
        return true;
    }
}
