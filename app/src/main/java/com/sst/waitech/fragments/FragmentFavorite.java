package com.sst.waitech.fragments;

import static com.sst.waitech.utils.Tools.EXTRA_OBJC;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sst.waitech.Config;
import com.sst.waitech.R;
import com.sst.waitech.activities.ActivityFavoriteDetail;
import com.sst.waitech.activities.ActivityPostDetail;
import com.sst.waitech.activities.MainActivity;
import com.sst.waitech.fragments.adapters.AdapterFavorite;
import com.sst.waitech.database.prefs.SharedPref;
import com.sst.waitech.database.sqlite.DbFavorite;
import com.sst.waitech.models.Post;
import com.sst.waitech.utils.Tools;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FragmentFavorite extends Fragment {

    private List<Post> posts = new ArrayList<>();
    private View rootView;
    LinearLayout lytNoFavorite;
    private RecyclerView recyclerView;
    private AdapterFavorite adapterFavorite;
    DbFavorite dbFavorite;
    private BottomSheetDialog mBottomSheetDialog;
    SharedPref sharedPref;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_favorite, container, false);
        sharedPref = new SharedPref(getActivity());
        recyclerView = rootView.findViewById(R.id.recyclerView);
        lytNoFavorite = rootView.findViewById(R.id.lyt_no_favorite);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (Config.DISPLAY_POST_LIST_DIVIDER) {
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        }
        //recyclerView.setHasFixedSize(true);

        loadDataFromDatabase();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onResume() {
        super.onResume();
        loadDataFromDatabase();
    }

    public void loadDataFromDatabase() {
        dbFavorite = new DbFavorite(getActivity());
        posts = dbFavorite.getAllData();

        //set data and list adapter
        adapterFavorite = new AdapterFavorite(getActivity(), recyclerView, posts);
        recyclerView.setAdapter(adapterFavorite);

        showNoItemView(posts.size() == 0);

        // on item list clicked
        adapterFavorite.setOnItemClickListener((v, obj, position) -> {
            if (Tools.isConnect(getActivity())) {
                Intent intent = new Intent(getActivity(), ActivityPostDetail.class);
                intent.putExtra(EXTRA_OBJC, obj);
                startActivity(intent);
                if (getActivity() != null) {
                    ((MainActivity) getActivity()).showInterstitialAd();
                }
            } else {
                Intent intent = new Intent(getActivity(), ActivityFavoriteDetail.class);
                intent.putExtra(EXTRA_OBJC, obj);
                startActivity(intent);
            }
            sharedPref.savePostId(obj.id);
        });

        adapterFavorite.setOnItemOverflowClickListener((view, obj, position) -> {
            showBottomSheetDialog(obj);
        });

    }

    private void showNoItemView(boolean show) {
        ((TextView) rootView.findViewById(R.id.no_item_message)).setText(R.string.no_favorite_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lytNoFavorite.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lytNoFavorite.setVisibility(View.GONE);
        }
    }

    private void showBottomSheetDialog(Post post) {
        @SuppressLint("InflateParams") final View view = getLayoutInflater().inflate(R.layout.include_bottom_sheet, null);
        FrameLayout lytBottomSheet = view.findViewById(R.id.bottom_sheet);
        TextView txtFavorite = view.findViewById(R.id.txt_favorite);

        ImageView imgFavorite = view.findViewById(R.id.img_favorite);
        ImageView imgShare = view.findViewById(R.id.img_share);
        ImageView imgViewSite = view.findViewById(R.id.img_view);

        if (this.sharedPref.getIsDarkTheme()) {
            lytBottomSheet.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.bg_rounded_dark));
            imgFavorite.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorWhite));
            imgShare.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorWhite));
            imgViewSite.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorWhite));
        } else {
            lytBottomSheet.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.bg_rounded_default));
            imgFavorite.setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey_dark));
            imgShare.setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey_dark));
            imgViewSite.setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey_dark));
        }

        LinearLayout btnFavorite = view.findViewById(R.id.btn_favorite);
        LinearLayout btnShare = view.findViewById(R.id.btn_share);
        LinearLayout btnView = view.findViewById(R.id.btn_view);
        if (!sharedPref.getDisplayViewOnSiteMenu().equals("true")) {
            btnView.setVisibility(View.GONE);
        }

        btnFavorite.setOnClickListener(action -> {
            List<Post> posts = dbFavorite.getFavRow(post.id);
            if (posts.size() == 0) {
                dbFavorite.AddToFavorite(new Post(post.id, post.title, post.labels, post.content, post.published));
                Snackbar.make(getActivity().findViewById(R.id.tab_coordinator_layout), getString(R.string.msg_favorite_added), Snackbar.LENGTH_SHORT).show();
                imgFavorite.setImageResource(R.drawable.ic_favorite_grey);
            } else {
                if (posts.get(0).getId().equals(post.id)) {
                    dbFavorite.RemoveFav(new Post(post.id));
                    Snackbar.make(getActivity().findViewById(R.id.tab_coordinator_layout), getString(R.string.msg_favorite_removed), Snackbar.LENGTH_SHORT).show();
                    imgFavorite.setImageResource(R.drawable.ic_favorite_outline_grey);
                    refreshFragment();
                }
            }
            mBottomSheetDialog.dismiss();
        });

        btnShare.setOnClickListener(action -> {
            Tools.shareArticle(getActivity(), post);
            mBottomSheetDialog.dismiss();
        });

        btnView.setOnClickListener(action -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(post.url)));
            mBottomSheetDialog.dismiss();
        });

        if (sharedPref.getIsDarkTheme()) {
            mBottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.SheetDialogDark);
        } else {
            mBottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.SheetDialogLight);
        }
        mBottomSheetDialog.setContentView(view);
        mBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(dialog -> mBottomSheetDialog = null);

        dbFavorite = new DbFavorite(getActivity());
        List<Post> posts = dbFavorite.getFavRow(post.id);
        if (posts.size() == 0) {
            txtFavorite.setText(getString(R.string.favorite_add));
            imgFavorite.setImageResource(R.drawable.ic_favorite_outline_grey);
        } else {
            if (posts.get(0).id.equals(post.id)) {
                txtFavorite.setText(getString(R.string.favorite_remove));
                imgFavorite.setImageResource(R.drawable.ic_favorite_grey);
            }
        }

    }

    public void refreshFragment() {
        adapterFavorite.resetListData();
        loadDataFromDatabase();
    }

}
