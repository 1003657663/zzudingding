package com.codeevery.mail;

<<<<<<< HEAD
import android.os.Handler;
=======
>>>>>>> f0ab609734f33157649664b1a6507e1068abdb6a
import android.util.Log;

public class SendEmailMain extends Thread {

<<<<<<< HEAD
    private String serveHost = "smtp.qq.com";
    private String mailServerPort = "25";
    private String userName = "1003657663";
    private String password = "19930926sch";
    private String fromAddress = "1003657663@qq.com";
    private String toAddress = "1003657663@qq.com";
    private String subject = "";//主题
    private String content = "";//内容
    private String xuehao = "";
    private Handler handler = null;

    public SendEmailMain(String subject,String content,String xuehao,Handler handler){
        this.subject = subject;
        this.content = content;
        this.xuehao = xuehao;
        this.handler = handler;
=======
    String serveHost = "smtp.qq.com";
    String mailServerPort = "25";
    String userName = "1003657663";
    String password = "19930926sch";
    String fromAddress = "1003657663@qq.com";
    String toAddress = "1003657663@qq.com";
    String subject = "";//主题
    String content = "";//内容
    String xuehao = "";

    public SendEmailMain(String subject,String content,String xuehao){
        this.subject = subject;
        this.content = content;
        this.xuehao = xuehao;
>>>>>>> f0ab609734f33157649664b1a6507e1068abdb6a
    }

    @Override
    public void run() {
        super.run();
        send();
    }

    private void send() {
        
        /*
        send.setText("Send Mail");
        userid.setText("XXX@vip.qq.com");  
        password.setText("XXXX");          
        from.setText("XXX@vip.qq.com"); 
        to.setText("XXX@vip.qq.com");  
        
        subject.setText("...");
        body.setText("...");*/

        try {
            MailSenderInfo mailInfo = new MailSenderInfo();
            mailInfo.setMailServerHost(serveHost);
            mailInfo.setMailServerPort(mailServerPort);
            mailInfo.setValidate(true);
            mailInfo.setUserName(userName);
            mailInfo.setPassword(password);
            mailInfo.setFromAddress(fromAddress);
            mailInfo.setToAddress(toAddress);
            mailInfo.setSubject(subject);
            mailInfo.setContent(content+"\n发送者学号:"+xuehao);

            SimpleMailSender sms = new SimpleMailSender();
<<<<<<< HEAD
            if(sms.sendTextMail(mailInfo)){
                handler.sendEmptyMessage(1);
            }else{
                handler.sendEmptyMessage(-1);
            }
=======
            sms.sendTextMail(mailInfo);
>>>>>>> f0ab609734f33157649664b1a6507e1068abdb6a

        } catch (Exception e) {
            Log.e("SendMail", e.getMessage(), e);
        }
    }
}