package com.opentok.helloworld;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.opentok.Publisher;
import com.opentok.Session;
import com.opentok.Stream;
import com.opentok.Subscriber;

public class MainActivity extends Activity implements Publisher.Listener, Session.Listener, Callback {
	ExecutorService executor;
	SurfaceView publisherView;
	SurfaceView subscriberView;
	Camera camera;
	Publisher publisher;
	Subscriber subscriber;
	private Session session;
	private WakeLock wakeLock;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		publisherView = (SurfaceView)findViewById(R.id.publisherview);
		subscriberView = (SurfaceView)findViewById(R.id.subscriberview);
		publisherView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		executor = Executors.newCachedThreadPool();
		publisherView.getHolder().addCallback(this);

		PowerManager powerManager =
				(PowerManager)getSystemService(Context.POWER_SERVICE);
		wakeLock =
				powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
						"Full Wake Lock");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onStop() {
		super.onStop();

		if (wakeLock.isHeld()) {
			wakeLock.release();
		}
		//what else needs to get in here?
		if (null != camera) camera.release();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!wakeLock.isHeld()) {
			wakeLock.acquire();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (wakeLock.isHeld()) {
			wakeLock.release();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		if (publisher == null) {
			executor.submit(new Runnable() {

				@Override
				public void run() {
					try {
						camera = Camera.open(Camera.getNumberOfCameras() - 1);
						camera.setPreviewDisplay(publisherView.getHolder());
						camera.startPreview();

						session = Session.newInstance("1_MX4zNDUyNjF-NzIuNS4xNjcuMTM0fldlZCBTZXAgMTkgMTU6NDk6MzQgUERUIDIwMTJ-MC45Nzc2Mjkzfg",
								"T1==cGFydG5lcl9pZD0zNDUyNjEmc2lnPTE0Y2JiNGY2ZGUzNjkwYjJlNzUyMjI2YTdlMDA1YjhhZGUzNjA1YjY6c2Vzc2lvbl9pZD0mY3JlYXRlX3RpbWU9MTM0ODA5NDk3NCZyb2xlPW1vZGVyYXRvciZub25jZT0xMzQ4MDk0OTc0LjE3NzkxNDk0NzQyNTgmZXhwaXJlX3RpbWU9MTM0ODY5OTc3NA==",
								"345261",
								MainActivity.this);
						session.connect();

					} catch (Throwable t) {
						t.printStackTrace();
					}

				}});
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSessionConnected() {
		executor.submit(new Runnable() {

			@Override
			public void run() {
				publisher = session.createPublisher(camera, publisherView.getHolder().getSurface());
				publisher.connect();
			}});
	}

	@Override
	public void onSessionDidReceiveStream(final Stream stream) {
		executor.submit(new Runnable() {

			@Override
			public void run() {
				try {
					if (publisher.getStreamId().equals(stream.getStreamId())) {
						subscriber = session.createSubscriber(subscriberView, stream);
						subscriber.connect();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}});
	}

	@Override
	public void onPublisherStreamingStarted() {
		Log.i("hello-world", "publisher is streaming!");
	}

	@Override
	public void onPublisherFailed() {
		Log.e("hello-world", "publisher failed!");
	}

}
