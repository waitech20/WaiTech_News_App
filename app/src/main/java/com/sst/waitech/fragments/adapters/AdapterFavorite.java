package com.sst.waitech.fragments.adapters;

import static com.sst.waitech.utils.Tools.EXTRA_OBJC;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sst.waitech.Config;
import com.sst.waitech.R;
import com.sst.waitech.activities.ActivityCategoryDetail;
import com.sst.waitech.models.Post;
import com.sst.waitech.utils.Constant;
import com.sst.waitech.utils.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdapterFavorite extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_PROG = 0;
    private final int VIEW_ITEM = 1;
    private List<Post> items;
    private Context context;
    private OnItemClickListener mOnItemClickListener;
    private OnItemOverflowClickListener mOnItemOverflowClickListener;
    AdapterCategoryList adapter;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    ArrayList<String> arrayListLabels;

    public interface OnItemClickListener {
        void onItemClick(View view, Post obj, int position);
    }

    public interface OnItemOverflowClickListener {
        void onItemOverflowClick(View view, Post obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public void setOnItemOverflowClickListener(final OnItemOverflowClickListener mItemOverflowClickListener) {
        this.mOnItemOverflowClickListener = mItemOverflowClickListener;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AdapterFavorite(Context context, RecyclerView view, List<Post> items) {
        this.items = items;
        this.context = context;
        lastItemViewDetector(view);
    }

    public static class OriginalViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView txtTitle;
        public TextView txtDescription;
        public TextView txtDate;
        public View txtUncategorized;
        public TextView txtAlphabet;
        public ImageView thumbnailImage;
        public ImageView imgOverflow;
        public RecyclerView recyclerView;
        public LinearLayout lytParent;

        public OriginalViewHolder(View v) {
            super(v);
            txtTitle = v.findViewById(R.id.txt_title);
            txtDescription = v.findViewById(R.id.txt_description);
            txtDate = v.findViewById(R.id.txt_date);
            txtUncategorized = v.findViewById(R.id.txt_label_uncategorized);
            txtAlphabet = v.findViewById(R.id.txt_alphabet);
            thumbnailImage = v.findViewById(R.id.thumbnail_image);
            imgOverflow = v.findViewById(R.id.img_overflow);
            recyclerView = v.findViewById(R.id.recycler_view);
            lytParent = v.findViewById(R.id.lyt_parent);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
            vh = new OriginalViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_load_more, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final Post p = items.get(position);
            final OriginalViewHolder vItem = (OriginalViewHolder) holder;

            Document html_data = Jsoup.parse(p.content);

            vItem.txtTitle.setText(p.title);

            if (Config.DISPLAY_POST_LIST_SHORT_DESCRIPTION) {
                vItem.txtTitle.setMaxLines(2);
                vItem.txtDescription.setText(Tools.parseHtml(html_data.text()));
            } else {
                vItem.txtTitle.setMaxLines(3);
                vItem.txtDescription.setVisibility(View.GONE);
            }

            if (Config.DISPLAY_DATE_LIST_POST) {
                vItem.txtDate.setText(Tools.getTimeAgo(p.published));
            } else {
                vItem.txtDate.setVisibility(View.GONE);
            }

            String labels = String.valueOf(p.labels).replace("[[", "").replace("]]", "").replace(", ", ",");
            arrayListLabels = new ArrayList(Arrays.asList((labels.split(","))));
            vItem.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            adapter = new AdapterCategoryList(context, arrayListLabels);
            vItem.recyclerView.setAdapter(adapter);
            adapter.setOnItemClickListener((view, items, pos) -> {
                Intent intent = new Intent(context, ActivityCategoryDetail.class);
                intent.putExtra(EXTRA_OBJC, items.get(pos));
                context.startActivity(intent);
            });

            if (p.labels.size() >= 1) {
                vItem.txtUncategorized.setVisibility(View.GONE);
            } else {
                vItem.txtUncategorized.setVisibility(View.VISIBLE);
                vItem.txtUncategorized.setOnClickListener(view -> {
                });
            }

            Elements element = html_data.select("img");
            if (element.hasAttr("src")) {
                Glide.with(context)
                        .load(element.get(0).attr("src").replace(" ", "%20"))
                        .transition(withCrossFade())
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().override(Constant.THUMBNAIL_WIDTH, Constant.THUMBNAIL_HEIGHT))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.bg_button_transparent)
                        .centerCrop()
                        .into(vItem.thumbnailImage);
                vItem.txtAlphabet.setVisibility(View.GONE);
            } else {
                vItem.thumbnailImage.setImageResource(R.drawable.bg_button_transparent);
                vItem.txtAlphabet.setVisibility(View.VISIBLE);
                vItem.txtAlphabet.setText(p.title.substring(0, 1));
            }

            vItem.lytParent.setOnClickListener(view -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, p, position);
                }
            });

            vItem.imgOverflow.setOnClickListener(view -> {
                if (mOnItemOverflowClickListener != null) {
                    mOnItemOverflowClickListener.onItemOverflowClick(view, p, position);
                }
            });

        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    public void insertData(List<Post> items) {
        setLoaded();
        int positionStart = getItemCount();
        int itemCount = items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    @SuppressWarnings("SuspiciousListRemoveInLoop")
    public void setLoaded() {
        loading = false;
        for (int i = 0; i < getItemCount(); i++) {
            if (items.get(i) == null) {
                items.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public void setLoading() {
        if (getItemCount() != 0) {
            this.items.add(null);
            notifyItemInserted(getItemCount() - 1);
            loading = true;
        }
    }

    public void resetListData() {
        //this.items = new ArrayList<>();
        //items.clear();
        this.items.clear();
        notifyDataSetChanged();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) != null) {
            return VIEW_ITEM;
        } else {
            return VIEW_PROG;
        }
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void lastItemViewDetector(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int lastPos = layoutManager.findLastVisibleItemPosition();
                    if (!loading && lastPos == getItemCount() - 1 && onLoadMoreListener != null) {
                        int current_page = getItemCount() / 10;
                        onLoadMoreListener.onLoadMore(current_page);
                        loading = true;
                    }
                }
            });
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

}