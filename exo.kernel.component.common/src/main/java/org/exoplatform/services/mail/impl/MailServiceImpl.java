/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.services.mail.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import jakarta.activation.DataHandler;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;
import jakarta.mail.util.ByteArrayDataSource;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.mail.Attachment;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.mail.Message;

/**
 * Basically this is {@link MailService} implementation build on top of jakarta.mail package.
 * You may define the behaviour of the service via {@link InitParams}, which can
 * be set in configuration file of the {@link ExoContainer}.
 * <p>
 * Note: To be able to send mails you must provide active SMTP server and
 * mention it in service configuration. 
 * <p>
 * Created by The eXo Platform SAS Author : Phung Hai Nam phunghainam@gmail.com
 * Dec 23, 2005
 */
public class MailServiceImpl implements MailService
{
   /**
    * String mapping of configuration parameter to define maximal number
    * of threads for asynchronous mail message sending
    */
   static final String MAX_THREAD_NUMBER = "mail.max.thread.number";

   /**
    * Enables DKIM signing of outgoing messages, disabled by default. It needs
    * {@link #DKIM_DOMAIN}, {@link #DKIM_SELECTOR} and {@link #DKIM_PRIVATE_KEY}
    */
   static final String DKIM_ENABLED = "mail.dkim.enabled";

   /** Signing domain, the {@code d=} tag; messages sent from another domain are not signed */
   static final String DKIM_DOMAIN = "mail.dkim.domain";

   /** Selector, the {@code s=} tag, of the DNS record that holds the public key */
   static final String DKIM_SELECTOR = "mail.dkim.selector";

   /** Path of an unencrypted RSA private key in PKCS#8 PEM format */
   static final String DKIM_PRIVATE_KEY = "mail.dkim.private-key";

   /**
    * Optional, {@code false} by default (lenient): a key file readable by other
    * users, or a key of 1024 to 2047 bits, is logged as a warning, and a mail
    * that cannot be signed is sent unsigned after an error is logged. When
    * {@code true} (strict), the service refuses to start with such a key, and a
    * mail that cannot be signed is not sent. A key under 1024 bits is refused in
    * both modes (RFC 8301). Where the file system has no POSIX permissions the
    * key file check is skipped in both modes and a warning says so.
    */
   static final String DKIM_STRICT = "mail.dkim.strict";

   /**
    * Optional comma separated list of the headers to sign, a default list is
    * used otherwise; Bcc is refused because the transport removes it
    */
   static final String DKIM_HEADERS = "mail.dkim.headers";

   private static final String[] ALLOW_8BITMIME = { "mail.smtp.allow8bitmime", "mail.smtps.allow8bitmime" };

   private static final Log LOG = ExoLogger.getLogger(MailServiceImpl.class);

   private Session mailSession_;

   private DkimSigner dkimSigner;

   private boolean dkimStrict;

   private volatile boolean eightBitMimeWarned;

   private Properties props_;

   /**
    * Provides thread pool routines for asynchronous mail message sending
    */
   private ExecutorService executorService;

   /**
    * Track current threads number used for asynchronouys mail send
    * to set explicit thread names.
    */
   private volatile int mailServiceThreadCounter = 0;

