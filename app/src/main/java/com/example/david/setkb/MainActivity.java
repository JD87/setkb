package com.example.david.setkb;

import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    private void setKeyboardView() {
        Keyboard keys = new Keyboard(this, R.xml.keys_layout);
        KeyboardView kv = (KeyboardView) findViewById(R.id.keyboard_view);
        kv.setKeyboard(keys);

        kv.setEnabled(true);
        kv.setClickable(true);

        KeyboardView.OnKeyboardActionListener listener = new KeyboardView.OnKeyboardActionListener() {

            @Override
            public void onKey(int primaryCode, int[] keyCodes) {
            }

            @Override
            public void onPress(int primaryCode) {
                TextView tv = (TextView) findViewById(R.id.text_view);
                tv.setText("" + primaryCode);
                Toast.makeText(getBaseContext(),"hey ohh! " + primaryCode, Toast.LENGTH_SHORT).show();
                
            }

            @Override
            public void onRelease(int primaryCode) {
            }

            @Override
            public void onText(CharSequence text) {
                // TODO Auto-generated method stub

            }

            @Override
            public void swipeDown() {
                // TODO Auto-generated method stub

            }

            @Override
            public void swipeLeft() {
                // TODO Auto-generated method stub

            }

            @Override
            public void swipeRight() {
                // TODO Auto-generated method stub

            }

            @Override
            public void swipeUp() {
                // TODO Auto-generated method stub

            }

        };
        kv.setOnKeyboardActionListener(listener);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setKeyboardView();

    }


}
