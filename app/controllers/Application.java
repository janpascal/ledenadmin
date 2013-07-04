package controllers;

import java.util.List;

import models.Lid;
import play.*;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {
  
    public static Result index() {
        return ok(index.render("Ledenadministratie"));
    }
    
}
