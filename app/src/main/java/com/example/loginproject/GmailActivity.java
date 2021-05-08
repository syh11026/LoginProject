package com.example.loginproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;


public class GmailActivity extends AppCompatActivity {

    TextView textView;

    Button Button;

    MainHandler mainHandler;


    EditText emailText;

    //인증코드
    String GmailCode;


    //인증번호 입력하는 곳
    EditText emailCodeText;

    Button emailCodeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView= findViewById(R.id.textView);

        Button = findViewById(R.id.Button);


        //이메일 입력하는 뷰
        emailText= findViewById(R.id.emailText);


        //인증번호 받는 부분은 GONE으로 안보이게 숨긴다
        emailCodeText= findViewById(R.id.emailCodeText);
        emailCodeText.setVisibility(View.GONE);

        emailCodeButton= findViewById(R.id.emailCodeButton);
        emailCodeButton.setVisibility(View.GONE);

        //이메일 인증 하는 부분
        //인증코드 시간초가 흐르는데 이때 인증을 마치지 못하면 인증 코드를 지우게 만든다.
        Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                이메일 인증부분을 보여준다.

                try {
                    Toast.makeText(getApplicationContext(), "이메일을 전송합니다. 잠시 기다려주세요.", Toast.LENGTH_SHORT).show();

                    GmailSender gMailSender = new GmailSender("계정이름@gmail.com", "2차 비밀번호");
                    //GMailSender.sendMail(제목, 본문내용, 받는사람);

                    //인증코드
                    GmailCode=gMailSender.getEmailCode();

                    gMailSender.sendMail("보동보동 회원가입 이메일 인증", GmailCode , emailText.getText().toString());

                    Toast.makeText(getApplicationContext(), "이메일을 성공적으로 보냈습니다.", Toast.LENGTH_SHORT).show();


                    //쓰레드 객체 생성
                    BackgrounThread backgroundThread = new BackgrounThread();
                    //쓰레드 스타트
                    backgroundThread.start();


                    //이메일이 보내지면 이 부분을 실행시킨다.
                    emailCodeText.setVisibility(View.VISIBLE);
                    emailCodeButton.setVisibility(View.VISIBLE);

                } catch (SendFailedException e) {
                    Toast.makeText(getApplicationContext(), "이메일 형식이 잘못되었습니다.", Toast.LENGTH_SHORT).show();
                } catch (MessagingException e) {
                    System.out.println("인터넷 문제"+e);
                    Toast.makeText(getApplicationContext(), "인터넷 연결을 확인해주십시오", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        //인증하는 버튼이다
        //혹시 이거랑 같으면 인증을 성공시켜라라
        emailCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setVisibility(View.INVISIBLE);
                //이메일로 전송한 인증코드와 내가 입력한 인증코드가 같을 때
                if(emailCodeText.getText().toString().equals(GmailCode)){
                    Toast.makeText(getApplicationContext(), "인증 되었습니다", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "인증번호를 다시 입력해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });


        //핸들러 객체 생성
        mainHandler=new MainHandler();

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .permitDiskReads()
                .permitDiskWrites()
                .permitNetwork().build());


    }

    //시간초가 카운트 되는 쓰레드
    class BackgrounThread extends Thread{
        //180초는 3분
        //메인 쓰레드에 value를 전달하여 시간초가 카운트다운 되게 한다.
        int value = 180;
        public void run(){
            while(true){
                value-=1;
                try{
                    Thread.sleep(1000);
                }catch (Exception e){

                }

                Message message = mainHandler.obtainMessage();
                //메세지는 번들의 객체 담아서 메인 핸들러에 전달한다.
                Bundle bundle = new Bundle();
                bundle.putInt("value", value);
                message.setData(bundle);

                //핸들러에 메세지 객체 보내기기
                mainHandler.sendMessage(message);

                if(value<=0){
                    GmailCode="";
                    break;
                }
            }
        }
    }

    //쓰레드로부터 메시지를 받아 처리하는 핸들러
    //메인에서 생성된 핸들러만이 Ui를 컨트롤 할 수 있다.
    class MainHandler extends Handler{
        @Override
        public void handleMessage(Message message){
            super.handleMessage(message);
            int min, sec;

            Bundle bundle = message.getData();
            int value = bundle.getInt("value");

            min = value/60;
            value = value % 60;
            //텍스트뷰에 시간초가 카운팅
            textView.setText(min+" : "+value);
            emailCodeText.setHint(min+" : "+value);
        }
    }

}
