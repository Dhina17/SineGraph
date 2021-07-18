package io.github.dhina17.sinegraph;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import io.github.dhina17.sinegraph.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {

    private GraphHandler mHandler;
    private static final LineGraphSeries<DataPoint> SERIES = new LineGraphSeries<>(
            new DataPoint[]{new DataPoint(0, 0)}
    );
    private static final String X_VALUE_KEY = "X_VALUE_KEY";
    private static final String Y_VALUE_KEY = "Y_VALUE_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        EditText editText = mBinding.inputView;
        GraphView graph = mBinding.graphView;

        // HandlerThread for Background thread with looper
        HandlerThread handlerThread = new HandlerThread("NotMainThread");
        handlerThread.start();

        // Handle instance creation
        mHandler = new GraphHandler(handlerThread.getLooper());

        // Add series to the graph view
        graph.addSeries(SERIES);

        // Enter button onClick Listener
        mBinding.enterButton.setOnClickListener((view) -> {
            // Get the input text
            String input = editText.getText().toString();
            // Handle empty text
            if (input.isEmpty()) {
                Toast.makeText(this, "Empty Input", Toast.LENGTH_SHORT).show();
                return;
            }
            // Add x value to the list
            enter(input);
            // Reset the text
            editText.setText("");
        });

        // Play button onClick
        mBinding.playButton.setOnClickListener((view) -> play());

        // Pause button onClick
        mBinding.pauseButton.setOnClickListener((view) -> pause());
    }

    private void enter(String input) {
        // Post the message
        mHandler.post(() -> {
            double x = Double.parseDouble(input);
            double inDeg = x / 3600;
            double y = Math.sin(inDeg);
            Message msg = Message.obtain(mHandler);
            Bundle data = new Bundle();
            data.putDouble(X_VALUE_KEY, x);
            data.putDouble(Y_VALUE_KEY, y);
            msg.setData(data);
            mHandler.sendMessage(msg);
        });
    }

    private void play() {
        mHandler.play();
        Toast.makeText(this, "Play", Toast.LENGTH_LONG).show();
    }

    private void pause() {
        mHandler.pause();
        Toast.makeText(this, "Pause", Toast.LENGTH_LONG).show();
    }

    // Custom Handler to handle pause and play action
    static class GraphHandler extends Handler {
        public GraphHandler(Looper looper) {
            super(looper);
            // Start the timer task.
            updateGraph();
        }

        // Queue to hold the data
        private final Queue<DataPoint> queue = new LinkedList<>();
        // Bool pointed to pause state
        private boolean isPaused = false;

        public synchronized void pause() {
            isPaused = true;
        }

        public synchronized void play() {
            isPaused = false;
        }

        // Update graph with timer
        // Update the graph data for every 3 seconds.(To show the background process correctly)
        private synchronized void updateGraph() {
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (!isPaused && !queue.isEmpty()) {
                        DataPoint dp = queue.remove();
                        Log.i("GraphView", "Plotted x = " + dp.getX() + " y = " + dp.getY());
                        MainActivity.SERIES.appendData(dp, false, 100);
                    }
                }
            }, 0, 3000L);
        }

        // Get the points from the message and add it to the queue.
        @Override
        public void handleMessage(@NonNull Message msg) {
            Bundle data = msg.getData();
            double x = data.getDouble(MainActivity.X_VALUE_KEY);
            double y = data.getDouble(MainActivity.Y_VALUE_KEY);
            DataPoint dataPoint = new DataPoint(x, y);
            queue.add(dataPoint);
        }
    }
}

