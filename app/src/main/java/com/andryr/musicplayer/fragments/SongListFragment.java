package com.andryr.musicplayer.fragments;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.andryr.musicplayer.Album;
import com.andryr.musicplayer.Artist;
import com.andryr.musicplayer.FastScroller;
import com.andryr.musicplayer.FragmentListener;
import com.andryr.musicplayer.Genre;
import com.andryr.musicplayer.MainActivity;
import com.andryr.musicplayer.Playlist;
import com.andryr.musicplayer.Playlists;
import com.andryr.musicplayer.R;
import com.andryr.musicplayer.Song;
import com.andryr.musicplayer.loaders.SongLoader;
import com.andryr.musicplayer.preferences.PreferencesActivity;
import com.andryr.musicplayer.preferences.ThemeHelper;

import java.util.List;


public class SongListFragment extends BaseFragment {

    private static final String PARAM_TYPE = "type";
    private static final String PARAM_ARTIST_ID = "artist_id";
    private static final String PARAM_ALBUM_ID = "album_id";
    private static final String PARAM_GENRE_ID = "genre_id";

    private static final String STATE_SHOW_TOOLBAR = "toolbar";
    private static final String STATE_SHOW_FASTSCROLLER = "fastscroller";

    private static final int ALL_SONGS = 1;
    private static final int ALBUM_SONGS = 2;
    private static final int ARTIST_SONGS = 3;
    private static final int ARTIST_ALBUM_SONGS = 4;
    private static final int GENRE_SONGS = 5;


    private FragmentListener mListener;

    private RecyclerView mRecyclerView;
    private SongListAdapter mAdapter;

    private boolean mShowToolbar = false;
    private boolean mShowFastScroller = true;

    private int mSongListType = ALL_SONGS;
    private long mArtistId;
    private long mAlbumId;
    private long mGenreId;

