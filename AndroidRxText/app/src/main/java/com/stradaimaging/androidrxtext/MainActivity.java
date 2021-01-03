package com.stradaimaging.androidrxtext;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

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
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    static class State implements Serializable {
        int counter;
        int factorCounter;
        boolean isLower;
        String text;

        public State() {
            counter = 0;
            factorCounter = 0;
            isLower = true;
            text = "something";
        }
    }

    private DisposableObserver<Long> getVarObserver() {
        return new DisposableObserver<Long>() {
            @Override
            public void onNext(@NonNull Long longVal) {
                state.counter = longVal.intValue();
                changeUI();
            }

            @Override
            public void onError(@NonNull Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        };
    }

    private DisposableObserver<Integer> getTextObserver() {
        return new DisposableObserver<Integer>() {
            @Override
            public void onNext(@NonNull Integer integer) {
                String value = String.format("Factors: %s", integer);
                state.text = state.isLower ? value.toUpperCase() : value.toLowerCase();
                changeUI();
            }

            @Override
            public void onError(@NonNull Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        };
    }

    private @NonNull Observable<Long> getVarObservable() {
        return Observable.interval(500, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void onSaveInstanceState(@androidx.annotation.NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("state", gson.toJson(state));
    }

    private void changeUI() {
        textView.setText(String.format("Counter: %s", state.counter));
        textView1.setText(String.format("Text is: %s", state.text));
    }
    /*** STATE ***/

    /*** MAIN CODE ***/
    private void incrementCounter() {
        state.counter = 0;
        state.factorCounter = 0;
        changeUI();
    }

    private void changeTextCase() {
        state.isLower = !state.isLower;
        changeUI();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        reactiveInit();
        if (savedInstanceState != null) {
            state = gson.fromJson(savedInstanceState.getString("state"), State.class);
        }
        changeUI();

        button.setOnClickListener(view -> incrementCounter());
        button1.setOnClickListener(view -> changeTextCase());
        button2.setOnClickListener(view -> startActivity(new Intent(this, SecondActivity.class)));
    }

    private void reactiveInit() {
        Observable<Long> varObservable = getVarObservable();
        DisposableObserver<Long> varObserver = getVarObserver();
        DisposableObserver<Integer> textObserver = getTextObserver();
        compositeDisposable.add(
                varObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(integer -> integer = (long) ++this.state.counter)
                        .subscribeWith(varObserver)
        );

        compositeDisposable.add(
                varObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(integer -> this.state.counter%5 == 0 ? ++this.state.factorCounter : this.state.factorCounter)
                        .subscribeWith(textObserver)
        );
    }

    private void init() {
        // declarations
        button = findViewById(R.id.button);
        textView = findViewById(R.id.textView);
        button1 = findViewById(R.id.button1);
        textView1 = findViewById(R.id.textView1);
        button2 = findViewById(R.id.button2);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    private void showSnackbar(String message, int len) {
        Snackbar.make(findViewById(android.R.id.content), message, len).show();
    }

    private void showToast(String message, int len) {
        Toast.makeText(this, message, len).show();
    }
}