package com.jying.rainbow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.jying.rainbow.Database.DatabaseHelper;

/**
 * Created by Jying on 2017/9/9.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    TextInputEditText et_username;
    TextInputEditText et_password;
    Button bt_login;
    Button bt_register;
    DatabaseHelper helper;
    SQLiteDatabase db;
    public static LoginActivity instance;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        instance=this;
        init();
        helper = new DatabaseHelper(this);
        db = helper.getReadableDatabase();
    }

    private void init() {
        et_username = (TextInputEditText) findViewById(R.id.et_username);
        et_password = (TextInputEditText) findViewById(R.id.et_password);
        bt_login = (Button) findViewById(R.id.bt_login_login);
        bt_register = (Button) findViewById(R.id.bt_login_register);
        bt_login.setOnClickListener(this);
        bt_register.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_login_login:
                String true_password = null;
                String username = et_username.getText().toString();
                String password = et_password.getText().toString();
                if (username.isEmpty()) {
                    et_username.setError("用户名不能为空");
                    return;
                } else if (password.isEmpty()) {
                    et_password.setError("密码不能为空");
                    return;
                } else {
                    Cursor cursor = db.rawQuery("select *from user where username=?", new String[]{username});
                    boolean isExist = cursor.moveToNext();
                    if (!isExist) {
                        Toast.makeText(this, "用户名不存在", Toast.LENGTH_SHORT).show();
                        et_username.setText("");
                        et_password.setText("");
                        return;
                    } else {
                        true_password = cursor.getString(2);
                    }
                    if (!true_password.equals(password)) {
                        Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        SharedPreferences sp=getSharedPreferences("isLogin", Context.MODE_PRIVATE);
                        SharedPreferences.Editor ed=sp.edit();
                        ed.putInt("flag",1);
                        ed.commit();
                        startActivity(new Intent(this, TulingActivity.class));
                        finish();
                    }
                }
                break;

            case R.id.bt_login_register:
                startActivity(new Intent(this, RegisterActivity.class));
                break;
        }
    }
}
