package me.samthompson.bubbleactions_sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.sam.bubbleactions_sample.R
import me.samthompson.bubbleactions.BubbleActions

/**
 * Created by sam on 11/28/15.
 */
public class KotlinActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kotlin)

        val textView = findViewById(R.id.text_view);
        textView.setOnLongClickListener {
            BubbleActions.on(textView)
                    .addAction("Star", R.drawable.bubble_star, {
                        Toast.makeText(textView.context, "Star pressed!", Toast.LENGTH_SHORT).show()
                    })
                    .addAction("Share", R.drawable.bubble_share, {
                        Toast.makeText(textView.context, "Share pressed!", Toast.LENGTH_SHORT).show()
                    })
                    .addAction("Hide", R.drawable.bubble_hide, {
                        Toast.makeText(textView.context, "Hide pressed!", Toast.LENGTH_SHORT).show()
                    })
                    .show()
            true
        }
    }

}