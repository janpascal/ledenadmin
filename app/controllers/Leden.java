package controllers;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;

import akka.japi.Function;

import models.*;
import play.*;
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
    
    public static Result csvimport() {
        return ok(csvimport.render());
   }
    
    public static Result upload() {
        RequestBody mainbody = request().body();
        System.out.println(mainbody.toString());
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
          return ok("File uploaded");
        } else {
          flash("error", "Missing file");
          return redirect(routes.Application.index());    
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
