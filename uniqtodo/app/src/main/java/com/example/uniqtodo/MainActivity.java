package com.example.uniqtodo;

        import android.app.AlarmManager;
        import android.app.PendingIntent;
        import android.arch.lifecycle.Observer;
        import android.arch.lifecycle.ViewModelProviders;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.os.Bundle;
        import android.support.annotation.Nullable;
        import android.support.design.widget.FloatingActionButton;
        import android.support.v7.app.AppCompatActivity;
        import android.support.v7.widget.DividerItemDecoration;
        import android.support.v7.widget.LinearLayoutManager;
        import android.support.v7.widget.RecyclerView;
        import android.support.v7.widget.helper.ItemTouchHelper;
        import android.view.Menu;
        import android.view.View;
        import android.widget.TextView;

        import com.example.uniqtodo.database.Todo;

        import java.util.Calendar;
        import java.util.List;

        import static android.support.v7.widget.RecyclerView.VERTICAL;

public class MainActivity extends AppCompatActivity implements TodoAdapter.ItemClickListener {

    // Constant for logging
    private static final String TAG = MainActivity.class.getSimpleName();
    // Member variables for the adapter and RecyclerView
    private RecyclerView mRecyclerView;
    private TodoAdapter mAdapter;
    private MainViewModel mViewModel;
    private TextView notodos;
    private int exppts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);


        mViewModel.getTodos().observe(this, new Observer<List<Todo>>() {
            @Override
            public void onChanged(@Nullable List<Todo> todos)
            {

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.SECOND,5);

                Intent intent = new Intent("android.intent.action.DISPLAY_NOTIFICATION");
                Bundle bundle = new Bundle();
                bundle.putInt("todo_count", todos.size());
                intent.putExtras(bundle);
                PendingIntent broadcast = PendingIntent.getBroadcast(MainActivity.this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), broadcast);
            }
        });
             // Set the RecyclerView to its corresponding view
        mRecyclerView = findViewById(R.id.recyclerViewTasks);
        notodos = findViewById(R.id.textView2);

        // Set the layout for the RecyclerView to be a linear layout, which measures and
        // positions items within a RecyclerView into a linear list
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new TodoAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);

        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        mRecyclerView.addItemDecoration(decoration);

        /*
         Add a touch helper to the RecyclerView to recognize when a user swipes to delete an item.
         An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
         and uses callbacks to signal when a user is performing these actions.
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                if(swipeDir == ItemTouchHelper.RIGHT){
                    SharedPreferences prefs = getSharedPreferences("EXP", MODE_PRIVATE);
                    exppts = prefs.getInt("EXP", 0); //0 is the default value.
                    exppts=exppts+1;
                    SharedPreferences.Editor editor = getSharedPreferences("EXP", MODE_PRIVATE).edit();
                    editor.putInt("EXP", exppts);
                    editor.apply();

                    invalidateOptionsMenu();
                 }

                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        int position = viewHolder.getAdapterPosition();
                        List<Todo> todoList = mAdapter.getTodos();
                        mViewModel.delete(todoList.get(position));

                    }
                });
            }
        }).attachToRecyclerView(mRecyclerView);

        /*
         Set the Floating Action Button (FAB) to its corresponding View.
         Attach an OnClickListener to it, so that when it's clicked, a new intent will be created
         to launch the AddTaskActivity.
         */
        FloatingActionButton fabButton = findViewById(R.id.fab);

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a new intent to start an AddTaskActivity
                Intent addTaskIntent = new Intent(MainActivity.this, AddTodoActivity.class);
                startActivity(addTaskIntent);
            }
        });


        setUpViewModel();
    }

    protected void onResume() {
        super.onResume();


    }

    private void setUpViewModel() {
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);


        mViewModel.getTodos().observe(this, new Observer<List<Todo>>() {
            @Override
            public void onChanged(@Nullable List<Todo> todos)
            {

                if (todos.toString()=="[]"){
                   notodos.setText("Press + button below to add a to-do task");
                }
                else {
                    notodos.setText("");
                }
                mAdapter.setTodoList(todos);

            }
        });


    }

    @Override
    public void onItemClickListener(int itemId) {
        // Launch AddTaskActivity adding the itemId as an extra in the intent
        Intent intent = new Intent(MainActivity.this, AddTodoActivity.class);
        intent.putExtra(AddTodoActivity.EXTRA_TASK_ID, itemId);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.menu_main, menu);
        SharedPreferences prefs = getSharedPreferences("EXP", MODE_PRIVATE);
        exppts = prefs.getInt("EXP", 0); //0 is the default value.

        SharedPreferences.Editor editor = getSharedPreferences("EXP", MODE_PRIVATE).edit();
        editor.putInt("EXP", exppts);
        editor.apply();

        menu.findItem(R.id.exp).setTitle("EXP:"+exppts);
         return  true;
    }

}
