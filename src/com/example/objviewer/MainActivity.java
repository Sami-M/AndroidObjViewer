package com.example.objviewer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.content.Context;
import android.content.res.AssetManager;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;
import android.view.MotionEvent;

public class MainActivity extends Activity {

    private GLSurfaceView mGLView;
    float coordinates[] = null;
	int counter2 = 0;
    Context thisContext;
    float [] v;
    FloatBuffer mVertexBuffer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        
        BufferedReader br = null;
		AssetManager assetMgr = getAssets();

		InputStream is = null;
		try {
			is = assetMgr.open("hammer.obj", AssetManager.ACCESS_STREAMING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			Log.i("blurg", "Loaded the stream" + is);
			
			String line;
			br = new BufferedReader(new InputStreamReader(is));
        
			mGLView = new MyGLSurfaceView(this);
			setContentView(mGLView);
		
    }
    
    @Override
    protected void onPause() {
        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        // consume significant memory here.
        super.onPause();
        mGLView.onPause();
    }

    @Override
    protected void onResume() {
        // The following call resumes a paused rendering thread.
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
        super.onResume();
        mGLView.onResume();
    }
    
    public class MyGLSurfaceView extends GLSurfaceView {

        private final MyGLRenderer mRenderer;

        public MyGLSurfaceView(Context context) {
            super(context);
            thisContext = context;

            // Set the Renderer for drawing on the GLSurfaceView
            mRenderer = new MyGLRenderer();
            setRenderer(mRenderer);

            // Render the view only when there is a change in the drawing data
            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        }

        private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
        private float mPreviousX;
        private float mPreviousY;

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            // MotionEvent reports input details from the touch screen
            // and other input controls. In this case, we are only
            // interested in events where the touch position changed.

            float x = e.getX();
            float y = e.getY();

            switch (e.getAction()) {
                case MotionEvent.ACTION_MOVE:

                    float dx = x - mPreviousX;
                    float dy = y - mPreviousY;

                    // reverse direction of rotation above the mid-line
                    if (y > getHeight() / 2) {
                        dx = dx * -1 ;
                    }

                    // reverse direction of rotation to left of the mid-line
                    if (x < getWidth() / 2) {
                        dy = dy * -1 ;
                    }

                    mRenderer.setAngle(
                            mRenderer.getAngle() +
                            ((dx + dy) * TOUCH_SCALE_FACTOR));  // = 180.0f / 320
                    requestRender();
            }

            mPreviousX = x;
            mPreviousY = y;
            return true;
        }

    }
    
    public class MyGLRenderer implements GLSurfaceView.Renderer {

        private Triangle mTriangle;
        private Mesh mMesh;
        private float mAngle;
        Context mContext;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // Set the background frame color
            gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

            mTriangle = new Triangle();
            mMesh = new Mesh(mContext);
        }

        @Override
        public void onDrawFrame(GL10 gl) {

            // Draw background color
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            // Set GL_MODELVIEW transformation mode
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();   // reset the matrix to its default state

            // When using GL_MODELVIEW, you must set the view point
            GLU.gluLookAt(gl, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

            // Draw square

            // Create a rotation for the triangle

            // Use the following code to generate constant rotation.
            // Leave this code out when using TouchEvents.
            // long time = SystemClock.uptimeMillis() % 4000L;
            // float angle = 0.090f * ((int) time);

            gl.glRotatef(mAngle, 0.0f, 0.0f, 1.0f);

            // Draw triangle
            mTriangle.draw(gl);
            mMesh.draw(gl);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            // Adjust the viewport based on geometry changes
            // such as screen rotations
            gl.glViewport(0, 0, width, height);

            // make adjustments for screen ratio
            float ratio = (float) width / height;
            gl.glMatrixMode(GL10.GL_PROJECTION);        // set matrix to projection mode
            gl.glLoadIdentity();                        // reset the matrix to its default state
            gl.glFrustumf(-ratio, ratio, -1, 1, 3, 7);  // apply the projection matrix
        }

        /**
         * Returns the rotation angle of the triangle shape (mTriangle).
         *
         * @return - A float representing the rotation angle.
         */
        public float getAngle() {
            return mAngle;
        }

        /**
         * Sets the rotation angle of the triangle shape (mTriangle).
         */
        public void setAngle(float angle) {
            mAngle = angle;
        }
    }
    
    public class Triangle {

        private final FloatBuffer vertexBuffer;

        // number of coordinates per vertex in this array
        static final int COORDS_PER_VERTEX = 3;
        float triangleCoords[] = {
                // in counterclockwise order:
                0.0f,  0.622008459f, 0.0f,// top
               -0.5f, -0.311004243f, 0.0f,// bottom left
                0.5f, -0.311004243f, 0.0f // bottom right
        };

        float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 0.0f };

        /**
         * Sets up the drawing object data for use in an OpenGL ES context.
         */
        public Triangle() {
            // initialize vertex byte buffer for shape coordinates
            ByteBuffer bb = ByteBuffer.allocateDirect(
                    // (number of coordinate values * 4 bytes per float)
                    triangleCoords.length * 4);
            // use the device hardware's native byte order
            bb.order(ByteOrder.nativeOrder());

            // create a floating point buffer from the ByteBuffer
            vertexBuffer = bb.asFloatBuffer();
            // add the coordinates to the FloatBuffer
            vertexBuffer.put(triangleCoords);
            // set the buffer to read the first coordinate
            vertexBuffer.position(0);
        }

