package org.zenframework.z8.server.utils;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Email {
	private static final String DZ_EMAIL = "";
	private static final String DZ_PASSWORD = "";
	private static final String CONTENT_TYPE = "text/html; charset=UTF-8";
	
	private static enum STRINGS {
		RegistrationSubjectText("Регистрация"),
		RegistrationMessageText("Добро пожаловать в Doczilla Pro! Для подтверждения Вашего адреса электронной почты, пожалуйста, нажмите на кнопку ниже."),
		RegistrationButtonText("ПОДТВЕРДИТЬ РЕГИСТРАЦИЮ"),
		RemindPasswordSubjectText("Смена пароля"),
		RemindPasswordMessage0Text("Вы отправили запрос на восстановление доступа к аккаунту в Doczilla Pro. Для продолжения, пожалуйста, нажмите кнопку ниже."),
		RemindPasswordMessage1Text("Если это письмо попало к Вам по ошибке, пожалуйста, проигнорируйте его и обратитесь к Вашему системному администратору."),
		RemindPasswordButtonText("СБРОСИТЬ ПАРОЛЬ"),
		VerificationSuccessSubjectText("Аккаунт подтвержден"),
		VerificationSuccessMessageText("Поздравляем! Ваш аккаунт в Doczilla Pro успешно подтверждён!"),
		PasswordChangedSubjectText("Пароль был изменен"),
		PasswordChangedMessage0Text("Пароль от Вашего аккаунта в Doczilla Pro успешно изменен."),
		PasswordChangedMessage1Text("Если Вы ничего не меняли, пожалуйста, обратитесь к Вашему системному администратору.");
		
		STRINGS(String str) {
			this.str = str;
		}
		
		private String str;
		
		private String get() {
			return this.str;
		}
		
	}
	
	private static Authenticator auth() {
		return new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication()
			{
				return new PasswordAuthentication(DZ_EMAIL, DZ_PASSWORD);
			}
		};
	}
	
	private static Properties props() {
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
		props.put("mail.smtp.ssl.protocols", "TLSv1.2");
		return props;
	}
	
	private static MimeMessage getMessage(InternetAddress from, InternetAddress to, TYPE type, String name, String verification) throws MessagingException {
		MimeMessage message = new MimeMessage(Session.getInstance(props(), auth()));
		message.setFrom(from);
		message.setRecipient(Message.RecipientType.TO, to);
		message.setSubject(type.getMessageSubject());
		message.setContent(type.getMessageContent(name, verification), CONTENT_TYPE);
		return message;
	}
	
	public static void send(String recipient, TYPE type, String name, String verification) {
		new Thread(() -> {
			try {
				InternetAddress from = new InternetAddress(DZ_EMAIL, "Doczilla Pro");
				InternetAddress to = new InternetAddress(recipient);
				
				Transport.send(getMessage(from, to, type, name, verification));
			} catch(UnsupportedEncodingException|MessagingException e) {
				e.printStackTrace(System.out);
			}
		}).start();
	}
	
	public static enum TYPE {
		Registration, VerificationSuccess, RemindPassword, PasswordChanged;
		
		private String getMessageSubject() {
			switch (this) {
			case Registration:
				return STRINGS.RegistrationSubjectText.get();
			case RemindPassword:
				return STRINGS.RemindPasswordSubjectText.get();
			case VerificationSuccess:
				return STRINGS.VerificationSuccessSubjectText.get();
			case PasswordChanged:
				return STRINGS.PasswordChangedSubjectText.get();
			}
			return "Notification";
		}
		
		private String getMessageContent(String name, String verification) {
			return start()+head()+body(name, verification)+end();
		}
		
		private String start() {
			return "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional //EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
					+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\">";
		}
		
		private String style() {
			return "<style type=\"text/css\">"
					+ "table, td { color: #000000; } a { color: #0000ee; text-decoration: underline; }"
					+ "@media only screen and (min-width: 620px) {"
					+ "  .u-row {"
					+ "    width: 600px !important;"
					+ "  }"
					+ "  .u-row .u-col {"
					+ "    vertical-align: top;"
					+ "  }"
					+ "  .u-row .u-col-100 {"
					+ "    width: 600px !important;"
					+ "  }"
					+ "}"
					+ "@media (max-width: 620px) {"
					+ "  .u-row-container {"
					+ "    max-width: 100% !important;"
					+ "    padding-left: 0px !important;"
					+ "    padding-right: 0px !important;"
					+ "  }"
					+ "  .u-row .u-col {"
					+ "    min-width: 320px !important;"
					+ "    max-width: 100% !important;"
					+ "    display: block !important;"
					+ "  }"
					+ "  .u-row {"
					+ "    width: calc(100% - 40px) !important;"
					+ "  }"
					+ "  .u-col {"
					+ "    width: 100% !important;"
					+ "  }"
					+ "  .u-col > div {"
					+ "    margin: 0 auto;"
					+ "  }"
					+ "}"
					+ "body {"
					+ "  margin: 0;"
					+ "  padding: 0;"
					+ "}"
					+ "table,"
					+ "tr,"
					+ "td {"
					+ "  vertical-align: top;"
					+ "  border-collapse: collapse;"
					+ "}"
					+ "p {"
					+ "  margin: 0;"
					+ "}"
					+ ".ie-container table,"
					+ ".mso-container table {"
					+ "  table-layout: fixed;"
					+ "}"
					+ "* {"
					+ "  line-height: inherit;"
					+ "}"
					+ "a[x-apple-data-detectors='true'] {"
					+ "  color: inherit !important;"
					+ "  text-decoration: none !important;"
					+ "}"
					+ "</style>"
					+ "<!--[if !mso]><!--><link href=\"https://fonts.googleapis.com/css2?family=Montserrat&display=swap\" rel=\"stylesheet\" type=\"text/css\"><!--<![endif]-->";
		}
		
		private String head() {
			return "<head>"
					+ "<!--[if gte mso 9]>"
					+ "<xml>"
					+ "  <o:OfficeDocumentSettings>"
					+ "    <o:AllowPNG/>"
					+ "    <o:PixelsPerInch>96</o:PixelsPerInch>"
					+ "  </o:OfficeDocumentSettings>"
					+ "</xml>"
					+ "<![endif]-->"
					+ "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">"
					+ "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
					+ "  <meta name=\"x-apple-disable-message-reformatting\">"
					+ "  <!--[if !mso]><!--><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"><!--<![endif]-->"
					+ "  <title></title>"
					+ style()
					+ "</head>";
		}
		
		private String end() {
			return "</html>";
		}
		
		private String body(String name, String verification) {
			switch (this) {
			case Registration:
				return body(name, STRINGS.RegistrationMessageText.get(), "http://localhost:9080/#verify_"+verification, STRINGS.RegistrationButtonText.get(), null);
			case RemindPassword:
				return body(name, STRINGS.RemindPasswordMessage0Text.get(), "http://localhost:9080/#remind_"+verification, STRINGS.RemindPasswordButtonText.get(), STRINGS.RemindPasswordMessage1Text.get());
			case VerificationSuccess:
				return body(name, STRINGS.VerificationSuccessMessageText.get(), null, null, null);
			case PasswordChanged:
				return body(name, STRINGS.PasswordChangedMessage0Text.get(), null, null, STRINGS.PasswordChangedMessage1Text.get());
			}
			return "<body>Notification</body>";
		}
		
		private String body(String name, String message0, String buttonLink, String buttonText, String message1) {
			String body =  "<body class=\"clean-body u_body\" style=\"margin: 0;padding: 0;-webkit-text-size-adjust: 100%;background-color: #f9f9f9;color: #000000\">"
					+ "  <!--[if IE]><div class=\"ie-container\"><![endif]-->"
					+ "  <!--[if mso]><div class=\"mso-container\"><![endif]-->"
					+ "  <table style=\"border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;min-width: 320px;Margin: 0 auto;background-color: #f9f9f9;width:100%\" cellpadding=\"0\" cellspacing=\"0\">"
					+ "  <tbody>"
					+ "  <tr style=\"vertical-align: top\">"
					+ "    <td style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top\">"
					+ "    <!--[if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td align=\"center\" style=\"background-color: #f9f9f9;\"><![endif]-->"
					+ "<div class=\"u-row-container\" style=\"padding: 0px;background-color: transparent\">"
					+ "  <div class=\"u-row\" style=\"Margin: 0 auto;min-width: 320px;max-width: 600px;overflow-wrap: break-word;word-wrap: break-word;word-break: break-word;background-color: #ffffff;\">"
					+ "    <div style=\"border-collapse: collapse;display: table;width: 100%;background-color: transparent;\">"
					+ "      <!--[if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"padding: 0px;background-color: transparent;\" align=\"center\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:600px;\"><tr style=\"background-color: #ffffff;\"><![endif]-->"
					+ "      "
					+ "<!--[if (mso)|(IE)]><td align=\"center\" width=\"600\" style=\"width: 600px;padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;\" valign=\"top\"><![endif]-->"
					+ "<div class=\"u-col u-col-100\" style=\"max-width: 320px;min-width: 600px;display: table-cell;vertical-align: top;\">"
					+ "  <div style=\"width: 100% !important;\">"
					+ "  <!--[if (!mso)&(!IE)]><!--><div style=\"padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;\"><!--<![endif]-->"
					+ "  "
					+ "<table style=\"font-family:'Montserrat',sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">"
					+ "  <tbody>"
					+ "    <tr>"
					+ "      <td style=\"overflow-wrap:break-word;word-break:break-word;padding:20px;font-family:'Montserrat',sans-serif;\" align=\"left\">"
					+ "        "
					+ "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">"
					+ "  <tr>"
					+ "    <td style=\"padding-right: 0px;padding-left: 0px;\" align=\"center\">"
					+ "      "
					+ "      <img align=\"center\" border=\"0\" src=\"https://static.tildacdn.com/tild3932-6238-4366-b565-633362383361/Frame_8.svg\" alt=\"DoczillaPro\" title=\"DoczillaPro\" style=\"outline: none;text-decoration: none;-ms-interpolation-mode: bicubic;clear: both;display: inline-block !important;border: none;height: auto;float: none;width: 32%;max-width: 179.2px;\" width=\"179.2\"/>"
					+ "      "
					+ "    </td>"
					+ "  </tr>"
					+ "</table>"
					+ "      </td>"
					+ "    </tr>"
					+ "  </tbody>"
					+ "</table>"
					+ "  <!--[if (!mso)&(!IE)]><!--></div><!--<![endif]-->"
					+ "  </div>"
					+ "</div>"
					+ "<!--[if (mso)|(IE)]></td><![endif]-->"
					+ "      <!--[if (mso)|(IE)]></tr></table></td></tr></table><![endif]-->"
					+ "    </div>"
					+ "  </div>"
					+ "</div>"
					+ "<div class=\"u-row-container\" style=\"padding: 0px;background-color: transparent\">"
					+ "  <div class=\"u-row\" style=\"Margin: 0 auto;min-width: 320px;max-width: 600px;overflow-wrap: break-word;word-wrap: break-word;word-break: break-word;background-color: #ffffff;\">"
					+ "    <div style=\"border-collapse: collapse;display: table;width: 100%;background-color: transparent;\">"
					+ "      <!--[if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"padding: 0px;background-color: transparent;\" align=\"center\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:600px;\"><tr style=\"background-color: #ffffff;\"><![endif]-->"
					+ "      "
					+ "<!--[if (mso)|(IE)]><td align=\"center\" width=\"600\" style=\"width: 600px;padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;\" valign=\"top\"><![endif]-->"
					+ "<div class=\"u-col u-col-100\" style=\"max-width: 320px;min-width: 600px;display: table-cell;vertical-align: top;\">"
					+ "  <div style=\"width: 100% !important;\">"
					+ "  <!--[if (!mso)&(!IE)]><!--><div style=\"padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;\"><!--<![endif]-->"
					+ "  "
					+ "<table style=\"font-family:'Montserrat',sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">"
					+ "  <tbody>"
					+ "    <tr>"
					+ "      <td style=\"overflow-wrap:break-word;word-break:break-word;padding:33px 55px;font-family:'Montserrat',sans-serif;\" align=\"left\">"
					+ "        "
					+ "  <div style=\"line-height: 160%; text-align: center; word-wrap: break-word;\">"
					+ "    <p style=\"font-size: 14px; line-height: 160%;\"><span style=\"font-size: 22px; line-height: 35.2px;\">Здравствуйте, " + name + "!</span></p>"
					+ "<p style=\"font-size: 14px; line-height: 160%;\"><span style=\"font-size: 18px; line-height: 28.8px;\">" + message0 + "</span></p>"
					+ "  </div>"
					+ "      </td>"
					+ "    </tr>"
					+ "  </tbody>"
					+ "</table>";
					
					/* If link is not set (or text of button), do not render confirmation button */
					if (buttonLink != null && !buttonLink.isEmpty() && buttonText != null && !buttonText.isEmpty()) {
						body += "<table style=\"font-family:'Montserrat',sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">"
								+ "  <tbody>"
								+ "    <tr>"
								+ "      <td style=\"overflow-wrap:break-word;word-break:break-word;padding:10px;font-family:'Montserrat',sans-serif;\" align=\"left\">"
								+ "        "
								+ "<div align=\"center\">"
								+ "  <!--[if mso]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-spacing: 0; border-collapse: collapse; mso-table-lspace:0pt; mso-table-rspace:0pt;font-family:'Montserrat',sans-serif;\"><tr><td style=\"font-family:'Montserrat',sans-serif;\" align=\"center\"><v:roundrect xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:w=\"urn:schemas-microsoft-com:office:word\" href=\"\" style=\"height:46px; v-text-anchor:middle; width:234px;\" arcsize=\"8.5%\" stroke=\"f\" fillcolor=\"#ff6f93\"><w:anchorlock/><center style=\"color:#FFFFFF;font-family:'Montserrat',sans-serif;\"><![endif]-->"
								+ "    <a href=\""+ buttonLink +"\" target=\"_blank\" style=\"box-sizing: border-box;display: inline-block;font-family: 'Montserrat',sans-serif;text-decoration: none;-webkit-text-size-adjust: none;text-align: center;color: #FFFFFF;background-color: #ff6f93;border-radius: 4px;-webkit-border-radius: 4px;-moz-border-radius: 4px;width: auto;max-width: 100%;overflow-wrap: break-word;word-break: break-word;word-wrap: break-word;mso-border-alt: none;\">"
								+ "      <span style=\"display:block;padding:14px 44px 13px;line-height:120%;\"><span style=\"font-size: 16px; line-height: 19.2px;\"><strong><span style=\"line-height: 19.2px; font-size: 16px;\">" + buttonText + "</span></strong></span></span>"
								+ "    </a>"
								+ "  <!--[if mso]></center></v:roundrect></td></tr></table><![endif]-->"
								+ "</div>"
								+ "      </td>"
								+ "    </tr>"
								+ "  </tbody>"
								+ "</table>";
					}
					
					/* Text after confirmation button */
					if (message1 != null && !message1.isEmpty()) {
						body += "<table style=\"font-family:'Montserrat',sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">"
								+ "  <tbody>"
								+ "    <tr>"
								+ "      <td style=\"overflow-wrap:break-word;word-break:break-word;padding:33px 55px;font-family:'Montserrat',sans-serif;\" align=\"left\">"
								+ "        "
								+ "  <div style=\"line-height: 160%; text-align: center; word-wrap: break-word;\">"
								+ "<p style=\"font-size: 14px; line-height: 160%;\"><span style=\"font-size: 18px; line-height: 28.8px;\">" + message1 + "</span></p>"
								+ "  </div>"
								+ "      </td>"
								+ "    </tr>"
								+ "  </tbody>"
								+ "</table>";
					}
					
					body += "<table style=\"font-family:'Montserrat',sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">"
					+ "  <tbody>"
					+ "    <tr>"
					+ "      <td style=\"overflow-wrap:break-word;word-break:break-word;padding:33px 55px 60px;font-family:'Montserrat',sans-serif;\" align=\"left\">"
					+ "        "
					+ "  <div style=\"line-height: 160%; text-align: center; word-wrap: break-word;\">"
					+ "    <p style=\"line-height: 160%; font-size: 14px;\"><span style=\"font-size: 18px; line-height: 28.8px;\">С уважением,</span></p>"
					+ "<p style=\"line-height: 160%; font-size: 14px;\"><span style=\"font-size: 18px; line-height: 28.8px;\">команда Doczilla</span></p>"
					+ "  </div>"
					+ "      </td>"
					+ "    </tr>"
					+ "  </tbody>"
					+ "</table>"
					+ "  <!--[if (!mso)&(!IE)]><!--></div><!--<![endif]-->"
					+ "  </div>"
					+ "</div>"
					+ "<!--[if (mso)|(IE)]></td><![endif]-->"
					+ "      <!--[if (mso)|(IE)]></tr></table></td></tr></table><![endif]-->"
					+ "    </div>"
					+ "  </div>"
					+ "</div>"
					+ "<div class=\"u-row-container\" style=\"padding: 0px;background-color: transparent\">"
					+ "  <div class=\"u-row\" style=\"Margin: 0 auto;min-width: 320px;max-width: 600px;overflow-wrap: break-word;word-wrap: break-word;word-break: break-word;background-color: #ffeaef;\">"
					+ "    <div style=\"border-collapse: collapse;display: table;width: 100%;background-color: transparent;\">"
					+ "      <!--[if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"padding: 0px;background-color: transparent;\" align=\"center\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:600px;\"><tr style=\"background-color: #ffeaef;\"><![endif]-->"
					+ "      "
					+ "<!--[if (mso)|(IE)]><td align=\"center\" width=\"600\" style=\"width: 600px;padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;\" valign=\"top\"><![endif]-->"
					+ "<div class=\"u-col u-col-100\" style=\"max-width: 320px;min-width: 600px;display: table-cell;vertical-align: top;\">"
					+ "  <div style=\"width: 100% !important;\">"
					+ "  <!--[if (!mso)&(!IE)]><!--><div style=\"padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;\"><!--<![endif]-->"
					+ "  "
					+ "<table style=\"font-family:'Montserrat',sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">"
					+ "  <tbody>"
					+ "    <tr>"
					+ "      <td style=\"overflow-wrap:break-word;word-break:break-word;padding:41px 55px 18px;font-family:'Montserrat',sans-serif;\" align=\"left\">"
					+ "        "
					+ "  <div style=\"color: #ff6f93; line-height: 160%; text-align: center; word-wrap: break-word;\">"
					+ "<p style=\"font-size: 14px; line-height: 160%;\"><span style=\"font-size: 16px; line-height: 25.6px; color: #000000;\">8 (800) 700-08-16</span></p>"
					+ "<p style=\"font-size: 14px; line-height: 160%;\"><span style=\"font-size: 16px; line-height: 25.6px; color: #000000;\">info@doczilla.pro</span></p>"
					+ "  </div>"
					+ "      </td>"
					+ "    </tr>"
					+ "  </tbody>"
					+ "</table>"
					+ "<table style=\"font-family:'Montserrat',sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">"
					+ "  <tbody>"
					+ "    <tr>"
					+ "      <td style=\"overflow-wrap:break-word;word-break:break-word;padding:10px 10px 33px;font-family:'Montserrat',sans-serif;\" align=\"left\">"
					+ "        "
					+ "<div align=\"center\">"
					+ "  <div style=\"display: table; max-width:244px;\">"
					+ "  <!--[if (mso)|(IE)]><table width=\"244\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"border-collapse:collapse;\" align=\"center\"><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse; mso-table-lspace: 0pt;mso-table-rspace: 0pt; width:244px;\"><tr><![endif]-->"
					+ "  "
					+ "    "
					+ "    <!--[if (mso)|(IE)]><td width=\"32\" style=\"width:32px; padding-right: 17px;\" valign=\"top\"><![endif]-->"
					+ "    <table align=\"left\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"32\" height=\"32\" style=\"border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;margin-right: 17px;margin-left: 17px;\">"
					+ "      <tbody><tr style=\"vertical-align: top\"><td align=\"left\" valign=\"middle\" style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top\">"
					+ "        <a href=\"https://www.facebook.com/doczilla.pro/\" title=\"Facebook\" target=\"_blank\">"
					+ "			<svg style=\"fill:#e84276;\" version=\"1.1\" id=\"Layer_1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\" width=\"30px\" height=\"30px\" viewBox=\"0 0 48 48\" enable-background=\"new 0 0 48 48\" xml:space=\"preserve\"> <desc>Facebook</desc> <path d=\"M47.761,24c0,13.121-10.638,23.76-23.758,23.76C10.877,47.76,0.239,37.121,0.239,24c0-13.124,10.638-23.76,23.764-23.76 C37.123,0.24,47.761,10.876,47.761,24 M20.033,38.85H26.2V24.01h4.163l0.539-5.242H26.2v-3.083c0-1.156,0.769-1.427,1.308-1.427 h3.318V9.168L26.258,9.15c-5.072,0-6.225,3.796-6.225,6.224v3.394H17.1v5.242h2.933V38.85z\"></path> </svg>"
					+ "		</a>"
					+ "      </td></tr>"
					+ "    </tbody></table>"
					+ "    <!--[if (mso)|(IE)]></td><![endif]-->"
					+ "    "
					+ "    <!--[if (mso)|(IE)]><td width=\"32\" style=\"width:32px; padding-right: 17px;\" valign=\"top\"><![endif]-->"
					+ "    <table align=\"left\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"32\" height=\"32\" style=\"border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;margin-right: 17px\">"
					+ "      <tbody><tr style=\"vertical-align: top\"><td align=\"left\" valign=\"middle\" style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top\">"
					+ "        <a href=\"https://www.youtube.com/channel/UCUnnEeUX1CIF99Pelw9PMdw\" title=\"YouTube\" target=\"_blank\">"
					+ "			<svg style=\"fill:#e84276;\" version=\"1.1\" id=\"Layer_1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\" width=\"30px\" height=\"30px\" viewBox=\"-455 257 48 48\" enable-background=\"new -455 257 48 48\" xml:space=\"preserve\"> <desc>Youtube</desc> <path d=\"M-431,257.013c13.248,0,23.987,10.74,23.987,23.987s-10.74,23.987-23.987,23.987s-23.987-10.74-23.987-23.987 S-444.248,257.013-431,257.013z M-419.185,275.093c-0.25-1.337-1.363-2.335-2.642-2.458c-3.054-0.196-6.119-0.355-9.178-0.357 c-3.059-0.002-6.113,0.154-9.167,0.347c-1.284,0.124-2.397,1.117-2.646,2.459c-0.284,1.933-0.426,3.885-0.426,5.836 s0.142,3.903,0.426,5.836c0.249,1.342,1.362,2.454,2.646,2.577c3.055,0.193,6.107,0.39,9.167,0.39c3.058,0,6.126-0.172,9.178-0.37 c1.279-0.124,2.392-1.269,2.642-2.606c0.286-1.93,0.429-3.879,0.429-5.828C-418.756,278.971-418.899,277.023-419.185,275.093z M-433.776,284.435v-7.115l6.627,3.558L-433.776,284.435z\"></path> </svg>"
					+ "		</a>"
					+ "      </td></tr>"
					+ "    </tbody></table>"
					+ "    <!--[if (mso)|(IE)]></td><![endif]-->"
					+ "	"
					+ "	<!--[if (mso)|(IE)]><td width=\"32\" style=\"width:32px; padding-right: 17px;\" valign=\"top\"><![endif]-->"
					+ "    <table align=\"left\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"32\" height=\"32\" style=\"border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;margin-right: 17px\">"
					+ "      <tbody><tr style=\"vertical-align: top\"><td align=\"left\" valign=\"middle\" style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top\">"
					+ "        <a href=\"https://www.linkedin.com/company/19013505\" title=\"LinkedIn\" target=\"_blank\">"
					+ "			<svg style=\"fill:#e84276;\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\" width=\"30px\" height=\"30px\" viewBox=\"-615 1477 48 48\" enable-background=\"new -615 1477 48 48\" xml:space=\"preserve\"> <desc>LinkedIn</desc> <path d=\"M-566.999,1501c0,13.256-10.746,24-24,24c-13.256,0-24.002-10.744-24.002-24c0-13.254,10.746-24,24.002-24 C-577.745,1477-566.999,1487.746-566.999,1501z M-605.506,1514.975h6.22v-20.004h-6.22V1514.975z M-602.396,1492.236 c1.987,0,3.602-1.615,3.602-3.606c0-1.99-1.615-3.605-3.602-3.605c-1.993,0-3.604,1.615-3.604,3.605 C-606,1490.621-604.389,1492.236-602.396,1492.236z M-576,1504.002c0-5.387-1.163-9.529-7.454-9.529 c-3.023,0-5.054,1.658-5.884,3.231h-0.085v-2.733h-5.964v20.004h6.216v-9.896c0-2.609,0.493-5.137,3.729-5.137 c3.186,0,3.232,2.984,3.232,5.305v9.729H-576V1504.002z\"></path> </svg>"
					+ "		</a>"
					+ "      </td></tr>"
					+ "    </tbody></table>"
					+ "    <!--[if (mso)|(IE)]></td><![endif]-->"
					+ "    "
					+ "    "
					+ "    <!--[if (mso)|(IE)]></tr></table></td></tr></table><![endif]-->"
					+ "  </div>"
					+ "</div>"
					+ "      </td>"
					+ "    </tr>"
					+ "  </tbody>"
					+ "</table>"
					+ "  <!--[if (!mso)&(!IE)]><!--></div><!--<![endif]-->"
					+ "  </div>"
					+ "</div>"
					+ "<!--[if (mso)|(IE)]></td><![endif]-->"
					+ "      <!--[if (mso)|(IE)]></tr></table></td></tr></table><![endif]-->"
					+ "    </div>"
					+ "  </div>"
					+ "</div>"
					+ "<div class=\"u-row-container\" style=\"padding: 0px;background-color: transparent\">"
					+ "  <div class=\"u-row\" style=\"Margin: 0 auto;min-width: 320px;max-width: 600px;overflow-wrap: break-word;word-wrap: break-word;word-break: break-word;background-color: #ff6f93;\">"
					+ "    <div style=\"border-collapse: collapse;display: table;width: 100%;background-color: transparent;\">"
					+ "      <!--[if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"padding: 0px;background-color: transparent;\" align=\"center\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:600px;\"><tr style=\"background-color: #ff6f93;\"><![endif]-->"
					+ "      "
					+ "<!--[if (mso)|(IE)]><td align=\"center\" width=\"600\" style=\"width: 600px;padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;\" valign=\"top\"><![endif]-->"
					+ "<div class=\"u-col u-col-100\" style=\"max-width: 320px;min-width: 600px;display: table-cell;vertical-align: top;\">"
					+ "  <div style=\"width: 100% !important;\">"
					+ "  <!--[if (!mso)&(!IE)]><!--><div style=\"padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;\"><!--<![endif]-->"
					+ "  "
					+ "<table style=\"font-family:'Montserrat',sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">"
					+ "  <tbody>"
					+ "    <tr>"
					+ "      <td style=\"overflow-wrap:break-word;word-break:break-word;padding:10px;font-family:'Montserrat',sans-serif;\" align=\"left\">"
					+ "        "
					+ "  <div style=\"color: #fafafa; line-height: 180%; text-align: center; word-wrap: break-word;\">"
					+ "    <p style=\"font-size: 14px; line-height: 180%;\"><span style=\"font-size: 16px; line-height: 28.8px;\">&copy; Doczilla 2020-2021</span></p>"
					+ "  </div>"
					+ "      </td>"
					+ "    </tr>"
					+ "  </tbody>"
					+ "</table>"
					+ "  <!--[if (!mso)&(!IE)]><!--></div><!--<![endif]-->"
					+ "  </div>"
					+ "</div>"
					+ "<!--[if (mso)|(IE)]></td><![endif]-->"
					+ "      <!--[if (mso)|(IE)]></tr></table></td></tr></table><![endif]-->"
					+ "    </div>"
					+ "  </div>"
					+ "</div>"
					+ "    <!--[if (mso)|(IE)]></td></tr></table><![endif]-->"
					+ "    </td>"
					+ "  </tr>"
					+ "  </tbody>"
					+ "  </table>"
					+ "  <!--[if mso]></div><![endif]-->"
					+ "  <!--[if IE]></div><![endif]-->"
					+ "</body>";
					
					return body;
		}
	}
}