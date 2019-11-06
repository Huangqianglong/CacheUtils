package com.hql.cacheutils;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "cache_Activity";
    private RecyclerView mListView;
    private ListAdapter mAdapter;
    private ArrayList<DataBean> dataList = new ArrayList<>();
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myRequetPermission();
        Log.d(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>初始化");
        mListView = findViewById(R.id.list);
        mAdapter = new ListAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mListView.setLayoutManager(layoutManager);
        mListView.setAdapter(mAdapter);
        initData();
        mListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mAdapter.isIdle(true);
                    mAdapter.notifyDataSetChanged();
                } else {
                    mAdapter.isIdle(false);
                }
            }
        });
    }


    private void initData() {
        Log.d(TAG, "initData  urlList:" + urlList.length);
        for (int i = 0; i < urlList.length; i++) {
            // Log.d(TAG,"url:"+urlList[i]);
            DataBean bean = new DataBean("title" + i, urlList[i]);// "https://www.baidu.com/img/bd_logo1.png"
            dataList.add(bean);
        }
        mAdapter.updateList(dataList);
    }

    private void myRequetPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            Toast.makeText(this, "没有写权限!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "您已经申请了权限!", Toast.LENGTH_SHORT).show();
        }
    }

    AlertDialog mDialog;
    int NOT_NOTICE = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {//选择了“始终允许”
                    Toast.makeText(this, "" + "权限" + permissions[i] + "申请成功", Toast.LENGTH_SHORT).show();
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {//用户选择了禁止不再询问

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("permission")
                                .setMessage("点击允许才可以使用我们的app哦")
                                .setPositiveButton("去允许", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (mDialog != null && mDialog.isShowing()) {
                                            mDialog.dismiss();
                                        }
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);//注意就是"package",不用改成自己的包名
                                        intent.setData(uri);
                                        startActivityForResult(intent, NOT_NOTICE);
                                    }
                                });
                        mDialog = builder.create();
                        mDialog.setCanceledOnTouchOutside(false);
                        mDialog.show();


                    } else {//选择禁止
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("permission")
                                .setMessage("点击允许才可以使用我们的app哦")
                                .setPositiveButton("去允许", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (mDialog != null && mDialog.isShowing()) {
                                            mDialog.dismiss();
                                        }
                                        ActivityCompat.requestPermissions(MainActivity.this,
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                                    }
                                });
                        mDialog = builder.create();
                        mDialog.setCanceledOnTouchOutside(false);
                        mDialog.show();
                    }

                }
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NOT_NOTICE) {
            myRequetPermission();//由于不知道是否选择了允许所以需要再次判断
        }
    }

     String[] urlList = new String[]{
             "/sdcard/Pictures/Amuro - 闲情记趣.mp3"
             /*,
              "/sdcard/Pictures/11.mp3",
              "/sdcard/Pictures/Braska - 我自齐天.mp3"*/
     };

  /*  String[] urlList = new String[]{
            "http://img3.imgtn.bdimg.com/it/u=3361934473,3725527506&fm=26&gp=0.jpg"
    };*/
    /*String[] urlList = new String[]{
            "http://img3.imgtn.bdimg.com/it/u=3361934473,3725527506&fm=26&gp=0.jpg"
            , "http://img3.imgtn.bdimg.com/it/u=3677209778,3519789803&fm=26&gp=0.jpg"
            , "http://img5.imgtn.bdimg.com/it/u=2884107401,3797902000&fm=26&gp=0.jpg"
            , "http://img4.imgtn.bdimg.com/it/u=4120979773,4081271083&fm=26&gp=0.jpg"
            , "http://img2.imgtn.bdimg.com/it/u=829044612,3699393036&fm=26&gp=0.jpg"
            , "http://img0.imgtn.bdimg.com/it/u=3293099503,606929711&fm=26&gp=0.jpg"
            , "http://img5.imgtn.bdimg.com/it/u=1675225930,3882737045&fm=26&gp=0.jpg"
            , "http://img0.imgtn.bdimg.com/it/u=3511572440,3646830680&fm=26&gp=0.jpg"
            , "http://img1.imgtn.bdimg.com/it/u=1051951030,907960917&fm=26&gp=0.jpg"
            , "http://img4.imgtn.bdimg.com/it/u=3134952146,3980288478&fm=26&gp=0.jpg"
            , "http://img1.imgtn.bdimg.com/it/u=2357912857,682090914&fm=26&gp=0.jpg"
            , "http://img4.imgtn.bdimg.com/it/u=2808673875,655214047&fm=26&gp=0.jpg"
            , "http://img5.imgtn.bdimg.com/it/u=4224769698,2746412817&fm=26&gp=0.jpg"
            , "http://img3.imgtn.bdimg.com/it/u=3677209778,3519789803&fm=26&gp=0.jpg"
            , "http://img5.imgtn.bdimg.com/it/u=2884107401,3797902000&fm=26&gp=0.jpg"
            , "http://img4.imgtn.bdimg.com/it/u=4120979773,4081271083&fm=26&gp=0.jpg"
            , "http://img2.imgtn.bdimg.com/it/u=829044612,3699393036&fm=26&gp=0.jpg"
            , "http://img0.imgtn.bdimg.com/it/u=3293099503,606929711&fm=26&gp=0.jpg"
            , "http://img5.imgtn.bdimg.com/it/u=1675225930,3882737045&fm=26&gp=0.jpg"
            , "http://img0.imgtn.bdimg.com/it/u=3511572440,3646830680&fm=26&gp=0.jpg"
            , "http://img1.imgtn.bdimg.com/it/u=1051951030,907960917&fm=26&gp=0.jpg"
            , "http://img4.imgtn.bdimg.com/it/u=3134952146,3980288478&fm=26&gp=0.jpg"
            , "http://img1.imgtn.bdimg.com/it/u=2357912857,682090914&fm=26&gp=0.jpg"
            , "http://img4.imgtn.bdimg.com/it/u=2808673875,655214047&fm=26&gp=0.jpg"
            , "http://img5.imgtn.bdimg.com/it/u=4224769698,2746412817&fm=26&gp=0.jpg"
            , "http://img3.imgtn.bdimg.com/it/u=3677209778,3519789803&fm=26&gp=0.jpg"
            , "http://img5.imgtn.bdimg.com/it/u=2884107401,3797902000&fm=26&gp=0.jpg"
            , "http://img4.imgtn.bdimg.com/it/u=4120979773,4081271083&fm=26&gp=0.jpg"
            , "http://img2.imgtn.bdimg.com/it/u=829044612,3699393036&fm=26&gp=0.jpg"
            , "http://img0.imgtn.bdimg.com/it/u=3293099503,606929711&fm=26&gp=0.jpg"
            , "http://img5.imgtn.bdimg.com/it/u=1675225930,3882737045&fm=26&gp=0.jpg"
            , "http://img0.imgtn.bdimg.com/it/u=3511572440,3646830680&fm=26&gp=0.jpg"
            , "http://img1.imgtn.bdimg.com/it/u=1051951030,907960917&fm=26&gp=0.jpg"
            , "http://img4.imgtn.bdimg.com/it/u=3134952146,3980288478&fm=26&gp=0.jpg"
            , "http://img1.imgtn.bdimg.com/it/u=2357912857,682090914&fm=26&gp=0.jpg"
            , "http://img4.imgtn.bdimg.com/it/u=2808673875,655214047&fm=26&gp=0.jpg"
            , "http://img5.imgtn.bdimg.com/it/u=4224769698,2746412817&fm=26&gp=0.jpg"
            , "http://img3.imgtn.bdimg.com/it/u=3677209778,3519789803&fm=26&gp=0.jpg"
            , "http://img5.imgtn.bdimg.com/it/u=2884107401,3797902000&fm=26&gp=0.jpg"
            , "http://img4.imgtn.bdimg.com/it/u=4120979773,4081271083&fm=26&gp=0.jpg"
            , "http://img2.imgtn.bdimg.com/it/u=829044612,3699393036&fm=26&gp=0.jpg"
            , "http://img0.imgtn.bdimg.com/it/u=3293099503,606929711&fm=26&gp=0.jpg"
            , "http://img5.imgtn.bdimg.com/it/u=1675225930,3882737045&fm=26&gp=0.jpg"
            , "http://img0.imgtn.bdimg.com/it/u=3511572440,3646830680&fm=26&gp=0.jpg"
            , "http://img1.imgtn.bdimg.com/it/u=1051951030,907960917&fm=26&gp=0.jpg"
            , "http://img4.imgtn.bdimg.com/it/u=3134952146,3980288478&fm=26&gp=0.jpg"
            , "http://img1.imgtn.bdimg.com/it/u=2357912857,682090914&fm=26&gp=0.jpg"
            , "http://img4.imgtn.bdimg.com/it/u=2808673875,655214047&fm=26&gp=0.jpg"
            , "http://img5.imgtn.bdimg.com/it/u=4224769698,2746412817&fm=26&gp=0.jpg"
            , "http://img3.imgtn.bdimg.com/it/u=3677209778,3519789803&fm=26&gp=0.jpg"
            , "http://img5.imgtn.bdimg.com/it/u=2884107401,3797902000&fm=26&gp=0.jpg"
            , "http://img4.imgtn.bdimg.com/it/u=4120979773,4081271083&fm=26&gp=0.jpg"
            , "http://img2.imgtn.bdimg.com/it/u=829044612,3699393036&fm=26&gp=0.jpg"
            , "http://img0.imgtn.bdimg.com/it/u=3293099503,606929711&fm=26&gp=0.jpg"
            , "http://img5.imgtn.bdimg.com/it/u=1675225930,3882737045&fm=26&gp=0.jpg"
            , "http://img0.imgtn.bdimg.com/it/u=3511572440,3646830680&fm=26&gp=0.jpg"
            , "http://img1.imgtn.bdimg.com/it/u=1051951030,907960917&fm=26&gp=0.jpg"
            , "http://img4.imgtn.bdimg.com/it/u=3134952146,3980288478&fm=26&gp=0.jpg"
            , "http://img1.imgtn.bdimg.com/it/u=2357912857,682090914&fm=26&gp=0.jpg"
            , "http://img4.imgtn.bdimg.com/it/u=2808673875,655214047&fm=26&gp=0.jpg"
            , "http://img5.imgtn.bdimg.com/it/u=4224769698,2746412817&fm=26&gp=0.jpg"
            , "http://img3.imgtn.bdimg.com/it/u=3677209778,3519789803&fm=26&gp=0.jpg"
            , "http://img5.imgtn.bdimg.com/it/u=2884107401,3797902000&fm=26&gp=0.jpg"
            , "http://img4.imgtn.bdimg.com/it/u=4120979773,4081271083&fm=26&gp=0.jpg"
            , "http://img2.imgtn.bdimg.com/it/u=829044612,3699393036&fm=26&gp=0.jpg"
            , "http://img0.imgtn.bdimg.com/it/u=3293099503,606929711&fm=26&gp=0.jpg"
            , "http://img5.imgtn.bdimg.com/it/u=1675225930,3882737045&fm=26&gp=0.jpg"
            , "http://img0.imgtn.bdimg.com/it/u=3511572440,3646830680&fm=26&gp=0.jpg"
            , "http://img1.imgtn.bdimg.com/it/u=1051951030,907960917&fm=26&gp=0.jpg"
            , "http://img4.imgtn.bdimg.com/it/u=3134952146,3980288478&fm=26&gp=0.jpg"
            , "http://img1.imgtn.bdimg.com/it/u=2357912857,682090914&fm=26&gp=0.jpg"
            , "http://img4.imgtn.bdimg.com/it/u=2808673875,655214047&fm=26&gp=0.jpg"
            , "http://img5.imgtn.bdimg.com/it/u=4224769698,2746412817&fm=26&gp=0.jpg"};*/
}
