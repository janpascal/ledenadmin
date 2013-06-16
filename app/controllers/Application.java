package controllers;

import java.util.List;

import models.Lid;
import play.*;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {
  
    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }
    
    public static Result ledenlijst() {
        List<Lid> leden = Lid.find.all();
        return ok(ledenlijst.render(leden));
    }
  
}
