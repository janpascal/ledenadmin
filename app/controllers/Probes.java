package controllers;

import java.io.*;
import java.lang.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.text.SimpleDateFormat;

import akka.japi.Function;

import au.com.bytecode.opencsv.CSVReader;

import com.feth.play.module.mail.Mailer;
import com.feth.play.module.mail.Mailer.Mail.Body;

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
        for(Persoon p: lid.personen) {
          sendProbe(p);
        }
        flash("success", "Emailverificatie verstuurd");
        return redirect(routes.Probes.lijst());
    }

    public static void sendProbe(Persoon persoon) {
        EmailCheckProbe probe = new EmailCheckProbe(persoon, EmailCheckProbe.ProbeType.EMAIL);
        probe.save();
        sendProbe(probe);
    }

    public static void sendProbe(EmailCheckProbe probe) {
        Configuration conf = Play.application().configuration();

        // Create the email message
        String imageUrl = controllers.routes.Probes.verifyImage(probe.token).absoluteURL(request());
        String verifyUrl = controllers.routes.Probes.verify(probe.token).absoluteURL(request());

        String txt =  views.txt.email.probe.render(imageUrl, verifyUrl).toString();
        String html =  views.html.email.probe.render(imageUrl, verifyUrl).toString();

        final Body body = new Body(txt, html);
        Mailer.getDefaultMailer().sendMail("Token mail", body, probe.email);
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
