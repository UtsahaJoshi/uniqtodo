package com.example.uniqtodo;

import android.app.DatePickerDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uniqtodo.database.AppDatabase;
import com.example.uniqtodo.database.Todo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTodoActivity extends AppCompatActivity {

    // Extra for the task ID to be received in the intent
    public static final String EXTRA_TASK_ID = "extraTaskId";
    // Extra for the task ID to be received after rotation
    public static final String INSTANCE_TASK_ID = "instanceTaskId";
    // Constants for priority
    public static final int PRIORITY_HIGH = 1;
    public static final int PRIORITY_MEDIUM = 2;
    public static final int PRIORITY_LOW = 3;
    // Constant for default task id to be used when not in update mode
    private static final int DEFAULT_TODO_ID = -1;
    // Constant for logging
    private static final String TAG = AddTodoActivity.class.getSimpleName();
    // Fields for views
    EditText mEditText;
    RadioGroup mRadioGroup;
    Button mButton;

    EditText Datepick;

    private int mTodoId = DEFAULT_TODO_ID;

    private TextView mDisplayDate;
    private EditText mDateText;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    // Member variable for the Database
    private AppDatabase mDb;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_todo);

        mDisplayDate = (TextView) findViewById(R.id.Datepick);

        mDisplayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        AddTodoActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                Log.d(TAG, "onDateSet: mm/dd/yyy: " + month + "/" + day + "/" + year);

                String date = month + "/" + day + "/" + year;
                mDisplayDate.setText(date);
            }
        };

        initViews();

        mDb = AppDatabase.getInstance(getApplicationContext());

        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_TASK_ID)) {
            mTodoId = savedInstanceState.getInt(INSTANCE_TASK_ID, DEFAULT_TODO_ID);
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_TASK_ID)) {
            mButton.setText(R.string.update_button);
            if (mTodoId == DEFAULT_TODO_ID) {
                // populate the UI
                mTodoId = intent.getIntExtra(EXTRA_TASK_ID, DEFAULT_TODO_ID);

                        AddTodoViewModelFactory factory = new AddTodoViewModelFactory(mDb, mTodoId);
                        final AddTodoViewModel viewModel = ViewModelProviders.of(this, factory).get(AddTodoViewModel.class);

                        viewModel.getTodo().observe(AddTodoActivity.this, new Observer<Todo>() {
                            @Override
                            public void onChanged(@Nullable Todo todoEntry) {
                                viewModel.getTodo().removeObserver(this);
                                Log.d(TAG, "Receiving database update from LiveData");
                                populateUI(todoEntry);
                            }
                        });



            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(INSTANCE_TASK_ID, mTodoId);
        super.onSaveInstanceState(outState);
    }

    /**
     * initViews is called from onCreate to init the member variable views
     */
    private void initViews() {
        mEditText = findViewById(R.id.editTextTaskDescription);
        mDateText = findViewById(R.id.Datepick);
        mRadioGroup = findViewById(R.id.radioGroup);
        Datepick = findViewById(R.id.Datepick);
        mButton = findViewById(R.id.saveButton);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSaveButtonClicked();
            }
        });
    }

    /**
     * populateUI would be called to populate the UI when in update mode
     *
     * @param todo the todo to populate the UI
     */
    private void populateUI(Todo todo) {
        if(todo == null)
            return;
        mEditText.setText( todo.getDescription());
        mDateText.setText(todo.getTaskDate());
        setPriorityInViews(todo.getPriority());


    }

    /**
     * onSaveButtonClicked is called when the "save" button is clicked.
     * It retrieves user input and inserts that new task data into the underlying database.
     */
    public void onSaveButtonClicked() {
        String description = mEditText.getText().toString();
        int priority = getPriorityFromViews();
        Date date = new Date();
        String task_date = Datepick.getText().toString();

        // TODO (4) Make todo final so it is visible inside the run method
       final  Todo todo = new Todo(description, task_date, priority, date);
       AppExecutors.getInstance().diskIO().execute(new Runnable() {
           @Override
           public void run() {
               if(mTodoId == DEFAULT_TODO_ID)
                mDb.todoDao().insertTodo(todo);
               else{
                   todo.setId(mTodoId);
                   mDb.todoDao().update(todo);
               }
           }
       });
        // TODO (2) Get the diskIO Executor from the instance of AppExecutors and
        // call the diskIO execute method with a new Runnable and implement its run method
        // TODO (3) Move the remaining logic inside the run method

        finish();
    }

    /**
     * getPriority is called whenever the selected priority needs to be retrieved
     */
    public int getPriorityFromViews() {
        int priority = 1;
        int checkedId = ((RadioGroup) findViewById(R.id.radioGroup)).getCheckedRadioButtonId();
        switch (checkedId) {
            case R.id.radButton1:
                priority = PRIORITY_HIGH;
                break;
            case R.id.radButton2:
                priority = PRIORITY_MEDIUM;
                break;
            case R.id.radButton3:
                priority = PRIORITY_LOW;
        }
        return priority;
    }

    /**
     * setPriority is called when we receive a task from MainActivity
     *
     * @param priority the priority value
     */
    public void setPriorityInViews(int priority) {
        switch (priority) {
            case PRIORITY_HIGH:
                ((RadioGroup) findViewById(R.id.radioGroup)).check(R.id.radButton1);
                break;
            case PRIORITY_MEDIUM:
                ((RadioGroup) findViewById(R.id.radioGroup)).check(R.id.radButton2);
                break;
            case PRIORITY_LOW:
                ((RadioGroup) findViewById(R.id.radioGroup)).check(R.id.radButton3);
        }
    }


    public void getSpeechInput(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        if (intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent,10);
        } else {
            Toast.makeText(this,"Your device doesn't support speech input",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 10:
                if (resultCode==RESULT_OK && data != null){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    mEditText.setText(result.get(0));
                }
                break;
        }
    }
}
