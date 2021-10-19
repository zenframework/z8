package org.zenframework.z8.server.utils;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.zenframework.z8.server.config.ServerConfig;

public class Email {
	private static final String DZ_EMAIL = ServerConfig.emailLogin();
	private static final String DZ_PASSWORD = ServerConfig.emailPassword();
	private static final String DZ_NAME = "Doczilla Pro";
	private static final String CONTENT_TYPE = "text/html; charset=UTF-8";
	
	public static enum TYPE {
		Registration, RemindPassword, PasswordChanged,
		// >>>>>>>>>>>>>>>>>>>>> not in use
		VerificationSuccess;
		// <<<<<<<<<<<<<<<<<<<<< not in use
	}
	
	private static enum STRINGS {
		RegistrationSubjectText("Регистрация"),
		RegistrationMessage0Text(p("Вы успешно зарегистрировались на платформе Doczilla Pro. Теперь вы сможете создавать документы для внешнеэкономической деятельности в несколько кликов и без ошибок!") + p() + p("Начните работу с конструктором документов и откройте для себя мир цифровой трансформации! Чтобы вам было проще, вы можете посмотреть обучающие ролики <a href=\"https://doczilla.pro/ru/academy/\" target=\"_blank\">Doczilla Academy</a>.")),
		RegistrationMessage1Text(p("Если вы хотите создавать собственные интерактивные шаблоны, мы с удовольствием проведем вам демо полного функционала платформы Doczilla Pro. Для заказа демо напишите пожалуйста на info@doczilla.pro.") + p() + p("Если у вас есть вопросы, мы всегда готовы ответить на них по почте support@doczilla.pro.")),
		RegistrationButtonText("НАЧАТЬ РАБОТУ"),
		RemindPasswordSubjectText("Смена пароля"),
		RemindPasswordMessage0Text(p("Здравствуйте, вы запросили смену пароля. Чтобы придумать новый пароль для вашей учетной записи/аккаунта/личного кабинета на сайте, нажмите на кнопку “Сменить пароль” ниже. Если вы не запрашивали смену пароля, проигнорируйте это письмо.")),
		RemindPasswordMessage1Text(p("Если у вас не получается сменить пароль или остались другие вопросы, мы готовы ответить на них по почте support@doczilla.pro.")),
		RemindPasswordButtonText("СМЕНИТЬ ПАРОЛЬ"),
		PasswordChangedSubjectText("Пароль был изменен"),
		PasswordChangedMessage0Text(p("Пароль от Вашего аккаунта в Doczilla Pro успешно изменен.")),
		PasswordChangedMessage1Text(p("Если Вы ничего не меняли, пожалуйста, обратитесь в техническую поддержку.")),
		// >>>>>>>>>>>>>>>>>>>>> not in use
		VerificationSuccessSubjectText("Аккаунт подтвержден"),
		VerificationSuccessMessageText("Поздравляем! Ваш аккаунт в Doczilla Pro успешно подтверждён!");
		// <<<<<<<<<<<<<<<<<<<<< not in use
		
		STRINGS(String str) {
			this.str = str;
		}
		
		private String str;
		
		private String get() {
			return this.str;
		}
		
		/* Styled message paragraph */
		private static String p(String p) {
			return "<p style=\"font-size: 14px; line-height: 160%; color: #000000;\"><span style=\"font-size: 18px; line-height: 28.8px;\">" + (p == null || p.isEmpty() ? "&nbsp;" : p) + "</span></p>";
		}
		
		/* Empty paragraph used as visual delimiter between two paragraphs */
		private static String p() {
			return "<p style=\"font-size: 14px; line-height: 160%; color: #000000;\"><span style=\"font-size: 18px; line-height: 28.8px;\">&nbsp;</span></p>";
		}
		
	}
	