        /**
         * Encapsulates the OpenGL ES instructions for drawing this shape.
         *
         * @param gl - The OpenGL ES context in which to draw this shape.
         */
        public void draw(GL10 gl) {
            // Since this shape uses vertex arrays, enable them
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

            // draw the shape
            gl.glColor4f(       // set color:
                    color[0], color[1],
                    color[2], color[3]);
            gl.glVertexPointer( // point to vertex data:
                    COORDS_PER_VERTEX,
                    GL10.GL_FLOAT, 0, vertexBuffer);
            gl.glDrawArrays(    // draw shape:
                    GL10.GL_TRIANGLES, 0,
                    triangleCoords.length / COORDS_PER_VERTEX);

            // Disable vertex array drawing to avoid
            // conflicts with shapes that don't use it
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        }
    }

    public class Mesh {

        private final FloatBuffer vertexBuffer;
        private final ShortBuffer drawListBuffer;

        // number of coordinates per vertex in this array
        static final int COORDS_PER_VERTEX = 3;
        float squareCoords[] = {
                -0.5f,  0.5f, 0.0f,   // top left
                -0.5f, -0.5f, 0.0f,   // bottom left
                 0.5f, -0.5f, 0.0f,   // bottom right
                 0.5f,  0.5f, 0.0f }; // top right
        
        float coordinates[] = new float[1000];

        private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

        float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };
    	private Context mContext;

        /**
         * Sets up the drawing object data for use in an OpenGL ES context.
         */
        //////////////////////////////////////////////////////////////////
        private void readObj (String fileName) {
        	System.out.println(fileName);
    		BufferedReader br = null;
    		try {
    			AssetManager mngr = getAssets();
    			InputStream is = mngr.open("hammer2.obj");
    			if(is==null){System.out.println("STREAM IS EMPTY");}
    			//InputStream is = getResources().getAssets().open("hammer.obj");
    			InputStreamReader inputStreamReader = new InputStreamReader(is);
    			br = new BufferedReader(inputStreamReader);    			
    			
    			List<Float> verticies = new ArrayList<Float>();
    			List<Float> vertexNormals = new ArrayList<Float>();
    			List<Short> faces = new ArrayList<Short>();
    			String line;
    			System.out.println("We got here");
    			float[] coords = new float[908];
    			while((line = br.readLine()) != null) {
    				String[] words;
    				words = line.split(" ");
    				
					//Log.i("line", "line:" + line);

    				if(line.startsWith("v")){
    					float x = Float.parseFloat(words[1]);
    					float y = Float.parseFloat(words[2]);
    					float z = Float.parseFloat(words[3]);
    					verticies.add(x); coords[counter2++] = x;
    					verticies.add(y); coords[counter2++] = y;
    					verticies.add(z); coords[counter2++] = z;
    				}
    				else if (words[0].equals("f")){
    					//List<Short> indices = parseFace(words);
    					
    					//faces.addAll(indices);
    				}
    				else if (words[0].equals("vn")){
    					float x = Float.parseFloat(words[1]);
    					float y = Float.parseFloat(words[2]);
    					float z = Float.parseFloat(words[3]);
    					vertexNormals.add(x);
    					vertexNormals.add(y);
    					vertexNormals.add(z);
    				}

    			}
        		mVertexBuffer = makeFloatBuffer(coords);

    			int i = 0;
    			v = new float[verticies.size()];
    			for(Float vertex: verticies) {
    				v[i++] = vertex;
    			}
    			i = 0;
    			short [] f = new short[faces.size()];
    			for (Short face: faces){
    				f[i++] = face;
    			}
    				
    			//Mesh mesh = new Mesh(v, f);
    		} catch (Exception e) {
    			//TDOO
    		}
    		System.out.println("Counter2 =" +counter2);

        }

        private FloatBuffer makeFloatBuffer(float[] arr)  
        {  
            ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * 4);  
            bb.order(ByteOrder.nativeOrder());  
            FloatBuffer fb = bb.asFloatBuffer();  
            fb.put(arr);  
            fb.position(0);  
            return fb;  
        }  

        /////////////////////////////////////////////////////////////////
        public Mesh(Context mContext) {
        	readObj("hammer.obj");
            // initialize vertex byte buffer for shape coordinates
            ByteBuffer bb = ByteBuffer.allocateDirect(
            // (# of coordinate values * 4 bytes per float)
                    counter2 * 4);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(mVertexBuffer);
            vertexBuffer.position(0);

            // initialize byte buffer for the draw list
            ByteBuffer dlb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 2 bytes per short)
                    drawOrder.length * 2);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(drawOrder);
            drawListBuffer.position(0);
        }

        /**
         * Encapsulates the OpenGL ES instructions for drawing this shape.
         *
         * @param gl - The OpenGL ES context in which to draw this shape.
         */
        public void draw(GL10 gl) {
            // Since this shape uses vertex arrays, enable them
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

            // draw the shape
            gl.glColor4f(       // set color
                    color[0], color[1],
                    color[2], color[3]);
            gl.glVertexPointer( // point to vertex data:
                    COORDS_PER_VERTEX,
                    GL10.GL_FLOAT, 0, vertexBuffer);
            gl.glDrawElements(  // draw shape:
                    GL10.GL_TRIANGLES,
                    drawOrder.length, GL10.GL_UNSIGNED_SHORT,
                    drawListBuffer);

            // Disable vertex array drawing to avoid
            // conflicts with shapes that don't use it
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        }
    }
    
}