    private LoaderManager.LoaderCallbacks<List<Song>> mLoaderCallbacks = new LoaderCallbacks<List<Song>>() {

        @Override
        public void onLoaderReset(Loader<List<Song>> loader) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLoadFinished(Loader<List<Song>> loader, List<Song> songList) {
            mAdapter.setData(songList);
            Log.e("test", "" + mAdapter.getItemCount());
        }

        @Override
        public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
            SongLoader loader = new SongLoader(getActivity());
            loader.setSongListType(mSongListType);
            loader.setAlbumId(mAlbumId);
            loader.setArtistId(mArtistId);
            loader.setGenreId(mGenreId);
            loader.setOrder(MediaStore.Audio.Media.TITLE);
            return loader;
        }
    };



    private ID3TagEditorDialog.OnTagsEditionSuccessListener mOnTagsEditionSuccessListener = new ID3TagEditorDialog.OnTagsEditionSuccessListener() {
        @Override
        public void onTagsEditionSuccess() {
            ((MainActivity)getActivity()).refresh();
        }
    };

    public void showMenu(final int position, View v) {
        PopupMenu popup = new PopupMenu(getActivity(), v);
        MenuInflater inflater = popup.getMenuInflater();
        final Song song = mAdapter.getItem(position);
        inflater.inflate(R.menu.song_list_item, popup.getMenu());
        popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_add_to_queue:
                        ((MainActivity) getActivity()).addToQueue(song);
                        return true;
                    case R.id.action_set_as_next_track:
                        ((MainActivity) getActivity()).setAsNextTrack(song);
                        return true;
                    case R.id.action_edit_tags:
                        showID3TagEditor(song);
                        return true;
                    case R.id.action_add_to_playlist:
                        showPlaylistPicker(song);
                        return true;
                }
                return false;
            }
        });
        popup.show();
    }

    private void showID3TagEditor(Song song) {
        ID3TagEditorDialog dialog = ID3TagEditorDialog.newInstance(song);
        dialog.setOnTagsEditionSuccessListener(mOnTagsEditionSuccessListener);
        dialog.show(getChildFragmentManager(), "edit_tags");
    }

    private void showPlaylistPicker(final Song song)
    {
        PlaylistPicker picker = PlaylistPicker.newInstance();
        picker.setListener(new PlaylistPicker.OnPlaylistPickedListener() {
            @Override
            public void onPlaylistPicked(Playlist playlist) {
                Playlists.addSongToPlaylist(getActivity().getContentResolver(), playlist.getId(), song.getId());
            }
        });
        picker.show(getChildFragmentManager(), "pick_playlist");

    }

    public static SongListFragment newInstance() {
        SongListFragment fragment = new SongListFragment();

        Bundle args = new Bundle();
        args.putInt(PARAM_TYPE, ALL_SONGS);
        fragment.setArguments(args);
        return fragment;
    }

    public static SongListFragment newInstance(Genre genre) {
        SongListFragment fragment = new SongListFragment();

        Bundle args = new Bundle();
        if (genre == null) {
            args.putInt(PARAM_TYPE, ALL_SONGS);
        } else {
            args.putInt(PARAM_TYPE, GENRE_SONGS);
            args.putLong(PARAM_GENRE_ID, genre.getId());

        }

        fragment.setArguments(args);
        return fragment;
    }

    public static SongListFragment newInstance(Artist artist, Album album) {
        SongListFragment fragment = new SongListFragment();

        Bundle args = new Bundle();
        int type;
        if (artist == null && album == null) {
            type = ALL_SONGS;

        } else if (artist != null && album == null) {
            type = ARTIST_SONGS;
        } else if (artist == null && album != null) {
            type = ALBUM_SONGS;
        } else {
            type = ARTIST_ALBUM_SONGS;
        }
        args.putInt(PARAM_TYPE, type);
        if (artist != null) {
            args.putLong(PARAM_ARTIST_ID, artist.getId());
        }
        if (album != null) {
            args.putLong(PARAM_ALBUM_ID, album.getId());
        }

        fragment.setArguments(args);
        return fragment;
    }

    public SongListFragment showToolbar(boolean show) {
        mShowToolbar = show;
        return this;
    }

    public SongListFragment showFastScroller(boolean show) {
        mShowFastScroller = show;
        return this;
    }

    public SongListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mSongListType = args.getInt(PARAM_TYPE, ALL_SONGS);
            mArtistId = args.getLong(PARAM_ARTIST_ID);
            mAlbumId = args.getLong(PARAM_ALBUM_ID);
            mGenreId = args.getLong(PARAM_GENRE_ID);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, mLoaderCallbacks);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_song_list,
                container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new SongListAdapter();
        mRecyclerView.setAdapter(mAdapter);

        if (savedInstanceState != null) {
            mShowToolbar = savedInstanceState.getBoolean(STATE_SHOW_TOOLBAR)
                    || mShowToolbar;
            mShowFastScroller = savedInstanceState
                    .getBoolean(STATE_SHOW_FASTSCROLLER) || mShowFastScroller;
        }

        FastScroller scroller = (FastScroller) rootView
                .findViewById(R.id.fastscroller);
        Log.e("fastsc",String.valueOf(mShowFastScroller));
        if (mShowFastScroller) {
            scroller.setRecyclerView(mRecyclerView);
            scroller.setSectionIndexer(mAdapter);
        } else {
            scroller.setVisibility(View.GONE);
        }

        if (mShowToolbar) {
            Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
            toolbar.setVisibility(View.VISIBLE);
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_SHOW_TOOLBAR, mShowToolbar);
        outState.putBoolean(STATE_SHOW_FASTSCROLLER, mShowFastScroller);

    }

    private void selectSong(int position) {

        if (mListener != null) {
            mListener.onSongSelected(mAdapter.mSongList, position);
        }
    }

    @Override
    public void refresh() {
        Log.d("frag", "ertr");

        getLoaderManager().restartLoader(0, null, mLoaderCallbacks);
    }

    class SongViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        TextView vTitle;
        TextView vArtist;

        public SongViewHolder(View itemView) {
            super(itemView);
            vTitle = (TextView) itemView.findViewById(R.id.title);
            vArtist = (TextView) itemView.findViewById(R.id.artist);
            itemView.findViewById(R.id.item_view).setOnClickListener(this);

            ImageButton menuButton = (ImageButton) itemView.findViewById(R.id.menu_button);
            menuButton.setOnClickListener(this);

            boolean dark = ThemeHelper.isDarkThemeSelected(getActivity());

            if(!dark) {
                Drawable drawable = menuButton.getDrawable();

                drawable.mutate();
                drawable.setColorFilter(getActivity().getResources().getColor(R.color.primary_text), PorterDuff.Mode.SRC_ATOP);
            }
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            Song song = mAdapter.getItem(position);
            Log.d("album","album id "+song.getAlbumId()+" "+song.getAlbum());
            switch (v.getId()) {
                case R.id.item_view:


                    selectSong(position);
                    break;
                case R.id.menu_button:
                    showMenu(position, v);
                    break;
            }
        }
    }

    class SongListAdapter extends RecyclerView.Adapter<SongViewHolder>
            implements FastScroller.SectionIndexer {

        private List<Song> mSongList;

        public void setData(List<Song> data) {
            mSongList = data;
            notifyDataSetChanged();
        }



        public Song getItem(int position)
        {
            return mSongList==null?null:mSongList.get(position);
        }

        @Override
        public int getItemCount() {
            return mSongList==null?0:mSongList.size();
        }

        @Override
        public void onBindViewHolder(SongViewHolder viewHolder, int position) {
            Song song = mAdapter.getItem(position);

            viewHolder.vTitle.setText(song.getTitle());
            viewHolder.vArtist.setText(song.getArtist());

        }

        @Override
        public SongViewHolder onCreateViewHolder(ViewGroup parent, int type) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.song_list_item, parent, false);


            SongViewHolder viewHolder = new SongViewHolder(itemView);

            return viewHolder;
        }


        @Override
        public String getSectionForPosition(int position) {
            return getItem(position).getTitle().substring(0,1);
        }
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
