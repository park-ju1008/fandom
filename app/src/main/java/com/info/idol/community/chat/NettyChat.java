package com.info.idol.community.chat;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class NettyChat {
    private static final String HOST = "35.229.103.161";
    private static final int PORT = 5001;
    private SocketChannel socketChannel;
    private String data; //서버로부터 받아온 스트림
    private String message; //보내는 메시지
    private boolean isFinish=false;
    private OnDataListener mOnDataListener;

    public NettyChat(String message) {
        this.message=message;
    }

    public void setOnDataListener(OnDataListener onDataListener) {
        mOnDataListener = onDataListener;
    }

    public void startClient() {
        //서버로 접속
        new Thread() {
            @Override
            public void run() {
                try {
                    socketChannel = SocketChannel.open();
                    socketChannel.configureBlocking(true);
                    socketChannel.connect(new InetSocketAddress(HOST, PORT));

                } catch (IOException e) {
                    Log.e("CHAT", "IOException");
                    e.printStackTrace();
                }
                if (socketChannel.isOpen()) {
                    new SendmsgTask().execute(message);
                    //소켓 채널을 성공적으로 열었다면 버퍼읽기를 시작.
                    receive();
                }
            }
        }.start();
    }

    private class SendmsgTask extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... strings) {
            try {
                Log.e("SEND_MESSAGE", strings[0]);
                socketChannel
                        .socket()
                        .getOutputStream()
                        .write(strings[0].getBytes("UTF-8"));
            } catch (IOException e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(isFinish){
                closeSocketChannel();
            }
        }
    }

    private void receive() {
        final StringBuilder strBuilder = new StringBuilder();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        Charset charset = Charset.forName("UTF-8");
        while (true) {
            try {
                int readByteCount = 0;
                do {
                    readByteCount = socketChannel.read(byteBuffer); //데이터받기
                    byteBuffer.flip(); //저장한 바이트 끝으로 limit 가 이동한다.
                    strBuilder.append(charset.decode(byteBuffer).toString());
                    byteBuffer.compact();
                } while (readByteCount == 1024);

                /*
                 * read() 메소드를 호출하면 상대방이 데이터를 보내기 전까지는 블로킹
                 * 블로킹이 해제되는 경우는 아래의 세가지 경우
                 * 서버가 정상적으로 Socket의 close()를 호출했을 경우 -1
                 * 서버가 비정상적으로 종료됬을때 IOException 발생
                 * 서버가 비정상적으로 종료됬을때 IOException 발생
                 * 서버가 데이터를 보냈을때 읽은 바이트 수
                 */
                if (readByteCount == -1) {
                    throw new IOException();
                }


                data = strBuilder.toString();
                Log.e("CHAT_reeive", "msg: " + data);
                strBuilder.delete(0, strBuilder.length());
                //UI를 변경할수 있도록 핸들러로 던져줌.
//                handler.post(showUpdate);
                mOnDataListener.onUpdate(data);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public void setFinish(boolean finish) {
        isFinish = finish;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void sendMessage(String message){
        new SendmsgTask().execute(message);
    }

    public void closeSocketChannel(){
        try {
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface OnDataListener{
        void onUpdate(String loadData);
    }


}
