package com.codeevery.mail;

import android.util.Log;

public class SendEmailMain extends Thread {

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
            sms.sendTextMail(mailInfo);

        } catch (Exception e) {
            Log.e("SendMail", e.getMessage(), e);
        }
    }
}