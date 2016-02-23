package com.dash.mxdevicespy;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.dash.mxdevicespy.R;

import android.content.Context;
import android.media.MediaPlayer;

public class Server implements Runnable{
	int port;
	Context ctx;
   public Server(int port, Context context) throws IOException
   {
      this.port = port;
      this.ctx = context;
   }

   public void run()
   {
      while(true)
      {
    	try{  
    		  ServerSocket ss=new ServerSocket(this.port);  
    		  Socket s=ss.accept();//establishes connection   
    		  DataInputStream dis=new DataInputStream(s.getInputStream());  
    		  String  str=(String)dis.readUTF();  
    		  if(str.equals("PLAY")){
    			  MediaPlayer mp = MediaPlayer.create(this.ctx, R.raw.sound_one);
    			  mp.start();
    		  }
    		  System.out.println("$$$message="+str);  
    		  ss.close();  
    	}catch(Exception e){
    		System.out.println(e);}  
    	}  
   }
}
