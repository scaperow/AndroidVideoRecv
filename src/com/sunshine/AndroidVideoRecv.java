package com.sunshine;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class AndroidVideoRecv extends Activity implements OnClickListener, Callback  {
	private String remoteIpStr=null;
	private EditText remoteIP=null;
	private Button btnConn=null;
	private  Kit kkit=null;
	
	
	private static final int width = 240;//240;
	private static final int height = 160;//160;
	private int iTotalFramePerImage=20;//一幅图像分iTotalFramePerImage次发送
	private static int stride=width+200;
	private static int[] mColors=new int[stride*height];
	
	private static final int dataLen = width*height*3/2;
	private int iOneFrameData=dataLen/iTotalFramePerImage;
	private int iNumFrame=0;
	private byte[] yuv420sp=new byte[dataLen];

	private static final int numBands = 3;
	//private byte[] byteArray = new byte[width * height * numBands];// 图像RGB数组
	private int[] byteArray = new int[width * height * numBands];// 图像RGB数组
	
	private SurfaceView mSurface=null;
	public SurfaceHolder mSurfaceHolder=null;
	private Bitmap bitMap2=null;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		/*getWindow().setFormat(PixelFormat.TRANSLUCENT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        setContentView(R.layout.main);

         mSurface=(SurfaceView)findViewById(R.id.surface_player);
         mSurfaceHolder=mSurface.getHolder();
         mSurfaceHolder.addCallback(this);
         
         btnConn=(Button)findViewById(R.id.connect);
		 remoteIP=(EditText)findViewById(R.id.remoteIP);
         btnConn.setOnClickListener(this);
         
         

    }
    class Kit implements Runnable
    {
    	Canvas canvas;
    	boolean run=true;
    	int[] getIntArray;
    	
		@Override
		public void run() {
			
			
			// TODO Auto-generated method stub
			//if(yuv420sp.length!=0){ 
			//bitMap=BitmapFactory.decodeByteArray(yuv420sp, 0, yuv420sp.length); 
			
			
			//}
				//while(true)
				//{		

					try{
						Socket socket=new Socket("192.168.1.101",7788);
						DataOutputStream dos=new DataOutputStream(socket.getOutputStream());
						DataInputStream dis=new DataInputStream(socket.getInputStream());
						while(true)
						{
							canvas=null;
							Log.d("INFO","reciving...");
							for(iNumFrame=0;iNumFrame<iTotalFramePerImage;iNumFrame++)
							{
								dis.read(yuv420sp,iNumFrame*iOneFrameData,iOneFrameData);
								dos.writeBoolean(true);
							}
							decodeYUV420SP(byteArray,yuv420sp,width,height);
							//mColors=initColors(byteArray);  
							bitMap2=Bitmap.createBitmap(mColors, 0, stride, width, height, Config.RGB_565); 
							

							//bitMap2=BitmapFactory.decodeResource(getResources(), R.drawable.red1);
					         if(null==bitMap2)
					         {
					        	 Log.d("INFO","can't get image...");
					         }
					         else
					         {
						         canvas=mSurfaceHolder.lockCanvas(null);
								 canvas.drawBitmap(bitMap2, 0, 0, new Paint());
								 mSurfaceHolder.unlockCanvasAndPost(canvas);// 更新屏幕显示内容
								 
									//canvas = mSurfaceHolder.lockCanvas(null);
									//canvas.drawColor(Color.BLACK);// 清除画布
									//mSurfaceHolder.unlockCanvasAndPost(canvas);
					         }
						}
					}
					catch(Exception e){
						
						Log.e("INFO", "exception >>>>>>>" + e.getLocalizedMessage());  
						Log.e("INFO","get Exception");
					}
				//}
		}
		 /**
		 将yuv420解码之后获得的rgb数据  转换成createBitmap可以用的int格式
		  */
		/*private int[] initColors(byte[] buf) {
			int[] colors=new int[stride*height];
			for (int y = 0; y < height; y++) {//use of x,y is legible then the use of i,j
				for (int x = 0; x < width; x++) {
					int r = buf[160*y+x];
					int g =  buf[160*y+x+1];
					int b =  buf[160*y+x+2];
					int a = 0x00;
					colors[y*stride+x]=(a<<24)|(r<<16)|(g<<8)|(b);//the shift operation generates the color ARGB
				}
			}
			return colors;
		}*/


    }
    
    
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v==btnConn)
			{
				remoteIpStr=remoteIP.getText().toString();
				//new Thread(AndroidVideoRecv.kit).start();
				kkit=new Kit();
				new Thread(this.kkit).start();
				
			}
	}
	public void doDraw(Canvas canvas)
	{
		canvas.drawBitmap(bitMap2, 0, 0, null);
	}
	@Override 
	public void surfaceChanged(SurfaceHolder holder , int format,int width,int height)
	{}
	@Override 
	public void surfaceCreated(SurfaceHolder holder)
	{}
	@Override 
	public void surfaceDestroyed(SurfaceHolder holder)
	{}
	//private static void decodeYUV420SP(byte[] rgbBuf, byte[] yuv420sp,
	private static void decodeYUV420SP(int[] rgbBuf, byte[] yuv420sp,
			int width, int height) {
		final int frameSize = width * height;
		if (rgbBuf == null)
			throw new NullPointerException("buffer 'rgbBuf' is null");
		if (rgbBuf.length < frameSize * 3)
			throw new IllegalArgumentException("buffer 'rgbBuf' size "
					+ rgbBuf.length + " < minimum " + frameSize * 3);

		if (yuv420sp == null)
			throw new NullPointerException("buffer 'yuv420sp' is null");

		if (yuv420sp.length < frameSize * 3 / 2)
			throw new IllegalArgumentException("buffer 'yuv420sp' size "
					+ yuv420sp.length + " < minimum " + frameSize * 3 / 2);

		int i = 0, y = 0;
		int uvp = 0, u = 0, v = 0;
		int y1192 = 0, r = 0, g = 0, b = 0;
		for (int j = 0, yp = 0; j < height; j++) {
			uvp = frameSize + (j >> 1) * width;
			u = 0;
			v = 0;
			for (i = 0; i < width; i++, yp++) {
				if (y < 0)
					y = 0;

				rgbBuf[yp * 3] = (byte) y;
				rgbBuf[yp * 3 + 1] = (byte) y;
				rgbBuf[yp * 3 + 2] = (byte) y;
				y = (0xff & ((int) yuv420sp[yp])) - 16;
				if (y < 0)
					y = 0;
	
				if ((i & 1) == 0) {
					v = (0xff & yuv420sp[uvp++]) - 128;
					u = (0xff & yuv420sp[uvp++]) - 128;
				}
				

				y1192 = 1192 * y;
				r = (y1192 + 1634 * v);
				g = (y1192 - 833 * v - 400 * u);
				b = (y1192 + 2066 * u);

				if (r < 0)
					r = 0;
				else if (r > 262143)
					r = 262143;
				if (g < 0)
					g = 0;
				else if (g > 262143)
					g = 262143;
				if (b < 0)
					b = 0;
				else if (b > 262143)
					b = 262143;
				//在这里转换成byte在手机上显示时  比较亮的部分解码效果很差，呈现黄色块状，可能是因为颜色信息丢失的原因
				//rgbBuf[yp * 3] = (byte) (r >> 10);
				rgbBuf[yp * 3] =  (r >> 10);
				rgbBuf[yp * 3 + 1] =  (g >> 10);
				rgbBuf[yp * 3 + 2] =  (b >> 10);
				
				//mColors[j*stride+i]=(0xf0<<16)|((rgbBuf[yp * 3] )<<16)|((rgbBuf[yp * 3 + 1])<<8)|(rgbBuf[yp * 3 + 2]);//the shift operation generates the color ARGB
				mColors[j*stride+i]=(0xff<<24) | (rgbBuf[yp * 3]<<16)|(rgbBuf[yp * 3 + 1]<<8)|(rgbBuf[yp * 3 + 2]);//the shift operation generates the color ARGB
			}
		}
	}
}



