package org.opencv.samples.tutorial1;

import java.io.IOException;

import javax.crypto.spec.GCMParameterSpec;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;



public class Tutorial1Activity extends Activity implements CvCameraViewListener2 {
	private Tutorial1Activity activity;
    private static final String TAG = "OCVSample::Activity";
    
	// FlagDraw
	private boolean flagDraw =false;
	private String FLAGDRAW = "FLAGDRAW";

    private CameraBridgeViewBase mOpenCvCameraView;
    private TextView imgTilte, imgDisp;
    
    private boolean              mIsJavaCamera = true;
    private MenuItem             mItemSwitchCamera = null;
    
 // A key for storing the index of the active image size.
 	private static final String STATE_IMAGE_SIZE_INDEX = "imageSizeIndex";

 	// Keys for storing the indices of the active filters.
 	private static final String STATE_IMAGE_DETECTION_FILTER_INDEX = "imageDetectionFilterIndex";
    
 // The filters.
 	private ImageDetectionFilter[] mImageDetectionFilters;
 	
 	// The indices of the active filters.
 	private int mImageDetectionFilterIndex;
 	
 	// Target found index.
 	private int foundTargetIndex = -1;

	private int falseCount = 0;
 	
 // The index of the active image size.
 	private int mImgSizeIndex;

 	private Camera mCamera;

	private ImageProcessTask imageProcessTask;
 	

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                
                	
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    
                    final ImageDetectionFilter chengpo;
    				try {
    					chengpo = new ImageDetectionFilter(Tutorial1Activity.this,
    							R.drawable.chengpo);
    				} catch (IOException e) {
    					Log.e(TAG, "Failed to load drawable: " + "chengpo");
    					e.printStackTrace();
    					break;
    				}
                    
    				final ImageDetectionFilter chiayi;
    				try {
    					chiayi = new ImageDetectionFilter(
    							Tutorial1Activity.this,
    							R.drawable.chiayi);
    				} catch (IOException e) {
    					Log.e(TAG, "Failed to load drawable: "
    							+ "chiayi");
    					e.printStackTrace();
    					break;
    				}

    				final ImageDetectionFilter summer_street;
    				try {
    					summer_street = new ImageDetectionFilter(
    							Tutorial1Activity.this,
    							R.drawable.summer_street);
    				} catch (IOException e) {
    					Log.e(TAG, "Failed to load drawable: "
    							+ "summer_street");
    					e.printStackTrace();
    					break;
    				}
    		
    				mImageDetectionFilters = new ImageDetectionFilter[] {summer_street, chengpo, chiayi };
    				  
                 break;
                 
                 
                default:
                
