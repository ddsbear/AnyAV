package com.dds.avdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dds.audio.AudioActivity;
import com.dds.camera.CameraActivity;
import com.dds.camera.SurfaceViewCamera2Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class ItemFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private Context mContext;
    private int mColumnCount = 1;

    static {
        addItem("Audio Demo", AudioActivity.class);
        addItem("Camera Demo", CameraActivity.class);
    }

    public static void addItem(String content, Class<?> clazz) {
        PlaceholderContent.addItem(new PlaceholderContent.PlaceholderItem(content, clazz));
    }

    public ItemFragment() {
    }

    public static ItemFragment newInstance(int columnCount) {
        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));

            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
                recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.HORIZONTAL));
            }
            MyItemRecyclerViewAdapter adapter = new MyItemRecyclerViewAdapter(PlaceholderContent.ITEMS);
            adapter.setOnItemClickListener((view1, placeholderItem, position) -> {
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.startActivity(new Intent(activity, placeholderItem.clazz));
                }
            });
            recyclerView.setAdapter(adapter);
        }
        return view;
    }


    public static class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

        private final List<PlaceholderContent.PlaceholderItem> mValues;
        private OnItemClickListener mOnItemClickListener;

        public MyItemRecyclerViewAdapter(List<PlaceholderContent.PlaceholderItem> items) {
            mValues = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ViewHolder viewHolder = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_item, parent, false));
            bindViewClickListener(viewHolder);
            return viewHolder;
        }

        private void bindViewClickListener(ViewHolder viewHolder) {
            bindViewItemClickListener(viewHolder);
        }

        private void bindViewItemClickListener(ViewHolder viewHolder) {
            viewHolder.itemView.setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    int position = viewHolder.getAdapterPosition();
                    PlaceholderContent.PlaceholderItem placeholderItem = mValues.get(position);
                    mOnItemClickListener.onItemClick(viewHolder.itemView, placeholderItem, position);
                }


            });
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mContentView.setText(mValues.get(position).content);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public void setOnItemClickListener(OnItemClickListener l) {
            mOnItemClickListener = l;
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public final TextView mContentView;
            public PlaceholderContent.PlaceholderItem mItem;

            public ViewHolder(View view) {
                super(view);
                mContentView = view.findViewById(R.id.content);
            }

            @NonNull
            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }

        }

        public interface OnItemClickListener {
            void onItemClick(View view, PlaceholderContent.PlaceholderItem placeholderItem, int position);
        }
    }

    public static class PlaceholderContent {

        public static final List<PlaceholderItem> ITEMS = new ArrayList<>();

        public static void addItem(PlaceholderItem item) {
            ITEMS.add(item);
        }

        /**
         * A placeholder item representing a piece of content.
         */
        public static class PlaceholderItem {
            public final String content;
            public final Class<?> clazz;

            public PlaceholderItem(String content, Class<?> clazz) {
                this.content = content;
                this.clazz = clazz;
            }

            @NonNull
            @Override
            public String toString() {
                return content;
            }
        }
    }


}