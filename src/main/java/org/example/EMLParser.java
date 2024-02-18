package org.example;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;
import java.util.Base64;
import java.util.Objects;

public class EMLParser {
    public static void main(String[] args) {
        try {
            // Load the .eml file
            InputStream inputStream = new FileInputStream(new File("/Users/akshatsaxena/Desktop/Akka-essentials/extractEml/src/main/java/org/example/attached.eml"));
            Session session = Session.getDefaultInstance(System.getProperties(), null);
            MimeMessage message = new MimeMessage(session, inputStream);

            // Extracting headers
            String to = message.getHeader("To", null);
            String from = message.getHeader("From", null);
            String subject = message.getSubject();
            String cc = message.getHeader("Cc", null);
            String bcc = message.getHeader("Bcc", null);

            // Extracting body
            String body = getTextFromMimeMultipart((MimeMultipart) message.getContent());

            // Extracting attachments and nested messages
            StringBuilder attachmentInfo = new StringBuilder();
            Object content = message.getContent();
            if (content instanceof Multipart) {
                Multipart multipart = (Multipart) content;
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);
                    if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                        // Handling attachment
                        String filename = bodyPart.getFileName();
                        String contentType = bodyPart.getContentType();

                        if(getFileExtension(filename).equals("eml")){
                            Session session1 = Session.getDefaultInstance(System.getProperties(), null);
                            MimeMessage message1 = new MimeMessage(session1, bodyPart.getInputStream());

                            // Extracting headers
                            String to1 = message1.getHeader("To", null);
                            String from1 = message1.getHeader("From", null);
                            String subject1 = message1.getSubject();
                            System.out.println(to1);
                            System.out.println(from1);
                            System.out.println(subject1);
                            continue;
                        }

                        String contentTransferEncoding = bodyPart.getHeader("Content-Transfer-Encoding")!=null?bodyPart.getHeader("Content-Transfer-Encoding")[0]:"";

                        String attachmentContent = null;
                        if ("base64".equalsIgnoreCase(contentTransferEncoding)) {
                            InputStream is = bodyPart.getInputStream();
                            ByteArrayOutputStream os = new ByteArrayOutputStream();
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = is.read(buffer)) != -1) {
                                os.write(buffer, 0, bytesRead);
                            }
                            byte[] attachmentBytes = os.toByteArray();
                            attachmentContent = new String(Base64.getEncoder().encode(attachmentBytes));

                        } else if ("8bit".equalsIgnoreCase(contentTransferEncoding) || "7bit".equalsIgnoreCase(contentTransferEncoding)) {
                            // Read content directly
                            attachmentContent = getAttachmentContent(bodyPart);
                        }

                        attachmentInfo.append("Attachment Filename: ").append(filename).append("\n")
                                .append("Content-Type: ").append(contentType).append("\n")
                                .append("Content-Transfer-Encoding: ").append(contentTransferEncoding).append("\n")
                                .append("Attachment Content: ").append(attachmentContent).append("\n\n");

                        System.out.println("Attachment Filename: "+filename);
                        System.out.println("Content-Type: "+contentType);
                        System.out.println("Content-Transfer-Encoding: "+contentTransferEncoding);
                        assert attachmentContent != null;
//                        System.out.println("Attachment Content: "+attachmentContent.substring(0,10));

                    }
                }
            }

            // Print or use the extracted data as needed
            System.out.println("To: " + to);
            System.out.println("From: " + from);
            System.out.println("Subject: " + subject);
            System.out.println("Cc: " + cc);
            System.out.println("Bcc: " + bcc);
            System.out.println("Body: " + body);
//            System.out.println("Attachment Information:\n" + attachmentInfo.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws Exception {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.getContent());
            } else if (bodyPart.isMimeType("text/html")) {
                result.append(org.jsoup.Jsoup.parse((String) bodyPart.getContent()).text());
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }

    private static String getAttachmentContent(BodyPart bodyPart) throws Exception {
        InputStream is = bodyPart.getInputStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        return os.toString();
    }
    private static String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex != -1) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }
}
