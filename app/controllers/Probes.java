package controllers;

import java.io.*;
import java.lang.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.text.SimpleDateFormat;

import akka.japi.Function;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.EmailException;

import models.*;
import play.*;
import play.data.Form;
import static play.data.Form.*;
import play.libs.F.Promise;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Http.RequestBody;

import views.html.*;

public class Probes extends Controller {
  
    public static Result index() {
        return redirect(routes.Probes.lijst());
    }
    
    public static Result lijst() {
        return TODO;
        //List<Lid> leden = Lid.find.all();
        //return ok(ledenlijst.render(leden));
    }
    
    public static Result sendProbe(Long lidId) {
      Lid lid = Lid.find.byId(lidId);
      try {
        for(Persoon p: lid.personen) {
          sendProbe(p);
        }
        flash("success", "Emailverificatie verstuurd");
      } catch (EmailException e) {
        flash("error", "Emailverificatie mislukt: "+e.getMessage());
        Logger.error("Fout bij versturen email", e);
      }
      return redirect(routes.Probes.lijst());
    }

    public static void sendProbe(Persoon persoon) throws EmailException {
      EmailCheckProbe probe = new EmailCheckProbe(persoon, EmailCheckProbe.ProbeType.EMAIL);
      probe.save();
      sendProbe(probe);
    }

    public static void sendProbe(EmailCheckProbe probe) throws EmailException {
      Configuration conf = Play.application().configuration();
      // Create the email message
      HtmlEmail email = new HtmlEmail();
      email.setHostName(conf.getString("mail.hostname"));
      email.addTo(probe.email, probe.persoon.name);
      email.setFrom(conf.getString("mail.from.address"), conf.getString("mail.from.name"));
      email.setSubject("Token mail");
      
      // embed the image and get the content id
      //URL url = new URL("http://www.apache.org/images/asf_logo_wide.gif");
      String imageUrl = controllers.routes.Probes.verifyImage(probe.token).absoluteURL(request());
      String verifyUrl = controllers.routes.Probes.verify(probe.token).absoluteURL(request());
      //String cid = email.embed(url, "Apache logo");
      
      // set the html message
      StringBuilder html = new StringBuilder();
      html.append("<html>");
      html.append("The apache logo - <img src=\""+imageUrl+"\">");
      html.append("Click <a href=\""+verifyUrl+"\">this link</a> to verify your email address");
      html.append("</html>");
      email.setHtmlMsg(html.toString());

      // set the alternative message
      email.setTextMsg("Your email client does not support HTML messages");

      // send the email
      email.send();
    }

    public static Result verifyImage(String token) {
      EmailCheckProbe probe = EmailCheckProbe.find.where()
          .eq("token", token)
          .findUnique();
      if(probe!=null) {
        Logger.info("Probe "+token+" for "+probe.email+" answered by image url view");
        probe.addResponse(ProbeResponse.ResponseType.IMGVIEW);
        probe.save();
      }
      return redirect(routes.Assets.at("images/logo.png"));
    }
    

    public static Result verify(String token) {
      EmailCheckProbe probe = EmailCheckProbe.find.where()
          .eq("token", token)
          .findUnique();
      if(probe!=null) {
        Logger.info("Probe "+token+" for "+probe.email+" answered by verify url view");
        probe.addResponse(ProbeResponse.ResponseType.WEBLINK);
        probe.save();
      }
      //return ok(probe_verify.render(probe));
      return TODO;
    }
    
}
