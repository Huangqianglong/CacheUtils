package com.hql.cacheutils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hql.cacheutils.utils.Loader;

import java.util.ArrayList;

/**
 * @author ly-huangql
 * <br /> Create time : 2019/6/28
 * <br /> Description :
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.VH> {
    private ArrayList<DataBean> dataList = new ArrayList<>();
    private Context mContext;
    private Loader mLoader;
    private boolean isIdle = true;//是否在滚动
    private final static String TAG = "CacheListAdapter";

    public ListAdapter(Context context) {
        mContext = context;
        mLoader = new Loader(mContext);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_list, viewGroup, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH vh, int i) {
        DataBean dataBean = dataList.get(i);
        vh.title.setText(dataBean.getTitle());
        //Log.d(TAG, "读取>>>>>>>>>>>isIdle" + isIdle);
        if (isIdle) {
            vh.icon.setTag(dataBean.getUrl());
             //mLoader.bindBitmapFromMedia(dataBean.getUrl(), vh.icon, 145, 145,false);
            mLoader.bindBitmapFromURL(dataBean.getUrl(), vh.icon, 145, 145, false);
            //Glide.with(mContext).load(dataBean.getUrl()).into(vh.icon);

        } else {
            vh.icon.setImageResource(R.drawable.ic_launcher_background);
            //Log.d(TAG,"getUrl>>>"+dataBean.getUrl());

        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void isIdle(boolean isIdle) {
        this.isIdle = isIdle;
    }

    public static class VH extends RecyclerView.ViewHolder {
        TextView title;
        ImageView icon;

        public VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_title);
            icon = itemView.findViewById(R.id.iv_icon);
        }

        public TextView getTitle() {
            return title;
        }

        public void setTitle(TextView title) {
            this.title = title;
        }

        public ImageView getIcon() {
            return icon;
        }

        public void setIcon(ImageView icon) {
            this.icon = icon;
        }
    }

    public void updateList(ArrayList<DataBean> list) {
        dataList.clear();
        dataList.addAll(list);
        notifyDataSetChanged();
    }

}
