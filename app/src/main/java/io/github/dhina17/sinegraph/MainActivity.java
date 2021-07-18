package io.github.dhina17.sinegraph;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import io.github.dhina17.sinegraph.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private Handler mHandler;
    private LineGraphSeries<DataPoint> series;
    private static final String X_VALUE_KEY = "X_VALUE_KEY";
    private static final String Y_VALUE_KEY = "Y_VALUE_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        EditText editText = mBinding.inputView;
        GraphView graph = mBinding.graphView;

        // Handle instance creation
        mHandler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                Bundle data = msg.getData();
                double x = data.getDouble(X_VALUE_KEY);
                double y = data.getDouble(Y_VALUE_KEY);
                series.appendData(new DataPoint(x, y), false, 10);
            }
        };

        // Initial empty graph values
        series = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(0, 0)
        });
        graph.addSeries(series);

        mBinding.enterButton.setOnClickListener((view) -> {
            // Get the input text
            String input = editText.getText().toString();

            // Post the Message to the queue by 3 seconds delay.
            mHandler.postDelayed(() -> {
                double x = Double.parseDouble(input);
                double inDeg = x / 3600;
                double y = Math.sin(inDeg);
                Message msg = Message.obtain(mHandler);
                Bundle data = new Bundle();
                data.putDouble(X_VALUE_KEY, x);
                data.putDouble(Y_VALUE_KEY, y);
                msg.setData(data);
                mHandler.sendMessage(msg);
            }, 3000);
            editText.setText("");
        });
    }
}