   public MailServiceImpl(InitParams params, final ExoContainerContext ctx) throws Exception
   {
      props_ = new Properties(System.getProperties());
      props_.putAll(params.getPropertiesParam("config").getProperties());
      if (Boolean.parseBoolean(props_.getProperty(DKIM_ENABLED)))
      {
         dkimStrict = Boolean.parseBoolean(props_.getProperty(DKIM_STRICT));
         dkimSigner = createDkimSigner(props_, dkimStrict);
         LOG.info("DKIM signing of outgoing mails is enabled for domain {} with selector {} ({} mode)",
                  dkimSigner.getDomain(), dkimSigner.getSelector(), dkimStrict ? "strict" : "lenient");
         for (String allow8bitmime : ALLOW_8BITMIME)
         {
            if (Boolean.parseBoolean(props_.getProperty(allow8bitmime)))
            {
               // The transport would re-encode a part to 8bit after it is signed, which invalidates the signature
               LOG.warn("{} is ignored while DKIM signing is enabled: it is set to false", allow8bitmime);
               props_.setProperty(allow8bitmime, "false");
            }
         }
      }
      if ("true".equals(props_.getProperty("mail.smtp.auth")))
      {
         String username = props_.getProperty("mail.smtp.auth.username");
         String password = props_.getProperty("mail.smtp.auth.password");
         final ExoAuthenticator auth = new ExoAuthenticator(username, password);
         mailSession_ = Session.getInstance(props_, auth);
      }
      else
      {
         mailSession_ = Session.getInstance(props_, null);
      }
      int threadNumber =
         props_.getProperty(MAX_THREAD_NUMBER) != null ? Integer.valueOf(props_.getProperty(MAX_THREAD_NUMBER))
            : Runtime.getRuntime().availableProcessors();

      executorService = Executors.newFixedThreadPool(threadNumber, new ThreadFactory()
      {
         public Thread newThread(Runnable arg0)
         {
            Thread thread = new Thread(arg0, ctx.getName() + "-MailServiceThread-" + mailServiceThreadCounter++);
            // A mail left sending when the server stops does not keep its JVM alive, whichever thread sent the first one
            thread.setDaemon(true);
            return thread;
         }
      });
   }

   /**
    * {@inheritDoc}
    */
   public Session getMailSession()
   {
      return mailSession_;
   }

   /**
    * {@inheritDoc}
    */
   public String getOutgoingMailServer()
   {
      return props_.getProperty("mail.smtp.host");
   }

   /**
    * {@inheritDoc}
    */
   public void sendMessage(String from, String to, String subject, String body) throws Exception
   {
      Message message = new Message();
      message.setFrom(from);
      message.setTo(to);
      message.setSubject(subject);
      message.setBody(body);
      sendMessage(message);
   }

