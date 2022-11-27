import javax.sound.sampled.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Oatmeala implements Runnable{

    AudioFormat format = new AudioFormat( 8000f, 16, 1, true, false);
    TargetDataLine targetDataLine;
    SourceDataLine sourceDataLine;

    OutputStream write;
    InputStream read;

    public Oatmeala(OutputStream write, InputStream read) {
        this.write = write;
        this.read = read;
        try{
            targetDataLine = AudioSystem.getTargetDataLine(format);
            targetDataLine.open(format);
            targetDataLine.start();
            sourceDataLine = AudioSystem.getSourceDataLine(format);
            sourceDataLine.open(format);
            sourceDataLine.start();}catch (Exception e){
        }
        int len;
        byte[] bytes = new byte[1024];
        try{
            while ((len = read.read(bytes))!=-1) {
                sourceDataLine.write(bytes, 0,len);//播放声音
                bytes = new byte[1024];
            }
        }catch (Exception e){

        }
    }
    byte[] b = new byte[1024];

    public void run(){
        try {
            while(true) {
                targetDataLine.read(b, 0, b.length);//录制声音
                write.write(b);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void main(String[] args) throws Exception{
        Socket socket = new Socket(InetAddress.getLocalHost(), 8888);
        OutputStream write = socket.getOutputStream();
        InputStream read = socket.getInputStream();
        new Oatmeala(write,read);
    }
}

