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

import android.os.Bundle;
import android.serialport.sample.R;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;

public class ConsoleActivity extends SerialPortActivity {
    private static final String TAG = "ConsoleActivity";
    EditText mReception;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.console);

        //		setTitle("Loopback test");
        mReception = (EditText) findViewById(R.id.EditTextReception);

        EditText Emission = (EditText) findViewById(R.id.EditTextEmission);
        Emission.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                int i;
                CharSequence t = v.getText();
                char[] text = new char[t.length()];
                for (i = 0; i < t.length(); i++) {
                    text[i] = t.charAt(i);
                }
                try {
                    mOutputStream.write(new String(text).getBytes());
                    mOutputStream.write('\n');
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);

    }

    ArrayList<Byte> getDatas = new ArrayList<>();

    @Override
    protected void onDataReceived(final byte[] buffer, final int size) {
        for (int i = 0; i < size; i++) {
            getDatas.add(buffer[i]);
            Log.d(TAG, "onDataReceived:getDatas.add=" + buffer[i]);
            if (getDatas.size() == 9 && ((getDatas.get(0) == 85 && getDatas.get(8) == -86) ||
                    (getDatas.get(0) == -86 && getDatas.get(8) == 85))) {
                //TODO 获取到一次正确的数据
                Log.e(TAG, "onDataReceived: getDatas.size()==9&&(getDatas.get(0)==85||getDatas" +
                        ".get(0)==-86)");

                ArrayList<Byte> data=new ArrayList<Byte>();
                data.addAll(getDatas);
                EventBus.getDefault().post(data);

                getDatas.clear();

            }
        }
        if (getDatas.size() >= 9) {
            getDatas.clear();
            Log.d(TAG, "onDataReceived: getDatas.size()>=9");
        }
        runOnUiThread(new Runnable() {
            public void run() {
                if (mReception != null) {
                    mReception.append(new String(buffer, 0, size));
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onMessage(ArrayList<Byte> getDatas) {
        Log.e(TAG, "onMessage: "
                + getDatas.get(0)
        );
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
