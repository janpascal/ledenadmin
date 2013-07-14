package controllers;

import java.util.List;

import models.Lid;
import models.Persoon;

import play.*;
import play.mvc.*;
import play.data.*;
import static play.data.Form.*;
import play.db.ebean.*;

import views.html.*;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.Group;

public class Application extends Controller {

   // -- Authentication
    
    public static class Login extends Model {
        
        public String account;
        public String password;
        
        public String validate() {
            try {
                if(Persoon.authenticate(account, password) == null) {
                    return "Invalid user or password";
                }
            } catch (Exception e) {
                Logger.info("Error verifying password", e);
                return "Invalid user or password";
            }
            return null;
        }
        
    }

    @Restrict({@Group(Persoon.LID_ROLE)})
    public static Result index() {
        return ok(index.render("Ledenadministratie"));
    }

    public static Result login() {
        return ok(login.render(form(Login.class)));
    }

    public static Result authenticate() throws Exception {
        Form<Login> loginForm = form(Login.class).bindFromRequest();
        if(loginForm.hasErrors()) {
            return badRequest(login.render(loginForm));
        } else {
            session("account", loginForm.get().account);
            return redirect(routes.Application.index());
        }
    }
    
}
