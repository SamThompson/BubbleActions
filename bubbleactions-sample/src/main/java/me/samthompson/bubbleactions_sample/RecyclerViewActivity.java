package me.samthompson.bubbleactions_sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sam.bubbleactions_sample.R;

import me.samthompson.bubbleactions.BubbleActions;

public class RecyclerViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new Adapter());
    }

    static class Adapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.item = position;
            holder.textView.setText(R.string.long_press_me);
        }

        @Override
        public int getItemCount() {
            return 10;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        int item;
        TextView textView;

        public ViewHolder(final View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.item_text);

            // on long click, show the bubble actions
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    BubbleActions.on(v)
                            .addAction("Star", R.drawable.ic_star, R.drawable.popup_item, new BubbleActions.Callback() {
                                @Override
                                public void doAction() {
                                    Toast.makeText(v.getContext(), "Star pressed on item " + item + "!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addAction("Share", R.drawable.ic_share, R.drawable.popup_item, new BubbleActions.Callback() {
                                @Override
                                public void doAction() {
                                    Toast.makeText(v.getContext(), "Share pressed on item " + item + "!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addAction("Hide", R.drawable.ic_hide, R.drawable.popup_item, new BubbleActions.Callback() {
                                @Override
                                public void doAction() {
                                    Toast.makeText(v.getContext(), "Hide pressed on item " + item + "!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .show();
                    return false;
                }
            });
        }
    }
}