	private static Authenticator auth() {
		return new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
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
	
	public static void send(Message message) {
		new Thread(() -> {
			try {
				Transport.send(message.toMimeMessage());
			} catch(UnsupportedEncodingException|MessagingException e) {
				e.printStackTrace(System.out);
			}
		}).start();
	}
	
	/**
	 * Class for concrete definition of the required data and appearance of email messages.
	 */
	public static class Message {
		
		private String recipientAddress;
		private String recipientName;
		private String buttonLink = null;
		private TYPE type;
		
		public Message(TYPE type) {
			this.type = type;
		}
		
		public Message setRecipientAddress(String recipientAddress) {
			this.recipientAddress = recipientAddress;
			return this;
		}
		
		private String getRecipientAddress() {
			return recipientAddress;
		}
		
		public Message setRecipientName(String recipientName) {
			this.recipientName = recipientName;
			return this;
		}
		
		public Message setType(TYPE type) {
			this.type = type;
			return this;
		}
		
		public Message setButtonLink(String link) {
			this.buttonLink = link;
			return this;
		}
		
		private String getMessageSubject() {
			switch (type) {
			case Registration:
				return STRINGS.RegistrationSubjectText.get();
			case RemindPassword:
				return STRINGS.RemindPasswordSubjectText.get();
			case PasswordChanged:
				return STRINGS.PasswordChangedSubjectText.get();
			// >>>>>>>>>>>>>>>>>>>>> not in use
			case VerificationSuccess:
				return STRINGS.VerificationSuccessSubjectText.get();
			// <<<<<<<<<<<<<<<<<<<<< not in use
			}
			return "Notification";
		}
		
		private String getMessageContent() {
			return new StringBuilder()
					.append(start())
					.append(head())
					.append(body(recipientName, buttonLink))
					.append(end()).toString();
		}
		
		private MimeMessage toMimeMessage() throws UnsupportedEncodingException, MessagingException {
			InternetAddress from = new InternetAddress(DZ_EMAIL, DZ_NAME);
			InternetAddress to = new InternetAddress(getRecipientAddress());
			
			MimeMessage mimeMessage = new MimeMessage(Session.getInstance(props(), auth()));
			mimeMessage.setFrom(from);
			mimeMessage.setRecipient(javax.mail.Message.RecipientType.TO, to);
			mimeMessage.setSubject(getMessageSubject());
			mimeMessage.setContent(getMessageContent(), CONTENT_TYPE);
			return mimeMessage;
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
		
		private String body(String name, String buttonLink) {
			switch (type) {
			case Registration:
				return body(name, STRINGS.RegistrationMessage0Text.get(), buttonLink, STRINGS.RegistrationButtonText.get(), STRINGS.RegistrationMessage1Text.get());
			case RemindPassword:
				return body(name, STRINGS.RemindPasswordMessage0Text.get(), buttonLink, STRINGS.RemindPasswordButtonText.get(), STRINGS.RemindPasswordMessage1Text.get());
			case PasswordChanged:
				return body(name, STRINGS.PasswordChangedMessage0Text.get(), null, null, STRINGS.PasswordChangedMessage1Text.get());
			// >>>>>>>>>>>>>>>>>>>>> not in use
			case VerificationSuccess:
				return body(name, STRINGS.VerificationSuccessMessageText.get(), null, null, null);
			// <<<<<<<<<<<<<<<<<<<<< not in use
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
					+ "      <img align=\"center\" border=\"0\" src=\"https://doczilla.pro/materials/DzPro_color_logo.png\" alt=\"DoczillaPro\" title=\"DoczillaPro\" style=\"outline: none;text-decoration: none;-ms-interpolation-mode: bicubic;clear: both;display: inline-block !important;border: none;height: auto;float: none;width: 32%;max-width: 179.2px;\" width=\"179.2\"/>"
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
					+ "    <p style=\"font-size: 14px; line-height: 160%;\"><span style=\"font-size: 22px; line-height: 35.2px;\">" + (type == TYPE.Registration ? "Поздравляем" : "Здравствуйте") + ", " + name + "!</span></p>"
					+ message0
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
								+ message1
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
					+ "<p style=\"line-height: 160%; font-size: 14px;\"><span style=\"font-size: 18px; line-height: 28.8px;\">Команда Doczilla Pro</span></p>"
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
					+ "			<img src=\"https://doczilla.pro/materials/facebook.png\" alt=\"Facebook\" title=\"Facebook\" width=\"32\" style=\"outline: none;text-decoration: none;-ms-interpolation-mode: bicubic;clear: both;display: block !important;border: none;height: auto;float: none;max-width: 32px !important\">"
					+ "		</a>"
					+ "      </td></tr>"
					+ "    </tbody></table>"
					+ "    <!--[if (mso)|(IE)]></td><![endif]-->"
					+ "    "
					+ "    <!--[if (mso)|(IE)]><td width=\"32\" style=\"width:32px; padding-right: 17px;\" valign=\"top\"><![endif]-->"
					+ "    <table align=\"left\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"32\" height=\"32\" style=\"border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;margin-right: 17px\">"
					+ "      <tbody><tr style=\"vertical-align: top\"><td align=\"left\" valign=\"middle\" style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top\">"
					+ "        <a href=\"https://www.youtube.com/channel/UCUnnEeUX1CIF99Pelw9PMdw\" title=\"YouTube\" target=\"_blank\">"
					+ "			<img src=\"https://doczilla.pro/materials/youtube.png\" alt=\"Youtube\" title=\"Youtube\" width=\"32\" style=\"outline: none;text-decoration: none;-ms-interpolation-mode: bicubic;clear: both;display: block !important;border: none;height: auto;float: none;max-width: 32px !important\">"
					+ "		</a>"
					+ "      </td></tr>"
					+ "    </tbody></table>"
					+ "    <!--[if (mso)|(IE)]></td><![endif]-->"
					+ "	"
					+ "	<!--[if (mso)|(IE)]><td width=\"32\" style=\"width:32px; padding-right: 17px;\" valign=\"top\"><![endif]-->"
					+ "    <table align=\"left\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"32\" height=\"32\" style=\"border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;margin-right: 17px\">"
					+ "      <tbody><tr style=\"vertical-align: top\"><td align=\"left\" valign=\"middle\" style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top\">"
					+ "        <a href=\"https://www.linkedin.com/company/19013505\" title=\"LinkedIn\" target=\"_blank\">"
					+ "			<img src=\"https://doczilla.pro/materials/linkedin.png\" alt=\"LinkedIn\" title=\"LinkedIn\" width=\"32\" style=\"outline: none;text-decoration: none;-ms-interpolation-mode: bicubic;clear: both;display: block !important;border: none;height: auto;float: none;max-width: 32px !important\">"
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