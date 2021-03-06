package com.example.quickbloxchat.activity;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.quickbloxchat.App;
import com.example.quickbloxchat.ChatHelper;
import com.example.quickbloxchat.Consts;
import com.example.quickbloxchat.R;
import com.example.quickbloxchat.activity.services.CallService;
import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.auth.session.QBSettings;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.core.utils.SharedPrefsHelper;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static final String APP_ID = "76720";
    static final String AUTH_KEY = "shkJz23eJhkVxOX";
    static final String AUTH_SECRET = "YUgd84Eedun2LWz";
    static final String ACCOUNT_KEY = "9bsxYem2DfdWm7xU79zc";

    private Button loginButton, signUpButton;

    private EditText userEditText, passEditText;

    private SharedPrefsHelper sharedPrefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setClicks();

        initializeFramework();

        restoreChatSession();

    }

    private void restoreChatSession() {
        if (ChatHelper.getInstance().isLogged() && sharedPrefsHelper.hasQbUser()) {
            startLoginService(sharedPrefsHelper.getQbUser());

        } else {
            QBUser currentUser = getUserFromSession();
            if (currentUser == null) {


            } else {

                startLoginService(currentUser);
                //loginToChat(currentUser);
            }
        }
    }

    private QBUser getUserFromSession() {
        QBUser user = SharedPrefsHelper.getInstance().getQbUser();
        QBSessionManager qbSessionManager = QBSessionManager.getInstance();
        if (qbSessionManager.getSessionParameters() == null) {
            ChatHelper.getInstance().destroy();
            return null;
        }
        Integer userId = qbSessionManager.getSessionParameters().getUserId();
        if(user!=null)
        {
            user.setId(userId);
        }

        return user;
    }

    protected void startLoginService(QBUser qbUser) {
        CallService.start(this, qbUser);
    }

    private void initializeFramework() {
        QBSettings.getInstance().init(MainActivity.this, APP_ID, AUTH_KEY, AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);
    }

    private void setClicks() {
        loginButton.setOnClickListener(this);
        signUpButton.setOnClickListener(this);
    }

    private void initViews() {
        loginButton = findViewById(R.id.main_btnLogin);
        signUpButton = findViewById(R.id.main_btnSignup);

        passEditText = findViewById(R.id.main_editPassword);
        userEditText = findViewById(R.id.main_editLogin);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_btnLogin:
                final String userName = userEditText.getText().toString();
                final String password = passEditText.getText().toString();

                QBUser qbUser = new QBUser(userName, password);
                qbUser.setPassword(password);
                qbUser.setOldPassword(password);

                QBUsers.signIn(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        startAfterLoginService(qbUser);
                        Toast.makeText(MainActivity.this, "Login successfully", Toast.LENGTH_SHORT).show();

                        QBChatService.setDebugEnabled(true); // enable chat logging

                        QBChatService.setDefaultPacketReplyTimeout(10000);

                        QBChatService.ConfigurationBuilder chatServiceConfigurationBuilder = new QBChatService.ConfigurationBuilder();
                        chatServiceConfigurationBuilder.setSocketTimeout(60); //Sets chat socket's read timeout in seconds
                        chatServiceConfigurationBuilder.setKeepAlive(true); //Sets connection socket's keepAlive option.
                        chatServiceConfigurationBuilder.setUseTls(true); //Sets the TLS security mode used when making the connection. By default TLS is disabled.
                        QBChatService.setConfigurationBuilder(chatServiceConfigurationBuilder);
                        Intent chatDialogIntent = new Intent(MainActivity.this, ChatDialogActivity.class);
                        chatDialogIntent.putExtra("userName", userName);
                        chatDialogIntent.putExtra("password", password);
                        chatDialogIntent.putExtra("id", qbUser.getId());
                        startActivity(chatDialogIntent);

                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                break;

            case R.id.main_btnSignup:
                startActivity(new Intent(MainActivity.this, SignUpActivity.class));
        }
    }

    private void startAfterLoginService(QBUser qbUser) {
        Intent tempIntent = new Intent(this, CallService.class);
        PendingIntent pendingIntent = createPendingResult(Consts.EXTRA_LOGIN_RESULT_CODE, tempIntent, 0);
        CallService.start(this, qbUser, pendingIntent);
    }
}
