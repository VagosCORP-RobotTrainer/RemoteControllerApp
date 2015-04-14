package com.vagoscorp.vc_rtrcontroller;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import vclibs.communication.android.Comunic;

import vclibs.communication.Eventos.OnConnectionListener;
import vclibs.communication.Eventos.OnComunicationListener;

public class MainActivity extends Activity implements Runnable{

    Comunic comunic = new Comunic();
    boolean firstrun = true;
    Thread thread;
    boolean run = false;
    float deltaX;
    float deltaY;
    SurfaceView sv_ang;
    SurfaceView sv_pos;
    SurfaceHolder angHolder;
    SurfaceHolder posHolder;
    Canvas angCanvas;
    Canvas posCanvas;
    float angcam = 0;
    float angzen = 0;
    float posx = 0;
    float posy = 0;
    float angWidth = 0;
    float posWidth = 0;
    float posHeigh = 0;
    TextView posX;
    TextView posY;
    Paint posPaint = new Paint();
    Paint angPaint = new Paint();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        posX = (TextView)findViewById(R.id.textViewX);
        posY = (TextView)findViewById(R.id.textViewY);
        sv_ang = (SurfaceView)findViewById(R.id.surfaceView);
        sv_pos = (SurfaceView)findViewById(R.id.surfaceView2);
        angHolder = sv_ang.getHolder();
        posHolder = sv_pos.getHolder();
        angPaint.setColor(Color.BLACK);
        posPaint.setColor(Color.BLACK);
        comunic = new Comunic();
//        sv_ang.setOnTouchListener(new View.OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                angcam = event.getX();
//                return true;
//            }
//        });
        sv_pos.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                posx = event.getX();
                posy = event.getY();
                float posyi = posHeigh - posy;
                posX.setText("" + (posx/deltaX));
                posY.setText("" + (posyi/deltaY));
                return true;
            }
        });
    }

    @Override
    public void onPause() {
        Log.i("F_Position", "onPause");
        run = false;
        try {
            if (thread != null)
                thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        thread = null;
        // ontouch = false;
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.i("F_Position", "onResume");
        // upd vars
        // ontouch = false;
        if (!run) {
            run = true;
            thread = new Thread(this);
            thread.start();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.i("RC", "onDestroy");
        comunic.Detener_Actividad();
        super.onDestroy();
    }

    @Override
    public void run() {
        while (run) {
            if (!angHolder.getSurface().isValid()
                    || !posHolder.getSurface().isValid())
                continue;
            angCanvas = angHolder.lockCanvas();
            posCanvas = posHolder.lockCanvas();
            angWidth = angCanvas.getWidth();
            angzen = angCanvas.getHeight()/2;
            posWidth = posCanvas.getWidth();
            posHeigh = posCanvas.getHeight();
            deltaX = posWidth/2;
            deltaY = posHeigh/2;
            if(firstrun) {
                angcam = angWidth/2;
                posy = posHeigh;
                firstrun = false;
            }
            angCanvas.drawColor(Color.CYAN);
            posCanvas.drawColor(Color.GREEN);
            angCanvas.drawCircle(angcam,angzen,20, angPaint);
            posCanvas.drawCircle(posx, posy, 20, posPaint);
            angHolder.unlockCanvasAndPost(angCanvas);
            posHolder.unlockCanvasAndPost(posCanvas);
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void enviarPos(View view) {
        comunic.enviar_Int8('X');
        comunic.enviar_Float((float)posx/deltaX);
        comunic.enviar_Int8('Y');
        comunic.enviar_Float((float)posy/deltaY);
    }

    public void tomarFoto(View view) {
        comunic.enviar_Int8('L');
    }

    public void hacerconectar(View view) {
        if(comunic == null || comunic.estado != comunic.CONNECTED) {
            comunic = new Comunic(this,"10.0.2.9",2550);
            comunic.setConnectionListener(new OnConnectionListener() {

                @Override
                public void onConnectionstablished() {
//                comunic.enviar("B");
//                comunic.enviar(distancia);
//                comunic.enviar(angulo);
                }

                @Override
                public void onConnectionfinished() {
//                connect();
                }
            });
            comunic.setComunicationListener(new OnComunicationListener() {

                @Override
                public void onDataReceived(int nbytes, String dato, int[] ndat, byte[] bdat) {
//                for(int val:ndat) {
//                    if(val != 'L')
//                        comunicPIC.enviar(val);
//                    else
//                        takePic();
//                }
                }
            });
            comunic.execute();
        }
    }

    public void Desconectar(View view) {
        comunic.Detener_Actividad();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
