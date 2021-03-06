package com.example.quickbloxchat.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.example.quickbloxchat.ChatHelper;
import com.example.quickbloxchat.ItemClickListener;
import com.example.quickbloxchat.adapter.ChatDialogAdapter;
import com.example.quickbloxchat.R;
import com.example.quickbloxchat.qb.QbDialogHolder;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.sample.core.ui.dialog.ProgressDialogFragment;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

public class ChatDialogActivity extends AppCompatActivity implements View.OnClickListener, ItemClickListener {

    private FloatingActionButton floatingActionButton;

    private RecyclerView recyclerView;
    public static final String EXTRA_QB_USERS = "qb_users";

    private ArrayList<QBUser> users = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_dialog);

        findIds();
        setClicks();

        createSessionForChat();

        loadChatDialogs();
    }

    private void loadChatDialogs() {

        QBRequestGetBuilder qbRequestGetBuilder = new QBRequestGetBuilder();
        qbRequestGetBuilder.setLimit(100);

        QBRestChatService.getChatDialogs(null, qbRequestGetBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {
                ChatDialogAdapter chatDialogAdapter = new ChatDialogAdapter(ChatDialogActivity.this, qbChatDialogs);
                recyclerView.setLayoutManager(new LinearLayoutManager(ChatDialogActivity.this));
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(chatDialogAdapter);
                chatDialogAdapter.setItemClickListener(ChatDialogActivity.this);
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    private void createSessionForChat() {
        final ProgressDialog progressDialog = new ProgressDialog(ChatDialogActivity.this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        String user, password;
        int id;

        user = getIntent().getStringExtra("userName");
        password = getIntent().getStringExtra("password");
        id = getIntent().getIntExtra("id", 0);



        final QBUser qbUser = new QBUser(user, password);
        qbUser.setId(id);


        QBChatService.getInstance().login(qbUser, new QBEntityCallback() {
            @Override
            public void onSuccess(Object o, Bundle bundle) {
                progressDialog.dismiss();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("Error", e.getMessage());
            }
        });
//            }
//
//            @Override
//            public void onError(QBResponseException e) {
//
//            }
//        });
    }

    private void setClicks() {
        floatingActionButton.setOnClickListener(this);
    }

    private void findIds() {
        floatingActionButton = findViewById(R.id.chat_dialog_add_user);
        recyclerView = findViewById(R.id.chat_dialog_recycler_view);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chat_dialog_add_user:
                Intent allUsersIntent = new Intent(ChatDialogActivity.this, ListUsersActivity.class);
                startActivity(allUsersIntent);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChatDialogs();
    }

    @Override
    public void onClick1(View view, int position) {
        //ChatActions();
    }

//    private void ChatActions() {
//
//
//        users.add(QBChatService.getInstance().getUser());
//
//        users.add(SellerQbUser);
//
//        if (isPrivateDialogExist(users)) {
//            users.remove(ChatHelper.getCurrentUser());
//            QBChatDialog existingPrivateDialog = QbDialogHolder.getInstance().getPrivateDialogWithUser(users.get(0));
//
//            ChatActivity.startForResult(activity, REQUEST_DIALOG_ID_FOR_UPDATE, existingPrivateDialog);
//            users.clear();
//        } else {
//            ProgressDialogFragment.show(getSupportFragmentManager(), R.string.loading_chat);
//            createDialog(users);
//            users.clear();
//        }
//
//    }
}