   /**
    * {@inheritDoc}
    */
   public void sendMessage(Message message) throws Exception
   {
      MimeMessage mimeMessage = new MimeMessage(getMailSession());
      String FROM = message.getFrom();
      String TO = message.getTo();
      String CC = message.getCC();
      String BCC = message.getBCC();
      String subject = message.getSubject();
      String mimeType = message.getMimeType();
      String body = message.getBody();
      String replyTo = message.getReplyTo();
      List<Attachment> attachment = message.getAttachment();
      // set From to the message
      if (FROM != null && !FROM.equals(""))
      {
         InternetAddress sentFrom = new InternetAddress(FROM);
         if(sentFrom.getPersonal() != null) {
           sentFrom.setPersonal(sentFrom.getPersonal(), "UTF-8");
         }
         mimeMessage.setFrom(sentFrom);
      }
      // set To to the message
      InternetAddress[] sendTo = new InternetAddress[getArrs(TO).length];
      for (int i = 0; i < getArrs(TO).length; i++)
      {
         sendTo[i] = new InternetAddress(getArrs(TO)[i]);
         if(sendTo[i].getPersonal() != null) {
           sendTo[i].setPersonal(sendTo[i].getPersonal(), "UTF-8");
         }
      }
      mimeMessage.setRecipients(jakarta.mail.Message.RecipientType.TO, sendTo);
      // set CC to the message
      if ((getArrs(CC) != null) && (getArrs(CC).length > 0))
      {
         InternetAddress[] copyTo = new InternetAddress[getArrs(CC).length];
         for (int i = 0; i < getArrs(CC).length; i++)
         {
            copyTo[i] = new InternetAddress(getArrs(CC)[i]);
            if(copyTo[i].getPersonal() != null) {
              copyTo[i].setPersonal(copyTo[i].getPersonal(), "UTF-8");
            }
         }
         mimeMessage.setRecipients(jakarta.mail.Message.RecipientType.CC, copyTo);
      }
      // set BCC to the message
      if ((getArrs(BCC) != null) && (getArrs(BCC).length > 0))
      {
         InternetAddress[] bccTo = new InternetAddress[getArrs(BCC).length];
         for (int i = 0; i < getArrs(BCC).length; i++)
         {
            bccTo[i] = new InternetAddress(getArrs(BCC)[i]);
            if(bccTo[i].getPersonal() != null) {
              bccTo[i].setPersonal(bccTo[i].getPersonal(), "UTF-8");
            }
         }
         mimeMessage.setRecipients(jakarta.mail.Message.RecipientType.BCC, bccTo);
      }
      // set the Reply-To to the message
      if ((getArrs(replyTo) != null) && (getArrs(replyTo).length > 0))
      {
         InternetAddress[] replyToArray = new InternetAddress[getArrs(replyTo).length];
         for (int i = 0; i < getArrs(replyTo).length; i++)
         {
            replyToArray[i] = new InternetAddress(getArrs(replyTo)[i]);
            if(replyToArray[i].getPersonal() != null) {
               replyToArray[i].setPersonal(replyToArray[i].getPersonal(), "UTF-8");
            }
         }
         mimeMessage.setReplyTo(replyToArray);
      }
      // set Subject to the message
      mimeMessage.setSubject(subject);
      mimeMessage.setSubject(message.getSubject(), "UTF-8");
      mimeMessage.setSentDate(new Date());

      MimeMultipart multipPartRoot = new MimeMultipart("mixed");

      MimeMultipart multipPartContent = new MimeMultipart("alternative");

      if (attachment != null && attachment.size() != 0)
      {
         MimeBodyPart contentPartRoot = new MimeBodyPart();
         if (mimeType != null && mimeType.indexOf("text/plain") > -1)
            contentPartRoot.setContent(body, "text/plain; charset=utf-8");
         else
            contentPartRoot.setContent(body, "text/html; charset=utf-8");
         MimeBodyPart mimeBodyPart1 = new MimeBodyPart();
         mimeBodyPart1.setContent(body, mimeType);
         multipPartContent.addBodyPart(mimeBodyPart1);
         multipPartRoot.addBodyPart(contentPartRoot);
         for (Attachment att : attachment)
         {
            InputStream is = att.getInputStream();
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(is, att.getMimeType());
            mimeBodyPart.setDataHandler(new DataHandler(byteArrayDataSource));

            mimeBodyPart.setDisposition(Part.ATTACHMENT);
            if (att.getName() != null)
               mimeBodyPart.setFileName(MimeUtility.encodeText(att.getName(), "utf-8", null));
            multipPartRoot.addBodyPart(mimeBodyPart);
         }
         mimeMessage.setContent(multipPartRoot);
      }
      else
      {
         if (mimeType != null && mimeType.indexOf("text/plain") > -1)
            mimeMessage.setContent(body, "text/plain; charset=utf-8");
         else
            mimeMessage.setContent(body, "text/html; charset=utf-8");
      }
      sendMessage(mimeMessage);
   }

   /**
    * {@inheritDoc}
    */
   public void sendMessage(MimeMessage message) throws Exception
   {
      Transport.send(dkimSigner == null ? message : sign(message));
   }

   private static DkimSigner createDkimSigner(Properties props, boolean strict)
   {
      String keyPath = props.getProperty(DKIM_PRIVATE_KEY);
      if (keyPath == null || keyPath.isBlank())
      {
         throw new IllegalStateException(DKIM_ENABLED + " is true but " + DKIM_PRIVATE_KEY + " is not set");
      }
      Path keyFile = Path.of(keyPath.trim());
      if (!DkimSigner.canCheckPermissions(keyFile))
      {
         LOG.warn("The permissions of the DKIM private key {} cannot be read on this file system, "
             + "so they are not checked ({} does not change that)", keyFile, DKIM_STRICT);
      }
      else if (DkimSigner.isReadableByOthers(keyFile))
      {
         rejectOrWarn(strict, "The DKIM private key " + keyFile + " can be read or written by users other than its owner: "
             + "restrict it, for example with chmod 600, and consider the key compromised if the file was exposed");
      }
      String headers = props.getProperty(DKIM_HEADERS);
      DkimSigner signer = new DkimSigner(props.getProperty(DKIM_DOMAIN),
                                         props.getProperty(DKIM_SELECTOR),
                                         DkimSigner.loadPrivateKey(keyFile),
                                         headers == null ? null : Arrays.asList(headers.split(",")));
      if (signer.isWeakKey())
      {
         rejectOrWarn(strict, "The DKIM key is " + signer.getKeyBits() + " bits: " + DkimSigner.RECOMMENDED_RSA_BITS
             + " bits or more is recommended (RFC 8301), generate a new key pair and publish its public key under a new selector");
      }
      return signer;
   }

