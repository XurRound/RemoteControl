package me.xurround.remotecontrol.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import me.xurround.remotecontrol.R;

public class ListFragment extends Fragment
{
    private ArrayAdapter<String> adapter;

    private AdapterView.OnItemClickListener listener;

    public ListFragment(ArrayAdapter<String> adapter, AdapterView.OnItemClickListener listener)
    {
        this.adapter = adapter;

        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        ListView listView = view.findViewById(R.id.listView);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(listener);

        return view;
    }
}