package controllers;

import java.io.*;
import java.lang.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.text.SimpleDateFormat;

import akka.japi.Function;

import au.com.bytecode.opencsv.CSVReader;

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

public class Leden extends Controller {
  
    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }
    
    public static Result lijst() {
        List<Lid> leden = Lid.find.all();
        return ok(ledenlijst.render(leden));
    }
    
    public static Result bewerkLid(Long id) {
        Lid lid = Lid.find.byId(id);
        Form<Lid> form = form(Lid.class);
        form.fill(lid);
        return ok(editlid.render(id, form));
    }
    
    public static Result saveLid(Long id) {
        Form<Lid> form = form(Lid.class).bindFromRequest();
        if(form.hasErrors()) {
            return badRequest(editlid.render(id,form));
        }
        form.get().save();
        // flash()
        return bewerkLid(id);
    }
    
    public static Result betaalStatus() {
        List<Lid> leden = Lid.find.all();
        List<Integer> jaren = new ArrayList<Integer>();
        jaren.add(2009);
        jaren.add(2010);
        jaren.add(2011);
        jaren.add(2012);
        jaren.add(2013);
        return ok(betaalstatus.render(leden, jaren));
    }
    
    public static Result csvimport() {
        return ok(csvimport.render());
   }
    
    public static Result upload() {
        RequestBody mainbody = request().body();
        MultipartFormData body = mainbody.asMultipartFormData();
        if(body==null) {
            flash("error", "Missing file");
            return redirect(routes.Application.index());
        }
        FilePart ledenlijst = body.getFile("ledenlijst");
        if (ledenlijst != null) {
          String fileName = ledenlijst.getFilename();
          String contentType = ledenlijst.getContentType(); 
          File file = ledenlijst.getFile();
          try {
            perform_csvimport(file);
          } catch (IOException e) {
            flash("error", "File not found");
            return redirect(routes.Application.index());    
          }
          return ok("File uploaded");
        } else {
          flash("error", "Missing file");
          return redirect(routes.Application.index());    
        }
      }

    public static void perform_csvimport(File file) throws
    FileNotFoundException, IOException {
        CSVReader reader = new CSVReader(new FileReader(file), ';');
        SimpleDateFormat dateFormatter = new SimpleDateFormat("d-M-yyyy");
        SimpleDateFormat dateFormatter2 = new SimpleDateFormat("d/M/yyyy");
        String [] nextLine;
        // Read and check header line
        nextLine = reader.readNext();
        if(nextLine.length<5) throw new IOException("niet genoeg kolommen");
        if(!nextLine[0].equals("lidnr")) throw new IOException("Eerste kolom moet zijn lidnr");
        while ((nextLine = reader.readNext()) != null) {
            String idString=null;
            String name1=null;
            String name2=null;
            String address=null;
            String sindsString = null;
            if (nextLine.length>=1) idString = nextLine[0];
            if (nextLine.length>=2) name1 = nextLine[1];
            if (nextLine.length>=3) name2 = nextLine[2];
            if (nextLine.length>=4) address = nextLine[3];
            if (nextLine.length>=5) sindsString = nextLine[4];
            Long id;
            try {
              id = Long.parseLong(idString);
            } catch (Exception e) {
              id = null;
            }
            Date lidSinds;
            if (sindsString.isEmpty()) {
              lidSinds = null;
            } else {
              try {
                lidSinds = dateFormatter.parse(sindsString);
              } catch (Exception e) {
                try {
                    lidSinds = dateFormatter2.parse(sindsString);
                } catch (Exception e2) {
                    System.out.println("Error parsing date string "+sindsString);
                    e2.printStackTrace();
                    lidSinds = null;
                }
              }
            }
            Lid lid = new Lid(id, name1, name2, address, lidSinds);    
            Lid.create(lid);
        }
    }
    /*
    public static Result testtesttest() {
        Promise<Integer> promiseOfInt = play.libs.Akka.future(
          new Callable<Integer>() {
            public Integer call() {
              return intensiveComputation();
            }
          }
        );
        return async(
          promiseOfInt.map(
            new Function<Integer,Result>() {
              public Result apply(Integer i) {
                return ok("Got result: " + i);
              } 
            }
          )
        );
      }
  */
}