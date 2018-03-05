package ag.boersego.bgjs.sample;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import ag.boersego.bgjs.BGJSGLView;
import ag.boersego.bgjs.JNIV8Function;
import ag.boersego.bgjs.V8Engine;
import ag.boersego.bgjs.V8TextureView;
import ag.boersego.bgjs.sample.dummy.DummyContent;
import okhttp3.OkHttpClient;

/**
 * A fragment representing a single Demo detail screen.
 * This fragment is either contained in a {@link DemoListActivity}
 * in two-pane mode (on tablets) or a {@link DemoDetailActivity}
 * on handsets.
 */
public class DemoEjectaFragment extends Fragment implements V8Engine.V8EngineHandler {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "param";

    /**
     * The dummy content this fragment is presenting.
     */
    private DummyContent.DummyItem mItem;
    private V8TextureView mView;
    private FrameLayout mRootView;
    private boolean mEngineReady;
    private V8Engine mV8Engine;
    private long mJSId;

    private static final String TAG = "DemoDetailFragment";
    private String mScriptCb = "startPlasma";
    private float mScale;
    private JNIV8Function mStartPlasmaFunction;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DemoEjectaFragment() {
    }

    public void onAttach(Context a) {
        super.onAttach(a);

        mV8Engine = V8Engine.getInstance(getActivity().getApplication(), "js/plasma.js");
        mV8Engine.setHttpClient(new OkHttpClient());

        mV8Engine.addStatusHandler(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mScriptCb = getArguments().getString(ARG_ITEM_ID);
        }
        mScale = getResources().getDisplayMetrics().density;
    }

    private void initializeV8 (final BGJSGLView jsObj) {
        mV8Engine.getGlobalObject().getV8FieldTyped("startPlasma", JNIV8Function.class).callAsV8Function(jsObj);
    }

    protected void createGLView() {

        // We need to wait until both our parent view and the v8Engine are ready
        if (mRootView == null || !mEngineReady) {
            return;
        }

        mRootView.post(new Runnable() {
            @Override
            public void run() {

                Log.d(TAG, "Creating GL view and calling callback " + mScriptCb);

                // HC and up have TextureVew
                final V8TextureView tv = new V8TextureView(getActivity(), mScriptCb, "") {

                    @Override
                    public void onGLCreated(BGJSGLView jsViewObject) {
                        initializeV8(jsViewObject);
                    }

                    @Override
                    public void onGLRecreated(BGJSGLView jsViewObject) {
                        onGLCreated(jsViewObject);
                    }

                    @Override
                    public void onGLCreateError(Exception ex) {
                        Log.d (TAG, "OpenGL error", ex);
                    }

                    @Override
                    public void onRenderAttentionNeeded(BGJSGLView jsViewObject) {

                    }
                };
                tv.doDebug(false);
                tv.dontClearOnFlip(true);
                mView = tv;

                mRootView.addView(mView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = (FrameLayout) inflater.inflate(R.layout.fragment_demo_detail, container, false);

        createGLView();

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mView != null) {
            // Resume the JS stack for this view
            mView.unpause();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mView != null) {
            // Pause the JS stack for this view
            mView.pause();
        }
    }

    @Override
    public void onReady() {
        mEngineReady = true;
        createGLView();
    }
}
