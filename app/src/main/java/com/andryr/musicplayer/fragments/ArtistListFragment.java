/*
 * Copyright 2016 andryr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andryr.musicplayer.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andryr.musicplayer.MainActivity;
import com.andryr.musicplayer.R;
import com.andryr.musicplayer.adapters.ArtistListAdapter;
import com.andryr.musicplayer.adapters.BaseAdapter;
import com.andryr.musicplayer.loaders.ArtistLoader;
import com.andryr.musicplayer.model.Artist;
import com.andryr.musicplayer.widgets.FastScroller;

import java.util.List;

/**
 * A simple {@link Fragment} subclass. Use the
 * {@link ArtistListFragment#newInstance} factory method to create an instance
 * of this fragment.
 */
public class ArtistListFragment extends BaseFragment {


    private static final String STATE_SHOW_FASTSCROLLER = "fastscroller";

    private RecyclerView mRecyclerView;

    private ArtistListAdapter mAdapter;

    private boolean mShowFastScroller = true;

    private LoaderManager.LoaderCallbacks<List<Artist>> mLoaderCallbacks = new LoaderCallbacks<List<Artist>>() {

        @Override
        public void onLoaderReset(Loader<List<Artist>> loader) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLoadFinished(Loader<List<Artist>> loader, List<Artist> data) {
            mAdapter.setData(data);

        }

        @Override
        public Loader<List<Artist>> onCreateLoader(int id, Bundle args) {


            return new ArtistLoader(getActivity());
        }
    };


    private BaseAdapter.OnItemClickListener mOnItemClickListener = new BaseAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            Artist artist = mAdapter.getItem(position);

            Fragment fragment = ArtistFragment.newInstance(artist);

            ((MainActivity) getActivity()).setFragment(fragment);
        }
    };


    public ArtistListFragment() {
        // Required empty public constructor
    }

    public static ArtistListFragment newInstance() {
        ArtistListFragment fragment = new ArtistListFragment();

        return fragment;
    }

    public ArtistListFragment showFastScroller(boolean show) {
        mShowFastScroller = show;
        return this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, mLoaderCallbacks);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist_list,
                container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        mAdapter = new ArtistListAdapter(getActivity());
        mAdapter.setOnItemClickListener(mOnItemClickListener);
        mRecyclerView.setAdapter(mAdapter);

        if (savedInstanceState != null) {
            mShowFastScroller = savedInstanceState
                    .getBoolean(STATE_SHOW_FASTSCROLLER) || mShowFastScroller;
        }

        FastScroller scroller = (FastScroller) rootView
                .findViewById(R.id.fastscroller);
        if (mShowFastScroller) {
            scroller.setRecyclerView(mRecyclerView);
            scroller.setSectionIndexer(mAdapter);
        } else {
            scroller.setVisibility(View.GONE);
        }

        return rootView;
    }

    @Override
    public void load() {
        getLoaderManager().restartLoader(0, null, mLoaderCallbacks);

    }


}
