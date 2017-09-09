package com.jying.rainbow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jying.rainbow.Adapter.ChatAdapter;
import com.jying.rainbow.Bean.ChatMessage;
import com.jying.rainbow.Utils.Key;
import com.turing.androidsdk.HttpRequestListener;
import com.turing.androidsdk.TuringManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jying on 2017/9/8.
 */

public class TulingActivity extends AppCompatActivity {
    RecyclerView chatRecyclewView;
    ChatAdapter chatAdapter;
    Context con;
    Button send_bt;
    EditText send_edit;
    private TuringManager turingManager;
    List<ChatMessage> lists = new ArrayList<>();
    Toolbar toolbar;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    ChatMessage tulingChat = new ChatMessage((String) msg.obj, ChatMessage.TULING);
                    lists.add(tulingChat);
                    chatAdapter.notifyItemInserted(lists.size() - 1);
                    chatRecyclewView.scrollToPosition(lists.size() - 1);
                    break;
                case 1:
                    String url = msg.getData().getString("url");
                    String test = (String) msg.obj;
                    ChatMessage chat_url = new ChatMessage(test, ChatMessage.TULING_URL);
                    chat_url.setUrl(url);
                    lists.add(chat_url);
                    chatAdapter.notifyItemInserted(lists.size() - 1);
                    chatRecyclewView.scrollToPosition(lists.size() - 1);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        con = this;
        init();
        toolbar.setTitle("图灵机器人");
        setSupportActionBar(toolbar);
        initTuling();
        dealData();
        initRecyclewView();
        ChatMessage init = new ChatMessage("只有帅的人才能向我提问,understand!?", ChatMessage.TULING);
        lists.add(init);
        chatAdapter.notifyItemInserted(lists.size() - 1);
    }

    private void init() {
        chatRecyclewView = (RecyclerView) findViewById(R.id.chatRecyclewView);
        send_bt = (Button) findViewById(R.id.send_bt);
        send_edit = (EditText) findViewById(R.id.send_edit);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
    }

    private void initTuling() {
        turingManager = new TuringManager(con, Key.TULINGAPI_KEY, Key.TULING_SECREY);
        turingManager.setHttpRequestListener(new HttpRequestListener() {
            @Override
            public void onSuccess(String s) {
                if (s != null) {
                    Log.e("test", s);
                    try {
                        JSONObject result_obj = new JSONObject(s);
                        int code = result_obj.getInt("code");
                        if (code == 100000) {
                            if (result_obj.has("text")) {
                                Message msg = new Message();
                                msg.what = 0;
                                msg.obj = result_obj.get("text");
                                handler.sendMessage(msg);
                            }
                        } else if (code == 200000) {
                            Message msg = new Message();
                            msg.what = 1;
                            msg.obj = result_obj.get("text");
                            Bundle bundle = new Bundle();
                            bundle.putString("url", (String) result_obj.get("url"));
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                        }
                    } catch (JSONException e) {
                        Log.e("test", "JSONException:" + e.getMessage());
                    }
                }
            }

            @Override
            public void onFail(int i, String s) {
                Log.e("test", "获取失败");
            }
        });
    }

    public void initRecyclewView() {
        chatAdapter = new ChatAdapter(this, lists);
        chatRecyclewView.setLayoutManager(new LinearLayoutManager(con, LinearLayoutManager.VERTICAL, false));
        chatRecyclewView.setAdapter(chatAdapter);
    }

    public void dealData() {
        send_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = send_edit.getText().toString();
                if (data.isEmpty()) {
                    Toast.makeText(con, "不说话怎么回答你?", Toast.LENGTH_SHORT).show();
                    return;
                }
                turingManager.requestTuring(data);
                send_edit.setText("");
                ChatMessage chatMessage = new ChatMessage(data, ChatMessage.ME);
                lists.add(chatMessage);
                chatAdapter.notifyItemInserted(lists.size() - 1);
                chatRecyclewView.scrollToPosition(lists.size() - 1);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.exit) {
            SharedPreferences sp=getSharedPreferences("isLogin", Context.MODE_PRIVATE);
            SharedPreferences.Editor ed=sp.edit();
            ed.clear();
            ed.commit();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
