package com.bangalore.barcamp.fragment;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.bangalore.barcamp.BCBSharedPrefUtils;
import com.bangalore.barcamp.R;
import com.bangalore.barcamp.SlotsListAdapter;
import com.bangalore.barcamp.activity.BCBFragmentActionbarActivity;
import com.bangalore.barcamp.activity.MainFragmentActivity;
import com.bangalore.barcamp.data.BarcampBangalore;
import com.bangalore.barcamp.data.BarcampData;
import com.bangalore.barcamp.data.Slot;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class ScheduleFragment extends BCBFragmentBaseClass {

	private List<Slot> slotsArray;
	private SlotsListAdapter adapter;
	public static final int CALL_REFRESH_DATA = 1;
	private static final int SHOW_ERROR_DIALOG = 100;
	private static final String BCB_DATA = "BCBData";
	private static final String LIST_POS = "ListPos";
	public static final String FROM_NOTIFICATION = "FromNotification";

    private boolean runOnce = false;

	AsyncTask<Void, Void, Void> mRegisterTask;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.schedule, null);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		BarcampData data = ((BarcampBangalore) getActivity()
				.getApplicationContext()).getBarcampData();

		if (data == null && savedInstanceState != null
				&& savedInstanceState.containsKey(BCB_DATA)) {
			((BarcampBangalore) getActivity().getApplicationContext())
					.setBarcampData((BarcampData) savedInstanceState
							.getSerializable(BCB_DATA));
		}
		Tracker t = ((BarcampBangalore) getActivity().getApplication())
				.getTracker();

		// Set screen name.
		t.setScreenName(this.getClass().getName());

		// Send a screen view.
		t.send(new HitBuilders.AppViewBuilder().build());
	}

	private void addScheduleItems(List<Slot> slotsArray) {
		ListView listView = (ListView) getView().findViewById(R.id.listView1);
		adapter = new SlotsListAdapter(getActivity(), slotsArray);
		this.slotsArray = slotsArray;
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {
				if (ScheduleFragment.this.slotsArray.get(pos).type
						.equals(Slot.SESSION)) {
					Intent intent = new Intent(getActivity(),
							SlotDetailsFragment.class);
					intent.putExtra(SlotDetailsFragment.EXTRA_POS, pos);
					((BCBFragmentActionbarActivity) getActivity())
							.callForFunction(
									MainFragmentActivity.CALL_SLOT_DETAILS,
									intent);
				}
			}
		});
	}

	public class SlotItemClickListener implements OnClickListener {

		Slot slot;
		Activity context;

		public SlotItemClickListener(Activity context, Slot slot) {
			this.slot = slot;
			this.context = context;
		}

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(context, SlotDetailsFragment.class);
			intent.putExtra(SlotDetailsFragment.EXTRA_POS, slot.pos);
			((BCBFragmentActionbarActivity) getActivity()).callForFunction(
					MainFragmentActivity.CALL_SLOT_DETAILS, intent);
		}
	}

	private void updateViews(BarcampData data) {
		View view = getView();
		view.findViewById(R.id.spinnerLayout).setVisibility(View.GONE);
		if (TextUtils.isEmpty(data.status)) {
			view.findViewById(R.id.listView1).setVisibility(View.VISIBLE);
			addScheduleItems(data.slotsArray);
			view.findViewById(R.id.infoText).setVisibility(View.GONE);
		} else {
			TextView infoText = ((TextView) view.findViewById(R.id.infoText));
			infoText.setMovementMethod(LinkMovementMethod.getInstance());
			infoText.setText(Html.fromHtml(data.status));
			infoText.setVisibility(View.VISIBLE);
			view.findViewById(R.id.listView1).setVisibility(View.GONE);
		}
	}

	@Override
	public Intent callForFunction(int id, Intent params) {
		if (id == CALL_REFRESH_DATA) {
			BarcampData data = ((BarcampBangalore) getActivity()
					.getApplicationContext()).getBarcampData();

			updateViews(data);
			return null;
		}
		return super.callForFunction(id, params);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (BCBSharedPrefUtils.getScheduleUpdated(getActivity())) {
			((BarcampBangalore) getActivity().getApplicationContext())
					.setBarcampData(null);
			BCBSharedPrefUtils.setScheduleUpdated(getActivity(), false);
		}
		BarcampData data = ((BarcampBangalore) getActivity()
				.getApplicationContext()).getBarcampData();
		BCBFragmentActionbarActivity activity = (BCBFragmentActionbarActivity) getActivity();
		if (data == null) {
			View view = getView();
			view.findViewById(R.id.listView1).setVisibility(View.GONE);
			view.findViewById(R.id.infoText).setVisibility(View.GONE);
			activity.callForFunction(MainFragmentActivity.CALL_REFRESH, null);

		} else {
			activity.callForFunction(
					MainFragmentActivity.DISMISS_PROGRESS_DIALOG, null);
			updateViews(data);
		}
	}

}