                    super.onManagerConnected(status);
                break;
            }
        }
    };

    public Tutorial1Activity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        
        if (savedInstanceState != null) {
			
			mImageDetectionFilterIndex = savedInstanceState.getInt(
					STATE_IMAGE_DETECTION_FILTER_INDEX, 0);
			mImgSizeIndex = savedInstanceState
					.getInt(STATE_IMAGE_SIZE_INDEX, 0);
			
		} else {
			
			mImgSizeIndex = 0;
			mImageDetectionFilterIndex = 0;
			
		}
        
        setContentView(R.layout.tutorial1_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
        
        imgTilte = (TextView) findViewById(R.id.imgTitle);
        imgDisp = (TextView) findViewById(R.id.imgDisp);

       
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setMaxFrameSize(1280, 720);

        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    

//    public void onCameraViewStarted(int width, int height) {
//        android.hardware.Camera.Size res = mOpenCvCameraView.getResolutionList().get(((Tutorial1Activity) mOpenCvCameraView).getResolutionList().size()-1);
//        mOpenCvCameraView.setResolution(res);
//    }
//    
//    public List<Size> getResolutionList() {
//        return mCamera.getParameters().getSupportedPreviewSizes();
//    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
    	final Mat rgba = inputFrame.rgba();


		if (imageProcessTask != null && !imageProcessTask.isCancelled()) {
			imageProcessTask.cancel(true);
			imageProcessTask = null;
        }

        if(imageProcessTask == null) {
			imageProcessTask = new ImageProcessTask();
			imageProcessTask.execute(rgba); // 傳入 rgba
        }




			// Apply the active filters.
    	
//   for(mImageDetectionFilterIndex = 0 ; mImageDetectionFilterIndex < mImageDetectionFilters.length;mImageDetectionFilterIndex++){
//
//    		if(mImageDetectionFilterIndex == mImageDetectionFilters.length){
//    			mImageDetectionFilterIndex = 0;
//   		}
//
//
//
//    		mImageDetectionFilters[mImageDetectionFilterIndex].apply(rgba, rgba);
//        	flagDraw = mImageDetectionFilters[mImageDetectionFilterIndex].targetFound();
//
//        	Log.d(FLAGDRAW, "flagDraw : "+ mImageDetectionFilterIndex);
//        	Log.d(FLAGDRAW, "flagDraw : "+ flagDraw);
//
//
//        		if(flagDraw){
//
//        			foundTargetIndex = mImageDetectionFilterIndex;
//        			//mImageDetectionFilters[foundTargetIndex].apply(rgba, rgba);
//            		//flagDraw = mImageDetectionFilters[mImageDetectionFilterIndex].targetFound();
//
//            		Log.d(FLAGDRAW, "!!!!flagDraw : "+ foundTargetIndex);
//            		Log.d(FLAGDRAW, "!!!!flagDraw : "+ flagDraw);
//
//            		switch (foundTargetIndex) {
//            		case 2:
//						// 設定文字說明
//						Thread chiayi = new Thread(new Runnable() {
//
//							@Override
//							public void run() {
//
//								mHandler.sendEmptyMessage(2);
//
//							}
//
//						});
//						chiayi.start();
//
//						break;
//
//
//					case 1:
//						// 設定文字說明
//						Thread chengpo = new Thread(new Runnable() {
//
//							@Override
//							public void run() {
//
//								mHandler.sendEmptyMessage(1);
//
//							}
//
//						});
//						chengpo.start();
//
//						break;
//
//					case 0:
//						// 設定文字說明
//						Thread summer_street = new Thread(new Runnable() {
//
//							@Override
//							public void run() {
//
//								mHandler.sendEmptyMessage(0);
//
//							}
//
//						});
//						summer_street.start();
//						break;
//
//					}
//
//
//        		}
//
//    		}
		return rgba;
    }
    
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
			if(msg.what == 3) {
			imgTilte.setText("碧潭");
				imgDisp.setText("靛藍色的潭面是此作的重點。正面的碧潭大橋退至遠處，畫面的重心遠離遊客，平靜澄澈，深不可測的潭面才是畫家的注意力所在。左側的喬木剛剛冒出嫩葉，垂釣的人似乎還感覺到些微涼意。畫中藍色調變化的潭面與天空、遠景，相互呼應，層次豐富，微微蕩漾的水面光景，充滿魅力。");
			}
        	 if(msg.what == 2) {
             	imgTilte.setText("嘉義街外");
             	imgDisp.setText("藝術家： 陳澄波"+"\n"+"年代： 1927"+"\n"+"畫中呈現出規矩方正的學院作風。也許這年暑假回到嘉義時，陳氏製作此畫，為後學者示範西洋畫自文藝復興時期以來所重視的透視法。畫面中活潑的雞群與人物都退避道旁，讓街道筆直地伸向遠方的地平線，並與電線桿會合。前景起伏不整的小路，以及突出的人物活動是全幅的精神所在。");
             }

            if(msg.what == 1) {
            	imgTilte.setText("廟口");
            	imgDisp.setText("藝術家： 陳澄波");
            }

            if(msg.what == 0){
            	imgTilte.setText("夏日街景");
            	imgDisp.setText("藝術家： 陳澄波"+"\n"+"年代： 1927");
            }

			if(msg.what == -1){
				imgTilte.setText("");
				imgDisp.setText("");
			}
            super.handleMessage(msg);
        }
    };

	@Override
	public void onCameraViewStarted(int width, int height) {
		// TODO Auto-generated method stub
		
	}


	private class ImageProcessTask extends AsyncTask<Mat,Mat,Mat>{

		private Mat bgMat;

		@Override
		protected Mat doInBackground(Mat... mats) {

			// 收到傳入的 Mat
			bgMat = mats[0];


			// 重複找
			for(mImageDetectionFilterIndex = 0 ; mImageDetectionFilterIndex < mImageDetectionFilters.length;mImageDetectionFilterIndex++) {

				if (mImageDetectionFilterIndex == mImageDetectionFilters.length) {
					mImageDetectionFilterIndex = 0;
				}

				mImageDetectionFilters[mImageDetectionFilterIndex].apply(bgMat, bgMat);
				flagDraw = mImageDetectionFilters[mImageDetectionFilterIndex].targetFound();

				Log.d(FLAGDRAW, "flagDraw : "+ mImageDetectionFilterIndex);
				Log.d(FLAGDRAW, "flagDraw : "+ flagDraw);

				// 找到的話就做下面的動作
				if(flagDraw){
					falseCount = 0;
        			foundTargetIndex = mImageDetectionFilterIndex;
        			//mImageDetectionFilters[foundTargetIndex].apply(rgba, rgba);
            		//flagDraw = mImageDetectionFilters[mImageDetectionFilterIndex].targetFound();

            		Log.d(FLAGDRAW, "!!!!flagDraw : "+ foundTargetIndex);
            		Log.d(FLAGDRAW, "!!!!flagDraw : "+ flagDraw);

            		switch (foundTargetIndex) {
            		case 2:
						// 設定文字說明
						Thread chiayi = new Thread(new Runnable() {

							@Override
							public void run() {

								mHandler.sendEmptyMessage(2);

							}

						});
						chiayi.start();

						break;


					case 1:
						// 設定文字說明
						Thread chengpo = new Thread(new Runnable() {

							@Override
							public void run() {

								mHandler.sendEmptyMessage(1);

							}

						});
						chengpo.start();

						break;

					case 0:
						// 設定文字說明
						Thread summer_street = new Thread(new Runnable() {

							@Override
							public void run() {

								mHandler.sendEmptyMessage(0);

							}

						});
						summer_street.start();
						break;

					}


				}else {
					falseCount = falseCount + 1;
					if (falseCount > 10) {
						Thread notfound = new Thread(new Runnable() {
							@Override
							public void run() {
								mHandler.sendEmptyMessage(-1);
							}
						});
						notfound.start();
					}
				}
			}

			return null;
		}
	}

	
   

}
