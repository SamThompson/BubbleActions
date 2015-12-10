package me.samthompson.bubbleactions_sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import me.samthompson.bubbleactions.BubbleActions;
import me.samthompson.bubbleactions.MenuCallback;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        findViewById(R.id.text_view).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                BubbleActions.on(v)
                        .fromMenu(R.menu.menu_actions, new MenuCallback() {
                            @Override
                            public void doAction(int itemId) {
                                switch (itemId) {
                                    case R.id.action_star:
                                        Toast.makeText(v.getContext(), "Star pressed!", Toast.LENGTH_SHORT).show();
                                        break;
                                    case R.id.action_share:
                                        Toast.makeText(v.getContext(), "Share pressed!", Toast.LENGTH_SHORT).show();
                                        break;
                                    case R.id.action_hide:
                                        Toast.makeText(v.getContext(), "Hide pressed!", Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            }
                        })
                        .show();
                return true;
            }
        });
    }
}
