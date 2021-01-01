package com.stradaimaging.androidrxtext;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.io.Serializable;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private TextView textView;
    private Button button1;
    private TextView textView1;
    private Button button2;
    private final String TAG = "LOGS";

    /*** STATE ***/
    private final Gson gson = new Gson();
    private State state = new State();
    private Subject<State> stateObservable;
    private Observer<State> stateObserver;
    private Disposable disposable;

    static class State implements Serializable {
        int counter;
        boolean isLower;
        String text;
        public State(){
            counter = 0;
            isLower = true;
            text = "something";
        }
    }

    private Subject<State> getStateObservable(){
        return PublishSubject.create();
    }

    private Observer<State> getStateObserver(){
        return new Observer<State>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(@NonNull State state) {
                textView.setText(String.valueOf(state.counter));
                button.setText(String.format("Count: %s with text: %s", state.counter, state.text));
                textView1.setText(state.text);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Log.d(TAG, "error => " + e.getLocalizedMessage());
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "state updated!");
            }
        };
    }

    private void setState(){
        stateObservable.onNext(state);
    }

    @Override
    protected void onSaveInstanceState(@androidx.annotation.NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("state", gson.toJson(state));
    }

    /*** STATE ***/

    /*** MAIN CODE ***/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        if (savedInstanceState != null) {
            state = gson.fromJson(savedInstanceState.getString("state"), State.class);
        }
        setState();

        button.setOnClickListener(view -> {
            ++state.counter;
            setState();
        });

        button1.setOnClickListener(view -> {
            state.text = state.isLower ? state.text.toUpperCase() : state.text.toLowerCase();
            state.isLower = !state.isLower;
            setState();
        });

        button2.setOnClickListener(view -> startActivity(new Intent(this, SecondActivity.class)));
    }

    private void init() {
        stateObservable = getStateObservable();
        stateObserver = getStateObserver();
        stateObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(stateObserver);

        button = findViewById(R.id.button);
        textView = findViewById(R.id.textView);
        button1 = findViewById(R.id.button1);
        textView1 = findViewById(R.id.textView1);
        button2 = findViewById(R.id.button2);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.dispose();
    }

    private void showSnackbar(String message, int len) {
        Snackbar.make(findViewById(android.R.id.content), message, len).show();
    }

    private void showToast(String message, int len) {
        Toast.makeText(this, message, len).show();
    }
}