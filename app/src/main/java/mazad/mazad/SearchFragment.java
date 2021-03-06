package mazad.mazad;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import mazad.mazad.adapters.PresentedItemAdapter;
import mazad.mazad.adapters.RecentSearchAdapter;
import mazad.mazad.models.PresentedItemModel;
import mazad.mazad.models.RecentSearchModel;
import mazad.mazad.models.UserModel;
import mazad.mazad.utils.Connector;
import mazad.mazad.utils.Helper;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {

    @BindView(R.id.search_text)
    EditText mSearchText;
    @BindView(R.id.cancel)
    TextView mCancelButton;
    @BindView(R.id.progress_indicator)
    ProgressBar mProgressBar;
    @BindView(R.id.normal_text)
    TextView mNormalText;
    @BindView(R.id.products)
    RecyclerView mProducts;
    @BindView(R.id.recent_search)
    RecyclerView mRecentSearch;

    Connector mConnector;
    Connector mConnectorRecentSearch;

    UserModel mUserModel;

    Intent mIntent;

    ArrayList<PresentedItemModel> mPresentedItemModels;
    ArrayList<RecentSearchModel> mRecentSearchModels;

    Map<String, String> mMap;
    Map<String,String> mMapRecentSearch;

    private final String TAG = "SearchFragment";

    private final int REQUEST_ID = 1;

    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.bind(this, v);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (getActivity() != null) {
                ViewCompat.setLayoutDirection(getActivity().findViewById(R.id.parent_layout), ViewCompat.LAYOUT_DIRECTION_RTL);
            }
        }

        mPresentedItemModels = new ArrayList<>();
        mRecentSearchModels = new ArrayList<>();


        final PresentedItemAdapter adapter = new PresentedItemAdapter(getActivity(), mPresentedItemModels, new PresentedItemAdapter.OnItemClick() {
            @Override
            public void setOnItemClick(int position) {
                getActivity().startActivityForResult(new Intent(getActivity(), AdDetailsActivity.class).putExtra("product", mPresentedItemModels.get(position)).putExtra("user", mUserModel), REQUEST_ID);
            }
        });


        final RecentSearchAdapter searchAdapter = new RecentSearchAdapter(getActivity(), mRecentSearchModels, new RecentSearchAdapter.OnItemClick() {
            @Override
            public void setOnItemClick(int position) {
                mSearchText.setText(mRecentSearchModels.get(position).getSearchText());
                mCancelButton.performClick();
            }
        });


        try {
            if (getActivity() != null) {
                mIntent = getActivity().getIntent();
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }


        if (mIntent != null) {
            if (mIntent.hasExtra("user") && mIntent.getSerializableExtra("user") != null) {
                mUserModel = (UserModel) mIntent.getSerializableExtra("user");
            }
        }


        mConnector = new Connector(getActivity(), new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)) {
                    mPresentedItemModels.clear();
                    mPresentedItemModels.addAll(Connector.getProductsJson(response));
                    adapter.notifyDataSetChanged();
                    mRecentSearch.setVisibility(View.INVISIBLE);
                    mProducts.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.INVISIBLE);
                } else {
                    mPresentedItemModels.clear();
                    adapter.notifyDataSetChanged();
                    mNormalText.setVisibility(View.VISIBLE);
                    Helper.showSnackBarMessage("لا نتائج", (AppCompatActivity) getActivity());
                    mProducts.setVisibility(View.VISIBLE);
                    mRecentSearch.setVisibility(View.INVISIBLE);
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                mPresentedItemModels.clear();
                adapter.notifyDataSetChanged();
                mRecentSearch.setVisibility(View.INVISIBLE);
                mProducts.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
                Helper.showSnackBarMessage("خطأ. من فضلك اعد المحاوله", (AppCompatActivity) getActivity());
            }
        });

        mConnectorRecentSearch = new Connector(getActivity(), new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkSearch(response)){
                    mRecentSearchModels.clear();
                    mRecentSearchModels.addAll(Connector.getRecentSearchJson(response));
                    searchAdapter.notifyDataSetChanged();
                    mRecentSearch.setVisibility(View.VISIBLE);
                    mProducts.setVisibility(View.INVISIBLE);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    if (mRecentSearchModels.isEmpty()){
                        mPresentedItemModels.clear();
                        mRecentSearchModels.clear();
                        adapter.notifyDataSetChanged();
                        searchAdapter.notifyDataSetChanged();
                        mNormalText.setVisibility(View.VISIBLE);
                    }
                } else {
                    mRecentSearchModels.clear();
                    searchAdapter.notifyDataSetChanged();
                    mNormalText.setVisibility(View.VISIBLE);
                    mProducts.setVisibility(View.INVISIBLE);
                    mRecentSearch.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.INVISIBLE);
                }

            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                mRecentSearchModels.clear();
                searchAdapter.notifyDataSetChanged();
                mProducts.setVisibility(View.INVISIBLE);
                mRecentSearch.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
                Helper.showSnackBarMessage("خطأ. من فضلك اعد المحاوله", (AppCompatActivity) getActivity());
            }
        });

        mMap = new HashMap<>();
        mMapRecentSearch = new HashMap<>();


        mSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s) && mPresentedItemModels.isEmpty()) {
                    mPresentedItemModels.clear();
                    mRecentSearchModels.clear();
                    adapter.notifyDataSetChanged();
                    searchAdapter.notifyDataSetChanged();
                    mNormalText.setVisibility(View.VISIBLE);
                } else if (TextUtils.isEmpty(s)) {
                    if (mUserModel != null) {
                        mMapRecentSearch.put("user_id", mUserModel.getId());
                        mNormalText.setVisibility(View.INVISIBLE);
                        mProducts.setVisibility(View.INVISIBLE);
                        mRecentSearch.setVisibility(View.VISIBLE);
                        mRecentSearchModels.clear();
                        searchAdapter.notifyDataSetChanged();
                        mProgressBar.setVisibility(View.VISIBLE);
                        mConnectorRecentSearch.setMap(mMapRecentSearch);
                        mConnectorRecentSearch.getRequest(TAG, Connector.createGetSearchUrl());
                        mPresentedItemModels.clear();
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchText = mSearchText.getText().toString();
                if (!Helper.validateFields(searchText)) {
                    Helper.showSnackBarMessage("من فضلك ادخل نص البحث", (AppCompatActivity) getActivity());
                } else {
                    mMap.put("search", searchText);
                    if (mUserModel != null) {
                        mMap.put("user_id", mUserModel.getId());
                    }
                    mNormalText.setVisibility(View.INVISIBLE);
                    mProducts.setVisibility(View.INVISIBLE);
                    mRecentSearch.setVisibility(View.INVISIBLE);
                    mRecentSearchModels.clear();
                    searchAdapter.notifyDataSetChanged();
                    mProgressBar.setVisibility(View.VISIBLE);
                    mConnector.setMap(mMap);
                    mConnector.getRequest(TAG, Connector.createGetProductsUrl());
                    mPresentedItemModels.clear();
                    adapter.notifyDataSetChanged();
                }
            }
        });


        mSearchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mProducts.setAdapter(adapter);


        mProducts.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));


        mRecentSearch.setAdapter(searchAdapter);

        mRecentSearch.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));


        if (mUserModel != null) {
            mMapRecentSearch.put("user_id", mUserModel.getId());
            mNormalText.setVisibility(View.INVISIBLE);
            mProducts.setVisibility(View.INVISIBLE);
            mRecentSearch.setVisibility(View.VISIBLE);
            mRecentSearchModels.clear();
            searchAdapter.notifyDataSetChanged();
            mProgressBar.setVisibility(View.VISIBLE);
            mConnectorRecentSearch.setMap(mMapRecentSearch);
            mConnectorRecentSearch.getRequest(TAG, Connector.createGetSearchUrl());
            mPresentedItemModels.clear();
            adapter.notifyDataSetChanged();
        }

        return v;
    }


    @Override
    public void onStop() {
        super.onStop();
        mConnector.cancelAllRequests(TAG);
    }
}
