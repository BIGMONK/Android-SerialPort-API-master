/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package km1930.serialport.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.serialport.sample.R;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import km1930.serialport.sample.mainboard.MainBoardUpdateActivity;

public class MainMenu extends Activity {

    @BindView(R.id.ButtonMainBoard)
    Button ButtonMainBoard;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ButterKnife.bind(this);

        final Button buttonSetup = (Button) findViewById(R.id.ButtonSetup);
        buttonSetup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainMenu.this, SerialPortPreferences.class));
            }
        });

        final Button buttonConsole = (Button) findViewById(R.id.ButtonConsole);
        buttonConsole.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainMenu.this, ConsoleActivity.class));
            }
        });

        final Button buttonLoopback = (Button) findViewById(R.id.ButtonLoopback);
        buttonLoopback.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainMenu.this, LoopbackActivity.class));
            }
        });

        final Button button01010101 = (Button) findViewById(R.id.Button01010101);
        button01010101.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainMenu.this, Sending01010101Activity.class));
            }
        });

        final Button buttonAbout = (Button) findViewById(R.id.ButtonAbout);
        buttonAbout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainMenu.this);
                builder.setTitle("About");
                builder.setMessage(R.string.about_msg);
                builder.show();
            }
        });

        final Button buttonQuit = (Button) findViewById(R.id.ButtonQuit);
        buttonQuit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainMenu.this.finish();
            }
        });
    }

    @OnClick(R.id.ButtonMainBoard)
    public void onViewClicked(View view) {
        switch (view.getId()){
            case R.id.ButtonMainBoard:
                startActivity(new Intent(MainMenu.this, MainBoardUpdateActivity.class));
                break;
        }
    }
}