   private static void rejectOrWarn(boolean strict, String problem)
   {
      if (strict)
      {
         throw new IllegalStateException(problem + " (" + DKIM_STRICT + " is true)");
      }
      LOG.warn(problem);
   }

   /** Lets a test replace the signer. */
   void setDkimSigner(DkimSigner signer, boolean strict)
   {
      this.dkimSigner = signer;
      this.dkimStrict = strict;
   }

   /**
    * Serializes the message once and signs those exact bytes: the signed
    * message, on the session of the original one, is not re-encoded when it is sent.
    */
   private MimeMessage sign(MimeMessage message) throws MessagingException, IOException
   {
      message.saveChanges();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      message.writeTo(out);
      // The transport writes every lone LF as CRLF: sign the bytes as they will be received
      byte[] raw = DkimSigner.toCrlf(out.toByteArray());
      if (!dkimSigner.isAligned(raw))
      {
         LOG.debug("The mail is not sent from the DKIM domain {}, it is sent unsigned", dkimSigner.getDomain());
         return message;
      }
      // The message keeps the session it was created with, as it does when it is sent unsigned
      Session session = message.getSession() == null ? getMailSession() : message.getSession();
      warnIfEightBitMime(session);
      try
      {
         return new SignedMimeMessage(session, dkimSigner.sign(raw));
      }
      catch (RuntimeException | MessagingException e)
      {
         if (dkimStrict)
         {
            throw e;
         }
         LOG.error("The mail could not be signed with DKIM, it is sent unsigned", e);
         return message;
      }
   }

   /**
    * A message on a caller's own session is sent on that session, where the
    * 8BITMIME setting cannot be forced off: the transport may then re-encode a
    * part after it is signed. Said once, as it holds for every such message.
    */
   private void warnIfEightBitMime(Session session)
   {
      if (session != mailSession_ && !eightBitMimeWarned
          && Arrays.stream(ALLOW_8BITMIME).anyMatch(name -> Boolean.parseBoolean(session.getProperty(name))))
      {
         eightBitMimeWarned = true;
         LOG.warn("A message is signed on a session that allows 8BITMIME: a part may be re-encoded when it is sent, "
             + "which invalidates its DKIM signature");
      }
   }

   /** A message parsed from its signed bytes that keeps them untouched when sent. */
   private static final class SignedMimeMessage extends MimeMessage
   {
      SignedMimeMessage(Session session, byte[] signed) throws MessagingException
      {
         super(session, new ByteArrayInputStream(signed));
      }

      @Override
      public void saveChanges()
      {
         // Already saved and signed: a new Message-ID or any re-encoding would break the signature
      }
   }

   /**
    * {@inheritDoc}
    */
   public Future<Boolean> sendMessageInFuture(final String from, final String to, final String subject,
      final String body)
   {
      return executorService.submit(new Callable<Boolean>()
      {
         public Boolean call() throws Exception
         {
            sendMessage(from, to, subject, body);
            return true;
         }
      });
   }

   /**
    * {@inheritDoc}
    */
   public Future<Boolean> sendMessageInFuture(final Message message)
   {
      return executorService.submit(new Callable<Boolean>()
      {
         public Boolean call() throws Exception
         {
            sendMessage(message);
            return true;
         }
      });
   }

   /**
    * {@inheritDoc}
    */
   public Future<Boolean> sendMessageInFuture(final MimeMessage message)
   {
      return executorService.submit(new Callable<Boolean>()
      {
         public Boolean call() throws Exception
         {
            sendMessage(message);
            return true;
         }
      });
   }

   protected String[] getArrs(String toArray)
   {
      if (toArray != null && !toArray.equals(""))
      {
         return toArray.split(",");
      }
      return null;
   }

}
